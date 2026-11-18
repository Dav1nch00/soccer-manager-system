package com.soccermanager.server.engine;

import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.server.engine.pipeline.ContextoPartido;
import com.soccermanager.server.engine.pipeline.PipelinePartidos;

import java.util.concurrent.Callable;

/**
 * Tarea concurrente Callable que ejecuta la simulación de un partido individual
 * de forma asíncrona e independiente dentro del pool de hilos del servidor.
 */
public class TareaPartido implements Callable<ResultadoPartidoDTO> {

    private final ContextoPartido contexto;
    private final PipelinePartidos pipeline;

    public TareaPartido(ContextoPartido contexto) {
        this(contexto, new PipelinePartidos());
    }

    public TareaPartido(ContextoPartido contexto, PipelinePartidos pipeline) {
        this.contexto = contexto;
        this.pipeline = pipeline;
    }

    @Override
    public ResultadoPartidoDTO call() throws Exception {
        return pipeline.ejecutar(contexto);
    }

    public ContextoPartido getContexto() {
        return contexto;
    }
}
