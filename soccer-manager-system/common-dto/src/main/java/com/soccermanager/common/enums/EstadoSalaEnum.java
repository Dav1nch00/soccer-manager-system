package com.soccermanager.common.enums;

/**
 * Estados de la sala multijugador para la coordinación previa a la simulación.
 */
public enum EstadoSalaEnum {
    ESPERANDO_JUGADORES("Esperando que ambos DTs inicien sesión"),
    EN_PLANIFICACION("Ambos DTs están diseñando su estrategia en paralelo"),
    CUENTA_REGRESIVA("Un DT ha confirmado. Temporizador de 60s en marcha"),
    SIMULANDO("Simulando los partidos de la jornada en paralelo"),
    FINALIZADA("Jornada concluida y resultados disponibles");

    private final String descripcion;

    EstadoSalaEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
