package com.soccermanager.server.lobby;

import com.soccermanager.common.dto.ReporteRivalDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.services.ScoutingFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Bloque 5: Coordinador de Sala, Modo 2P, Timer de 60s y ScoutingFacade.
 */
public class LobbyYScoutingTest {

    private GestorModoYSala gestorSala;
    private ScoutingFacade scoutingFacade;

    @BeforeEach
    public void setUp() {
        gestorSala = new GestorModoYSala();
        scoutingFacade = new ScoutingFacade();
    }

    @AfterEach
    public void tearDown() {
        gestorSala.getTemporizador().apagar();
    }

    @Test
    public void testModoIndividualSimulacionDirecta() {
        gestorSala.configurarModo(ModoJuegoEnum.SOLO);
        gestorSala.conectarDT(1, "dt_solitario", "club-1", "Nacional");

        AtomicBoolean simulacionEjecutada = new AtomicBoolean(false);

        gestorSala.solicitarSimulacion("dt_solitario", () -> simulacionEjecutada.set(true));

        assertTrue(simulacionEjecutada.get(), "En modo individual la simulación debe dispararse directamente.");
        SalaEstadoDTO estado = gestorSala.getEstadoSala();
        assertEquals(EstadoSalaEnum.FINALIZADA, estado.getEstadoSala());
    }

    @Test
    public void testModoDosJugadoresRequiereDobleConexion() {
        gestorSala.configurarModo(ModoJuegoEnum.DOS_JUGADORES);
        gestorSala.conectarDT(1, "dt_uno", "club-1", "Nacional");

        // Faltando DT2, intentar simular debe fallar
        assertThrows(IllegalStateException.class, () -> {
            gestorSala.solicitarSimulacion("dt_uno", () -> {});
        });
    }

    @Test
    public void testModoDosJugadoresPrimerEnvioActivaContador60s() {
        gestorSala.configurarModo(ModoJuegoEnum.DOS_JUGADORES);
        gestorSala.conectarDT(1, "dt_uno", "c-1", "Nacional");
        gestorSala.conectarDT(2, "dt_dos", "c-2", "Millonarios");

        AtomicBoolean simulacionEjecutada = new AtomicBoolean(false);

        // DT 1 termina primero y presiona Simular
        gestorSala.solicitarSimulacion("dt_uno", () -> simulacionEjecutada.set(true));

        // No se debe simular aún
        assertFalse(simulacionEjecutada.get(), "No debe simularse inmediatamente; debe esperar al segundo DT.");

        SalaEstadoDTO estado = gestorSala.getEstadoSala();
        assertEquals(EstadoSalaEnum.CUENTA_REGRESIVA, estado.getEstadoSala());
        assertEquals("dt_uno", estado.getQuienDisparoTimer());
        assertTrue(estado.getSegundosRestantes() > 0, "El temporizador debe tener segundos restantes (máx 60).");
        assertTrue(estado.isDt1Listo());
        assertFalse(estado.isDt2Listo());
    }

    @Test
    public void testModoDosJugadoresResolucionAnticipada() {
        gestorSala.configurarModo(ModoJuegoEnum.DOS_JUGADORES);
        gestorSala.conectarDT(1, "dt_uno", "c-1", "Nacional");
        gestorSala.conectarDT(2, "dt_dos", "c-2", "Millonarios");

        AtomicBoolean simulacionEjecutada = new AtomicBoolean(false);

        // DT 1 activa el timer
        gestorSala.solicitarSimulacion("dt_uno", () -> simulacionEjecutada.set(true));
        assertTrue(gestorSala.getTemporizador().isActivo());

        // DT 2 confirma a tiempo antes de los 60s
        gestorSala.solicitarSimulacion("dt_dos", () -> simulacionEjecutada.set(true));

        // El timer debe cancelarse y la simulación debe ejecutarse
        assertFalse(gestorSala.getTemporizador().isActivo(), "El timer debe haberse cancelado.");
        assertTrue(simulacionEjecutada.get(), "La simulación debe ejecutarse de inmediato.");
        assertEquals(EstadoSalaEnum.FINALIZADA, gestorSala.getEstadoSala().getEstadoSala());
    }

    @Test
    public void testTemporizadorEjecutaTimeoutAlLlegarACero() throws InterruptedException {
        TemporizadorJornada temporizador = new TemporizadorJornada();
        CountDownLatch timeout = new CountDownLatch(1);
        List<Integer> ticks = new ArrayList<>();

        try {
            temporizador.iniciarCountdown(1, ticks::add, timeout::countDown);

            assertTrue(timeout.await(3, TimeUnit.SECONDS), "El timeout debe ejecutarse al llegar a cero.");
            assertEquals(List.of(0), ticks);
            assertEquals(0, temporizador.getSegundosRestantes());
            assertFalse(temporizador.isActivo());
        } finally {
            temporizador.apagar();
        }
    }

    @Test
    public void testScoutingFacadeGeneraReporteCompleto() {
        Club rival = new Club("riv-01", "Junior FC", "Barranquilla", "Metropolitano");
        rival.setFormacionPredeterminada(FormacionEnum.F_4_3_3);
        rival.setEstrategiaPredeterminada(EstrategiaEnum.CONTRAATAQUE);

        // Agregar futbolistas
        rival.agregarJugador(new Jugador("r-1", "Viera", 1, PosicionEnum.PORTERO, "riv-01", 60, 60, 30, 80, 80));
        rival.agregarJugador(new Jugador("r-2", "Fuentes", 3, PosicionEnum.DEFENSA, "riv-01", 75, 70, 40, 75, 78));
        rival.agregarJugador(new Jugador("r-3", "Cantillo", 8, PosicionEnum.MEDIOCAMPISTA, "riv-01", 70, 85, 65, 70, 75));
        rival.agregarJugador(new Jugador("r-4", "Bacca", 9, PosicionEnum.DELANTERO, "riv-01", 80, 75, 90, 40, 82));

        List<ResultadoPartidoDTO> historial = new ArrayList<>();
        historial.add(new ResultadoPartidoDTO("p-1", 1, "riv-01", "Junior FC", 2, "otro", "Rival", 0)); // Victoria
        historial.add(new ResultadoPartidoDTO("p-2", 2, "riv-01", "Junior FC", 1, "otro", "Rival", 1)); // Empate

        ReporteRivalDTO reporte = scoutingFacade.generarReporteRival(rival, historial);

        assertNotNull(reporte);
        assertEquals("Junior FC", reporte.getNombreRival());
        assertEquals(FormacionEnum.F_4_3_3, reporte.getFormacionHabitual());
        assertEquals(EstrategiaEnum.CONTRAATAQUE, reporte.getEstiloPredominante());
        assertTrue(reporte.getMediaDefensa() > 0);
        assertTrue(reporte.getMediaMediocampo() > 0);
        assertTrue(reporte.getMediaAtaque() > 0);
        assertNotNull(reporte.getJugadorEstrella());
        assertEquals("Bacca", reporte.getJugadorEstrella().getNombre());
        assertEquals(2, reporte.getRachaUltimos5().size());
        assertEquals("V", reporte.getRachaUltimos5().get(0));
        assertEquals("E", reporte.getRachaUltimos5().get(1));
    }
}
