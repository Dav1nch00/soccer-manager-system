package com.soccermanager.server.engine.pipeline;

/**
 * Interfaz Filter del Patrón Arquitectónico Pipes and Filters.
 * Representa una etapa autónoma en el procesamiento y simulación de un partido.
 */
public interface FiltroPartido {

    /**
     * Procesa, valida o transforma el contexto del encuentro deportivo.
     *
     * @param contexto Datos actuales del partido.
     * @return Contexto transformado para la siguiente etapa de la tubería.
     */
    ContextoPartido procesar(ContextoPartido contexto);
}
