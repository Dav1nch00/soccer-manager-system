package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Punto de entrada de la interfaz gráfica del cliente.
 * Inicia el diálogo de login y, tras autenticación, muestra el panel principal.
 */
public class ClienteGUI {

    public static void main(String[] args) {
        String urlServidor = args.length > 0 ? args[0] : "http://localhost:8080";

        SwingUtilities.invokeLater(() -> {
            try {
                // Look and feel nativo del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Usar el look and feel por defecto si falla
            }

            MotorConexion conexion = new MotorConexion(urlServidor);

            // Diálogo de login (modal)
            VentanaLogin dialogoLogin = new VentanaLogin(conexion);
            dialogoLogin.setLocationRelativeTo(null);
            dialogoLogin.setVisible(true);

            SesionDTO sesion = dialogoLogin.getSesion();
            if (sesion != null) {
                // Login exitoso: abrir ventana principal
                PanelPrincipal ventanaPrincipal = new PanelPrincipal(conexion, sesion);
                ventanaPrincipal.setLocationRelativeTo(null);
                ventanaPrincipal.setVisible(true);
            }
        });
    }
}