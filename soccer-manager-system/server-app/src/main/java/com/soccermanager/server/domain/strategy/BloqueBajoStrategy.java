package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.server.domain.model.Equipo;

/**
 * Estrategia de Bloque Bajo: Todos los efectivos replegados cerca del área propia.
 * Máxima solidez defensiva a costa de sacrificar presencia en ataque; bajo desgaste físico.
 */
public class BloqueBajoStrategy implements EstrategiaTactica {
    private static final long serialVersionUID = 1L;

    @Override
    public ModificadorTactico aplicarModificadores(Equipo miEquipo, EstrategiaTactica estrategiaRival) {
        ModificadorTactico mod = new ModificadorTactico();
        mod.setBonoDefensivo(0.35);  // +35% de muro defensivo
        mod.setBonoOfensivo(-0.25);  // -25% por pocos efectivos en delantera
        mod.setBonoControl(-0.20);   // Cede la posesión
        mod.setDesgasteExtraEnergia(-5); // -5 puntos (menor desgaste físico al no correr a campo abierto)
        mod.setFactorFaltas(0.80);   // Menor riesgo de tarjetas en campo abierto

        return mod;
    }

    @Override
    public int getDesgasteBase() {
        return 12; // Apenas 12 puntos de energía base
    }

    @Override
    public EstrategiaEnum getTipo() {
        return EstrategiaEnum.BLOQUE_BAJO;
    }
}
