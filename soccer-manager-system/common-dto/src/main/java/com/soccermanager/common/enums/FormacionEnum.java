package com.soccermanager.common.enums;

/**
 * Representa los esquemas tácticos permitidos en el simulador.
 */
public enum FormacionEnum {
    F_4_4_2("4-4-2", 1, 4, 4, 2),
    F_4_3_3("4-3-3", 1, 4, 3, 3),
    F_3_5_2("3-5-2", 1, 3, 5, 2),
    F_5_3_2("5-3-2", 1, 5, 3, 2),
    F_4_2_3_1("4-2-3-1", 1, 4, 5, 1);

    private final String etiqueta;
    private final int porteros;
    private final int defensas;
    private final int mediocampistas;
    private final int delanteros;

    FormacionEnum(String etiqueta, int porteros, int defensas, int mediocampistas, int delanteros) {
        this.etiqueta = etiqueta;
        this.porteros = porteros;
        this.defensas = defensas;
        this.mediocampistas = mediocampistas;
        this.delanteros = delanteros;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getPorteros() {
        return porteros;
    }

    public int getDefensas() {
        return defensas;
    }

    public int getMediocampistas() {
        return mediocampistas;
    }

    public int getDelanteros() {
        return delanteros;
    }

    public int getTotalJugadores() {
        return porteros + defensas + mediocampistas + delanteros;
    }
}
