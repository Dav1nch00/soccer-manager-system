package com.soccermanager.server.domain.state;

import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.server.domain.model.Jugador;

/**
 * Estado Sancionado: Suspensión disciplinaria por tarjetas rojas o acumulación de amarillas.
 * Inhabilitado reglamentariamente para ser convocado.
 */
public class EstadoSancionado implements EstadoJugador {
    private static final long serialVersionUID = 1L;

    private int jornadasRestantes;

    public EstadoSancionado(int jornadasRestantes) {
        this.jornadasRestantes = Math.max(1, jornadasRestantes);
    }

    @Override
    public boolean puedeJugar() {
        return false;
    }

    @Override
    public double obtenerFactorRendimiento() {
        return 0.0;
    }

    @Override
    public EstadoJugadorEnum getTipo() {
        return EstadoJugadorEnum.SANCIONADO;
    }

    @Override
    public void avanzarJornada(Jugador jugador) {
        jornadasRestantes--;
        jugador.recuperarEnergia(20);
        if (jornadasRestantes <= 0) {
            // Cumplió la sanción arbitral
            jugador.cambiarEstado(new EstadoOptimo());
        }
    }

    public int getJornadasRestantes() {
        return jornadasRestantes;
    }

    @Override
    public String toString() {
        return "Sancionado (" + jornadasRestantes + " fechas restantes)";
    }
}
