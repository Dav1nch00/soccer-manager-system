package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.SesionDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Diálogo de autenticación y auto-creación de perfil.
 * Reemplaza la vista de login por consola (VistaLogin) con una interfaz gráfica.
 */
public class VentanaLogin extends JDialog {

    private final MotorConexion motorConexion;
    private SesionDTO sesion;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JLabel lblEstado;
    private JButton btnIngresar;

    public VentanaLogin(MotorConexion motorConexion) {
        this.motorConexion = motorConexion;
        this.sesion = null;

        setTitle("Soccer Manager - Iniciar Sesión");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 320);
        setResizable(false);

        inicializarComponentes();
        disposicionComponentes();
        configurarEventos();
    }

    private void inicializarComponentes() {
        txtUsuario = new JTextField(20);
        txtPassword = new JPasswordField(20);
        lblEstado = new JLabel("Ingrese sus credenciales");
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setForeground(Color.DARK_GRAY);

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setPreferredSize(new Dimension(120, 35));
    }

    private void disposicionComponentes() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior con título
        JPanel panelTitulo = new JPanel();
        JLabel lblTitulo = new JLabel("SIMULADOR TÁCTICO SOCCER MANAGER");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(0, 102, 204));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre de Usuario (DT):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panelFormulario.add(lblEstado, gbc);
        gbc.gridwidth = 1;

        add(panelFormulario, BorderLayout.CENTER);

        // Panel inferior con botón
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.add(btnIngresar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void configurarEventos() {
        btnIngresar.addActionListener(e -> intentarLogin());

        // Permitir presionar Enter para ingresar
        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    intentarLogin();
                }
            }
        });
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblEstado.setText("Error: Usuario y contraseña no pueden estar vacíos.");
            lblEstado.setForeground(Color.RED);
            return;
        }

        btnIngresar.setEnabled(false);
        lblEstado.setText("Conectando con el servidor...");
        lblEstado.setForeground(Color.DARK_GRAY);

        // Ejecutar en hilo separado para no bloquear la UI
        SwingWorker<SesionDTO, Exception> worker = new SwingWorker<>() {
            @Override
            protected SesionDTO doInBackground() throws Exception {
                return motorConexion.loginORegistro(usuario, password, "club-nal");
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                try {
                    sesion = get();
                    if (sesion.isPerfilNuevoCreado()) {
                        lblEstado.setText("¡Nuevo perfil registrado con éxito!");
                        lblEstado.setForeground(new Color(0, 153, 0));
                    } else {
                        lblEstado.setText("¡Bienvenido de nuevo, " + sesion.getNombreUsuario() + "!");
                        lblEstado.setForeground(new Color(0, 153, 0));
                    }
                    // Cerrar diálogo tras breve pausa
                    Timer timer = new Timer(800, e -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                } catch (Exception ex) {
                    lblEstado.setText("Error: " + ex.getMessage());
                    lblEstado.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    public SesionDTO getSesion() {
        return sesion;
    }
}