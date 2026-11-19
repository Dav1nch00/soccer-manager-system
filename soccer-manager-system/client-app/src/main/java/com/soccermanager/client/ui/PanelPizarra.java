package com.soccermanager.client.ui;

import com.soccermanager.client.builders.PlanDeJuegoBuilder;
import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.PlanDeJuegoDTO;
import com.soccermanager.common.dto.SalaEstadoDTO;
import com.soccermanager.common.dto.SesionDTO;
import com.soccermanager.common.enums.EstadoSalaEnum;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Pizarra Táctica Visual.
 * Muestra un campo de fútbol con los 11 titulares posicionados
 * y un panel de suplentes a la derecha. Diseño intuitivo estilo videojuego.
 */
public class PanelPizarra extends JPanel {

    private final MotorConexion conexion;
    private final SesionDTO sesion;
    private final PlanDeJuegoBuilder builder;

    private JPanel panelCampo;
    private JPanel panelSuplentes;
    private JComboBox<FormacionEnum> comboFormacion;
    private JComboBox<EstrategiaEnum> comboEstrategia;
    private JButton btnSimular;
    private JLabel lblInfoTitulares;
    private JLabel lblClubInfo;

    // Mapas para rastrear los paneles de jugadores en el campo y suplentes
    private final List<JugadorCampoPanel> titularesEnCampo = new ArrayList<>();
    private final List<JugadorCampoPanel> suplentesPanel = new ArrayList<>();

    // Posiciones predeterminadas del campo (x, y) para formación 4-4-2
    private static final int[][] POSICIONES_CAMPO = {
            {50, 300},   // Portero
            {120, 180},  // Defensa Izq
            {120, 420},  // Defensa Der
            {250, 120},  // Defensa Central Izq
            {250, 480},  // Defensa Central Der
            {400, 150},  // Medio Izq
            {400, 450},  // Medio Der
            {500, 250},  // Medio Centro Izq
            {500, 350},  // Medio Centro Der
            {750, 200},  // Delantero Izq
            {750, 400}   // Delantero Der
    };

    public PanelPizarra(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;
        this.builder = new PlanDeJuegoBuilder();
        inicializarComponentes();
        inicializarAlineacion();
    }

    /**
     * Panel pequeño que representa a un jugador en el campo o en suplentes.
     * Muestra dorsal y nombre dentro de un círculo con borde de color.
     */
    private static class JugadorCampoPanel extends JPanel {
        private final JLabel lblDorsal;
        private final JLabel lblNombre;

        public JugadorCampoPanel(JugadorDTO jugador, boolean esTitular) {
            setLayout(new BorderLayout(2, 2));
            setPreferredSize(new Dimension(80, 60));
            setMaximumSize(new Dimension(80, 60));
            setMinimumSize(new Dimension(80, 60));

            // Color de borde según estado
            Color bordeColor = esTitular ? new Color(0, 153, 0) : new Color(102, 102, 102);
            if (!jugador.isDisponible()) {
                bordeColor = new Color(204, 0, 0);
            }
            setBorder(new LineBorder(bordeColor, 2, true));
            setBackground(new Color(240, 248, 255));

            lblDorsal = new JLabel(String.valueOf(jugador.getDorsal()), SwingConstants.CENTER);
            lblDorsal.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblDorsal.setForeground(new Color(0, 102, 204));

            lblNombre = new JLabel(jugador.getNombre(), SwingConstants.CENTER);
            lblNombre.setFont(new Font("SansSerif", Font.PLAIN, 10));
            lblNombre.setForeground(Color.DARK_GRAY);

            add(lblDorsal, BorderLayout.NORTH);
            add(lblNombre, BorderLayout.SOUTH);
        }

        public void actualizar(JugadorDTO jugador, boolean esTitular) {
            lblDorsal.setText(String.valueOf(jugador.getDorsal()));
            lblNombre.setText(jugador.getNombre());
            Color bordeColor = esTitular ? new Color(0, 153, 0) : new Color(102, 102, 102);
            if (!jugador.isDisponible()) {
                bordeColor = new Color(204, 0, 0);
            }
            setBorder(new LineBorder(bordeColor, 2, true));
            repaint();
        }
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        lblClubInfo = new JLabel("Club: " + sesion.getNombreClub());
        lblClubInfo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblClubInfo.setForeground(new Color(0, 102, 204));
        panelEncabezado.add(lblClubInfo, BorderLayout.WEST);

        btnSimular = new JButton("Simular Partido");
        btnSimular.setBackground(new Color(0, 153, 0));
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSimular.addActionListener(e -> confirmarYSimular());
        panelEncabezado.add(btnSimular, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));

        panelCampo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(34, 139, 34));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));

                g2.drawRect(10, 10, getWidth() - 20, getHeight() - 20);

                g2.drawLine(getWidth() / 2, 10, getWidth() / 2, getHeight() - 10);

                int centroX = getWidth() / 2;
                int centroY = getHeight() / 2;
                int radio = 60;
                g2.drawOval(centroX - radio, centroY - radio, radio * 2, radio * 2);
                g2.fillOval(centroX - 3, centroY - 3, 6, 6);

                int areaAncho = 130;
                int areaAlto = 200;
                g2.drawRect(10, centroY - areaAlto / 2, areaAncho, areaAlto);
                g2.drawRect(getWidth() - 10 - areaAncho, centroY - areaAlto / 2, areaAncho, areaAlto);

                int metaAncho = 50;
                int metaAlto = 100;
                g2.drawRect(10, centroY - metaAlto / 2, metaAncho, metaAlto);
                g2.drawRect(getWidth() - 10 - metaAncho, centroY - metaAlto / 2, metaAncho, metaAlto);

                g2.setColor(new Color(200, 200, 200));
                g2.drawLine(10, centroY - metaAlto / 2, 10, centroY + metaAlto / 2);
                g2.drawLine(getWidth() - 10, centroY - metaAlto / 2, getWidth() - 10, centroY + metaAlto / 2);
            }
        };
        panelCampo.setPreferredSize(new Dimension(850, 500));
        panelCampo.setMinimumSize(new Dimension(600, 400));
        panelCampo.setLayout(null);
        panelCentral.add(panelCampo, BorderLayout.CENTER);

        panelSuplentes = new JPanel();
        panelSuplentes.setLayout(new BoxLayout(panelSuplentes, BoxLayout.Y_AXIS));
        panelSuplentes.setPreferredSize(new Dimension(200, 500));
        panelSuplentes.setBorder(BorderFactory.createTitledBorder("Suplentes"));

        JScrollPane scrollSuplentes = new JScrollPane(panelSuplentes);
        scrollSuplentes.setPreferredSize(new Dimension(220, 500));
        panelCentral.add(scrollSuplentes, BorderLayout.EAST);

        add(panelCentral, BorderLayout.CENTER);

        JPanel panelConfig = new JPanel(new GridBagLayout());
        panelConfig.setBorder(BorderFactory.createTitledBorder("Configuración Táctica"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("Formación:"), gbc);
        gbc.gridx = 1;
        comboFormacion = new JComboBox<>(FormacionEnum.values());
        comboFormacion.setSelectedItem(FormacionEnum.F_4_4_2);
        comboFormacion.addActionListener(e -> actualizarFormacion());
        panelConfig.add(comboFormacion, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        panelConfig.add(new JLabel("Estrategia:"), gbc);
        gbc.gridx = 3;
        comboEstrategia = new JComboBox<>(EstrategiaEnum.values());
        comboEstrategia.setSelectedItem(EstrategiaEnum.POSESION);
        comboEstrategia.addActionListener(e -> actualizarEstrategia());
        panelConfig.add(comboEstrategia, gbc);

        add(panelConfig, BorderLayout.SOUTH);

        lblInfoTitulares = new JLabel("Titulares: 0 / 11 | Suplentes: 0 / 11");
        lblInfoTitulares.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblInfoTitulares, BorderLayout.PAGE_END);
    }

    private void inicializarAlineacion() {
        try {
            List<JugadorDTO> plantilla = conexion.obtenerPlantilla(sesion.getClubId());
            builder.setClub(sesion.getClubId(), sesion.getNombreClub());
            builder.setFormacion(FormacionEnum.F_4_4_2);
            builder.setEstrategia(EstrategiaEnum.POSESION);
            builder.limpiarTitulares();

            alinearPorPosicion(plantilla, PosicionEnum.PORTERO, 1);
            alinearPorPosicion(plantilla, PosicionEnum.DEFENSA, 4);
            alinearPorPosicion(plantilla, PosicionEnum.MEDIOCAMPISTA, 4);
            alinearPorPosicion(plantilla, PosicionEnum.DELANTERO, 2);

            actualizarCampo();
        } catch (Exception e) {
            lblClubInfo.setText("Error al cargar plantilla: " + e.getMessage());
        }
    }

    private void alinearPorPosicion(List<JugadorDTO> plantilla, PosicionEnum pos, int requeridos) {
        int count = 0;
        for (JugadorDTO j : plantilla) {
            if (count >= requeridos) break;
            if (j.getPosicion() == pos && j.isDisponible()) {
                builder.agregarTitular(j);
                count++;
            }
        }
    }

    private void actualizarFormacion() {
        FormacionEnum formacion = (FormacionEnum) comboFormacion.getSelectedItem();
        builder.setFormacion(formacion);
        actualizarCampo();
    }

    private void actualizarEstrategia() {
        EstrategiaEnum estrategia = (EstrategiaEnum) comboEstrategia.getSelectedItem();
        builder.setEstrategia(estrategia);
    }

    private void actualizarCampo() {
        panelCampo.removeAll();
        panelSuplentes.removeAll();
        titularesEnCampo.clear();
        suplentesPanel.clear();

        List<JugadorDTO> titulares = builder.getTitularesActuales();
        List<JugadorDTO> plantilla;
        try {
            plantilla = conexion.obtenerPlantilla(sesion.getClubId());
        } catch (Exception e) {
            plantilla = new ArrayList<>();
        }

        int posIndex = 0;
        int alturaCampo = panelCampo.getHeight() > 0 ? panelCampo.getHeight() : 500;
        int anchoCampo = panelCampo.getWidth() > 0 ? panelCampo.getWidth() : 850;

        for (JugadorDTO j : titulares) {
            int x, y;
            switch (j.getPosicion()) {
                case PORTERO:
                    x = 50; y = alturaCampo / 2;
                    break;
                case DEFENSA:
                    x = 120 + (posIndex % 4) * 130;
                    y = posIndex < 2 ? 150 : alturaCampo - 150;
                    break;
                case MEDIOCAMPISTA:
                    x = 350 + (posIndex % 4) * 100;
                    y = posIndex < 2 ? 120 : alturaCampo - 120;
                    break;
                case DELANTERO:
                    x = anchoCampo - 150 + (posIndex % 2) * 50;
                    y = posIndex < 1 ? 180 : alturaCampo - 180;
                    break;
                default:
                    x = 400; y = alturaCampo / 2;
            }

            JugadorCampoPanel panelJugador = new JugadorCampoPanel(j, true);
            panelJugador.setBounds(x - 40, y - 30, 80, 60);
            panelCampo.add(panelJugador);
            titularesEnCampo.add(panelJugador);
            posIndex++;
        }

        int suplenteIndex = 0;
        for (JugadorDTO j : plantilla) {
            boolean esTitular = titulares.stream().anyMatch(t -> t.getIdJugador().equals(j.getIdJugador()));
            if (!esTitular) {
                JugadorCampoPanel panelJugador = new JugadorCampoPanel(j, false);
                panelJugador.setMaximumSize(new Dimension(180, 60));
                panelSuplentes.add(panelJugador);
                panelSuplentes.add(Box.createRigidArea(new Dimension(0, 5)));
                suplentesPanel.add(panelJugador);
                suplenteIndex++;
            }
        }

        lblInfoTitulares.setText(
                "Titulares: " + builder.getCantidadTitularesActual() + " / 11 | " +
                        "Suplentes: " + suplenteIndex + " | " +
                        "Formación: " + builder.getFormacion().getEtiqueta() + " | " +
                        "Estrategia: " + builder.getEstrategia().getNombre()
        );

        panelCampo.revalidate();
        panelCampo.repaint();
        panelSuplentes.revalidate();
        panelSuplentes.repaint();
    }

    private void confirmarYSimular() {
        try {
            PlanDeJuegoDTO plan = builder.build();
            JOptionPane.showMessageDialog(this,
                    "Alineación verificada con éxito (" + plan.getCantidadTitulares() + " titulares).",
                    "Validación Táctica", JOptionPane.INFORMATION_MESSAGE);

            btnSimular.setEnabled(false);
            btnSimular.setText("Enviando...");

            SwingWorker<SalaEstadoDTO, Exception> worker = new SwingWorker<>() {
                @Override
                protected SalaEstadoDTO doInBackground() throws Exception {
                    conexion.enviarPlanJuego(sesion.getNombreUsuario(), plan);
                    return conexion.solicitarSimulacion(sesion.getNombreUsuario());
                }

                @Override
                protected void done() {
                    btnSimular.setEnabled(true);
                    btnSimular.setText("Simular Partido");
                    try {
                        SalaEstadoDTO sala = get();
                        if (sala.getEstadoSala() == EstadoSalaEnum.CUENTA_REGRESIVA) {
                            mostrarCuentaRegresiva(sala);
                        } else {
                            JOptionPane.showMessageDialog(PanelPizarra.this,
                                    "Servidor: " + sala.getMensaje(),
                                    "Simulación", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelPizarra.this,
                                "Error al simular: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de validación táctica: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarCuentaRegresiva(SalaEstadoDTO salaInicial) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            owner = new Frame();
        }
        JDialog dialogo = new JDialog(owner,
                "Cuenta Regresiva - 60 segundos",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(400, 200);
        dialogo.setLocationRelativeTo(this);

        JLabel lblTemporizador = new JLabel("60s", SwingConstants.CENTER);
        lblTemporizador.setFont(new Font("SansSerif", Font.BOLD, 48));
        lblTemporizador.setForeground(new Color(204, 0, 0));

        JLabel lblMensaje = new JLabel("El rival confirmó su estrategia.", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton btnCancelar = new JButton("Aceptar simulación automática");
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(lblTemporizador, BorderLayout.CENTER);
        panel.add(lblMensaje, BorderLayout.SOUTH);
        panel.add(btnCancelar, BorderLayout.PAGE_END);
        dialogo.setContentPane(panel);

        Timer temporizador = new Timer(1000, e -> {
            try {
                SalaEstadoDTO estado = conexion.consultarEstadoSala();
                int segundos = estado.getSegundosRestantes();
                lblTemporizador.setText(segundos + "s");

                if (estado.getEstadoSala() == EstadoSalaEnum.FINALIZADA ||
                        estado.getEstadoSala() == EstadoSalaEnum.SIMULANDO) {
                    ((Timer) e.getSource()).stop();
                    lblMensaje.setText("Simulación completada: " + estado.getMensaje());
                    btnCancelar.setText("Cerrar");
                } else if (segundos <= 0) {
                    ((Timer) e.getSource()).stop();
                    lblMensaje.setText("Tiempo agotado. Simulando con alineación actual...");
                }
            } catch (Exception ex) {
                ((Timer) e.getSource()).stop();
                dialogo.dispose();
            }
        });

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                temporizador.stop();
            }
        });

        temporizador.start();
        dialogo.setVisible(true);
    }
}