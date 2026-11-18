package com.soccermanager.server.domain.model;

import java.io.Serializable;

/**
 * Interfaz Component del Patrón Composite.
 * Define las operaciones uniformes para calcular potenciales de juego tanto para
 * futbolistas individuales (Hojas) como para líneas tácticas y clubes completos (Compuestos).
 */
public interface ComponenteTactico extends Serializable {

    /**
     * Calcula la potencia ofensiva del componente (tiro, velocidad, ataque coordinado).
     */
    double calcularPoderOfensivo();

    /**
     * Calcula la solidez defensiva del componente (defensa, físico, intercepciones).
     */
    double calcularPoderDefensivo();

    /**
     * Calcula la capacidad de control y distribución (pase, visión de juego).
     */
    double calcularPoderControl();

    /**
     * Retorna la cantidad de futbolistas que componen este elemento.
     */
    int getCantidadElementos();
}
