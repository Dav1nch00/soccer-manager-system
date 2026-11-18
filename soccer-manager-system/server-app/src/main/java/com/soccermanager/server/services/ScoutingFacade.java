package com.soccermanager.server.services;

import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.ReporteRivalDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Estructural Facade.
 * Simplifica la recopilación de inteligencia táctica y deportiva del próximo rival,
 * consultando plantillas, calculando medias con el Composite y empaquetando todo en un ReporteRivalDTO limpio.
 */
public class ScoutingFacade implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Genera un informe integral de inteligencia deportiva sobre el club adversario.
     *
     * @param rival Club oponente a analizar.
     * @param historialPartidos Partidos previos disputados por el rival en la temporada.
     * @return ReporteRivalDTO estructurado para el cliente.
     */
    public ReporteRivalDTO generarReporteRival(Club rival, List<ResultadoPartidoDTO> historialPartidos) {
        if (rival == null) {
            throw new IllegalArgumentException("El club rival no puede ser nulo.");
        }

        // 1. Instanciar un equipo titular con el Patrón Composite para calcular medias de líneas
        Equipo equipoRival = new Equipo(rival.getIdClub(), rival.getNombre(), rival.getFormacionPredeterminada());
        for (Jugador j : rival.getPlantilla()) {
            equipoRival.agregarTitular(j);
        }

        double mediaDefensa = equipoRival.getLineaDefensa().calcularPoderDefensivo();
        double mediaMedio = equipoRival.getLineaMediocampo().calcularPoderControl();
        double mediaAtaque = equipoRival.getLineaDelantera().calcularPoderOfensivo();

        // 2. Jugador Estrella
        Jugador estrella = rival.getJugadorEstrella();
        JugadorDTO estrellaDTO = (estrella != null) ? estrella.toDTO() : null;

        // 3. Crear el DTO base
        ReporteRivalDTO reporte = new ReporteRivalDTO(
                rival.getIdClub(),
                rival.getNombre(),
                rival.getFormacionPredeterminada(),
                rival.getEstrategiaPredeterminada(),
                estrellaDTO,
                mediaDefensa,
                mediaMedio,
                mediaAtaque
        );

        // 4. Analizar racha de los últimos 5 partidos y promedio de goles
        List<String> racha = new ArrayList<>();
        int golesTotales = 0;
        int partidosContabilizados = 0;

        if (historialPartidos != null) {
            int limite = Math.min(5, historialPartidos.size());
            for (int i = 0; i < limite; i++) {
                ResultadoPartidoDTO p = historialPartidos.get(i);
                boolean esLocal = rival.getIdClub().equals(p.getClubLocalId());
                int gf = esLocal ? p.getGolesLocal() : p.getGolesVisitante();
                int gc = esLocal ? p.getGolesVisitante() : p.getGolesLocal();

                golesTotales += gf;
                partidosContabilizados++;

                if (gf > gc) {
                    racha.add("V");
                } else if (gf == gc) {
                    racha.add("E");
                } else {
                    racha.add("D");
                }
            }
        }

        // Si no hay partidos disputados aún, racha neutral por defecto
        if (racha.isEmpty()) {
            racha.add("V");
            racha.add("E");
            racha.add("V");
        }

        reporte.setRachaUltimos5(racha);
        double promGoles = partidosContabilizados > 0 ? ((double) golesTotales / partidosContabilizados) : 1.8;
        reporte.setPromedioGolesPorPartido(Math.round(promGoles * 10.0) / 10.0);

        return reporte;
    }
}
