package com.soccermanager.server.engine;

import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.server.engine.pipeline.ContextoPartido;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Orquestador concurrente de la jornada deportiva.
 * Ejecuta simultáneamente los N/2 partidos de la fecha utilizando un pool de hilos fijo
 * (ExecutorService) y una barrera de sincronización (invokeAll).
 */
public class SimuladorJornada {

    private final ExecutorService executorService;
    private final int poolSize;

    public SimuladorJornada() {
        this(Math.max(2, Runtime.getRuntime().availableProcessors()));
    }

    public SimuladorJornada(int poolSize) {
        this.poolSize = poolSize;
        this.executorService = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            private int count = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "HiloPartido-" + (count++));
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Simula en paralelo todos los encuentros de una fecha deportiva.
     * El hilo principal se detiene en una barrera de sincronización hasta que todos los partidos concluyan.
     *
     * @param partidos Lista de contextos de los partidos de la jornada.
     * @return Lista de ResultadoPartidoDTO consolidados.
     * @throws InterruptedException Si ocurre interrupción durante la espera en la barrera.
     * @throws ExecutionException Si algún hilo de partido falla.
     */
    public List<ResultadoPartidoDTO> simularJornada(List<ContextoPartido> partidos)
            throws InterruptedException, ExecutionException {
        if (partidos == null || partidos.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Crear las tareas Callable independientes
        List<Callable<ResultadoPartidoDTO>> tareas = new ArrayList<>();
        for (ContextoPartido partido : partidos) {
            tareas.add(new TareaPartido(partido));
        }

        // 2. Despachar al pool de hilos y esperar en la barrera (invokeAll)
        List<Future<ResultadoPartidoDTO>> futures = executorService.invokeAll(tareas);

        // 3. Recopilar resultados consolidados
        List<ResultadoPartidoDTO> resultados = new ArrayList<>();
        for (Future<ResultadoPartidoDTO> f : futures) {
            resultados.add(f.get());
        }

        return resultados;
    }

    /**
     * Cierra el pool de hilos ordenadamente.
     */
    public void apagar() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public int getPoolSize() {
        return poolSize;
    }
}
