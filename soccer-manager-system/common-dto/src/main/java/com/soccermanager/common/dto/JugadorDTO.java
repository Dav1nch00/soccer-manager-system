package com.soccermanager.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.common.enums.PosicionEnum;

import java.io.Serializable;

/**
 * DTO que representa a un futbolista con sus atributos, nivel físico y estadísticas.
 */
public class JugadorDTO implements Serializable {
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

    // Estado fisiológico y disciplinario
    private int energia; // 0 a 100%
    private EstadoJugadorEnum estado;
    private int jornadasSancionRestantes;
    private int semanasLesionRestantes;

    // Estadísticas en la temporada
    private int partidosJugados;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private double calificacionPromedio;

    public JugadorDTO() {
        this.estado = EstadoJugadorEnum.OPTIMO;
        this.energia = 100;
    }

    public JugadorDTO(String idJugador, String nombre, int dorsal, PosicionEnum posicion, String clubId,
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
        this.estado = EstadoJugadorEnum.OPTIMO;
        this.calificacionPromedio = 6.0;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDorsal() {
        return dorsal;
    }

    public void setDorsal(int dorsal) {
        this.dorsal = dorsal;
    }

    public PosicionEnum getPosicion() {
        return posicion;
    }

    public void setPosicion(PosicionEnum posicion) {
        this.posicion = posicion;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public int getPase() {
        return pase;
    }

    public void setPase(int pase) {
        this.pase = pase;
    }

    public int getTiro() {
        return tiro;
    }

    public void setTiro(int tiro) {
        this.tiro = tiro;
    }

    public int getDefensa() {
        return defensa;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    public int getFisico() {
        return fisico;
    }

    public void setFisico(int fisico) {
        this.fisico = fisico;
    }

    public int getEnergia() {
        return energia;
    }

    public void setEnergia(int energia) {
        this.energia = Math.max(0, Math.min(100, energia));
    }

    public EstadoJugadorEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoJugadorEnum estado) {
        this.estado = estado;
    }

    public int getJornadasSancionRestantes() {
        return jornadasSancionRestantes;
    }

    public void setJornadasSancionRestantes(int jornadasSancionRestantes) {
        this.jornadasSancionRestantes = jornadasSancionRestantes;
    }

    public int getSemanasLesionRestantes() {
        return semanasLesionRestantes;
    }

    public void setSemanasLesionRestantes(int semanasLesionRestantes) {
        this.semanasLesionRestantes = semanasLesionRestantes;
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

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    /**
     * Calcula la media general ponderada del futbolista.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getMediaGeneral() {
        return (velocidad + pase + tiro + defensa + fisico) / 5.0;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isDisponible() {
        return estado != null && estado.isAptoParaJugar();
    }

    @Override
    public String toString() {
        return "JugadorDTO{" +
                "#" + dorsal + " " + nombre +
                " [" + posicion + "]" +
                ", Media=" + String.format("%.1f", getMediaGeneral()) +
                ", Energía=" + energia + "%" +
                ", Estado=" + estado +
                '}';
    }
}
