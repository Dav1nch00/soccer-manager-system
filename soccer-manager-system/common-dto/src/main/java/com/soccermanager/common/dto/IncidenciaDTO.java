package com.soccermanager.common.dto;

import com.soccermanager.common.enums.TipoIncidenciaEnum;

import java.io.Serializable;

/**
 * DTO que representa un evento ocurrido en un minuto específico del partido.
 */
public class IncidenciaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int minuto; // 1 a 90+
    private TipoIncidenciaEnum tipo;
    private String clubId;
    private String nombreClub;
    private String jugadorId;
    private String nombreJugador;
    private String asistenteId;
    private String nombreAsistente;
    private String descripcion;

    public IncidenciaDTO() {
    }

    public IncidenciaDTO(int minuto, TipoIncidenciaEnum tipo, String clubId, String nombreClub,
                         String jugadorId, String nombreJugador, String descripcion) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.descripcion = descripcion;
    }

    public int getMinuto() {
        return minuto;
    }

    public void setMinuto(int minuto) {
        this.minuto = minuto;
    }

    public TipoIncidenciaEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoIncidenciaEnum tipo) {
        this.tipo = tipo;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getNombreClub() {
        return nombreClub;
    }

    public void setNombreClub(String nombreClub) {
        this.nombreClub = nombreClub;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getAsistenteId() {
        return asistenteId;
    }

    public void setAsistenteId(String asistenteId) {
        this.asistenteId = asistenteId;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return String.format("[%d'] %s - %s (%s)", minuto, tipo.getEtiqueta(), nombreJugador, nombreClub);
    }
}
