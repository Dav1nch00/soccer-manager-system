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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelPizarra extends JPanel {

    private final MotorConexion conexion;
    private final SesionDTO sesion;
    private final PlanDeJuegoBuilder builder;

    private JComboBox<FormacionEnum> comboFormacion;
    private JComboBox<EstrategiaEnum> comboEstrategia;
    private JTable tablaTitulares;
    private DefaultTableModel modeloTitulares;
    private JButton btnSimular;
    private JLabel lblInfoTitulares;
    private JLabel lblClubInfo;

    public PanelPizarra(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;
        this.builder = new PlanDeJuegoBuilder();
        inicializarComponentes();
        inicializarAlineacion();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

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

        JPanel panelConfig = new JPanel(new GridBagLayout());
        panelConfig.setBorder(BorderFactory.createTitledBorder("Configuracion Tactica"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("Formacion:"), gbc);
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

        add(panelConfig, BorderLayout.CENTER);

        String[] columnas = {"#", "Dorsal", "Nombre", "Posicion", "Media", "Energia", "Estado"};
        modeloTitulares = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaTitulares = new JTable(modeloTitulares);
        tablaTitulares.setRowHeight(26);
        tablaTitulares.getTableHeader().setBackground(new Color(0, 102, 204));
        tablaTitulares.getTableHeader().setForeground(Color.WHITE);
        tablaTitulares.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaTitulares.getColumnCount(); i++) {
            tablaTitulares.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaTitulares);
        add(scrollPane, BorderLayout.CENTER);

        lblInfoTitulares = new JLabel("Titulares: 0 / 11");
        lblInfoTitulares.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblInfoTitulares, BorderLayout.SOUTH);
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

            actualizarTablaTitulares();
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
        actualizarTablaTitulares();
    }

    private void actualizarEstrategia() {
        EstrategiaEnum estrategia = (EstrategiaEnum) comboEstrategia.getSelectedItem();
        builder.setEstrategia(estrategia);
    }

    private void actualizarTablaTitulares() {
        modeloTitulares.setRowCount(0);
        List<JugadorDTO> titulares = builder.getTitularesActuales();

        for (int i = 0; i < titulares.size(); i++) {
            JugadorDTO j = titulares.get(i);
            Object[] fila = {
                    i + 1,
                    j.getDorsal(),
                    j.getNombre(),
                    j.getPosicion().getAbreviatura(),
                    String.format("%.1f", j.getMediaGeneral()),
                    j.getEnergia() + "%",
                    j.getEstado().getEtiqueta()
            };
            modeloTitulares.addRow(fila);
        }

        lblInfoTitulares.setText(
                "Titulares: " + builder.getCantidadTitularesActual() + " / 11 | " +
                        "Formacion: " + builder.getFormacion().getEtiqueta() + " | " +
                        "Estrategia: " + builder.getEstrategia().getNombre()
        );
    }

    private void confirmarYSimular() {
        try {
            PlanDeJuegoDTO plan = builder.build();
            JOptionPane.showMessageDialog(this,
                    "Alineacion verificada con exito (" + plan.getCantidadTitulares() + " titulares).",
                    "Validacion Tactica", JOptionPane.INFORMATION_MESSAGE);

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
                                    "Simulacion", JOptionPane.INFORMATION_MESSAGE);
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
                    "Error de validacion tactica: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarCuentaRegresiva(SalaEstadoDTO salaInicial) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Cuenta Regresiva - 60 segundos", true);
        dialogo.setSize(400, 200);
        dialogo.setLocationRelativeTo(this);

        JLabel lblTemporizador = new JLabel("60s", SwingConstants.CENTER);
        lblTemporizador.setFont(new Font("SansSerif", Font.BOLD, 48));
        lblTemporizador.setForeground(new Color(204, 0, 0));

        JLabel lblMensaje = new JLabel("El rival confirmo su estrategia.", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton btnCancelar = new JButton("Aceptar simulacion automatica");
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
                    lblMensaje.setText("Simulacion completada: " + estado.getMensaje());
                    btnCancelar.setText("Cerrar");
                } else if (segundos <= 0) {
                    ((Timer) e.getSource()).stop();
                    lblMensaje.setText("Tiempo agotado. Simulando con alineacion actual...");
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