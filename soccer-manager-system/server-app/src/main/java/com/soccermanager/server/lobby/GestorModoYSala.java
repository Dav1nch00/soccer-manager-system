package com.soccermanager.server.lobby;

import com.soccermanager.common.dto.PlanDeJuegoDTO;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinador de sesión y sala multijugador.
 * Gestiona la selección de modo (Solo vs 2 Jugadores), la edición paralela de estrategias
 * y la cuenta regresiva competitiva de 60 segundos con resolución anticipada o timeout forzado.
 */
public class GestorModoYSala {

    private final ReentrantLock lock;
    private final TemporizadorJornada temporizador;

    private ModoJuegoEnum modoActivo;
    private EstadoSalaEnum estadoSala;

    // DT 1
    private String usuarioDT1;
    private String clubIdDT1;
    private String clubNombreDT1;
    private boolean dt1Listo;
    private PlanDeJuegoDTO planDT1;

    // DT 2
    private String usuarioDT2;
    private String clubIdDT2;
    private String clubNombreDT2;
    private boolean dt2Listo;
    private PlanDeJuegoDTO planDT2;

    private String quienDisparoTimer;
    private String mensajeSala;

    public GestorModoYSala() {
        this(new TemporizadorJornada());
    }

    public GestorModoYSala(TemporizadorJornada temporizador) {
        this.lock = new ReentrantLock();
        this.temporizador = temporizador;
        this.modoActivo = ModoJuegoEnum.SOLO;
        this.estadoSala = EstadoSalaEnum.EN_PLANIFICACION;
        this.mensajeSala = "Sala lista en Modo Individual.";
    }

    public void configurarModo(ModoJuegoEnum nuevoModo) {
        lock.lock();
        try {
            this.modoActivo = nuevoModo;
            if (nuevoModo == ModoJuegoEnum.DOS_JUGADORES) {
                this.estadoSala = (usuarioDT1 != null && usuarioDT2 != null)
                        ? EstadoSalaEnum.EN_PLANIFICACION
                        : EstadoSalaEnum.ESPERANDO_JUGADORES;
                this.mensajeSala = "Modo 2 Jugadores activo: Ambos pueden editar tácticas en paralelo.";
            } else {
                this.estadoSala = EstadoSalaEnum.EN_PLANIFICACION;
                this.mensajeSala = "Modo Individual activo (vs IA).";
            }
        } finally {
            lock.unlock();
        }
    }

    public void conectarDT(int numeroDT, String usuario, String clubId, String clubNombre) {
        lock.lock();
        try {
            if (numeroDT == 1) {
                this.usuarioDT1 = usuario;
                this.clubIdDT1 = clubId;
                this.clubNombreDT1 = clubNombre;
            } else if (numeroDT == 2) {
                this.usuarioDT2 = usuario;
                this.clubIdDT2 = clubId;
                this.clubNombreDT2 = clubNombre;
            }

            if (modoActivo == ModoJuegoEnum.DOS_JUGADORES) {
                if (usuarioDT1 != null && usuarioDT2 != null) {
                    this.estadoSala = EstadoSalaEnum.EN_PLANIFICACION;
                    this.mensajeSala = "¡Ambos DTs conectados! Diseñen sus alineaciones en paralelo.";
                } else {
                    this.estadoSala = EstadoSalaEnum.ESPERANDO_JUGADORES;
                    this.mensajeSala = "Esperando que el segundo DT inicie sesión...";
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void guardarPlanTactico(String usuario, PlanDeJuegoDTO plan) {
        lock.lock();
        try {
            if (usuario.equalsIgnoreCase(usuarioDT1)) {
                this.planDT1 = plan;
            } else if (usuario.equalsIgnoreCase(usuarioDT2)) {
                this.planDT2 = plan;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Solicitud de confirmación táctica y simulación de la jornada.
     * En Modo 2P, si es el primer usuario en confirmar, activa el temporizador de 60s.
     * Si es el segundo o si expiran los 60s, ejecuta el callback de simulación.
     */
    public void solicitarSimulacion(String usuario, Runnable onSimularJornada) {
        lock.lock();
        try {
            // MODO 1: INDIVIDUAL (SOLO VS IA)
            if (modoActivo == ModoJuegoEnum.SOLO) {
                dt1Listo = true;
                estadoSala = EstadoSalaEnum.SIMULANDO;
                mensajeSala = "Simulando jornada en modo individual...";
                ejecutarSimulacionSegura(onSimularJornada);
                return;
            }

            // MODO 2: DOS JUGADORES EN RED
            if (usuarioDT1 == null || usuarioDT2 == null) {
                throw new IllegalStateException("Se requiere que ambos DTs hayan iniciado sesión antes de simular.");
            }

            // Marcar listo al usuario que presiona
            boolean esDT1 = usuario.equalsIgnoreCase(usuarioDT1);
            if (esDT1) dt1Listo = true;
            else dt2Listo = true;

            // CASO A: Ambos jugadores confirmaron antes de agotarse el tiempo
            if (dt1Listo && dt2Listo) {
                temporizador.cancelar();
                estadoSala = EstadoSalaEnum.SIMULANDO;
                mensajeSala = "¡Ambos DTs listos! Disparando simulación de jornada simultánea...";
                ejecutarSimulacionSegura(onSimularJornada);
                return;
            }

            // CASO B: El primer jugador confirma -> Arranca el temporizador de 60 segundos
            if (estadoSala != EstadoSalaEnum.CUENTA_REGRESIVA) {
                estadoSala = EstadoSalaEnum.CUENTA_REGRESIVA;
                quienDisparoTimer = usuario;
                mensajeSala = "¡" + usuario + " ha confirmado su táctica! El rival tiene 60 segundos para finalizar.";

                temporizador.iniciarCountdown(60,
                        segundosRestantes -> {
                            // Actualización periódica en cada segundo
                        },
                        () -> {
                            // CASO C: Expiración de los 60 segundos (TIMEOUT)
                            gestionarTimeout(onSimularJornada);
                        }
                );
            }
        } finally {
            lock.unlock();
        }
    }

    private void gestionarTimeout(Runnable onSimularJornada) {
        lock.lock();
        try {
            if (estadoSala == EstadoSalaEnum.CUENTA_REGRESIVA) {
                estadoSala = EstadoSalaEnum.SIMULANDO;
                mensajeSala = "¡Tiempo agotado (60s)! Se simula con la táctica configurada hasta este momento.";

                // Asegurar planes para ambos
                if (planDT1 == null) {
                    planDT1 = new PlanDeJuegoDTO(clubIdDT1, clubNombreDT1, FormacionEnum.F_4_4_2, EstrategiaEnum.POSESION, null);
                }
                if (planDT2 == null) {
                    planDT2 = new PlanDeJuegoDTO(clubIdDT2, clubNombreDT2, FormacionEnum.F_4_4_2, EstrategiaEnum.CONTRAATAQUE, null);
                }

                dt1Listo = true;
                dt2Listo = true;

                ejecutarSimulacionSegura(onSimularJornada);
            }
        } finally {
            lock.unlock();
        }
    }

    private void ejecutarSimulacionSegura(Runnable onSimularJornada) {
        try {
            if (onSimularJornada != null) {
                onSimularJornada.run();
            }
        } finally {
            estadoSala = EstadoSalaEnum.FINALIZADA;
            mensajeSala = "Jornada simulada exitosamente. Marcadores y posiciones actualizadas.";
        }
    }

    public void resetearSalaParaNuevaJornada() {
        lock.lock();
        try {
            temporizador.cancelar();
            this.dt1Listo = false;
            this.dt2Listo = false;
            this.planDT1 = null;
            this.planDT2 = null;
            this.quienDisparoTimer = null;
            this.estadoSala = EstadoSalaEnum.EN_PLANIFICACION;
            this.mensajeSala = "Nueva jornada habilitada. Diseñen sus planes de juego.";
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retorna el DTO de estado en vivo de la sala para el cliente.
     */
    public SalaEstadoDTO getEstadoSala() {
        lock.lock();
        try {
            SalaEstadoDTO dto = new SalaEstadoDTO();
            dto.setModoActivo(this.modoActivo);
            dto.setEstadoSala(this.estadoSala);
            dto.setDt1Conectado(this.usuarioDT1 != null);
            dto.setDt2Conectado(this.usuarioDT2 != null);
            dto.setNombreDt1(this.usuarioDT1);
            dto.setNombreDt2(this.usuarioDT2);
            dto.setClubDt1(this.clubNombreDT1);
            dto.setClubDt2(this.clubNombreDT2);
            dto.setDt1Listo(this.dt1Listo);
            dto.setDt2Listo(this.dt2Listo);
            dto.setSegundosRestantes(temporizador.isActivo() ? temporizador.getSegundosRestantes() : 0);
            dto.setQuienDisparoTimer(this.quienDisparoTimer);
            dto.setMensaje(this.mensajeSala);
            return dto;
        } finally {
            lock.unlock();
        }
    }

    public PlanDeJuegoDTO getPlanDT1() { return planDT1; }
    public PlanDeJuegoDTO getPlanDT2() { return planDT2; }
    public TemporizadorJornada getTemporizador() { return temporizador; }
}
