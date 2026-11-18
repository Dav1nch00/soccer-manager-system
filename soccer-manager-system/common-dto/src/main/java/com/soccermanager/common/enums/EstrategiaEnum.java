package com.soccermanager.common.enums;

/**
 * Filosofías y estrategias de juego disponibles para los partidos (Patrón Strategy).
 */
public enum EstrategiaEnum {
    POSESION("Posesión", "Control de balón y circulación paciente. Vulnerable al contraataque."),
    CONTRAATAQUE("Contraataque", "Transiciones rápidas. Bonificación frente a posesión; mayor desgaste físico."),
    PRESION_ALTA("Presión Alta", "Recuperación en campo rival. Genera más faltas y alto consumo de energía."),
    BLOQUE_BAJO("Bloque Bajo", "Líneas retrasadas para proteger el arco. Reduce opciones ofensivas propias.");

    private final String nombre;
    private final String descripcion;

    EstrategiaEnum(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
