package com.soccermanager.server.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soccermanager.common.dto.PerfilDTO;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia que representa el archivo físico individual del mánager en el servidor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerfilUsuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPerfil;
    private String nombreUsuario;
    private String passwordHash;
    private String clubId;
    private String nombreClub;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimaConexion;

    // Métricas deportivas persistidas
    private int partidosJugados;
    private int partidosGanados;
    private int partidosEmpatados;
    private int partidosPerdidos;
    private int golesAFavor;
    private int golesEnContra;
    private int puntosTotales;
    private int titulosGanados;

    public PerfilUsuario() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaConexion = LocalDateTime.now();
    }

    public PerfilUsuario(String idPerfil, String nombreUsuario, String passwordHash, String clubId, String nombreClub) {
        this.idPerfil = idPerfil;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaConexion = LocalDateTime.now();
    }

    // Métodos de negocio para actualización de estadísticas tras simulación
    public void registrarVictoria(int golesAnotados, int golesRecibidos) {
        this.partidosJugados++;
        this.partidosGanados++;
        this.golesAFavor += golesAnotados;
        this.golesEnContra += golesRecibidos;
        this.puntosTotales += 3;
        this.ultimaConexion = LocalDateTime.now();
    }

    public void registrarEmpate(int golesAnotados, int golesRecibidos) {
        this.partidosJugados++;
        this.partidosEmpatados++;
        this.golesAFavor += golesAnotados;
        this.golesEnContra += golesRecibidos;
        this.puntosTotales += 1;
        this.ultimaConexion = LocalDateTime.now();
    }

    public void registrarDerrota(int golesAnotados, int golesRecibidos) {
        this.partidosJugados++;
        this.partidosPerdidos++;
        this.golesAFavor += golesAnotados;
        this.golesEnContra += golesRecibidos;
        this.ultimaConexion = LocalDateTime.now();
    }

    public PerfilDTO toDTO() {
        PerfilDTO dto = new PerfilDTO(this.idPerfil, this.nombreUsuario, this.clubId, this.nombreClub);
        dto.setFechaCreacion(this.fechaCreacion);
        dto.setUltimaConexion(this.ultimaConexion);
        dto.setPartidosJugados(this.partidosJugados);
        dto.setPartidosGanados(this.partidosGanados);
        dto.setPartidosEmpatados(this.partidosEmpatados);
        dto.setPartidosPerdidos(this.partidosPerdidos);
        dto.setGolesAFavor(this.golesAFavor);
        dto.setGolesEnContra(this.golesEnContra);
        dto.setPuntosTotales(this.puntosTotales);
        dto.setTitulosGanados(this.titulosGanados);
        return dto;
    }

    // Getters y Setters
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
}
