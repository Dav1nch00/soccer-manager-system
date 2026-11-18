package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.dto.IncidenciaDTO;
import com.soccermanager.common.enums.TipoIncidenciaEnum;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.domain.state.EstadoLesionado;

import java.util.Random;

/**
 * Filtro 4: Aplica el decremento de energía y fatiga según la intensidad de la estrategia
 * y evalúa probabilidades estocásticas de lesión deportiva.
 */
public class FiltroDesgaste implements FiltroPartido {

    private final Random random;

    public FiltroDesgaste() {
        this(new Random());
    }

    public FiltroDesgaste(Random random) {
        this.random = random;
    }

    @Override
    public ContextoPartido procesar(ContextoPartido contexto) {
        if (contexto == null || !contexto.isValido()) return contexto;

        // Desgaste físico según estrategia
        int desgasteLocal = contexto.getEstrategiaLocal().getDesgasteBase() + contexto.getModLocal().getDesgasteExtraEnergia();
        int desgasteVisitante = contexto.getEstrategiaVisitante().getDesgasteBase() + contexto.getModVisitante().getDesgasteExtraEnergia();

        aplicarDesgasteYLesiones(contexto, contexto.getEquipoLocal(), contexto.getClubLocal().getNombre(), desgasteLocal);
        aplicarDesgasteYLesiones(contexto, contexto.getEquipoVisitante(), contexto.getClubVisitante().getNombre(), desgasteVisitante);

        return contexto;
    }

    private void aplicarDesgasteYLesiones(ContextoPartido ctx, Equipo equipo, String nombreClub, int desgaste) {
        for (Jugador j : equipo.getTodosLosTitulares()) {
            j.reducirEnergia(desgaste);

            // Probabilidad estocástica de lesión (mayor si el jugador está fatigado)
            double probLesion = (j.getEnergia() < 40) ? 0.08 : 0.02;

            if (random.nextDouble() < probLesion) {
                int semanasIncapacidad = 1 + random.nextInt(3); // 1 a 3 jornadas fuera
                j.cambiarEstado(new EstadoLesionado(semanasIncapacidad));

                int minutoLesion = 45 + random.nextInt(45);
                ctx.agregarIncidencia(new IncidenciaDTO(
                        minutoLesion,
                        TipoIncidenciaEnum.LESION,
                        equipo.getClubId(),
                        nombreClub,
                        j.getIdJugador(),
                        j.getNombre(),
                        "Lesión muscular: baja médica estimada por " + semanasIncapacidad + " semanas"
                ));
            }
        }
    }
}
