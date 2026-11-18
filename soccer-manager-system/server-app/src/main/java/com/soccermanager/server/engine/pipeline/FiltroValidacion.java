package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.enums.PosicionEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;

import java.util.List;

/**
 * Filtro 1: Inspecciona y valida que ambos clubes dispongan de 11 jugadores habilitados.
 * Si faltan posiciones o hay jugadores lesionados/sancionados, auto-completa con suplentes aptos.
 */
public class FiltroValidacion implements FiltroPartido {

    @Override
    public ContextoPartido procesar(ContextoPartido contexto) {
        if (contexto == null) return null;

        asegurarAlineacionCompleta(contexto.getEquipoLocal(), contexto.getClubLocal());
        asegurarAlineacionCompleta(contexto.getEquipoVisitante(), contexto.getClubVisitante());

        contexto.setValido(true);
        contexto.setMensajeValidacion("Alineaciones validadas correctamente (11 vs 11).");
        return contexto;
    }

    private void asegurarAlineacionCompleta(Equipo equipo, Club club) {
        if (equipo == null || club == null) return;

        List<Jugador> titularesActuales = equipo.getTodosLosTitulares();

        // 1. Remover jugadores inhabilitados (lesionados o sancionados)
        for (Jugador titular : titularesActuales) {
            if (!titular.getEstado().puedeJugar()) {
                equipo.getLineaPorteria().remover(titular);
                equipo.getLineaDefensa().remover(titular);
                equipo.getLineaMediocampo().remover(titular);
                equipo.getLineaDelantera().remover(titular);
            }
        }

        // 2. Si faltan para completar 11, buscar en la plantilla suplentes disponibles
        if (equipo.getCantidadElementos() < 11) {
            List<Jugador> disponibles = club.getJugadoresDisponibles();
            List<Jugador> yaAlineados = equipo.getTodosLosTitulares();

            for (Jugador suplente : disponibles) {
                if (equipo.getCantidadElementos() >= 11) break;
                if (!yaAlineados.contains(suplente)) {
                    equipo.agregarTitular(suplente);
                }
            }

            // 3. Si aún faltasen jugadores en la plantilla, generar suplentes de emergencia
            int deficit = 11 - equipo.getCantidadElementos();
            for (int i = 0; i < deficit; i++) {
                Jugador canterano = new Jugador(
                        "emer-" + club.getIdClub() + "-" + i,
                        "Canterano " + (i + 1),
                        30 + i,
                        PosicionEnum.MEDIOCAMPISTA,
                        club.getIdClub(),
                        55, 55, 55, 55, 55
                );
                equipo.agregarTitular(canterano);
            }
        }
    }
}
