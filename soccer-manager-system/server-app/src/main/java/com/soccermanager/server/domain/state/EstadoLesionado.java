package com.soccermanager.server.domain.state;

import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.server.domain.model.Jugador;

/**
 * Estado Lesionado: El jugador está bajo tratamiento médico. Inhabilitado para jugar.
 * Mantiene un contador regresivo de semanas/jornadas de incapacidad.
 */
public class EstadoLesionado implements EstadoJugador {
    private static final long serialVersionUID = 1L;

    private int semanasRestantes;

    public EstadoLesionado(int semanasRestantes) {
        this.semanasRestantes = Math.max(1, semanasRestantes);
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
        return EstadoJugadorEnum.LESIONADO;
    }

    @Override
    public void avanzarJornada(Jugador jugador) {
        semanasRestantes--;
        jugador.recuperarEnergia(15);
        if (semanasRestantes <= 0) {
            // Alta médica
            jugador.cambiarEstado(new EstadoOptimo());
        }
    }

    public int getSemanasRestantes() {
        return semanasRestantes;
    }

    @Override
    public String toString() {
        return "Lesionado (" + semanasRestantes + " semanas restantes)";
    }
}
