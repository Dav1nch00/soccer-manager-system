package com.soccermanager.common.enums;

/**
 * Estados del ciclo de vida y disponibilidad física/disciplinaria del futbolista (Patrón State).
 */
public enum EstadoJugadorEnum {
    OPTIMO("Óptimo", true, 1.0),
    CANSADO("Cansado / Fatiga", true, 0.75),
    LESIONADO("Lesionado", false, 0.0),
    SANCIONADO("Sancionado", false, 0.0);

    private final String etiqueta;
    private final boolean aptoParaJugar;
    private final double factorRendimiento;

    EstadoJugadorEnum(String etiqueta, boolean aptoParaJugar, double factorRendimiento) {
        this.etiqueta = etiqueta;
        this.aptoParaJugar = aptoParaJugar;
        this.factorRendimiento = factorRendimiento;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isAptoParaJugar() {
        return aptoParaJugar;
    }

    public double getFactorRendimiento() {
        return factorRendimiento;
    }
}
