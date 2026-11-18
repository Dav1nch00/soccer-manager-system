package com.soccermanager.server.domain.state;

import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.server.domain.model.Jugador;

/**
 * Estado Óptimo: El jugador está en plenas condiciones físicas y disponible para jugar al 100%.
 */
public class EstadoOptimo implements EstadoJugador {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean puedeJugar() {
        return true;
    }

    @Override
    public double obtenerFactorRendimiento() {
        return 1.0;
    }

    @Override
    public EstadoJugadorEnum getTipo() {
        return EstadoJugadorEnum.OPTIMO;
    }

    @Override
    public void avanzarJornada(Jugador jugador) {
        // En descanso o entre fechas, si tiene menos de 100% de energía, recupera energía
        jugador.recuperarEnergia(20);
    }

    @Override
    public String toString() {
        return "Óptimo (100%)";
    }
}
