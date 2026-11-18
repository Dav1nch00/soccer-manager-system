package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.server.domain.model.Equipo;

/**
 * Estrategia de Posesión: Monopolio del balón, control del ritmo y circulación.
 * Vulnerable a las transiciones rápidas del contraataque.
 */
public class PosesionStrategy implements EstrategiaTactica {
    private static final long serialVersionUID = 1L;

    @Override
    public ModificadorTactico aplicarModificadores(Equipo miEquipo, EstrategiaTactica estrategiaRival) {
        ModificadorTactico mod = new ModificadorTactico();
        mod.setBonoControl(0.25);   // +25% control de balón
        mod.setBonoOfensivo(0.10);  // +10% ataque paciente
        mod.setBonoDefensivo(0.0);
        mod.setDesgasteExtraEnergia(0);
        mod.setFactorFaltas(0.9);   // Menos faltas por tener el balón

        // Contrarresto: Si el rival juega al Contraataque, sufrimos riesgo defensivo
        if (estrategiaRival instanceof ContraataqueStrategy) {
            mod.setBonoDefensivo(-0.15); // -15% defensivo por dejar espacios a la espalda
        }

        return mod;
    }

    @Override
    public int getDesgasteBase() {
        return 15; // 15 puntos de energía
    }

    @Override
    public EstrategiaEnum getTipo() {
        return EstrategiaEnum.POSESION;
    }
}
