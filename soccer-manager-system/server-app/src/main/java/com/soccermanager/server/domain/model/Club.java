package com.soccermanager.server.domain.model;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad de dominio que representa a una institución deportiva con su plantilla completa.
 */
public class Club implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idClub;
    private String nombre;
    private String ciudad;
    private String estadio;
    private FormacionEnum formacionPredeterminada;
    private EstrategiaEnum estrategiaPredeterminada;

    private final List<Jugador> plantilla;

    public Club(String idClub, String nombre, String ciudad, String estadio) {
        this.idClub = idClub;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.estadio = estadio;
        this.formacionPredeterminada = FormacionEnum.F_4_4_2;
        this.estrategiaPredeterminada = EstrategiaEnum.POSESION;
        this.plantilla = new ArrayList<>();
    }

    public void agregarJugador(Jugador jugador) {
        if (jugador != null && !plantilla.contains(jugador)) {
            plantilla.add(jugador);
        }
    }

    public List<Jugador> getJugadoresDisponibles() {
        return plantilla.stream()
                .filter(j -> j.getEstado().puedeJugar())
                .collect(Collectors.toList());
    }

    public List<Jugador> getJugadoresPorPosicion(PosicionEnum posicion) {
        return plantilla.stream()
                .filter(j -> j.getPosicion() == posicion)
                .collect(Collectors.toList());
    }

    public Jugador getJugadorEstrella() {
        return plantilla.stream()
                .max((j1, j2) -> Double.compare(
                        (j1.getTiro() + j1.getPase() + j1.getVelocidad() + j1.getDefensa() + j1.getFisico()),
                        (j2.getTiro() + j2.getPase() + j2.getVelocidad() + j2.getDefensa() + j2.getFisico())
                ))
                .orElse(null);
    }

    public String getIdClub() {
        return idClub;
    }

    public void setIdClub(String idClub) {
        this.idClub = idClub;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
    }

    public FormacionEnum getFormacionPredeterminada() {
        return formacionPredeterminada;
    }

    public void setFormacionPredeterminada(FormacionEnum formacionPredeterminada) {
        this.formacionPredeterminada = formacionPredeterminada;
    }

    public EstrategiaEnum getEstrategiaPredeterminada() {
        return estrategiaPredeterminada;
    }

    public void setEstrategiaPredeterminada(EstrategiaEnum estrategiaPredeterminada) {
        this.estrategiaPredeterminada = estrategiaPredeterminada;
    }

    public List<Jugador> getPlantilla() {
        return plantilla;
    }
}
