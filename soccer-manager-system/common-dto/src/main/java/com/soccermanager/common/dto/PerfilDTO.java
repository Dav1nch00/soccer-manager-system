package com.soccermanager.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que representa el perfil persistido del mánager y sus estadísticas históricas.
 */
public class PerfilDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPerfil;
    private String nombreUsuario;
    private String clubId;
    private String nombreClub;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaConexion;

    // Métricas históricas
    private int partidosJugados;
    private int partidosGanados;
    private int partidosEmpatados;
    private int partidosPerdidos;
    private int golesAFavor;
    private int golesEnContra;
    private int puntosTotales;
    private int titulosGanados;

    public PerfilDTO() {
    }

    public PerfilDTO(String idPerfil, String nombreUsuario, String clubId, String nombreClub) {
        this.idPerfil = idPerfil;
        this.nombreUsuario = nombreUsuario;
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaConexion = LocalDateTime.now();
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimaConexion() {
        return ultimaConexion;
    }

    public void setUltimaConexion(LocalDateTime ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(int partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public int getPartidosGanados() {
        return partidosGanados;
    }

    public void setPartidosGanados(int partidosGanados) {
        this.partidosGanados = partidosGanados;
    }

    public int getPartidosEmpatados() {
        return partidosEmpatados;
    }

    public void setPartidosEmpatados(int partidosEmpatados) {
        this.partidosEmpatados = partidosEmpatados;
    }

    public int getPartidosPerdidos() {
        return partidosPerdidos;
    }

    public void setPartidosPerdidos(int partidosPerdidos) {
        this.partidosPerdidos = partidosPerdidos;
    }

    public int getGolesAFavor() {
        return golesAFavor;
    }

    public void setGolesAFavor(int golesAFavor) {
        this.golesAFavor = golesAFavor;
    }

    public int getGolesEnContra() {
        return golesEnContra;
    }

    public void setGolesEnContra(int golesEnContra) {
        this.golesEnContra = golesEnContra;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }

    public void setPuntosTotales(int puntosTotales) {
        this.puntosTotales = puntosTotales;
    }

    public int getTitulosGanados() {
        return titulosGanados;
    }

    public void setTitulosGanados(int titulosGanados) {
        this.titulosGanados = titulosGanados;
    }

    public int getDiferenciaGoles() {
        return golesAFavor - golesEnContra;
    }

    @Override
    public String toString() {
        return "PerfilDTO{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", club='" + nombreClub + '\'' +
                ", PJ=" + partidosJugados +
                ", PG=" + partidosGanados +
                ", PE=" + partidosEmpatados +
                ", PP=" + partidosPerdidos +
                ", Puntos=" + puntosTotales +
                '}';
    }
}
