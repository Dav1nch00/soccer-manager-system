package com.soccermanager.server.domain.state;

import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.server.domain.model.Jugador;

/**
 * Estado Cansado: El jugador acusa fatiga muscular. Puede jugar, pero con penalización del 25%
 * en sus atributos efectivos y mayor propensión a sufrir lesiones.
 */
public class EstadoCansado implements EstadoJugador {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean puedeJugar() {
        return true;
    }

    @Override
    public double obtenerFactorRendimiento() {
        return 0.75; // 25% de penalización
    }

    @Override
    public EstadoJugadorEnum getTipo() {
        return EstadoJugadorEnum.CANSADO;
    }

    @Override
    public void avanzarJornada(Jugador jugador) {
        // Al descansar entre fechas, recupera energía y transiciona a Óptimo si supera el 75%
        jugador.recuperarEnergia(30);
        if (jugador.getEnergia() >= 75) {
            jugador.cambiarEstado(new EstadoOptimo());
        }
    }

    @Override
    public String toString() {
        return "Cansado (75% rendimiento)";
    }
}
