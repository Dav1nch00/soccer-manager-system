package com.soccermanager.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que empaqueta la inteligencia previa del próximo contrincante (Patrón Facade en servidor).
 */
public class ReporteRivalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String rivalId;
    private String nombreRival;
    private FormacionEnum formacionHabitual;
    private EstrategiaEnum estiloPredominante;
    private List<String> rachaUltimos5; // "V", "E", "D"
    private JugadorDTO jugadorEstrella;
    private double promedioGolesPorPartido;

    // Medias de potencia por línea
    private double mediaDefensa;
    private double mediaMediocampo;
    private double mediaAtaque;

    public ReporteRivalDTO() {
        this.rachaUltimos5 = new ArrayList<>();
    }

    public ReporteRivalDTO(String rivalId, String nombreRival, FormacionEnum formacionHabitual,
                           EstrategiaEnum estiloPredominante, JugadorDTO jugadorEstrella,
                           double mediaDefensa, double mediaMediocampo, double mediaAtaque) {
        this.rivalId = rivalId;
        this.nombreRival = nombreRival;
        this.formacionHabitual = formacionHabitual;
        this.estiloPredominante = estiloPredominante;
        this.jugadorEstrella = jugadorEstrella;
        this.mediaDefensa = mediaDefensa;
        this.mediaMediocampo = mediaMediocampo;
        this.mediaAtaque = mediaAtaque;
        this.rachaUltimos5 = new ArrayList<>();
    }

    public String getRivalId() {
        return rivalId;
    }

    public void setRivalId(String rivalId) {
        this.rivalId = rivalId;
    }

    public String getNombreRival() {
        return nombreRival;
    }

    public void setNombreRival(String nombreRival) {
        this.nombreRival = nombreRival;
    }

    public FormacionEnum getFormacionHabitual() {
        return formacionHabitual;
    }

    public void setFormacionHabitual(FormacionEnum formacionHabitual) {
        this.formacionHabitual = formacionHabitual;
    }

    public EstrategiaEnum getEstiloPredominante() {
        return estiloPredominante;
    }

    public void setEstiloPredominante(EstrategiaEnum estiloPredominante) {
        this.estiloPredominante = estiloPredominante;
    }

    public List<String> getRachaUltimos5() {
        return rachaUltimos5;
    }

    public void setRachaUltimos5(List<String> rachaUltimos5) {
        this.rachaUltimos5 = rachaUltimos5;
    }

    public JugadorDTO getJugadorEstrella() {
        return jugadorEstrella;
    }

    public void setJugadorEstrella(JugadorDTO jugadorEstrella) {
        this.jugadorEstrella = jugadorEstrella;
    }

    public double getPromedioGolesPorPartido() {
        return promedioGolesPorPartido;
    }

    public void setPromedioGolesPorPartido(double promedioGolesPorPartido) {
        this.promedioGolesPorPartido = promedioGolesPorPartido;
    }

    public double getMediaDefensa() {
        return mediaDefensa;
    }

    public void setMediaDefensa(double mediaDefensa) {
        this.mediaDefensa = mediaDefensa;
    }

    public double getMediaMediocampo() {
        return mediaMediocampo;
    }

    public void setMediaMediocampo(double mediaMediocampo) {
        this.mediaMediocampo = mediaMediocampo;
    }

    public double getMediaAtaque() {
        return mediaAtaque;
    }

    public void setMediaAtaque(double mediaAtaque) {
        this.mediaAtaque = mediaAtaque;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public double getMediaGlobalRival() {
        return (mediaDefensa + mediaMediocampo + mediaAtaque) / 3.0;
    }

    @Override
    public String toString() {
        return "ReporteRivalDTO{" +
                "rival='" + nombreRival + '\'' +
                ", formacion=" + formacionHabitual +
                ", estilo=" + estiloPredominante +
                ", estrella=" + (jugadorEstrella != null ? jugadorEstrella.getNombre() : "N/A") +
                ", DEF=" + String.format("%.1f", mediaDefensa) +
                ", MED=" + String.format("%.1f", mediaMediocampo) +
                ", ATA=" + String.format("%.1f", mediaAtaque) +
                '}';
    }
}
