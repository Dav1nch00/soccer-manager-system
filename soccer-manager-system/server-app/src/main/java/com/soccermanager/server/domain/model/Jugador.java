package com.soccermanager.server.domain.model;

import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.state.EstadoCansado;
import com.soccermanager.server.domain.state.EstadoJugador;
import com.soccermanager.server.domain.state.EstadoLesionado;
import com.soccermanager.server.domain.state.EstadoOptimo;
import com.soccermanager.server.domain.state.EstadoSancionado;

/**
 * Hoja (Leaf) en el Patrón Composite.
 * Representa a un futbolista individual y actúa como Contexto para el Patrón State.
 */
public class Jugador implements ComponenteTactico {
    private static final long serialVersionUID = 1L;

    private String idJugador;
    private String nombre;
    private int dorsal;
    private PosicionEnum posicion;
    private String clubId;

    // Atributos base (escala 1 a 100)
    private int velocidad;
    private int pase;
    private int tiro;
    private int defensa;
    private int fisico;

    // Estado físico y fisiológico (Patrón State)
    private int energia; // 0 a 100%
    private EstadoJugador estado;

    // Estadísticas acumuladas
    private int partidosJugados;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private double calificacionAcumulada;

    public Jugador(String idJugador, String nombre, int dorsal, PosicionEnum posicion, String clubId,
                   int velocidad, int pase, int tiro, int defensa, int fisico) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.dorsal = dorsal;
        this.posicion = posicion;
        this.clubId = clubId;
        this.velocidad = velocidad;
        this.pase = pase;
        this.tiro = tiro;
        this.defensa = defensa;
        this.fisico = fisico;
        this.energia = 100;
        this.estado = new EstadoOptimo();
    }

    // --- Implementación de ComponenteTactico (Patrón Composite) ---

    @Override
    public double calcularPoderOfensivo() {
        if (!estado.puedeJugar()) return 0.0;
        double base = (tiro * 0.5) + (velocidad * 0.3) + (pase * 0.2);
        return base * estado.obtenerFactorRendimiento() * getFactorEnergia();
    }

    @Override
    public double calcularPoderDefensivo() {
        if (!estado.puedeJugar()) return 0.0;
        double base = (defensa * 0.5) + (fisico * 0.3) + (velocidad * 0.2);
        return base * estado.obtenerFactorRendimiento() * getFactorEnergia();
    }

    @Override
    public double calcularPoderControl() {
        if (!estado.puedeJugar()) return 0.0;
        double base = (pase * 0.6) + (velocidad * 0.2) + (fisico * 0.2);
        return base * estado.obtenerFactorRendimiento() * getFactorEnergia();
    }

    @Override
    public int getCantidadElementos() {
        return 1;
    }

    // --- Gestión del Patrón State y Energía ---

    public double getFactorEnergia() {
        return Math.max(0.5, energia / 100.0);
    }

    public void cambiarEstado(EstadoJugador nuevoEstado) {
        if (nuevoEstado != null) {
            this.estado = nuevoEstado;
        }
    }

    public void reducirEnergia(int cantidad) {
        this.energia = Math.max(0, this.energia - cantidad);
        if (this.energia < 50 && this.estado instanceof EstadoOptimo) {
            cambiarEstado(new EstadoCansado());
        }
    }

    public void recuperarEnergia(int cantidad) {
        this.energia = Math.min(100, this.energia + cantidad);
        if (this.energia >= 75 && this.estado instanceof EstadoCansado) {
            cambiarEstado(new EstadoOptimo());
        }
    }

    public void avanzarJornada() {
        estado.avanzarJornada(this);
    }

    public void registrarGol() {
        this.goles++;
    }

    public void registrarAsistencia() {
        this.asistencias++;
    }

    public void registrarTarjetaAmarilla() {
        this.tarjetasAmarillas++;
        if (this.tarjetasAmarillas % 5 == 0) {
            // Suspensión por ciclo de 5 amarillas
            cambiarEstado(new EstadoSancionado(1));
        }
    }

    public void registrarTarjetaRoja() {
        this.tarjetasRojas++;
        // Expulsión directa: mínimo 1 jornada
        cambiarEstado(new EstadoSancionado(1));
    }

    public void registrarParticipacion(double calificacion) {
        this.partidosJugados++;
        this.calificacionAcumulada += calificacion;
    }

    public double getCalificacionPromedio() {
        return partidosJugados > 0 ? (calificacionAcumulada / partidosJugados) : 6.0;
    }

    // --- Conversión a DTO ---

    public JugadorDTO toDTO() {
        JugadorDTO dto = new JugadorDTO(idJugador, nombre, dorsal, posicion, clubId, velocidad, pase, tiro, defensa, fisico);
        dto.setEnergia(this.energia);
        dto.setEstado(this.estado.getTipo());
        dto.setPartidosJugados(this.partidosJugados);
        dto.setGoles(this.goles);
        dto.setAsistencias(this.asistencias);
        dto.setTarjetasAmarillas(this.tarjetasAmarillas);
        dto.setTarjetasRojas(this.tarjetasRojas);
        dto.setCalificacionPromedio(getCalificacionPromedio());
        if (this.estado instanceof EstadoLesionado) {
            dto.setSemanasLesionRestantes(((EstadoLesionado) this.estado).getSemanasRestantes());
        } else if (this.estado instanceof EstadoSancionado) {
            dto.setJornadasSancionRestantes(((EstadoSancionado) this.estado).getJornadasRestantes());
        }
        return dto;
    }

    public static Jugador fromDTO(JugadorDTO dto) {
        if (dto == null) return null;
        Jugador j = new Jugador(
                dto.getIdJugador(), dto.getNombre(), dto.getDorsal(), dto.getPosicion(),
                dto.getClubId(), dto.getVelocidad(), dto.getPase(), dto.getTiro(),
                dto.getDefensa(), dto.getFisico()
        );
        j.setEnergia(dto.getEnergia());
        j.setPartidosJugados(dto.getPartidosJugados());
        j.setGoles(dto.getGoles());
        j.setAsistencias(dto.getAsistencias());
        j.setTarjetasAmarillas(dto.getTarjetasAmarillas());
        j.setTarjetasRojas(dto.getTarjetasRojas());

        if (dto.getEstado() == EstadoJugadorEnum.CANSADO) {
            j.cambiarEstado(new EstadoCansado());
        } else if (dto.getEstado() == EstadoJugadorEnum.LESIONADO) {
            j.cambiarEstado(new EstadoLesionado(Math.max(1, dto.getSemanasLesionRestantes())));
        } else if (dto.getEstado() == EstadoJugadorEnum.SANCIONADO) {
            j.cambiarEstado(new EstadoSancionado(Math.max(1, dto.getJornadasSancionRestantes())));
        } else {
            j.cambiarEstado(new EstadoOptimo());
        }

        return j;
    }

    // Getters y Setters
    public String getIdJugador() {
        return idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDorsal() {
        return dorsal;
    }

    public PosicionEnum getPosicion() {
        return posicion;
    }

    public String getClubId() {
        return clubId;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public int getPase() {
        return pase;
    }

    public int getTiro() {
        return tiro;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getFisico() {
        return fisico;
    }

    public int getEnergia() {
        return energia;
    }

    public void setEnergia(int energia) {
        this.energia = Math.max(0, Math.min(100, energia));
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(int partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public int getGoles() {
        return goles;
    }

    public void setGoles(int goles) {
        this.goles = goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(int asistencias) {
        this.asistencias = asistencias;
    }

    public int getTarjetasAmarillas() {
        return tarjetasAmarillas;
    }

    public void setTarjetasAmarillas(int tarjetasAmarillas) {
        this.tarjetasAmarillas = tarjetasAmarillas;
    }

    public int getTarjetasRojas() {
        return tarjetasRojas;
    }

    public void setTarjetasRojas(int tarjetasRojas) {
        this.tarjetasRojas = tarjetasRojas;
    }
}
