package com.soccermanager.server.persistence;

import com.soccermanager.common.dto.PerfilDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.dto.SesionDTO;
import com.soccermanager.server.services.PerfilService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la persistencia en archivos de perfiles (Bloque 2).
 */
public class PerfilServiceTest {

    private static final String RUTA_TEST = "data/perfiles_test";
    private PerfilArchivoRepository repository;
    private PerfilService service;

    @BeforeEach
    public void setUp() {
        limpiarDirectorioPrueba();
        repository = new PerfilArchivoRepository(RUTA_TEST);
        service = new PerfilService(repository);
    }

    @AfterEach
    public void tearDown() {
        limpiarDirectorioPrueba();
    }

    private void limpiarDirectorioPrueba() {
        Path path = Paths.get(RUTA_TEST);
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
    public void testCreacionAutomaticaDePerfilNuevo() {
        String usuario = "dt_novato";
        String pass = "secreto123";

        assertFalse(repository.existePerfil(usuario));

        SesionDTO sesion = service.autenticarOCrearPerfil(usuario, pass, "club-01", "Atlético Prueba");

        assertNotNull(sesion);
        assertTrue(sesion.isPerfilNuevoCreado(), "Debe marcar que el perfil fue creado automáticamente.");
        assertEquals(usuario, sesion.getNombreUsuario());
        assertEquals("club-01", sesion.getClubId());

        // Comprobar que el archivo existe físicamente en disco
        assertTrue(repository.existePerfil(usuario), "El archivo JSON debe existir físicamente en disco.");

        Optional<PerfilUsuario> guardado = repository.buscarPorNombreUsuario(usuario);
        assertTrue(guardado.isPresent());
        assertEquals(usuario, guardado.get().getNombreUsuario());
        assertNotEquals(pass, guardado.get().getPasswordHash(), "La contraseña debe estar hasheada y no en texto plano.");
    }

    @Test
    public void testLoginConPerfilExistenteYPasswordCorrecta() {
        String usuario = "dt_veterano";
        String pass = "clave456";

        // Primer ingreso: auto-creación
        service.autenticarOCrearPerfil(usuario, pass, "club-02", "Deportivo Prueba");

        // Segundo ingreso: login exitoso
        SesionDTO sesion2 = service.autenticarOCrearPerfil(usuario, pass, "club-02", "Deportivo Prueba");

        assertNotNull(sesion2);
        assertFalse(sesion2.isPerfilNuevoCreado(), "No debe ser nuevo perfil.");
        assertEquals(usuario, sesion2.getNombreUsuario());
    }

    @Test
    public void testLoginActualizaNombreDeClubRenombrado() {
        String usuario = "dt_barcelona";
        String pass = "clave123";
        service.autenticarOCrearPerfil(usuario, pass, "club-nal", "Atlético Nacional");

        SesionDTO sesion = service.autenticarOCrearPerfil(usuario, pass, "club-nal", "Barcelona");

        assertEquals("Barcelona", sesion.getNombreClub());
        assertEquals("Barcelona", repository.buscarPorNombreUsuario(usuario).orElseThrow().getNombreClub());
    }

    @Test
    public void testLoginConPasswordIncorrectaLanzaExcepcion() {
        String usuario = "dt_seguridad";
        String pass = "clave_correcta";

        service.autenticarOCrearPerfil(usuario, pass, "club-03", "Racing Prueba");

        assertThrows(SecurityException.class, () -> {
            service.autenticarOCrearPerfil(usuario, "clave_falsa", "club-03", "Racing Prueba");
        }, "Debe arrojar SecurityException cuando la contraseña es errónea.");
    }

    @Test
    public void testActualizacionDeEstadisticasYPersistenciaEnArchivo() {
        String usuario = "dt_campeon";
        String pass = "pass123";

        service.autenticarOCrearPerfil(usuario, pass, "club-01", "Club Triunfo");

        // Simular victoria: 3 a 1 jugando como local
        ResultadoPartidoDTO resultado = new ResultadoPartidoDTO("part-01", 1, "club-01", "Club Triunfo", 3, "club-02", "Rival", 1);
        service.registrarResultadoPartido(usuario, resultado, true);

        Optional<PerfilDTO> perfilActualizado = service.obtenerPerfil(usuario);
        assertTrue(perfilActualizado.isPresent());
        assertEquals(1, perfilActualizado.get().getPartidosJugados());
        assertEquals(1, perfilActualizado.get().getPartidosGanados());
        assertEquals(0, perfilActualizado.get().getPartidosPerdidos());
        assertEquals(3, perfilActualizado.get().getPuntosTotales());
        assertEquals(3, perfilActualizado.get().getGolesAFavor());
        assertEquals(1, perfilActualizado.get().getGolesEnContra());
    }
}
