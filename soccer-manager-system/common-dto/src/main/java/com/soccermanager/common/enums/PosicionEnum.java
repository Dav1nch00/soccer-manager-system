package com.soccermanager.common.enums;

/**
 * Roles posicionales en el terreno de juego.
 */
public enum PosicionEnum {
    PORTERO("Portero", "POR"),
    DEFENSA("Defensa", "DEF"),
    MEDIOCAMPISTA("Mediocampista", "MED"),
    DELANTERO("Delantero", "DEL");

    private final String nombre;
    private final String abreviatura;

    PosicionEnum(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }
}
