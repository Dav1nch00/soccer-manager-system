package com.soccermanager.client.ui;

import com.soccermanager.client.builders.PlanDeJuegoBuilder;
import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.PlanDeJuegoDTO;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de la Pizarra Táctica: Alineación con PlanDeJuegoBuilder, selección de estrategia
 * y widget de cuenta regresiva de 60 segundos en tiempo real.
 */
public class VistaPizarraTactica {

    private final MotorConexion motorConexion;
    private final PlanDeJuegoBuilder builder;

    public VistaPizarraTactica(MotorConexion motorConexion) {
        this.motorConexion = motorConexion;
        this.builder = new PlanDeJuegoBuilder();
    }

    public void inicializarAlineacionPredeterminada(String clubId, String clubNombre, List<JugadorDTO> plantilla) {
        builder.setClub(clubId, clubNombre);
        builder.setFormacion(FormacionEnum.F_4_4_2);
        builder.setEstrategia(EstrategiaEnum.POSESION);
        builder.limpiarTitulares();

        // Auto-seleccionar 11 disponibles respetando posiciones
        alinearPorPosicion(plantilla, PosicionEnum.PORTERO, 1);
        alinearPorPosicion(plantilla, PosicionEnum.DEFENSA, 4);
        alinearPorPosicion(plantilla, PosicionEnum.MEDIOCAMPISTA, 4);
        alinearPorPosicion(plantilla, PosicionEnum.DELANTERO, 2);
    }

    private void alinearPorPosicion(List<JugadorDTO> plantilla, PosicionEnum pos, int requeridos) {
        int count = 0;
        for (JugadorDTO j : plantilla) {
            if (count >= requeridos) break;
            if (j.getPosicion() == pos && j.isDisponible()) {
                builder.agregarTitular(j);
                count++;
            }
        }
    }

    public boolean gestionarPizarra(Scanner scanner, String usuario) {
        while (true) {
            System.out.println("==================================================================");
            System.out.println("                     PIZARRA TÁCTICA DEL MÁNAGER                  ");
            System.out.println("==================================================================");
            System.out.println("  Formación:  " + builder.getFormacion().getEtiqueta() +
                    " | Estrategia: " + builder.getEstrategia().getNombre());
            System.out.println("  Titulares:  " + builder.getCantidadTitularesActual() + " / 11 convocados");
            System.out.println("------------------------------------------------------------------");

            List<JugadorDTO> titulares = builder.getTitularesActuales();
            for (int i = 0; i < titulares.size(); i++) {
                JugadorDTO j = titulares.get(i);
                System.out.printf("  %2d. [%-3s] #%-2d %-16s | Media: %4.1f | %3d%%%n",
                        (i + 1), j.getPosicion().getAbreviatura(), j.getDorsal(),
                        j.getNombre(), j.getMediaGeneral(), j.getEnergia());
            }

            System.out.println("------------------------------------------------------------------");
            System.out.println("1. Cambiar Formación Táctica");
            System.out.println("2. Cambiar Filosofía de Juego (Estrategia)");
            System.out.println("3. CONFIRMAR PLAN Y SIMULAR JORNADA");
            System.out.println("4. Volver al Menú Principal");
            System.out.print("\nSeleccione opción: ");

            String op = scanner.nextLine().trim();

            if ("1".equals(op)) {
                cambiarFormacion(scanner);
            } else if ("2".equals(op)) {
                cambiarEstrategia(scanner);
            } else if ("3".equals(op)) {
                boolean exito = confirmarYSimular(scanner, usuario);
                if (exito) return true;
            } else if ("4".equals(op)) {
                return false;
            }
        }
    }

    private void cambiarFormacion(Scanner scanner) {
        System.out.println("\nSeleccione Esquema:");
        System.out.println("1. 4-4-2 (Equilibrado Clásico)");
        System.out.println("2. 4-3-3 (Ofensivo por Bandas)");
        System.out.println("3. 5-3-2 (Muro Defensivo con Carrileros)");
        System.out.println("4. 3-5-2 (Población en Mediocampo)");
        System.out.print("Opción (1-4): ");
        String f = scanner.nextLine().trim();
        switch (f) {
            case "2": builder.setFormacion(FormacionEnum.F_4_3_3); break;
            case "3": builder.setFormacion(FormacionEnum.F_5_3_2); break;
            case "4": builder.setFormacion(FormacionEnum.F_3_5_2); break;
            default: builder.setFormacion(FormacionEnum.F_4_4_2); break;
        }
        System.out.println(">> Formación actualizada a: " + builder.getFormacion().getEtiqueta() + "\n");
    }

    private void cambiarEstrategia(Scanner scanner) {
        System.out.println("\nSeleccione Filosofía de Juego (Patrón Strategy):");
        System.out.println("1. Posesión (Control paciente; vulnerable al contraataque)");
        System.out.println("2. Contraataque (Transición fulgurante; bonificación ante Posesión)");
        System.out.println("3. Presión Alta (Recuperación en campo rival; alto desgaste y faltas)");
        System.out.println("4. Bloque Bajo (Solidez extrema en área propia; desgaste mínimo)");
        System.out.print("Opción (1-4): ");
        String e = scanner.nextLine().trim();
        switch (e) {
            case "2": builder.setEstrategia(EstrategiaEnum.CONTRAATAQUE); break;
            case "3": builder.setEstrategia(EstrategiaEnum.PRESION_ALTA); break;
            case "4": builder.setEstrategia(EstrategiaEnum.BLOQUE_BAJO); break;
            default: builder.setEstrategia(EstrategiaEnum.POSESION); break;
        }
        System.out.println(">> Estrategia actualizada a: " + builder.getEstrategia().getNombre() + "\n");
    }

    private boolean confirmarYSimular(Scanner scanner, String usuario) {
        try {
            // Validar con el Patrón Builder
            System.out.println("\nValidando alineación con PlanDeJuegoBuilder...");
            PlanDeJuegoDTO plan = builder.build();
            System.out.println(">> ¡Alineación verificada con éxito! (11 titulares hábiles).");

            // Enviar plan táctico al servidor
            motorConexion.enviarPlanJuego(usuario, plan);

            // Solicitar simulación
            System.out.println("Enviando orden de simulación al servidor...");
            SalaEstadoDTO sala = motorConexion.solicitarSimulacion(usuario);

            // Si entra en cuenta regresiva de 60s (Modo 2 Jugadores)
            if (sala.getEstadoSala() == EstadoSalaEnum.CUENTA_REGRESIVA) {
                mostrarWidgetCuentaRegresiva(usuario);
            } else {
                System.out.println(">> " + sala.getMensaje());
            }

            return true;
        } catch (IllegalStateException e) {
            System.out.println(">> ERROR DE VALIDACIÓN TÁCTICA: " + e.getMessage() + "\n");
            return false;
        } catch (IOException | InterruptedException e) {
            System.out.println(">> ERROR DE COMUNICACIÓN CON EL SERVIDOR: " + e.getMessage() + "\n");
            return false;
        }
    }

    private void mostrarWidgetCuentaRegresiva(String usuario) {
        System.out.println("\n==================================================================");
        System.out.println("   ¡CUENTA REGRESIVA DE 60 SEGUNDOS ACTIVADA POR EL RIVAL!        ");
        System.out.println("==================================================================");
        System.out.println("El rival ya confirmó su estrategia. Tienes 60 segundos o se simulará");
        System.out.println("automáticamente con tu alineación actual.");
        System.out.println("Monitoreando servidor en tiempo real...\n");

        while (true) {
            try {
                Thread.sleep(1000);
                SalaEstadoDTO estado = motorConexion.consultarEstadoSala();

                if (estado.getEstadoSala() == EstadoSalaEnum.FINALIZADA ||
                        estado.getEstadoSala() == EstadoSalaEnum.SIMULANDO) {
                    System.out.println("\n>> ¡TIEMPO CONCLUIDO O AMBOS CONFIRMARON!");
                    System.out.println(">> " + estado.getMensaje() + "\n");
                    break;
                }

                System.out.print("\r[TEMPORIZADOR EN VIVO] " + estado.getSegundosRestantes() + "s restantes...    ");
            } catch (Exception e) {
                break;
            }
        }
    }
}
