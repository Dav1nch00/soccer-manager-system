package com.soccermanager.server.domain.strategy;

import com.soccermanager.common.enums.EstrategiaEnum;

/**
 * Fábrica para la instanciación de estrategias tácticas a partir de las enumeraciones del sistema.
 */
public final class EstrategiaFactory {

    private EstrategiaFactory() {
    }

    public static EstrategiaTactica obtenerEstrategia(EstrategiaEnum tipo) {
        if (tipo == null) {
            return new PosesionStrategy();
        }

        switch (tipo) {
            case CONTRAATAQUE:
                return new ContraataqueStrategy();
            case PRESION_ALTA:
                return new PresionAltaStrategy();
            case BLOQUE_BAJO:
                return new BloqueBajoStrategy();
            case POSESION:
            default:
                return new PosesionStrategy();
        }
    }
}
