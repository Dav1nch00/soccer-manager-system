package com.soccermanager.server.services;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Jugador;

import java.util.*;

/**
 * Gestor maestro del torneo. Almacena la nómina de clubes participantes,
 * sus plantillas completas y el calendario deportivo.
 */
public class GestorLiga {

    private final Map<String, Club> clubesPorId;

    public GestorLiga() {
        this.clubesPorId = new HashMap<>();
        inicializarLigaPredeterminada();
    }

    private void inicializarLigaPredeterminada() {
        // Club 1
        registrarClubCompleto("club-nal", "Barcelona", "Barcelona", "Camp Nou",
                FormacionEnum.F_4_3_3, EstrategiaEnum.POSESION);
        // Club 2
        registrarClubCompleto("club-mil", "Millonarios FC", "Bogotá", "El Campín",
                FormacionEnum.F_4_4_2, EstrategiaEnum.CONTRAATAQUE);
        // Club 3
        registrarClubCompleto("club-jun", "Junior FC", "Barranquilla", "Metropolitano",
                FormacionEnum.F_4_3_3, EstrategiaEnum.PRESION_ALTA);
        // Club 4
        registrarClubCompleto("club-ame", "América de Cali", "Cali", "Pascual Guerrero",
                FormacionEnum.F_5_3_2, EstrategiaEnum.BLOQUE_BAJO);
    }

    private void registrarClubCompleto(String id, String nombre, String ciudad, String estadio,
                                       FormacionEnum formacion, EstrategiaEnum estrategia) {
        Club club = new Club(id, nombre, ciudad, estadio);
        club.setFormacionPredeterminada(formacion);
        club.setEstrategiaPredeterminada(estrategia);

        // 1 Portero
        club.agregarJugador(new Jugador(id + "-1", "Portero " + nombre, 1, PosicionEnum.PORTERO, id, 65, 68, 30, 84, 82));

        // 4 Defensas
        club.agregarJugador(new Jugador(id + "-2", "Lateral Der " + nombre, 2, PosicionEnum.DEFENSA, id, 80, 72, 45, 76, 78));
        club.agregarJugador(new Jugador(id + "-3", "Central Uno " + nombre, 3, PosicionEnum.DEFENSA, id, 72, 68, 40, 82, 85));
        club.agregarJugador(new Jugador(id + "-4", "Central Dos " + nombre, 4, PosicionEnum.DEFENSA, id, 70, 70, 38, 80, 83));
        club.agregarJugador(new Jugador(id + "-5", "Lateral Izq " + nombre, 5, PosicionEnum.DEFENSA, id, 78, 74, 48, 75, 77));

        // 4 Mediocampistas
        club.agregarJugador(new Jugador(id + "-6", "Volante Def " + nombre, 6, PosicionEnum.MEDIOCAMPISTA, id, 74, 80, 60, 79, 82));
        club.agregarJugador(new Jugador(id + "-7", "Volante Mixto " + nombre, 8, PosicionEnum.MEDIOCAMPISTA, id, 76, 82, 70, 72, 79));
        club.agregarJugador(new Jugador(id + "-8", "Extremo Der " + nombre, 7, PosicionEnum.MEDIOCAMPISTA, id, 86, 78, 74, 55, 73));
        club.agregarJugador(new Jugador(id + "-9", "Enganche " + nombre, 10, PosicionEnum.MEDIOCAMPISTA, id, 78, 88, 80, 50, 70));

        // 2 Delanteros
        club.agregarJugador(new Jugador(id + "-10", "Extremo Izq " + nombre, 11, PosicionEnum.DELANTERO, id, 88, 76, 78, 45, 74));
        club.agregarJugador(new Jugador(id + "-11", "Centrodelantero " + nombre, 9, PosicionEnum.DELANTERO, id, 82, 74, 88, 48, 80));

        // 2 Suplentes
        club.agregarJugador(new Jugador(id + "-12", "Suplente Uno " + nombre, 14, PosicionEnum.DEFENSA, id, 70, 68, 40, 72, 74));
        club.agregarJugador(new Jugador(id + "-13", "Suplente Dos " + nombre, 19, PosicionEnum.DELANTERO, id, 78, 70, 75, 42, 71));

        clubesPorId.put(id, club);
    }

    public Optional<Club> obtenerClubPorId(String id) {
        return Optional.ofNullable(clubesPorId.get(id));
    }

    public List<Club> listarClubes() {
        return new ArrayList<>(clubesPorId.values());
    }

    public Club obtenerPrimerClub() {
        return clubesPorId.values().iterator().next();
    }
}
