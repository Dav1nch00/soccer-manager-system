package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.server.domain.model.Equipo;

import java.io.Serializable;

/**
 * Interfaz del Patrón Strategy.
 * Encapsula la familia de algoritmos que modulan las probabilidades del partido
 * según el estilo futbolístico seleccionado y los contrarrestos tácticos del rival.
 */
public interface EstrategiaTactica extends Serializable {

    /**
     * Evalúa el cruce táctico frente a la estrategia rival y computa los modificadores de probabilidad.
     *
     * @param miEquipo Equipo propio configurado.
     * @param estrategiaRival Estrategia desplegada por el adversario.
     * @return Objeto ModificadorTactico con bonificaciones y penalizaciones.
     */
    ModificadorTactico aplicarModificadores(Equipo miEquipo, EstrategiaTactica estrategiaRival);

    /**
     * Retorna el consumo base de energía física (desgaste) por partido de esta estrategia.
     */
    int getDesgasteBase();

    /**
     * Retorna el tipo de estrategia correspondiente a la enumeración.
     */
    EstrategiaEnum getTipo();
}
