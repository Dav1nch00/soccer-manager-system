package com.soccermanager.server.domain.strategy;

import java.io.Serializable;

/**
 * Encapsula las bonificaciones y penalizaciones porcentuales resultantes del cruce
 * táctico entre las filosofías de juego de ambos equipos.
 */
public class ModificadorTactico implements Serializable {
    private static final long serialVersionUID = 1L;

    private double bonoOfensivo;       // Multiplicador porcentual (+0.15 = +15%)
    private double bonoDefensivo;      // Multiplicador porcentual (+0.20 = +20%)
    private double bonoControl;        // Posesión y generación de juego
    private int desgasteExtraEnergia;  // Puntos adicionales de fatiga que sufrirán los futbolistas
    private double factorFaltas;       // Multiplicador de probabilidad de faltas/tarjetas

    public ModificadorTactico() {
        this.bonoOfensivo = 0.0;
        this.bonoDefensivo = 0.0;
        this.bonoControl = 0.0;
        this.desgasteExtraEnergia = 0;
        this.factorFaltas = 1.0;
    }

    public ModificadorTactico(double bonoOfensivo, double bonoDefensivo, double bonoControl,
                              int desgasteExtraEnergia, double factorFaltas) {
        this.bonoOfensivo = bonoOfensivo;
        this.bonoDefensivo = bonoDefensivo;
        this.bonoControl = bonoControl;
        this.desgasteExtraEnergia = desgasteExtraEnergia;
        this.factorFaltas = factorFaltas;
    }

    public double getBonoOfensivo() {
        return bonoOfensivo;
    }

    public void setBonoOfensivo(double bonoOfensivo) {
        this.bonoOfensivo = bonoOfensivo;
    }

    public double getBonoDefensivo() {
        return bonoDefensivo;
    }

    public void setBonoDefensivo(double bonoDefensivo) {
        this.bonoDefensivo = bonoDefensivo;
    }

    public double getBonoControl() {
        return bonoControl;
    }

    public void setBonoControl(double bonoControl) {
        this.bonoControl = bonoControl;
    }

    public int getDesgasteExtraEnergia() {
        return desgasteExtraEnergia;
    }

    public void setDesgasteExtraEnergia(int desgasteExtraEnergia) {
        this.desgasteExtraEnergia = desgasteExtraEnergia;
    }

    public double getFactorFaltas() {
        return factorFaltas;
    }

    public void setFactorFaltas(double factorFaltas) {
        this.factorFaltas = factorFaltas;
    }
}
