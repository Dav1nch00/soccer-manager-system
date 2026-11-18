package com.soccermanager.common.enums;

/**
 * Modos de simulación deportiva disponibles en el sistema.
 */
public enum ModoJuegoEnum {
    SOLO("Simular Solo (vs IA)", "1 Jugador humano compitiendo contra clubes controlados por IA."),
    DOS_JUGADORES("Simular con Otra Persona", "2 Jugadores humanos compitiendo en la misma liga con sincronización y temporizador de 60s.");

    private final String titulo;
    private final String descripcion;

    ModoJuegoEnum(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
