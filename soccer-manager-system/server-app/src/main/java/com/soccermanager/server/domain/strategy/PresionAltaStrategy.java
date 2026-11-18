package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.server.domain.model.Equipo;

/**
 * Estrategia de Presión Alta: Acoso constante en campo contrario para forzar pérdidas del rival.
 * Aumenta la tasa de recuperación, pero genera un altísimo desgaste físico y riesgo de tarjetas.
 */
public class PresionAltaStrategy implements EstrategiaTactica {
    private static final long serialVersionUID = 1L;

    @Override
    public ModificadorTactico aplicarModificadores(Equipo miEquipo, EstrategiaTactica estrategiaRival) {
        ModificadorTactico mod = new ModificadorTactico();
        mod.setBonoControl(0.20);        // +20% recuperación rápida
        mod.setBonoOfensivo(0.20);       // +20% ocasiones por errores forzados
        mod.setBonoDefensivo(0.05);
        mod.setDesgasteExtraEnergia(12); // +12 puntos de fatiga por presión extenuante
        mod.setFactorFaltas(1.45);       // +45% probabilidad de infracciones y amonestaciones

        return mod;
    }

    @Override
    public int getDesgasteBase() {
        return 28; // 28 puntos de energía base
    }

    @Override
    public EstrategiaEnum getTipo() {
        return EstrategiaEnum.PRESION_ALTA;
    }
}
