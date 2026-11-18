package com.soccermanager.common.enums;

/**
 * Tipos de eventos o incidencias que pueden ocurrir durante los 90 minutos de un partido.
 */
public enum TipoIncidenciaEnum {
    GOL("Gol"),
    TARJETA_AMARILLA("Tarjeta Amarilla"),
    TARJETA_ROJA("Tarjeta Roja"),
    LESION("Lesión"),
    CAMBIO_TACTICO("Ajuste Táctico");

    private final String etiqueta;

    TipoIncidenciaEnum(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
