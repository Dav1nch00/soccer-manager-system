package com.soccermanager.server.domain.state;

import com.soccermanager.common.enums.EstadoJugadorEnum;
import com.soccermanager.server.domain.model.Jugador;

import java.io.Serializable;

/**
 * Interfaz del Patrón State que define el comportamiento y disponibilidad del futbolista
 * según su estado físico o disciplinario.
 */
public interface EstadoJugador extends Serializable {

    /**
     * Indica si el jugador está reglamentaria y físicamente habilitado para disputar partidos.
     */
    boolean puedeJugar();

    /**
     * Factor multiplicador sobre los atributos base (de 0.0 a 1.0).
     */
    double obtenerFactorRendimiento();

    /**
     * Retorna el tipo de estado correspondiente a la enumeración compartida.
     */
    EstadoJugadorEnum getTipo();

    /**
     * Ejecuta las transiciones de estado y recuperación al avanzar una jornada del torneo.
     *
     * @param jugador Instancia del jugador cuyo estado evoluciona.
     */
    void avanzarJornada(Jugador jugador);
}
