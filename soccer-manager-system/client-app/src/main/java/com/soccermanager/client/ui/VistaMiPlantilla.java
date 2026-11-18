package com.soccermanager.client.ui;

import com.soccermanager.common.dto.JugadorDTO;

import java.util.List;

/**
 * Vista de Consulta y Monitoreo de la Plantilla de Futbolistas.
 */
public class VistaMiPlantilla {

    public void mostrar(List<JugadorDTO> plantilla, String nombreClub) {
        System.out.println("==========================================================================================");
        System.out.println("                  PLANTILLA OFICIAL: " + nombreClub.toUpperCase());
        System.out.println("==========================================================================================");
        System.out.printf("%-4s | %-18s | %-4s | %-4s %-4s %-4s %-4s %-4s | %-5s | %-16s | %-15s%n",
                "#", "Nombre", "Pos", "VEL", "PAS", "TIR", "DEF", "FIS", "Media", "Energía", "Estado");
        System.out.println("------------------------------------------------------------------------------------------");

        for (JugadorDTO j : plantilla) {
            String barra = generarBarraEnergia(j.getEnergia());
            String estadoStr = j.getEstado().getEtiqueta();
            if (j.getSemanasLesionRestantes() > 0) {
                estadoStr += " (" + j.getSemanasLesionRestantes() + " sem)";
            } else if (j.getJornadasSancionRestantes() > 0) {
                estadoStr += " (" + j.getJornadasSancionRestantes() + " fec)";
            }

            System.out.printf("%-4d | %-18s | %-4s | %-4d %-4d %-4d %-4d %-4d | %-5.1f | %-16s | %-15s%n",
                    j.getDorsal(),
                    truncar(j.getNombre(), 18),
                    j.getPosicion().getAbreviatura(),
                    j.getVelocidad(), j.getPase(), j.getTiro(), j.getDefensa(), j.getFisico(),
                    j.getMediaGeneral(),
                    barra,
                    estadoStr
            );
        }
        System.out.println("==========================================================================================\n");
    }

    private String generarBarraEnergia(int energia) {
        int bloques = Math.max(0, Math.min(10, energia / 10));
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < bloques) sb.append("#");
            else sb.append(" ");
        }
        sb.append("] ").append(energia).append("%");
        return sb.toString();
    }

    private String truncar(String str, int len) {
        if (str == null) return "";
        return str.length() > len ? str.substring(0, len - 2) + ".." : str;
    }
}
