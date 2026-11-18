package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.dto.ResultadoPartidoDTO;

/**
 * Filtro 5: Empaqueta y consolida el ResultadoPartidoDTO final con estadísticas,
 * incidencias cronológicas y calificaciones de rendimiento.
 */
public class FiltroConsolidacion implements FiltroPartido {

    @Override
    public ContextoPartido procesar(ContextoPartido contexto) {
        if (contexto == null) return null;

        ResultadoPartidoDTO resultado = new ResultadoPartidoDTO(
                contexto.getIdPartido(),
                contexto.getJornadaNumero(),
                contexto.getClubLocal().getIdClub(),
                contexto.getClubLocal().getNombre(),
                contexto.getGolesLocal(),
                contexto.getClubVisitante().getIdClub(),
                contexto.getClubVisitante().getNombre(),
                contexto.getGolesVisitante()
        );

        resultado.setPosesionLocal(contexto.getPosesionLocal());
        resultado.setPosesionVisitante(contexto.getPosesionVisitante());
        resultado.setTirosLocal(contexto.getTirosLocal());
        resultado.setTirosVisitante(contexto.getTirosVisitante());
        resultado.setIncidencias(contexto.getIncidencias());
        resultado.setCalificacionesJugadores(contexto.getCalificaciones());

        contexto.setResultadoFinal(resultado);
        return contexto;
    }
}
