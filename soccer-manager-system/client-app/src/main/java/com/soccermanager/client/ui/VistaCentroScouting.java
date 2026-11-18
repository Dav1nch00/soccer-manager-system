package com.soccermanager.client.ui;

import com.soccermanager.common.dto.ReporteRivalDTO;

/**
 * Vista de Inteligencia y Análisis del Rival provisto por ScoutingFacade.
 */
public class VistaCentroScouting {

    public void mostrar(ReporteRivalDTO reporte) {
        if (reporte == null) {
            System.out.println(">> No hay informe de scouting disponible.\n");
            return;
        }

        System.out.println("==================================================================");
        System.out.println("         CENTRO DE SCOUTING: INFORME DEL PRÓXIMO RIVAL            ");
        System.out.println("==================================================================");
        System.out.println("  Club Rival:                " + reporte.getNombreRival());
        System.out.println("  Formación Habitual:        " + reporte.getFormacionHabitual().getEtiqueta());
        System.out.println("  Estilo Predominante:       " + reporte.getEstiloPredominante().getNombre());
        System.out.println("  Promedio Goles / Partido:  " + reporte.getPromedioGolesPorPartido());

        System.out.print("  Racha Últimos 5 Partidos:  ");
        for (String r : reporte.getRachaUltimos5()) {
            System.out.print("[" + r + "] ");
        }
        System.out.println("\n------------------------------------------------------------------");

        System.out.println("  POTENCIAL POR LÍNEAS:");
        System.out.printf("  - Solidez Defensiva:       %-5.1f  %s%n", reporte.getMediaDefensa(), generarBarra(reporte.getMediaDefensa()));
        System.out.printf("  - Control de Mediocampo:   %-5.1f  %s%n", reporte.getMediaMediocampo(), generarBarra(reporte.getMediaMediocampo()));
        System.out.printf("  - Peligro Ofensivo:        %-5.1f  %s%n", reporte.getMediaAtaque(), generarBarra(reporte.getMediaAtaque()));
        System.out.printf("  - MEDIA GLOBAL RIVAL:      %-5.1f%n", reporte.getMediaGlobalRival());

        System.out.println("------------------------------------------------------------------");
        if (reporte.getJugadorEstrella() != null) {
            System.out.println("  ¡JUGADOR ESTRELLA A NEUTRALIZAR!");
            System.out.println("  Nombre: #" + reporte.getJugadorEstrella().getDorsal() + " " + reporte.getJugadorEstrella().getNombre() +
                    " (" + reporte.getJugadorEstrella().getPosicion() + ") - Media: " + String.format("%.1f", reporte.getJugadorEstrella().getMediaGeneral()));
        }
        System.out.println("==================================================================\n");
    }

    private String generarBarra(double valor) {
        int bloques = (int) Math.round(valor / 10.0);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < bloques) sb.append("=");
            else sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
