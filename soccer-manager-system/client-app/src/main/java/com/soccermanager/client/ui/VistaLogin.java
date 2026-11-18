package com.soccermanager.client.ui;

import com.soccermanager.common.dto.SesionDTO;
import com.soccermanager.client.network.MotorConexion;

import java.io.IOException;
import java.util.Scanner;

/**
 * Vista de Autenticación y Registro Automático de Perfil.
 */
public class VistaLogin {

    private final MotorConexion motorConexion;

    public VistaLogin(MotorConexion motorConexion) {
        this.motorConexion = motorConexion;
    }

    public SesionDTO ejecutar(Scanner scanner) {
        System.out.println("============================================================");
        System.out.println("       SIMULADOR TÁCTICO 'SOCCER MANAGER' - LOGIN           ");
        System.out.println("============================================================");
        System.out.println("Ingrese sus credenciales. Si es su primera vez, el sistema");
        System.out.println("creará automáticamente su archivo de perfil persistente.\n");

        while (true) {
            System.out.print("Nombre de Usuario (DT): ");
            String usuario = scanner.nextLine().trim();

            System.out.print("Contraseña: ");
            String password = scanner.nextLine().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                System.out.println(">> Error: Usuario y contraseña no pueden estar vacíos.\n");
                continue;
            }

            try {
                System.out.println("Conectando con el servidor en: " + motorConexion.getUrlBase() + " ...");
                SesionDTO sesion = motorConexion.loginORegistro(usuario, password, "club-nal");

                System.out.println("\n------------------------------------------------------------");
                if (sesion.isPerfilNuevoCreado()) {
                    System.out.println(">> ¡NUEVO PERFIL REGISTRADO CON ÉXITO!");
                    System.out.println(">> Se ha generado su archivo físico: data/perfiles/" + usuario + ".json");
                } else {
                    System.out.println(">> ¡BIENVENIDO DE NUEVO, DT " + sesion.getNombreUsuario().toUpperCase() + "!");
                    System.out.println(">> Perfil cargado exitosamente desde su archivo en disco.");
                }
                System.out.println(">> Club Asignado: " + sesion.getNombreClub());
                System.out.println("------------------------------------------------------------\n");

                return sesion;
            } catch (IOException | InterruptedException e) {
                System.out.println(">> Error de conexión o credenciales: " + e.getMessage());
                System.out.println(">> Intente de nuevo.\n");
            }
        }
    }
}
