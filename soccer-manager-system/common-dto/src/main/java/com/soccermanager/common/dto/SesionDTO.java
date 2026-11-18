package com.soccermanager.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que encapsula la información de sesión tras la autenticación o creación de perfil.
 */
public class SesionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idSesion;
    private String idPerfil;
    private String nombreUsuario;
    private String clubId;
    private String nombreClub;
    private boolean perfilNuevoCreado;
    private LocalDateTime fechaInicio;

    public SesionDTO() {
    }

    public SesionDTO(String idSesion, String idPerfil, String nombreUsuario, String clubId, String nombreClub, boolean perfilNuevoCreado) {
        this.idSesion = idSesion;
        this.idPerfil = idPerfil;
        this.nombreUsuario = nombreUsuario;
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.perfilNuevoCreado = perfilNuevoCreado;
        this.fechaInicio = LocalDateTime.now();
    }

    public String getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(String idSesion) {
        this.idSesion = idSesion;
    }

    public String getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(String idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
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

    public boolean isPerfilNuevoCreado() {
        return perfilNuevoCreado;
    }

    public void setPerfilNuevoCreado(boolean perfilNuevoCreado) {
        this.perfilNuevoCreado = perfilNuevoCreado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    @Override
    public String toString() {
        return "SesionDTO{" +
                "idSesion='" + idSesion + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", clubId='" + clubId + '\'' +
                ", nombreClub='" + nombreClub + '\'' +
                ", perfilNuevoCreado=" + perfilNuevoCreado +
                '}';
    }
}
