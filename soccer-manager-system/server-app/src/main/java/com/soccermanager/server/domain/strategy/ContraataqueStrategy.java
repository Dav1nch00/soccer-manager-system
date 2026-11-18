package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.server.domain.model.Equipo;

/**
 * Estrategia de Contraataque: Repliegue y salidas a toda velocidad al recuperar el balón.
 * Obtiene ventaja decisiva frente a equipos de Posesión, pero demanda gran esfuerzo físico.
 */
public class ContraataqueStrategy implements EstrategiaTactica {
    private static final long serialVersionUID = 1L;

    @Override
    public ModificadorTactico aplicarModificadores(Equipo miEquipo, EstrategiaTactica estrategiaRival) {
        ModificadorTactico mod = new ModificadorTactico();
        mod.setBonoControl(-0.15);  // Cede la posesión
        mod.setBonoDefensivo(0.10);  // Bloque compacto
        mod.setBonoOfensivo(0.15);   // Verticalidad base
        mod.setDesgasteExtraEnergia(8); // +8 puntos de fatiga por carreras intensas
        mod.setFactorFaltas(1.0);

        // Contrarresto Clave frente a Posesión
        if (estrategiaRival instanceof PosesionStrategy) {
            mod.setBonoOfensivo(0.35); // +35% de contundencia en los espacios rivales
        } else if (estrategiaRival instanceof BloqueBajoStrategy) {
            mod.setBonoOfensivo(-0.10); // Sin espacios para correr frente a otro bloque bajo
        }

        return mod;
    }

    @Override
    public int getDesgasteBase() {
        return 22; // 22 puntos de energía base
    }

    @Override
    public EstrategiaEnum getTipo() {
        return EstrategiaEnum.CONTRAATAQUE;
    }
}
