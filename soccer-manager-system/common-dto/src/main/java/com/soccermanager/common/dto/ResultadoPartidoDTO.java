package com.soccermanager.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO que encapsula el resultado consolidado de un partido simulado.
 */
@JsonIgnoreProperties(value = "marcadorTexto", allowGetters = true)
public class ResultadoPartidoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPartido;
    private int jornadaNumero;

    private String clubLocalId;
    private String nombreLocal;
    private int golesLocal;

    private String clubVisitanteId;
    private String nombreVisitante;
    private int golesVisitante;

    private int posesionLocal;
    private int posesionVisitante;
    private int tirosLocal;
    private int tirosVisitante;

    private List<IncidenciaDTO> incidencias;
    private Map<String, Double> calificacionesJugadores; // jugadorId -> calificacion (1.0 - 10.0)

    public ResultadoPartidoDTO() {
        this.incidencias = new ArrayList<>();
        this.calificacionesJugadores = new HashMap<>();
    }

    public ResultadoPartidoDTO(String idPartido, int jornadaNumero, String clubLocalId, String nombreLocal,
                               int golesLocal, String clubVisitanteId, String nombreVisitante, int golesVisitante) {
        this.idPartido = idPartido;
        this.jornadaNumero = jornadaNumero;
        this.clubLocalId = clubLocalId;
        this.nombreLocal = nombreLocal;
        this.golesLocal = golesLocal;
        this.clubVisitanteId = clubVisitanteId;
        this.nombreVisitante = nombreVisitante;
        this.golesVisitante = golesVisitante;
        this.incidencias = new ArrayList<>();
        this.calificacionesJugadores = new HashMap<>();
    }

    public String getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(String idPartido) {
        this.idPartido = idPartido;
    }

    public int getJornadaNumero() {
        return jornadaNumero;
    }

    public void setJornadaNumero(int jornadaNumero) {
        this.jornadaNumero = jornadaNumero;
    }

    public String getClubLocalId() {
        return clubLocalId;
    }

    public void setClubLocalId(String clubLocalId) {
        this.clubLocalId = clubLocalId;
    }

    public String getNombreLocal() {
        return nombreLocal;
    }

    public void setNombreLocal(String nombreLocal) {
        this.nombreLocal = nombreLocal;
    }

    public int getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(int golesLocal) {
        this.golesLocal = golesLocal;
    }

    public String getClubVisitanteId() {
        return clubVisitanteId;
    }

    public void setClubVisitanteId(String clubVisitanteId) {
        this.clubVisitanteId = clubVisitanteId;
    }

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public void setNombreVisitante(String nombreVisitante) {
        this.nombreVisitante = nombreVisitante;
    }

    public int getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(int golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public int getPosesionLocal() {
        return posesionLocal;
    }

    public void setPosesionLocal(int posesionLocal) {
        this.posesionLocal = posesionLocal;
    }

    public int getPosesionVisitante() {
        return posesionVisitante;
    }

    public void setPosesionVisitante(int posesionVisitante) {
        this.posesionVisitante = posesionVisitante;
    }

    public int getTirosLocal() {
        return tirosLocal;
    }

    public void setTirosLocal(int tirosLocal) {
        this.tirosLocal = tirosLocal;
    }

    public int getTirosVisitante() {
        return tirosVisitante;
    }

    public void setTirosVisitante(int tirosVisitante) {
        this.tirosVisitante = tirosVisitante;
    }

    public List<IncidenciaDTO> getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(List<IncidenciaDTO> incidencias) {
        this.incidencias = incidencias;
    }

    public Map<String, Double> getCalificacionesJugadores() {
        return calificacionesJugadores;
    }

    public void setCalificacionesJugadores(Map<String, Double> calificacionesJugadores) {
        this.calificacionesJugadores = calificacionesJugadores;
    }

    public String getMarcadorTexto() {
        return nombreLocal + " " + golesLocal + " - " + golesVisitante + " " + nombreVisitante;
    }

    public boolean esEmpate() {
        return golesLocal == golesVisitante;
    }

    public boolean ganoLocal() {
        return golesLocal > golesVisitante;
    }

    public boolean ganoVisitante() {
        return golesVisitante > golesLocal;
    }

    @Override
    public String toString() {
        return "ResultadoPartidoDTO{" +
                "Jornada=" + jornadaNumero +
                ", Marcador='" + getMarcadorTexto() + '\'' +
                ", Posesión=" + posesionLocal + "% - " + posesionVisitante + "%" +
                ", Incidencias=" + incidencias.size() +
                '}';
    }
}
