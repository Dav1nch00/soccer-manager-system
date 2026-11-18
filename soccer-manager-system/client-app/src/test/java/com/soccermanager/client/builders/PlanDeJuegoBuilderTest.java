package com.soccermanager.client.builders;

import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.PlanDeJuegoDTO;
import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Patrón Creacional Builder (PlanDeJuegoBuilder).
 * Verifica las validaciones locales de alineación (11 titulares, no repetidos, posiciones cubiertas, no lesionados).
 */
public class PlanDeJuegoBuilderTest {

    private PlanDeJuegoBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = new PlanDeJuegoBuilder();
        builder.setClub("club-01", "Barcelona");
        builder.setFormacion(FormacionEnum.F_4_4_2);
        builder.setEstrategia(EstrategiaEnum.POSESION);
    }

    private JugadorDTO crearJugador(String id, String nombre, PosicionEnum pos, EstadoJugadorEnum estado) {
        JugadorDTO j = new JugadorDTO(id, nombre, 10, pos, "club-01", 75, 75, 75, 75, 75);
        j.setEstado(estado);
        return j;
    }

    @Test
    public void testConstruccionExitosa11Titulares() {
        // 1 Portero
        builder.agregarTitular(crearJugador("p-1", "Ospina", PosicionEnum.PORTERO, EstadoJugadorEnum.OPTIMO));
        // 4 Defensas
        builder.agregarTitular(crearJugador("d-1", "Central 1", PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("d-2", "Central 2", PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("d-3", "Lateral D", PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("d-4", "Lateral I", PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        // 4 Medios
        builder.agregarTitular(crearJugador("m-1", "Volante 1", PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("m-2", "Volante 2", PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("m-3", "Extremo D", PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("m-4", "Extremo I", PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        // 2 Delanteros
        builder.agregarTitular(crearJugador("f-1", "Delantero 1", PosicionEnum.DELANTERO, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("f-2", "Delantero 2", PosicionEnum.DELANTERO, EstadoJugadorEnum.OPTIMO));

        PlanDeJuegoDTO plan = builder.build();

        assertNotNull(plan);
        assertEquals(11, plan.getCantidadTitulares());
        assertEquals("club-01", plan.getClubId());
        assertEquals(FormacionEnum.F_4_4_2, plan.getFormacion());
        assertEquals(EstrategiaEnum.POSESION, plan.getEstrategia());
    }

    @Test
    public void testAlineacionIncompletaLanzaExcepcion() {
        // Solo 2 jugadores
        builder.agregarTitular(crearJugador("p-1", "Ospina", PosicionEnum.PORTERO, EstadoJugadorEnum.OPTIMO));
        builder.agregarTitular(crearJugador("d-1", "Central 1", PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));

        Exception ex = assertThrows(IllegalStateException.class, () -> builder.build());
        assertTrue(ex.getMessage().contains("debe tener exactamente 11 jugadores"));
    }

    @Test
    public void testJugadorDuplicadoLanzaExcepcion() {
        JugadorDTO jugador = crearJugador("j-repetido", "Crack", PosicionEnum.DELANTERO, EstadoJugadorEnum.OPTIMO);
        builder.agregarTitular(jugador);

        // Intentar alinearlo dos veces
        Exception ex = assertThrows(IllegalStateException.class, () -> builder.agregarTitular(jugador));
        assertTrue(ex.getMessage().contains("ya está alineado"));
    }

    @Test
    public void testJugadorLesionadoOSancionadoNoPuedeAlinear() {
        JugadorDTO lesionado = crearJugador("j-lesionado", "Enfermo", PosicionEnum.DEFENSA, EstadoJugadorEnum.LESIONADO);
        Exception exLes = assertThrows(IllegalStateException.class, () -> builder.agregarTitular(lesionado));
        assertTrue(exLes.getMessage().contains("no está disponible"));

        JugadorDTO sancionado = crearJugador("j-sancionado", "Expulsado", PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.SANCIONADO);
        Exception exSanc = assertThrows(IllegalStateException.class, () -> builder.agregarTitular(sancionado));
        assertTrue(exSanc.getMessage().contains("no está disponible"));
    }

    @Test
    public void testFaltaDePorteroLanzaExcepcion() {
        // 11 jugadores pero ninguno es portero
        for (int i = 1; i <= 6; i++) {
            builder.agregarTitular(crearJugador("def-" + i, "Def " + i, PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        }
        for (int i = 1; i <= 5; i++) {
            builder.agregarTitular(crearJugador("med-" + i, "Med " + i, PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        }

        Exception ex = assertThrows(IllegalStateException.class, () -> builder.build());
        assertTrue(ex.getMessage().contains("exactamente 1 arquero"));
    }

    @Test
    public void testFormacionIncompatibleConPosicionesLanzaExcepcion() {
        builder.agregarTitular(crearJugador("p-1", "Ospina", PosicionEnum.PORTERO, EstadoJugadorEnum.OPTIMO));
        for (int i = 1; i <= 5; i++) {
            builder.agregarTitular(crearJugador("d-" + i, "Defensa " + i, PosicionEnum.DEFENSA, EstadoJugadorEnum.OPTIMO));
        }
        for (int i = 1; i <= 3; i++) {
            builder.agregarTitular(crearJugador("m-" + i, "Medio " + i, PosicionEnum.MEDIOCAMPISTA, EstadoJugadorEnum.OPTIMO));
        }
        for (int i = 1; i <= 2; i++) {
            builder.agregarTitular(crearJugador("f-" + i, "Delantero " + i, PosicionEnum.DELANTERO, EstadoJugadorEnum.OPTIMO));
        }

        Exception ex = assertThrows(IllegalStateException.class, () -> builder.build());
        assertTrue(ex.getMessage().contains("formación 4-4-2 requiere 4 defensas"));
    }
}
