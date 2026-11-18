package com.soccermanager.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;

import java.io.Serializable;

/**
 * DTO que reporta el estado de sincronización en tiempo real de la sala de espera y el temporizador de 60s.
 */
public class SalaEstadoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private ModoJuegoEnum modoActivo;
    private EstadoSalaEnum estadoSala;

    // Participantes
    private boolean dt1Conectado;
    private boolean dt2Conectado;
    private String nombreDt1;
    private String nombreDt2;
    private String clubDt1;
    private String clubDt2;

    // Estado táctico y simulación
    private boolean dt1Listo;
    private boolean dt2Listo;

    // Temporizador de 60 segundos
    private int segundosRestantes;
    private String quienDisparoTimer; // Nombre del DT que confirmó primero
    private String mensaje;

    public SalaEstadoDTO() {
        this.modoActivo = ModoJuegoEnum.SOLO;
        this.estadoSala = EstadoSalaEnum.EN_PLANIFICACION;
        this.segundosRestantes = 0;
    }

    public SalaEstadoDTO(ModoJuegoEnum modoActivo, EstadoSalaEnum estadoSala,
                         boolean dt1Listo, boolean dt2Listo, int segundosRestantes, String mensaje) {
        this.modoActivo = modoActivo;
        this.estadoSala = estadoSala;
        this.dt1Listo = dt1Listo;
        this.dt2Listo = dt2Listo;
        this.segundosRestantes = segundosRestantes;
        this.mensaje = mensaje;
    }

    public ModoJuegoEnum getModoActivo() {
        return modoActivo;
    }

    public void setModoActivo(ModoJuegoEnum modoActivo) {
        this.modoActivo = modoActivo;
    }

    public EstadoSalaEnum getEstadoSala() {
        return estadoSala;
    }

    public void setEstadoSala(EstadoSalaEnum estadoSala) {
        this.estadoSala = estadoSala;
    }

    public boolean isDt1Conectado() {
        return dt1Conectado;
    }

    public void setDt1Conectado(boolean dt1Conectado) {
        this.dt1Conectado = dt1Conectado;
    }

    public boolean isDt2Conectado() {
        return dt2Conectado;
    }

    public void setDt2Conectado(boolean dt2Conectado) {
        this.dt2Conectado = dt2Conectado;
    }

    public String getNombreDt1() {
        return nombreDt1;
    }

    public void setNombreDt1(String nombreDt1) {
        this.nombreDt1 = nombreDt1;
    }

    public String getNombreDt2() {
        return nombreDt2;
    }

    public void setNombreDt2(String nombreDt2) {
        this.nombreDt2 = nombreDt2;
    }

    public String getClubDt1() {
        return clubDt1;
    }

    public void setClubDt1(String clubDt1) {
        this.clubDt1 = clubDt1;
    }

    public String getClubDt2() {
        return clubDt2;
    }

    public void setClubDt2(String clubDt2) {
        this.clubDt2 = clubDt2;
    }

    public boolean isDt1Listo() {
        return dt1Listo;
    }

    public void setDt1Listo(boolean dt1Listo) {
        this.dt1Listo = dt1Listo;
    }

    public boolean isDt2Listo() {
        return dt2Listo;
    }

    public void setDt2Listo(boolean dt2Listo) {
        this.dt2Listo = dt2Listo;
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(int segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }

    public String getQuienDisparoTimer() {
        return quienDisparoTimer;
    }

    public void setQuienDisparoTimer(String quienDisparoTimer) {
        this.quienDisparoTimer = quienDisparoTimer;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isCuentaRegresivaActiva() {
        return estadoSala == EstadoSalaEnum.CUENTA_REGRESIVA && segundosRestantes > 0;
    }

    @Override
    public String toString() {
        return "SalaEstadoDTO{" +
                "modo=" + modoActivo +
                ", estado=" + estadoSala +
                ", DT1_Listo=" + dt1Listo +
                ", DT2_Listo=" + dt2Listo +
                ", SegundosRestantes=" + segundosRestantes +
                ", Mensaje='" + mensaje + '\'' +
                '}';
    }
}
