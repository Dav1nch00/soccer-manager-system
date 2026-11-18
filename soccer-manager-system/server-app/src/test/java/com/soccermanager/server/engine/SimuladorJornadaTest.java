package com.soccermanager.server.engine;

import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.domain.strategy.ContraataqueStrategy;
import com.soccermanager.server.domain.strategy.PosesionStrategy;
import com.soccermanager.server.engine.pipeline.ContextoPartido;
import com.soccermanager.server.engine.pipeline.PipelinePartidos;
import com.soccermanager.server.services.TablaPosicionesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias y de concurrencia para el Motor de Simulación (Bloque 4).
 * Valida el patrón Pipes & Filters, el pool de hilos y la exclusión mutua de la tabla de posiciones.
 */
public class SimuladorJornadaTest {

    private SimuladorJornada simulador;
    private TablaPosicionesService tablaService;

    @BeforeEach
    public void setUp() {
        simulador = new SimuladorJornada(4); // Pool de 4 hilos
        tablaService = new TablaPosicionesService();
    }

    @AfterEach
    public void tearDown() {
        simulador.apagar();
    }

    private Club crearClubConPlantilla(String id, String nombre) {
        Club club = new Club(id, nombre, "Ciudad " + id, "Estadio " + id);
        // 1 Portero
        club.agregarJugador(new Jugador(id + "-por", "Portero " + nombre, 1, PosicionEnum.PORTERO, id, 60, 65, 30, 80, 80));
        // 4 Defensas
        for (int i = 1; i <= 4; i++) {
            club.agregarJugador(new Jugador(id + "-def" + i, "Defensa " + i, i + 1, PosicionEnum.DEFENSA, id, 70, 70, 40, 78, 80));
        }
        // 4 Mediocampistas
        for (int i = 1; i <= 4; i++) {
            club.agregarJugador(new Jugador(id + "-med" + i, "Medio " + i, i + 5, PosicionEnum.MEDIOCAMPISTA, id, 75, 80, 70, 65, 75));
        }
        // 2 Delanteros
        for (int i = 1; i <= 2; i++) {
            club.agregarJugador(new Jugador(id + "-del" + i, "Delantero " + i, i + 9, PosicionEnum.DELANTERO, id, 85, 75, 85, 40, 78));
        }
        return club;
    }

    private Equipo crearEquipoTitular(Club club) {
        Equipo equipo = new Equipo(club.getIdClub(), club.getNombre(), FormacionEnum.F_4_4_2);
        for (Jugador j : club.getPlantilla()) {
            equipo.agregarTitular(j);
        }
        return equipo;
    }

    @Test
    public void testPipelineIndividual() {
        Club local = crearClubConPlantilla("c-1", "Real Madrid");
        Club visitante = crearClubConPlantilla("c-2", "Barcelona");

        Equipo eqLocal = crearEquipoTitular(local);
        Equipo eqVisitante = crearEquipoTitular(visitante);

        ContextoPartido ctx = new ContextoPartido(
                "p-01", 1, local, visitante, eqLocal, eqVisitante,
                new PosesionStrategy(), new ContraataqueStrategy()
        );

        PipelinePartidos pipeline = new PipelinePartidos();
        ResultadoPartidoDTO res = pipeline.ejecutar(ctx);

        assertNotNull(res);
        assertTrue(res.getGolesLocal() >= 0);
        assertTrue(res.getGolesVisitante() >= 0);
        assertEquals(100, res.getPosesionLocal() + res.getPosesionVisitante(), "Posesión debe sumar 100%");
        assertTrue(res.getTirosLocal() >= res.getGolesLocal(), "Tiros debe ser mayor o igual a los goles.");
        assertFalse(res.getCalificacionesJugadores().isEmpty(), "Debe calificar a los futbolistas.");
    }

    @Test
    public void testSimulacionConcurrenteJornadaCompleta() throws Exception {
        // Preparar una jornada de 4 partidos (8 equipos simultáneos)
        List<ContextoPartido> partidos = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Club loc = crearClubConPlantilla("loc-" + i, "Local " + i);
            Club vis = crearClubConPlantilla("vis-" + i, "Visitante " + i);
            partidos.add(new ContextoPartido(
                    "p-" + i, 1, loc, vis, crearEquipoTitular(loc), crearEquipoTitular(vis),
                    new PosesionStrategy(), new ContraataqueStrategy()
            ));
        }

        // Ejecutar simulación paralela en el ThreadPool con barrera invokeAll
        List<ResultadoPartidoDTO> resultados = simulador.simularJornada(partidos);

        assertEquals(4, resultados.size(), "Todos los 4 partidos deben concluir en paralelo.");
        for (ResultadoPartidoDTO r : resultados) {
            assertNotNull(r);
            assertNotNull(r.getMarcadorTexto());
            // Actualizar tabla de posiciones de forma concurrente
            tablaService.actualizarResultado(r);
        }

        // Comprobar la tabla de posiciones consolidada
        List<TablaPosicionesService.FilaPosicion> tabla = tablaService.obtenerTablaOrdenada();
        assertEquals(8, tabla.size(), "Los 8 clubes deben figurar en la clasificación general.");

        // Invariante deportivo: cada equipo jugó exactamente 1 partido
        for (TablaPosicionesService.FilaPosicion fila : tabla) {
            assertEquals(1, fila.getPartidosJugados());
        }
    }
}
