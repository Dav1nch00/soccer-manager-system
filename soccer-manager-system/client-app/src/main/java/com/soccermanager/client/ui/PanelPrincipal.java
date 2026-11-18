package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del cliente con navegación por pestañas.
 * Agrupa todas las vistas funcionales del Director Técnico.
 */
public class PanelPrincipal extends JFrame {

    private final MotorConexion conexion;
    private final SesionDTO sesion;

    public PanelPrincipal(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;

        setTitle("Soccer Manager - Panel del Director Técnico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));

        inicializarInterfaz();
    }

    private void inicializarInterfaz() {
        // Barra de encabezado con información del DT
        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(new Color(0, 102, 204));
        panelEncabezado.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblBienvenida = new JLabel(
                "<html><b>DT:</b> " + sesion.getNombreUsuario() +
                        " &nbsp;|&nbsp; <b>Club:</b> " + sesion.getNombreClub() +
                        " &nbsp;|&nbsp; <b>Sesión:</b> " + sesion.getIdSesion() + "</html>");
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBienvenida.setForeground(Color.WHITE);

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(204, 0, 0));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro que desea cerrar la sesión?",
                    "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                dispose();
                // Volver al login
                VentanaLogin login = new VentanaLogin(conexion);
                login.setLocationRelativeTo(null);
                login.setVisible(true);
            }
        });

        panelEncabezado.add(lblBienvenida, BorderLayout.WEST);
        panelEncabezado.add(btnCerrarSesion, BorderLayout.EAST);
        add(panelEncabezado, BorderLayout.NORTH);

        // Pestañas principales
        JTabbedPane pestañas = new JTabbedPane(SwingConstants.TOP);

        PanelPlantilla panelPlantilla = new PanelPlantilla(conexion, sesion);
        PanelScouting panelScouting = new PanelScouting(conexion, sesion);
        PanelPizarra panelPizarra = new PanelPizarra(conexion, sesion);
        PanelResultados panelResultados = new PanelResultados(conexion, sesion);

        pestañas.addTab("Mi Plantilla", panelPlantilla);
        pestañas.addTab("Centro de Scouting", panelScouting);
        pestañas.addTab("Pizarra Táctica", panelPizarra);
        pestañas.addTab("Resultados y Tabla", panelResultados);

        add(pestañas, BorderLayout.CENTER);

        // Barra de estado inferior
        JLabel lblEstado = new JLabel("Listo - Conectado a " + conexion.getUrlBase());
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblEstado, BorderLayout.SOUTH);
    }
}