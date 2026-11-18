package com.soccermanager.server.domain.model;

import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Raíz del Patrón Composite.
 * Representa al conjunto de 11 jugadores alineados en sus respectivas líneas tácticas.
 */
public class Equipo implements ComponenteTactico {
    private static final long serialVersionUID = 1L;

    private String clubId;
    private String nombreClub;
    private FormacionEnum formacion;

    private final LineaTactica lineaPorteria;
    private final LineaTactica lineaDefensa;
    private final LineaTactica lineaMediocampo;
    private final LineaTactica lineaDelantera;

    public Equipo(String clubId, String nombreClub, FormacionEnum formacion) {
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        this.formacion = formacion != null ? formacion : FormacionEnum.F_4_4_2;

        this.lineaPorteria = new LineaTactica("Portería", PosicionEnum.PORTERO);
        this.lineaDefensa = new LineaTactica("Defensa", PosicionEnum.DEFENSA);
        this.lineaMediocampo = new LineaTactica("Mediocampo", PosicionEnum.MEDIOCAMPISTA);
        this.lineaDelantera = new LineaTactica("Delantera", PosicionEnum.DELANTERO);
    }

    /**
     * Agrega un jugador titular a la línea táctica que le corresponde según su rol posicional.
     */
    public void agregarTitular(Jugador jugador) {
        if (jugador == null) return;

        switch (jugador.getPosicion()) {
            case PORTERO:
                lineaPorteria.agregar(jugador);
                break;
            case DEFENSA:
                lineaDefensa.agregar(jugador);
                break;
            case MEDIOCAMPISTA:
                lineaMediocampo.agregar(jugador);
                break;
            case DELANTERO:
                lineaDelantera.agregar(jugador);
                break;
        }
    }

    public void limpiarAlineacion() {
        lineaPorteria.limpiar();
        lineaDefensa.limpiar();
        lineaMediocampo.limpiar();
        lineaDelantera.limpiar();
    }

    @Override
    public double calcularPoderOfensivo() {
        // Ponderación de ataque global: Delantera (50%), Mediocampo (35%), Defensa (15%)
        double ataDel = lineaDelantera.calcularPoderOfensivo() * 0.50;
        double ataMed = lineaMediocampo.calcularPoderOfensivo() * 0.35;
        double ataDef = lineaDefensa.calcularPoderOfensivo() * 0.15;
        return ataDel + ataMed + ataDef;
    }

    @Override
    public double calcularPoderDefensivo() {
        // Ponderación defensiva global: Portería (25%), Defensa (50%), Mediocampo (25%)
        double defPor = lineaPorteria.calcularPoderDefensivo() * 0.25;
        double defDef = lineaDefensa.calcularPoderDefensivo() * 0.50;
        double defMed = lineaMediocampo.calcularPoderDefensivo() * 0.25;
        return defPor + defDef + defMed;
    }

    @Override
    public double calcularPoderControl() {
        // Ponderación de posesión/control: Mediocampo (60%), Defensa (20%), Delantera (20%)
        double ctlMed = lineaMediocampo.calcularPoderControl() * 0.60;
        double ctlDef = lineaDefensa.calcularPoderControl() * 0.20;
        double ctlDel = lineaDelantera.calcularPoderControl() * 0.20;
        return ctlMed + ctlDef + ctlDel;
    }

    @Override
    public int getCantidadElementos() {
        return lineaPorteria.getCantidadElementos() +
                lineaDefensa.getCantidadElementos() +
                lineaMediocampo.getCantidadElementos() +
                lineaDelantera.getCantidadElementos();
    }

    public List<Jugador> getTodosLosTitulares() {
        List<Jugador> titulares = new ArrayList<>();
        agregarHojas(lineaPorteria, titulares);
        agregarHojas(lineaDefensa, titulares);
        agregarHojas(lineaMediocampo, titulares);
        agregarHojas(lineaDelantera, titulares);
        return titulares;
    }

    private void agregarHojas(LineaTactica linea, List<Jugador> resultado) {
        for (ComponenteTactico c : linea.getElementos()) {
            if (c instanceof Jugador) {
                resultado.add((Jugador) c);
            }
        }
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getNombreClub() {
        return nombreClub;
    }

    public void setNombreClub(String nombreClub) {
        this.nombreClub = nombreClub;
    }

    public FormacionEnum getFormacion() {
        return formacion;
    }

    public void setFormacion(FormacionEnum formacion) {
        this.formacion = formacion;
    }

    public LineaTactica getLineaPorteria() {
        return lineaPorteria;
    }

    public LineaTactica getLineaDefensa() {
        return lineaDefensa;
    }

    public LineaTactica getLineaMediocampo() {
        return lineaMediocampo;
    }

    public LineaTactica getLineaDelantera() {
        return lineaDelantera;
    }
}
