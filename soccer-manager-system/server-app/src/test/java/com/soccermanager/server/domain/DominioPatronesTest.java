package com.soccermanager.server.domain;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.domain.model.LineaTactica;
import com.soccermanager.server.domain.state.EstadoCansado;
import com.soccermanager.server.domain.state.EstadoLesionado;
import com.soccermanager.server.domain.state.EstadoOptimo;
import com.soccermanager.server.domain.state.EstadoSancionado;
import com.soccermanager.server.domain.strategy.BloqueBajoStrategy;
import com.soccermanager.server.domain.strategy.ContraataqueStrategy;
import com.soccermanager.server.domain.strategy.EstrategiaFactory;
import com.soccermanager.server.domain.strategy.EstrategiaTactica;
import com.soccermanager.server.domain.strategy.ModificadorTactico;
import com.soccermanager.server.domain.strategy.PosesionStrategy;
import com.soccermanager.server.domain.strategy.PresionAltaStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para los patrones GoF del Bloque 3: Composite, Strategy y State.
 */
public class DominioPatronesTest {

    // =========================================================================
    // PRUEBAS DEL PATRÓN COMPOSITE (Hoja -> Línea -> Equipo)
    // =========================================================================

    @Test
    public void testPatronCompositeCalculoUniforme() {
        // Hoja 1: Delantero estrella
        Jugador delantero = new Jugador("j-01", "Goleador", 9, PosicionEnum.DELANTERO, "club-1", 85, 75, 90, 40, 80);
        assertTrue(delantero.calcularPoderOfensivo() > 70.0);
        assertEquals(1, delantero.getCantidadElementos());

        // Hoja 2: Portero
        Jugador portero = new Jugador("j-02", "Arquero", 1, PosicionEnum.PORTERO, "club-1", 60, 65, 30, 88, 85);
        assertTrue(portero.calcularPoderDefensivo() > 60.0);

        // Compuesto 1: Línea Táctica Delantera
        LineaTactica lineaDelantera = new LineaTactica("Delantera", PosicionEnum.DELANTERO);
        lineaDelantera.agregar(delantero);
        assertEquals(1, lineaDelantera.getCantidadElementos());
        assertEquals(delantero.calcularPoderOfensivo(), lineaDelantera.calcularPoderOfensivo(), 0.001);

        // Compuesto Raíz: Equipo
        Equipo equipo = new Equipo("club-1", "FC Barcelona", FormacionEnum.F_4_3_3);
        equipo.agregarTitular(portero);
        equipo.agregarTitular(delantero);

        assertEquals(2, equipo.getCantidadElementos());
        assertTrue(equipo.calcularPoderOfensivo() > 0.0);
        assertTrue(equipo.calcularPoderDefensivo() > 0.0);
    }

    // =========================================================================
    // PRUEBAS DEL PATRÓN STRATEGY (Contrarrestos y Modificadores)
    // =========================================================================

    @Test
    public void testPatronStrategyContrarrestoContraataqueFrenteAPosesion() {
        Equipo equipo1 = new Equipo("c-1", "Equipo A", FormacionEnum.F_4_4_2);

        EstrategiaTactica contraataque = EstrategiaFactory.obtenerEstrategia(EstrategiaEnum.CONTRAATAQUE);
        EstrategiaTactica posesion = EstrategiaFactory.obtenerEstrategia(EstrategiaEnum.POSESION);

        // Contraataque vs Posesión -> Debe otorgar bono ofensivo superior (+35%)
        ModificadorTactico modContra = contraataque.aplicarModificadores(equipo1, posesion);
        assertEquals(0.35, modContra.getBonoOfensivo(), 0.001);

        // Posesión vs Contraataque -> Debe sufrir penalización defensiva (-15%)
        ModificadorTactico modPosesion = posesion.aplicarModificadores(equipo1, contraataque);
        assertEquals(-0.15, modPosesion.getBonoDefensivo(), 0.001);
        assertEquals(0.25, modPosesion.getBonoControl(), 0.001);
    }

    @Test
    public void testPatronStrategyPresionAltaYBloqueBajo() {
        Equipo equipo = new Equipo("c-2", "Equipo B", FormacionEnum.F_4_3_3);

        EstrategiaTactica presionAlta = new PresionAltaStrategy();
        ModificadorTactico modPresion = presionAlta.aplicarModificadores(equipo, new PosesionStrategy());
        assertTrue(modPresion.getFactorFaltas() > 1.3, "Presión alta debe incrementar sustancialmente las faltas.");
        assertTrue(presionAlta.getDesgasteBase() >= 25, "Presión alta debe tener alto desgaste base.");

        EstrategiaTactica bloqueBajo = new BloqueBajoStrategy();
        ModificadorTactico modBloque = bloqueBajo.aplicarModificadores(equipo, new PosesionStrategy());
        assertEquals(0.35, modBloque.getBonoDefensivo(), 0.001, "Bloque bajo debe otorgar +35% de solidez defensiva.");
        assertTrue(modBloque.getBonoOfensivo() < 0, "Bloque bajo penaliza la generación ofensiva propia.");
    }

    // =========================================================================
    // PRUEBAS DEL PATRÓN STATE (Ciclo de Vida del Futbolista)
    // =========================================================================

    @Test
    public void testPatronStateTransicionCansancioYRecuperacion() {
        Jugador jugador = new Jugador("j-10", "Mediapunta", 10, PosicionEnum.MEDIOCAMPISTA, "c-1", 80, 85, 80, 60, 75);
        assertTrue(jugador.getEstado() instanceof EstadoOptimo);
        assertEquals(1.0, jugador.getEstado().obtenerFactorRendimiento());

        // Reducir energía por debajo de 50 -> Transiciona a Cansado
        jugador.reducirEnergia(60); // Energía cae a 40%
        assertTrue(jugador.getEstado() instanceof EstadoCansado, "Debe transicionar a EstadoCansado.");
        assertEquals(0.75, jugador.getEstado().obtenerFactorRendimiento(), 0.001);

        // Descanso / avanzar jornada -> Recupera energía y transiciona a Óptimo
        jugador.avanzarJornada(); // Recupera 30% -> Energía 70%
        jugador.recuperarEnergia(10); // Supera 75%
        assertTrue(jugador.getEstado() instanceof EstadoOptimo, "Debe volver a EstadoOptimo al superar 75% de energía.");
    }

    @Test
    public void testPatronStateLesionYSancionInhabilitanParaJugar() {
        Jugador jugador = new Jugador("j-04", "Central", 4, PosicionEnum.DEFENSA, "c-1", 70, 65, 40, 88, 85);

        // Caso Lesión por 2 jornadas
        jugador.cambiarEstado(new EstadoLesionado(2));
        assertFalse(jugador.getEstado().puedeJugar(), "Un lesionado no puede jugar.");
        assertEquals(0.0, jugador.calcularPoderDefensivo(), "Poder defensivo debe ser 0 al estar inhabilitado.");

        // Avanzar jornada 1
        jugador.avanzarJornada();
        assertFalse(jugador.getEstado().puedeJugar(), "Aún le queda 1 jornada de lesión.");

        // Avanzar jornada 2 -> Alta médica
        jugador.avanzarJornada();
        assertTrue(jugador.getEstado().puedeJugar(), "Al cumplir el periodo de recuperación debe quedar habilitado.");
        assertTrue(jugador.getEstado() instanceof EstadoOptimo);

        // Caso Sanción por Tarjeta Roja
        jugador.registrarTarjetaRoja();
        assertTrue(jugador.getEstado() instanceof EstadoSancionado, "Debe quedar sancionado tras roja directa.");
        assertFalse(jugador.getEstado().puedeJugar());

        jugador.avanzarJornada();
        assertTrue(jugador.getEstado().puedeJugar(), "Al cumplir la fecha arbitral debe volver a estar disponible.");
    }
}
