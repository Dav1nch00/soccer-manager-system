package com.soccermanager.server.lobby;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Temporizador regresivo concurrente de 60 segundos basado en ScheduledExecutorService.
 * Emite pulsos por segundo hacia los clientes y ejecuta callbacks de finalización o timeout.
 */
public class TemporizadorJornada {

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> tareaPeriodica;
    private final AtomicInteger segundosRestantes;
    private final AtomicBoolean activo;

    public TemporizadorJornada() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Timer-Jornada-60s");
            t.setDaemon(true);
            return t;
        });
        this.segundosRestantes = new AtomicInteger(60);
        this.activo = new AtomicBoolean(false);
    }

    /**
     * Inicia la cuenta regresiva desde una cantidad de segundos especificada (ej. 60s).
     *
     * @param segundos Duración inicial en segundos.
     * @param onTick Callback ejecutado en cada segundo transcurrido (recibe segundos restantes).
     * @param onTimeout Callback ejecutado cuando el contador alcanza exactamente cero.
     */
    public synchronized void iniciarCountdown(int segundos, Consumer<Integer> onTick, Runnable onTimeout) {
        cancelar(); // Cancelar cualquier cuenta previa activa

        segundosRestantes.set(segundos);
        activo.set(true);

        tareaPeriodica = scheduler.scheduleAtFixedRate(() -> {
            int actual = segundosRestantes.decrementAndGet();

            if (onTick != null) {
                onTick.accept(actual);
            }

            if (actual <= 0) {
                cancelar();
                if (onTimeout != null) {
                    onTimeout.run();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Detiene y cancela inmediatamente la cuenta regresiva (ej. si el segundo DT confirma a tiempo).
     */
    public synchronized void cancelar() {
        if (tareaPeriodica != null && !tareaPeriodica.isDone()) {
            tareaPeriodica.cancel(true);
        }
        activo.set(false);
    }

    public int getSegundosRestantes() {
        return Math.max(0, segundosRestantes.get());
    }

    public boolean isActivo() {
        return activo.get();
    }

    public void apagar() {
        cancelar();
        scheduler.shutdownNow();
    }
}
