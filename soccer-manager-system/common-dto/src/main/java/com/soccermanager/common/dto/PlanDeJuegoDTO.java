package com.soccermanager.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que encapsula la configuración táctica enviada al servidor (Patrón Builder en cliente).
 */
public class PlanDeJuegoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clubId;
    private String nombreClub;
    private FormacionEnum formacion;
    private EstrategiaEnum estrategia;
    private List<String> titularesIds;
    private List<JugadorDTO> titulares;
    private LocalDateTime fechaEnvio;

    public PlanDeJuegoDTO() {
        this.titularesIds = new ArrayList<>();
        this.titulares = new ArrayList<>();
        this.fechaEnvio = LocalDateTime.now();
    }

    public PlanDeJuegoDTO(String clubId, String nombreClub, FormacionEnum formacion, EstrategiaEnum estrategia, List<JugadorDTO> titulares) {
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.formacion = formacion;
        this.estrategia = estrategia;
        this.titulares = titulares != null ? titulares : new ArrayList<>();
        this.titularesIds = new ArrayList<>();
        if (titulares != null) {
            for (JugadorDTO j : titulares) {
                if (j != null && j.getIdJugador() != null) {
                    this.titularesIds.add(j.getIdJugador());
                }
            }
        }
        this.fechaEnvio = LocalDateTime.now();
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

    public FormacionEnum getFormacion() {
        return formacion;
    }

    public void setFormacion(FormacionEnum formacion) {
        this.formacion = formacion;
    }

    public EstrategiaEnum getEstrategia() {
        return estrategia;
    }

    public void setEstrategia(EstrategiaEnum estrategia) {
        this.estrategia = estrategia;
    }

    public List<String> getTitularesIds() {
        return titularesIds;
    }

    public void setTitularesIds(List<String> titularesIds) {
        this.titularesIds = titularesIds;
    }

    public List<JugadorDTO> getTitulares() {
        return titulares;
    }

    public void setTitulares(List<JugadorDTO> titulares) {
        this.titulares = titulares;
        this.titularesIds.clear();
        if (titulares != null) {
            for (JugadorDTO j : titulares) {
                if (j != null && j.getIdJugador() != null) {
                    this.titularesIds.add(j.getIdJugador());
                }
            }
        }
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    @JsonIgnore
    public int getCantidadTitulares() {
        return titulares != null ? titulares.size() : (titularesIds != null ? titularesIds.size() : 0);
    }

    @Override
    public String toString() {
        return "PlanDeJuegoDTO{" +
                "club='" + nombreClub + '\'' +
                ", formacion=" + formacion +
                ", estrategia=" + estrategia +
                ", cantidadTitulares=" + getCantidadTitulares() +
                '}';
    }
}
