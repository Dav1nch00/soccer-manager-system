package com.soccermanager.client.network;

import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.ReporteRivalDTO;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.dto.SesionDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;
import com.soccermanager.server.api.ServidorHttp;
import com.soccermanager.server.persistence.PerfilArchivoRepository;
import com.soccermanager.server.services.PerfilService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para la Capa de Red y API REST (Bloque 6).
 * Levanta el ServidorHttp en un puerto efímero y ejecuta peticiones reales mediante MotorConexion.
 */
public class ClienteServidorApiTest {

    private static final String RUTA_PERFILES_TEST = "data/perfiles_api_test";
    private ServidorHttp servidor;
    private MotorConexion cliente;

    @BeforeEach
    public void setUp() throws IOException {
        limpiarDirectorio(RUTA_PERFILES_TEST);
        PerfilArchivoRepository repo = new PerfilArchivoRepository(RUTA_PERFILES_TEST);
        PerfilService perfilService = new PerfilService(repo);

        // Puerto 0 para asignación dinámica automática del SO
        servidor = new ServidorHttp(0, perfilService, new com.soccermanager.server.lobby.GestorModoYSala(),
                new com.soccermanager.server.services.GestorLiga(),
                new com.soccermanager.server.services.ScoutingFacade(),
                new com.soccermanager.server.engine.SimuladorJornada(2),
                new com.soccermanager.server.services.TablaPosicionesService());
        servidor.iniciar();

        int puertoAsignado = servidor.getPuertoAsignado();
        cliente = new MotorConexion("http://localhost:" + puertoAsignado);
    }

    @AfterEach
    public void tearDown() {
        if (servidor != null) {
            servidor.detener();
        }
        limpiarDirectorio(RUTA_PERFILES_TEST);
    }

    private void limpiarDirectorio(String ruta) {
        Path path = Paths.get(ruta);
        if (Files.exists(path)) {
            try {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void testFlujoCompletoClienteServidorHttp() throws Exception {
        // 1. Autenticación / Registro automático de perfil vía red
        SesionDTO sesion = cliente.loginORegistro("dt_red_test", "claveSegura123", "club-nal");
        assertNotNull(sesion);
        assertEquals("dt_red_test", sesion.getNombreUsuario());
        assertEquals("Barcelona", sesion.getNombreClub());
        assertTrue(sesion.isPerfilNuevoCreado());

        // 2. Configuración de Modo
        SalaEstadoDTO sala = cliente.configurarModo(ModoJuegoEnum.SOLO);
        assertNotNull(sala);
        assertEquals(ModoJuegoEnum.SOLO, sala.getModoActivo());

        // 3. Consulta de Plantilla
        List<JugadorDTO> plantilla = cliente.obtenerPlantilla("club-nal");
        assertNotNull(plantilla);
        assertTrue(plantilla.size() >= 11, "Debe tener al menos 11 futbolistas.");

        // 4. Consulta de Scouting del Rival
        ReporteRivalDTO scouting = cliente.obtenerScouting("club-mil");
        assertNotNull(scouting);
        assertEquals("Millonarios FC", scouting.getNombreRival());
        assertTrue(scouting.getMediaAtaque() > 0);

        // 5. Simulación de Jornada
        SalaEstadoDTO resultadoSimulacion = cliente.solicitarSimulacion("dt_red_test");
        assertNotNull(resultadoSimulacion);
        assertEquals(EstadoSalaEnum.FINALIZADA, resultadoSimulacion.getEstadoSala());

        // 6. Consulta de Clasificación y Marcadores
        Map<String, Object> torneo = cliente.obtenerTablaYResultados();
        assertNotNull(torneo);
        assertTrue(torneo.containsKey("tabla"));
        assertTrue(torneo.containsKey("resultadosUltimaFecha"));
    }
}
