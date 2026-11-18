package com.soccermanager.server.services;

import com.soccermanager.common.dto.IncidenciaDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.enums.TipoIncidenciaEnum;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la clasificación general de la liga y la tabla de goleadores.
 * Aplica exclusión mutua mediante ReentrantLock para garantizar operaciones thread-safe
 * cuando múltiples hilos de partidos terminan al mismo microsegundo.
 */
public class TablaPosicionesService implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class FilaPosicion implements Serializable, Comparable<FilaPosicion> {
        private String clubId;
        private String nombreClub;
        private int puntos;
        private int partidosJugados;
        private int partidosGanados;
        private int partidosEmpatados;
        private int partidosPerdidos;
        private int golesAFavor;
        private int golesEnContra;

        public FilaPosicion(String clubId, String nombreClub) {
            this.clubId = clubId;
            this.nombreClub = nombreClub;
        }

        public void sumarVictoria(int gf, int gc) {
            this.partidosJugados++;
            this.partidosGanados++;
            this.puntos += 3;
            this.golesAFavor += gf;
            this.golesEnContra += gc;
        }

        public void sumarEmpate(int gf, int gc) {
            this.partidosJugados++;
            this.partidosEmpatados++;
            this.puntos += 1;
            this.golesAFavor += gf;
            this.golesEnContra += gc;
        }

        public void sumarDerrota(int gf, int gc) {
            this.partidosJugados++;
            this.partidosPerdidos++;
            this.golesAFavor += gf;
            this.golesEnContra += gc;
        }

        public int getDiferenciaGoles() {
            return golesAFavor - golesEnContra;
        }

        @Override
        public int compareTo(FilaPosicion o) {
            // Criterios de desempate oficiales: 1. Puntos, 2. Diferencia de Gol, 3. Goles a Favor
            int cmpPuntos = Integer.compare(o.puntos, this.puntos);
            if (cmpPuntos != 0) return cmpPuntos;

            int cmpDif = Integer.compare(o.getDiferenciaGoles(), this.getDiferenciaGoles());
            if (cmpDif != 0) return cmpDif;

            int cmpGf = Integer.compare(o.golesAFavor, this.golesAFavor);
            if (cmpGf != 0) return cmpGf;

            return this.nombreClub.compareToIgnoreCase(o.nombreClub);
        }

        public String getClubId() { return clubId; }
        public String getNombreClub() { return nombreClub; }
        public int getPuntos() { return puntos; }
        public int getPartidosJugados() { return partidosJugados; }
        public int getPartidosGanados() { return partidosGanados; }
        public int getPartidosEmpatados() { return partidosEmpatados; }
        public int getPartidosPerdidos() { return partidosPerdidos; }
        public int getGolesAFavor() { return golesAFavor; }
        public int getGolesEnContra() { return golesEnContra; }
    }

    private final Map<String, FilaPosicion> tablaPorClub;
    private final ConcurrentHashMap<String, Integer> registroGoleadores;
    private final ReentrantLock lockTabla;

    public TablaPosicionesService() {
        this.tablaPorClub = new HashMap<>();
        this.registroGoleadores = new ConcurrentHashMap<>();
        this.lockTabla = new ReentrantLock();
    }

    public void registrarClub(String clubId, String nombreClub) {
        lockTabla.lock();
        try {
            tablaPorClub.putIfAbsent(clubId, new FilaPosicion(clubId, nombreClub));
        } finally {
            lockTabla.unlock();
        }
    }

    /**
     * Actualiza la tabla de posiciones con el resultado de un partido de forma atómica y thread-safe.
     */
    public void actualizarResultado(ResultadoPartidoDTO resultado) {
        if (resultado == null) return;

        lockTabla.lock();
        try {
            FilaPosicion local = tablaPorClub.computeIfAbsent(resultado.getClubLocalId(),
                    id -> new FilaPosicion(id, resultado.getNombreLocal()));
            FilaPosicion visitante = tablaPorClub.computeIfAbsent(resultado.getClubVisitanteId(),
                    id -> new FilaPosicion(id, resultado.getNombreVisitante()));

            int gfLocal = resultado.getGolesLocal();
            int gfVisitante = resultado.getGolesVisitante();

            if (gfLocal > gfVisitante) {
                local.sumarVictoria(gfLocal, gfVisitante);
                visitante.sumarDerrota(gfVisitante, gfLocal);
            } else if (gfLocal == gfVisitante) {
                local.sumarEmpate(gfLocal, gfVisitante);
                visitante.sumarEmpate(gfVisitante, gfLocal);
            } else {
                local.sumarDerrota(gfLocal, gfVisitante);
                visitante.sumarVictoria(gfVisitante, gfLocal);
            }

            // Actualizar goleadores concurrentemente
            for (IncidenciaDTO inc : resultado.getIncidencias()) {
                if (inc.getTipo() == TipoIncidenciaEnum.GOL && inc.getNombreJugador() != null) {
                    registroGoleadores.merge(inc.getNombreJugador(), 1, Integer::sum);
                }
            }
        } finally {
            lockTabla.unlock();
        }
    }

    /**
     * Retorna la tabla ordenada por mérito deportivo.
     */
    public List<FilaPosicion> obtenerTablaOrdenada() {
        lockTabla.lock();
        try {
            return tablaPorClub.values().stream()
                    .sorted()
                    .collect(Collectors.toList());
        } finally {
            lockTabla.unlock();
        }
    }

    public Map<String, Integer> obtenerGoleadores() {
        return Collections.unmodifiableMap(registroGoleadores);
    }
}
