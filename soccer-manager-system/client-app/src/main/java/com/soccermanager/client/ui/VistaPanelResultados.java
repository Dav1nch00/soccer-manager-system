package com.soccermanager.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccermanager.common.dto.IncidenciaDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;

import java.util.List;
import java.util.Map;

/**
 * Vista de Marcadores, Incidencias y Clasificación General de la Liga.
 */
public class VistaPanelResultados {

    private final ObjectMapper mapper = new ObjectMapper();

    public void mostrar(Map<String, Object> dataTorneo) {
        System.out.println("==========================================================================================");
        System.out.println("                 RESULTADOS DE LA JORNADA SIMULADA EN PARALELO            ");
        System.out.println("==========================================================================================");

        List<ResultadoPartidoDTO> partidos = mapper.convertValue(
                dataTorneo.get("resultadosUltimaFecha"),
                new TypeReference<List<ResultadoPartidoDTO>>() {}
        );

        if (partidos != null && !partidos.isEmpty()) {
            for (ResultadoPartidoDTO r : partidos) {
                System.out.println("------------------------------------------------------------------------------------------");
                System.out.printf("  %-25s  %2d - %-2d  %-25s%n",
                        r.getNombreLocal(), r.getGolesLocal(), r.getGolesVisitante(), r.getNombreVisitante());
                System.out.printf("  [Posesión: %d%% - %d%% | Tiros: %d - %d]%n",
                        r.getPosesionLocal(), r.getPosesionVisitante(), r.getTirosLocal(), r.getTirosVisitante());

                if (r.getIncidencias() != null && !r.getIncidencias().isEmpty()) {
                    System.out.println("  Incidencias:");
                    for (IncidenciaDTO inc : r.getIncidencias()) {
                        System.out.println("    * " + inc.toString());
                    }
                }
            }
        }

        System.out.println("\n==========================================================================================");
        System.out.println("                         TABLA DE POSICIONES ACTUALIZADA                  ");
        System.out.println("==========================================================================================");
        System.out.printf("%-4s | %-24s | %-4s | %-3s %-3s %-3s %-3s | %-4s %-4s | %-4s%n",
                "Pos", "Club", "PTS", "PJ", "PG", "PE", "PP", "GF", "GC", "DIF");
        System.out.println("------------------------------------------------------------------------------------------");

        List<Map<String, Object>> tabla = (List<Map<String, Object>>) dataTorneo.get("tabla");
        if (tabla != null) {
            int pos = 1;
            for (Map<String, Object> fila : tabla) {
                int gf = ((Number) fila.get("golesAFavor")).intValue();
                int gc = ((Number) fila.get("golesEnContra")).intValue();
                int dif = gf - gc;

                System.out.printf("%-4d | %-24s | %-4d | %-3d %-3d %-3d %-3d | %-4d %-4d | %+4d%n",
                        pos++,
                        fila.get("nombreClub"),
                        ((Number) fila.get("puntos")).intValue(),
                        ((Number) fila.get("partidosJugados")).intValue(),
                        ((Number) fila.get("partidosGanados")).intValue(),
                        ((Number) fila.get("partidosEmpatados")).intValue(),
                        ((Number) fila.get("partidosPerdidos")).intValue(),
                        gf, gc, dif
                );
            }
        }
        System.out.println("==========================================================================================\n");
    }
}
