package com.soccermanager.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PanelResultados extends JPanel {

    private final MotorConexion conexion;
    private final SesionDTO sesion;
    private final ObjectMapper mapper = new ObjectMapper();

    private DefaultTableModel modeloPartidos;
    private DefaultTableModel modeloTabla;
    private JTable tablaPartidos;
    private JTable tablaPosiciones;
    private JButton btnRefrescar;
    private JLabel lblInfo;

    public PanelResultados(MotorConexion conexion, SesionDTO sesion) {
        this.conexion = conexion;
        this.sesion = sesion;
        inicializarComponentes();
        cargarResultados();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Resultados de la Jornada y Tabla de Posiciones");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0, 102, 204));
        panelEncabezado.add(lblTitulo, BorderLayout.WEST);

        btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarResultados());
        panelEncabezado.add(btnRefrescar, BorderLayout.EAST);

        add(panelEncabezado, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel panelPartidos = new JPanel(new BorderLayout());
        panelPartidos.setBorder(BorderFactory.createTitledBorder("Resultados de la Última Jornada"));

        String[] columnasPartidos = {"Local", "Goles", "Visitante", "Posesión", "Tiros"};
        modeloPartidos = new DefaultTableModel(columnasPartidos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPartidos = new JTable(modeloPartidos);
        tablaPartidos.setRowHeight(24);
        tablaPartidos.getTableHeader().setBackground(new Color(0, 102, 204));
        tablaPartidos.getTableHeader().setForeground(Color.WHITE);
        panelPartidos.add(new JScrollPane(tablaPartidos), BorderLayout.CENTER);

        splitPane.setTopComponent(panelPartidos);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Clasificación General"));

        String[] columnasTabla = {"Pos", "Club", "PTS", "PJ", "PG", "PE", "PP", "GF", "GC", "DIF"};
        modeloTabla = new DefaultTableModel(columnasTabla, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPosiciones = new JTable(modeloTabla);
        tablaPosiciones.setRowHeight(24);
        tablaPosiciones.getTableHeader().setBackground(new Color(0, 102, 204));
        tablaPosiciones.getTableHeader().setForeground(Color.WHITE);
        panelTabla.add(new JScrollPane(tablaPosiciones), BorderLayout.CENTER);

        splitPane.setBottomComponent(panelTabla);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);

        lblInfo = new JLabel("Listo");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblInfo, BorderLayout.SOUTH);
    }

    private void cargarResultados() {
        btnRefrescar.setEnabled(false);
        lblInfo.setText("Cargando resultados...");

        SwingWorker<Map<String, Object>, Exception> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return conexion.obtenerTablaYResultados();
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    Map<String, Object> dataTorneo = get();
                    mostrarResultados(dataTorneo);
                    lblInfo.setText("Resultados actualizados correctamente");
                } catch (Exception ex) {
                    lblInfo.setText("Error al cargar resultados: " + ex.getMessage());
                    lblInfo.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(PanelResultados.this,
                            "No se pudieron cargar los resultados: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void mostrarResultados(Map<String, Object> dataTorneo) {
        try {
            List<ResultadoPartidoDTO> partidos = mapper.convertValue(
                    dataTorneo.get("resultadosUltimaFecha"),
                    new TypeReference<List<ResultadoPartidoDTO>>() {}
            );

            modeloPartidos.setRowCount(0);
            if (partidos != null) {
                for (ResultadoPartidoDTO r : partidos) {
                    Object[] fila = {
                            r.getNombreLocal(),
                            r.getGolesLocal() + " - " + r.getGolesVisitante(),
                            r.getNombreVisitante(),
                            r.getPosesionLocal() + "% - " + r.getPosesionVisitante() + "%",
                            r.getTirosLocal() + " - " + r.getTirosVisitante()
                    };
                    modeloPartidos.addRow(fila);
                }
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tabla = (List<Map<String, Object>>) dataTorneo.get("tabla");
            modeloTabla.setRowCount(0);
            if (tabla != null) {
                int pos = 1;
                for (Map<String, Object> fila : tabla) {
                    int gf = ((Number) fila.get("golesAFavor")).intValue();
                    int gc = ((Number) fila.get("golesEnContra")).intValue();
                    int dif = gf - gc;

                    Object[] filaTabla = {
                            pos++,
                            fila.get("nombreClub"),
                            ((Number) fila.get("puntos")).intValue(),
                            ((Number) fila.get("partidosJugados")).intValue(),
                            ((Number) fila.get("partidosGanados")).intValue(),
                            ((Number) fila.get("partidosEmpatados")).intValue(),
                            ((Number) fila.get("partidosPerdidos")).intValue(),
                            gf,
                            gc,
                            dif
                    };
                    modeloTabla.addRow(filaTabla);
                }
            }
        } catch (Exception ex) {
            lblInfo.setText("Error al procesar resultados: " + ex.getMessage());
            lblInfo.setForeground(Color.RED);
        }
    }
}