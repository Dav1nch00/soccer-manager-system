package com.soccermanager.server;

import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.domain.strategy.EstrategiaFactory;
import com.soccermanager.server.engine.SimuladorJornada;
import com.soccermanager.server.engine.pipeline.ContextoPartido;
import com.soccermanager.server.lobby.GestorModoYSala;
import com.soccermanager.server.lobby.TemporizadorJornada;
import com.soccermanager.server.persistence.PerfilArchivoRepository;
import com.soccermanager.server.persistence.PerfilUsuario;
import com.soccermanager.server.services.PerfilService;
import com.soccermanager.server.services.TablaPosicionesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Pruebas Integradas, Concurrencia y Calidad (QA) - Bloque 8.
 * Valida el comportamiento bajo estrés concurrente, persistencia multi-hilo en disco
 * y la coordinación de torneos multijugador.
 */
public class SimuladorCompletoIntegrationTest {

    private static final String RUTA_TEST = "data/perfiles_qa_test";
    private PerfilArchivoRepository repo;
    private PerfilService perfilService;
    private SimuladorJornada simuladorJornada;
    private TablaPosicionesService tablaService;

    @BeforeEach
    public void setUp() throws IOException {
        limpiarDirectorioPrueba(RUTA_TEST);
        repo = new PerfilArchivoRepository(RUTA_TEST);
        perfilService = new PerfilService(repo);
        simuladorJornada = new SimuladorJornada(4);
        tablaService = new TablaPosicionesService();
    }

    @AfterEach
    public void tearDown() throws IOException {
        simuladorJornada.apagar();
        limpiarDirectorioPrueba(RUTA_TEST);
    }

    private void limpiarDirectorioPrueba(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        if (Files.exists(path)) {
            try (Stream<Path> paths = Files.walk(path)) {
                for (Path entry : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(entry);
                }
            }
        }
    }

    private Club crearClubConNombres(String id, String nombre) {
        Club c = new Club(id, nombre, "Ciudad", "Estadio");
        c.agregarJugador(new Jugador(id + "-1", "POR " + nombre, 1, PosicionEnum.PORTERO, id, 65, 65, 30, 80, 80));
        for (int i = 2; i <= 5; i++) {
            c.agregarJugador(new Jugador(id + "-" + i, "DEF " + i, i, PosicionEnum.DEFENSA, id, 70, 70, 40, 75, 78));
        }
        for (int i = 6; i <= 9; i++) {
            c.agregarJugador(new Jugador(id + "-" + i, "MED " + i, i, PosicionEnum.MEDIOCAMPISTA, id, 75, 80, 70, 65, 75));
        }
        for (int i = 10; i <= 11; i++) {
            c.agregarJugador(new Jugador(id + "-" + i, "DEL " + i, i, PosicionEnum.DELANTERO, id, 85, 75, 85, 40, 78));
        }
        return c;
    }

    @Test
    public void testSimulacionConcurrenteMasivaSinRaceConditions() throws Exception {
        int totalPartidos = 10; // 20 equipos compitiendo a la vez
        List<ContextoPartido> partidos = new ArrayList<>();

        for (int i = 1; i <= totalPartidos; i++) {
            Club loc = crearClubConNombres("loc-" + i, "Local " + i);
            Club vis = crearClubConNombres("vis-" + i, "Visitante " + i);

            Equipo eqLoc = new Equipo(loc.getIdClub(), loc.getNombre(), FormacionEnum.F_4_4_2);
            for (Jugador j : loc.getPlantilla()) eqLoc.agregarTitular(j);

            Equipo eqVis = new Equipo(vis.getIdClub(), vis.getNombre(), FormacionEnum.F_4_4_2);
            for (Jugador j : vis.getPlantilla()) eqVis.agregarTitular(j);

            partidos.add(new ContextoPartido("part-" + i, 1, loc, vis, eqLoc, eqVis,
                    EstrategiaFactory.obtenerEstrategia(EstrategiaEnum.POSESION),
                    EstrategiaFactory.obtenerEstrategia(EstrategiaEnum.CONTRAATAQUE)));
        }

        // Simular los 10 partidos simultáneamente
        List<ResultadoPartidoDTO> resultados = simuladorJornada.simularJornada(partidos);
        assertEquals(totalPartidos, resultados.size(), "Todos los 10 partidos deben finalizar.");

        // Actualizar la tabla de forma concurrente con múltiples hilos
        ExecutorService poolEscritura = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(resultados.size());
        List<Future<?>> actualizaciones = new ArrayList<>();

        for (ResultadoPartidoDTO r : resultados) {
            actualizaciones.add(poolEscritura.submit(() -> {
                try {
                    tablaService.actualizarResultado(r);
                } finally {
                    latch.countDown();
                }
            }));
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Todas las actualizaciones deben finalizar.");
            for (Future<?> actualizacion : actualizaciones) {
                actualizacion.get(5, TimeUnit.SECONDS);
            }
        } finally {
            poolEscritura.shutdownNow();
        }

        // Validar invariantes deportivos en la clasificación general
        List<TablaPosicionesService.FilaPosicion> tabla = tablaService.obtenerTablaOrdenada();
        assertEquals(20, tabla.size(), "Deben figurar exactamente los 20 clubes.");

        int totalPuntosSumados = 0;
        int totalGolesFavor = 0;
        int totalGolesContra = 0;

        for (TablaPosicionesService.FilaPosicion fila : tabla) {
            assertEquals(1, fila.getPartidosJugados(), "Cada club debe tener exactamente 1 partido jugado.");
            totalPuntosSumados += fila.getPuntos();
            totalGolesFavor += fila.getGolesAFavor();
            totalGolesContra += fila.getGolesEnContra();
        }

        // Invariante de goles: total goles a favor debe ser idéntico al total en contra
        assertEquals(totalGolesFavor, totalGolesContra, "Invariante matemático: goles a favor == goles en contra.");
        assertTrue(totalPuntosSumados >= totalPartidos * 2, "Puntos mínimos (si todos empatasen serian 2 por partido).");
    }

    @Test
    public void testConcurrenciaArchivosDePerfilMultiplesHilos() throws Exception {
        int hilos = 10;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(hilos);
        List<Future<?>> tareas = new ArrayList<>();

        for (int i = 0; i < hilos; i++) {
            final int id = i;
            tareas.add(pool.submit(() -> {
                try {
                    String usuario = "dt_concurrente_" + id;
                    perfilService.autenticarOCrearPerfil(usuario, "pass" + id, "club-" + id, "Club " + id);

                    // Escribir estadísticas concurrentemente
                    ResultadoPartidoDTO res = new ResultadoPartidoDTO(
                            "p-" + id, 1, "club-" + id, "Club " + id, 2, "rival", "Rival", 1);
                    perfilService.registrarResultadoPartido(usuario, res, true);

                    // Verificar que el archivo en disco no se corrompió
                    Optional<PerfilUsuario> guardado = repo.buscarPorNombreUsuario(usuario);
                    assertTrue(guardado.isPresent());
                    assertEquals(1, guardado.get().getPartidosGanados());
                    assertEquals(3, guardado.get().getPuntosTotales());
                } finally {
                    latch.countDown();
                }
            }));
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Todos los perfiles deben guardarse dentro del plazo.");
            for (Future<?> tarea : tareas) {
                tarea.get(5, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testFlujoCompletoTemporizador60sYTimeout() {
        TemporizadorControlado temporizador = new TemporizadorControlado();
        GestorModoYSala gestor = new GestorModoYSala(temporizador);
        gestor.configurarModo(ModoJuegoEnum.DOS_JUGADORES);
        gestor.conectarDT(1, "dt_alfa", "c-1", "Club Alfa");
        gestor.conectarDT(2, "dt_beta", "c-2", "Club Beta");

        AtomicBoolean jornadaSimulada = new AtomicBoolean(false);

        // DT 1 envía y solicita simular -> Arranca timer
        gestor.solicitarSimulacion("dt_alfa", () -> jornadaSimulada.set(true));
        assertEquals(EstadoSalaEnum.CUENTA_REGRESIVA, gestor.getEstadoSala().getEstadoSala());
        assertTrue(temporizador.isActivo());

        temporizador.dispararTimeout();

        assertTrue(jornadaSimulada.get(), "El timeout debe disparar la simulación.");
        assertEquals(EstadoSalaEnum.FINALIZADA, gestor.getEstadoSala().getEstadoSala());
        assertNotNull(gestor.getPlanDT1(), "El timeout debe completar el plan faltante del primer DT.");
        assertNotNull(gestor.getPlanDT2(), "El timeout debe completar el plan faltante del segundo DT.");
        temporizador.apagar();
    }

    @Test
    public void testPersistenciaHistoricaMultiplesJornadas() {
        String usuario = "dt_trayectoria";
        perfilService.autenticarOCrearPerfil(usuario, "secreto", "club-1", "Nacional");

        // Jornada 1: Victoria 3-1
        ResultadoPartidoDTO r1 = new ResultadoPartidoDTO("p1", 1, "club-1", "Nacional", 3, "c-2", "Rival", 1);
        perfilService.registrarResultadoPartido(usuario, r1, true);

        // Jornada 2: Empate 2-2
        ResultadoPartidoDTO r2 = new ResultadoPartidoDTO("p2", 2, "club-1", "Nacional", 2, "c-3", "Rival", 2);
        perfilService.registrarResultadoPartido(usuario, r2, true);

        // Jornada 3: Derrota 0-1
        ResultadoPartidoDTO r3 = new ResultadoPartidoDTO("p3", 3, "club-1", "Nacional", 0, "c-4", "Rival", 1);
        perfilService.registrarResultadoPartido(usuario, r3, true);

        // Validar en disco
        Optional<PerfilUsuario> perfil = repo.buscarPorNombreUsuario(usuario);
        assertTrue(perfil.isPresent());
        assertEquals(3, perfil.get().getPartidosJugados());
        assertEquals(1, perfil.get().getPartidosGanados());
        assertEquals(1, perfil.get().getPartidosEmpatados());
        assertEquals(1, perfil.get().getPartidosPerdidos());
        assertEquals(4, perfil.get().getPuntosTotales()); // 3 + 1 + 0 = 4 puntos
        assertEquals(5, perfil.get().getGolesAFavor());    // 3 + 2 + 0 = 5 goles
        assertEquals(4, perfil.get().getGolesEnContra());  // 1 + 2 + 1 = 4 goles
    }

    private static class TemporizadorControlado extends TemporizadorJornada {
        private Runnable onTimeout;
        private boolean activo;

        @Override
        public synchronized void iniciarCountdown(int segundos, Consumer<Integer> onTick, Runnable onTimeout) {
            this.onTimeout = onTimeout;
            this.activo = true;
        }

        @Override
        public synchronized void cancelar() {
            activo = false;
        }

        @Override
        public synchronized boolean isActivo() {
            return activo;
        }

        private void dispararTimeout() {
            Runnable callback;
            synchronized (this) {
                callback = onTimeout;
                activo = false;
            }
            if (callback != null) {
                callback.run();
            }
        }

        @Override
        public void apagar() {
            cancelar();
            super.apagar();
        }
    }
}
