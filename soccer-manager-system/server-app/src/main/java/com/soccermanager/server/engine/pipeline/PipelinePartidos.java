package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.dto.ResultadoPartidoDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador de la cadena de procesamiento Tuberías y Filtros (Pipes & Filters).
 * Conecta secuencialmente los 5 filtros de la simulación de partido.
 */
public class PipelinePartidos implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<FiltroPartido> filtros;

    public PipelinePartidos() {
        this.filtros = new ArrayList<>();
        // Registrar la secuencia oficial de 5 filtros
        this.filtros.add(new FiltroValidacion());
        this.filtros.add(new FiltroCalculoTactico());
        this.filtros.add(new FiltroSimulacionEventos());
        this.filtros.add(new FiltroDesgaste());
        this.filtros.add(new FiltroConsolidacion());
    }

    public PipelinePartidos(List<FiltroPartido> filtrosPersonalizados) {
        this.filtros = filtrosPersonalizados != null ? filtrosPersonalizados : new ArrayList<>();
    }

    /**
     * Ejecuta el contexto del partido a través de la tubería de filtros.
     *
     * @param contextoInicial Contexto preparado con ambos clubes y tácticas.
     * @return ResultadoPartidoDTO consolidado tras superar todos los filtros.
     */
    public ResultadoPartidoDTO ejecutar(ContextoPartido contextoInicial) {
        ContextoPartido actual = contextoInicial;

        for (FiltroPartido filtro : filtros) {
            actual = filtro.procesar(actual);
            if (actual == null || !actual.isValido()) {
                throw new IllegalStateException("Fallo en la tubería de simulación: " +
                        (actual != null ? actual.getMensajeValidacion() : "Contexto nulo"));
            }
        }

        return actual.getResultadoFinal();
    }

    public List<FiltroPartido> getFiltros() {
        return filtros;
    }
}
