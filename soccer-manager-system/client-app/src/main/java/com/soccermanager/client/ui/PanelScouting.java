package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.ReporteRivalDTO;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import java.awt.*;

public class PanelScouting extends JPanel {

    private final MotorConexion conexion;
    private final SesionDTO sesion;

    private JLabel lblNombreRival;
    private JLabel lblFormacion;
    private JLabel lblEstilo;
    private JLabel lblPromedioGoles;
    private JLabel lblRacha;
    private JLabel lblMediaDefensa;
    private JLabel lblMediaMediocampo;
    private JLabel lblMediaAtaque;
    private JLabel lblMediaGlobal;
    private JLabel lblJugadorEstrella;
    private JButton btnRefrescar;

    public PanelScouting(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;
        inicializarComponentes();
        cargarScouting();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Centro de Scouting - Informe del Rival");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0, 102, 204));
        panelEncabezado.add(lblTitulo, BorderLayout.WEST);

        btnRefrescar = new JButton("Refrescar Informe");
        btnRefrescar.addActionListener(e -> cargarScouting());
        panelEncabezado.add(btnRefrescar, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);

        JPanel panelInfo = new JPanel(new GridBagLayout());
        panelInfo.setBorder(BorderFactory.createTitledBorder("Informe de Inteligencia del Rival"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        lblNombreRival = crearCampo(panelInfo, "Club Rival:", gbc, 0);
        lblFormacion = crearCampo(panelInfo, "Formacion Habitual:", gbc, 1);
        lblEstilo = crearCampo(panelInfo, "Estilo Predominante:", gbc, 2);
        lblPromedioGoles = crearCampo(panelInfo, "Promedio Goles/Partido:", gbc, 3);
        lblRacha = crearCampo(panelInfo, "Racha Ultimos 5:", gbc, 4);
        lblMediaDefensa = crearCampo(panelInfo, "Solidez Defensiva:", gbc, 5);
        lblMediaMediocampo = crearCampo(panelInfo, "Control de Mediocampo:", gbc, 6);
        lblMediaAtaque = crearCampo(panelInfo, "Peligro Ofensivo:", gbc, 7);
        lblMediaGlobal = crearCampo(panelInfo, "Media Global del Rival:", gbc, 8);
        lblJugadorEstrella = crearCampo(panelInfo, "Jugador Estrella:", gbc, 9);

        add(panelInfo, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblNota = new JLabel("Nota: Use esta informacion para ajustar su formacion y estrategia en la Pizarra Tactica.");
        lblNota.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblNota.setForeground(Color.GRAY);
        panelInferior.add(lblNota);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JLabel crearCampo(JPanel panel, String etiqueta, GridBagConstraints gbc, int fila) {
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lblEtiqueta, gbc);

        JLabel lblValor = new JLabel("--");
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = fila;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblValor, gbc);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        return lblValor;
    }

    private void cargarScouting() {
        btnRefrescar.setEnabled(false);

        String rivalId = sesion.getClubId().equalsIgnoreCase("club-nal") ? "club-mil" : "club-nal";

        SwingWorker<ReporteRivalDTO, Exception> worker = new SwingWorker<>() {
            @Override
            protected ReporteRivalDTO doInBackground() throws Exception {
                return conexion.obtenerScouting(rivalId);
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    ReporteRivalDTO reporte = get();
                    if (reporte == null) {
                        limpiarCampos();
                        return;
                    }
                    mostrarReporte(reporte);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelScouting.this,
                            "No se pudo cargar el informe de scouting: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void mostrarReporte(ReporteRivalDTO reporte) {
        lblNombreRival.setText(reporte.getNombreRival());
        lblFormacion.setText(reporte.getFormacionHabitual() != null
                ? reporte.getFormacionHabitual().getEtiqueta() : "--");
        lblEstilo.setText(reporte.getEstiloPredominante() != null
                ? reporte.getEstiloPredominante().getNombre() : "--");
        lblPromedioGoles.setText(String.format("%.2f", reporte.getPromedioGolesPorPartido()));

        StringBuilder racha = new StringBuilder();
        for (String r : reporte.getRachaUltimos5()) {
            racha.append("[").append(r).append("] ");
        }
        lblRacha.setText(racha.length() > 0 ? racha.toString().trim() : "--");

        lblMediaDefensa.setText(String.format("%.1f / 100", reporte.getMediaDefensa()));
        lblMediaMediocampo.setText(String.format("%.1f / 100", reporte.getMediaMediocampo()));
        lblMediaAtaque.setText(String.format("%.1f / 100", reporte.getMediaAtaque()));
        lblMediaGlobal.setText(String.format("%.1f / 100", reporte.getMediaGlobalRival()));

        if (reporte.getJugadorEstrella() != null) {
            lblJugadorEstrella.setText(String.format(
                    "#%d %s (%s) - Media: %.1f",
                    reporte.getJugadorEstrella().getDorsal(),
                    reporte.getJugadorEstrella().getNombre(),
                    reporte.getJugadorEstrella().getPosicion(),
                    reporte.getJugadorEstrella().getMediaGeneral()
            ));
        } else {
            lblJugadorEstrella.setText("--");
        }
    }

    private void limpiarCampos() {
        lblNombreRival.setText("--");
        lblFormacion.setText("--");
        lblEstilo.setText("--");
        lblPromedioGoles.setText("--");
        lblRacha.setText("--");
        lblMediaDefensa.setText("--");
        lblMediaMediocampo.setText("--");
        lblMediaAtaque.setText("--");
        lblMediaGlobal.setText("--");
        lblJugadorEstrella.setText("--");
    }
}