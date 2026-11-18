package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.enums.ModoJuegoEnum;

import java.io.IOException;
import java.util.Scanner;

/**
 * Vista para seleccionar la modalidad de juego (Solo vs 2 Jugadores) y monitorear la sala.
 */
public class VistaSelectorModo {

    private final MotorConexion motorConexion;

    public VistaSelectorModo(MotorConexion motorConexion) {
        this.motorConexion = motorConexion;
    }

    public SalaEstadoDTO ejecutar(Scanner scanner) {
        System.out.println("============================================================");
        System.out.println("            SELECCIÓN DE MODO DE SIMULACIÓN                 ");
        System.out.println("============================================================");
        System.out.println("1. Modo Individual (Simular Solo vs IA)");
        System.out.println("2. Modo Multijugador (Simular con Otra Persona en Red - 60s Timer)");
        System.out.print("\nSeleccione una opción (1-2): ");

        while (true) {
            String op = scanner.nextLine().trim();
            ModoJuegoEnum modo;
            if ("1".equals(op)) {
                modo = ModoJuegoEnum.SOLO;
            } else if ("2".equals(op)) {
                modo = ModoJuegoEnum.DOS_JUGADORES;
            } else {
                System.out.print("Opción inválida. Ingrese 1 o 2: ");
                continue;
            }

            try {
                SalaEstadoDTO sala = motorConexion.configurarModo(modo);
                System.out.println("\n------------------------------------------------------------");
                System.out.println(">> Modo configurado: " + modo.getTitulo());
                System.out.println(">> Descripción: " + modo.getDescripcion());
                System.out.println(">> Estado de Sala: " + sala.getMensaje());
                System.out.println("------------------------------------------------------------\n");
                return sala;
            } catch (IOException | InterruptedException e) {
                System.out.println(">> Error al configurar modo: " + e.getMessage());
                return new SalaEstadoDTO();
            }
        }
    }
}
