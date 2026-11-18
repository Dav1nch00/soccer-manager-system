package com.soccermanager.server.domain.model;

import com.soccermanager.common.enums.PosicionEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite intermedio del Patrón Composite.
 * Agrupa los componentes de una línea táctica específica (Portería, Defensa, Mediocampo, Delantera)
 * y computa métricas consolidadas de forma uniforme.
 */
public class LineaTactica implements ComponenteTactico {
    private static final long serialVersionUID = 1L;

    private String nombreLinea;
    private PosicionEnum tipoLinea;
    private final List<ComponenteTactico> elementos;

    public LineaTactica(String nombreLinea, PosicionEnum tipoLinea) {
        this.nombreLinea = nombreLinea;
        this.tipoLinea = tipoLinea;
        this.elementos = new ArrayList<>();
    }

    public void agregar(ComponenteTactico componente) {
        if (componente != null && !elementos.contains(componente)) {
            elementos.add(componente);
        }
    }

    public void remover(ComponenteTactico componente) {
        elementos.remove(componente);
    }

    public void limpiar() {
        elementos.clear();
    }

    @Override
    public double calcularPoderOfensivo() {
        if (elementos.isEmpty()) return 0.0;
        double total = 0.0;
        for (ComponenteTactico c : elementos) {
            total += c.calcularPoderOfensivo();
        }
        return total / elementos.size();
    }

    @Override
    public double calcularPoderDefensivo() {
        if (elementos.isEmpty()) return 0.0;
        double total = 0.0;
        for (ComponenteTactico c : elementos) {
            total += c.calcularPoderDefensivo();
        }
        return total / elementos.size();
    }

    @Override
    public double calcularPoderControl() {
        if (elementos.isEmpty()) return 0.0;
        double total = 0.0;
        for (ComponenteTactico c : elementos) {
            total += c.calcularPoderControl();
        }
        return total / elementos.size();
    }

    @Override
    public int getCantidadElementos() {
        int total = 0;
        for (ComponenteTactico c : elementos) {
            total += c.getCantidadElementos();
        }
        return total;
    }

    public String getNombreLinea() {
        return nombreLinea;
    }

    public void setNombreLinea(String nombreLinea) {
        this.nombreLinea = nombreLinea;
    }

    public PosicionEnum getTipoLinea() {
        return tipoLinea;
    }

    public void setTipoLinea(PosicionEnum tipoLinea) {
        this.tipoLinea = tipoLinea;
    }

    public List<ComponenteTactico> getElementos() {
        return Collections.unmodifiableList(elementos);
    }
}
