package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de visualización de la plantilla del club.
 * Muestra a todos los jugadores en una tabla con sus atributos, energía y estado.
 */
public class PanelPlantilla extends JPanel {

    private final MotorConexion conexion;
    private final SesionDTO sesion;

    private DefaultTableModel modeloTabla;
    private JTable tablaJugadores;
    private JButton btnRefrescar;
    private JLabel lblInfoClub;

    public PanelPlantilla(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;
        inicializarComponentes();
        cargarPlantilla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblInfoClub = new JLabel("Cargando plantilla...");
        lblInfoClub.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblInfoClub.setForeground(new Color(0, 102, 204));

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.add(lblInfoClub, BorderLayout.WEST);

        btnRefrescar = new JButton("Refrescar Plantilla");
        btnRefrescar.addActionListener(e -> cargarPlantilla());
        panelEncabezado.add(btnRefrescar, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);

        String[] columnas = {
                "#", "Nombre", "Posición", "Velocidad", "Pase", "Tiro",
                "Defensa", "Físico", "Media", "Energía", "Estado", "Disponible"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaJugadores = new JTable(modeloTabla);
        tablaJugadores.setRowHeight(28);
        tablaJugadores.getTableHeader().setReorderingAllowed(false);
        tablaJugadores.getTableHeader().setBackground(new Color(0, 102, 204));
        tablaJugadores.getTableHeader().setForeground(Color.WHITE);
        tablaJugadores.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaJugadores.getColumnCount(); i++) {
            tablaJugadores.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaJugadores);
        add(scrollPane, BorderLayout.CENTER);

        JLabel lblLeyenda = new JLabel(
                "Estado: Óptimo = puede jugar | Cansado = fatigado | Lesionado/Sancionado = no disponible");
        lblLeyenda.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblLeyenda.setForeground(Color.GRAY);
        add(lblLeyenda, BorderLayout.SOUTH);
    }

    private void cargarPlantilla() {
        btnRefrescar.setEnabled(false);
        lblInfoClub.setText("Cargando plantilla de " + sesion.getNombreClub() + "...");

        SwingWorker<List<JugadorDTO>, Exception> worker = new SwingWorker<>() {
            @Override
            protected List<JugadorDTO> doInBackground() throws Exception {
                return conexion.obtenerPlantilla(sesion.getClubId());
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    List<JugadorDTO> plantilla = get();
                    lblInfoClub.setText(
                            "Plantilla de " + sesion.getNombreClub() +
                                    " - " + plantilla.size() + " jugadores"
                    );
                    llenarTabla(plantilla);
                } catch (Exception ex) {
                    lblInfoClub.setText("Error al cargar plantilla: " + ex.getMessage());
                    lblInfoClub.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(PanelPlantilla.this,
                            "No se pudo cargar la plantilla: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void llenarTabla(List<JugadorDTO> plantilla) {
        modeloTabla.setRowCount(0);

        for (JugadorDTO j : plantilla) {
            String disponible = j.isDisponible() ? "Sí" : "No";

            Object[] fila = {
                    j.getDorsal(),
                    j.getNombre(),
                    j.getPosicion().getAbreviatura(),
                    j.getVelocidad(),
                    j.getPase(),
                    j.getTiro(),
                    j.getDefensa(),
                    j.getFisico(),
                    String.format("%.1f", j.getMediaGeneral()),
                    j.getEnergia() + "%",
                    j.getEstado().getEtiqueta(),
                    disponible
            };
            modeloTabla.addRow(fila);
        }

        tablaJugadores.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaJugadores.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaJugadores.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaJugadores.getColumnModel().getColumn(10).setPreferredWidth(100);
        tablaJugadores.getColumnModel().getColumn(11).setPreferredWidth(80);
    }
}