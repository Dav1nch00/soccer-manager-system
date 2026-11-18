package com.soccermanager.server.engine.pipeline;

import com.soccermanager.server.domain.strategy.ModificadorTactico;

/**
 * Filtro 2: Evalúa los potenciales de las líneas con el Patrón Composite,
 * aplica los contrarrestos con el Patrón Strategy y calcula las probabilidades relativas de gol y posesión.
 */
public class FiltroCalculoTactico implements FiltroPartido {

    @Override
    public ContextoPartido procesar(ContextoPartido contexto) {
        if (contexto == null || !contexto.isValido()) return contexto;

        // 1. Evaluación de Modificadores del Patrón Strategy
        ModificadorTactico modLocal = contexto.getEstrategiaLocal().aplicarModificadores(
                contexto.getEquipoLocal(), contexto.getEstrategiaVisitante()
        );
        ModificadorTactico modVisitante = contexto.getEstrategiaVisitante().aplicarModificadores(
                contexto.getEquipoVisitante(), contexto.getEstrategiaLocal()
        );

        contexto.setModLocal(modLocal);
        contexto.setModVisitante(modVisitante);

        // 2. Cálculo del Poder de Control (Posesión de Balón)
        double ctlLocal = contexto.getEquipoLocal().calcularPoderControl() * (1.0 + modLocal.getBonoControl()) * 1.05; // 5% ventaja localía
        double ctlVisitante = contexto.getEquipoVisitante().calcularPoderControl() * (1.0 + modVisitante.getBonoControl());

        double totalControl = Math.max(1.0, ctlLocal + ctlVisitante);
        int posLocal = (int) Math.round((ctlLocal / totalControl) * 100);
        posLocal = Math.max(30, Math.min(70, posLocal)); // Rango creíble entre 30% y 70%
        int posVisitante = 100 - posLocal;

        contexto.setPosesionLocal(posLocal);
        contexto.setPosesionVisitante(posVisitante);

        return contexto;
    }
}
