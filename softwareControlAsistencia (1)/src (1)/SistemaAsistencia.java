import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SistemaAsistencia {
    private static Connection conexion;
    private static String usuarioActual;
    private static String rolActual;

    public static void main(String[] args) {
        conectarBD();
        mostrarLogin();
    }

    private static void conectarBD() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/sistema_asistencia",
                    "root",
                    "");
            System.out.println("Conexión exitosa a la BD");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la BD: " + e.getMessage());
            System.exit(0);
        }
    }

    private static void mostrarLogin() {
        JFrame frame = new JFrame("Login - Sistema de Asistencia");
        frame.setSize(350, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblUsuario = new JLabel("Usuario:");
        JTextField txtUsuario = new JTextField();
        JLabel lblPassword = new JLabel("Contraseña:");
        JPasswordField txtPassword = new JPasswordField();
        JButton btnLogin = new JButton("Iniciar Sesión");
        JButton btnSalir = new JButton("Salir");

        panel.add(lblUsuario);
        panel.add(txtUsuario);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(new JLabel());
        panel.add(btnLogin);
        panel.add(new JLabel());
        panel.add(btnSalir);

        btnLogin.addActionListener(e -> {
            String usuario = txtUsuario.getText();
            String password = new String(txtPassword.getPassword());

            try {
                String sql = "SELECT * FROM usuarios WHERE username = ? AND password = SHA2(?, 256)";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, usuario);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    usuarioActual = usuario;
                    rolActual = rs.getString("rol");
                    frame.dispose();
                    mostrarMenuPrincipal();
                } else {
                    JOptionPane.showMessageDialog(frame, "Usuario o contraseña incorrectos");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al verificar credenciales: " + ex.getMessage());
            }
        });

        btnSalir.addActionListener(e -> System.exit(0));

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void mostrarMenuPrincipal() {
        JFrame frame = new JFrame("Sistema de Asistencia - " + usuarioActual + " (" + rolActual + ")");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new GridLayout(0, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnRegistrarAsistencia = new JButton("Registrar Asistencia");
        JButton btnConsultarAsistencia = new JButton("Consultar Asistencia");
        JButton btnReportes = new JButton("Generar Reportes");
        JButton btnJustificarInasistencia = new JButton("Justificar Inasistencia");
        JButton btnAdminUsuarios = new JButton("Administrar Usuarios");
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");

        panelBotones.add(btnRegistrarAsistencia);
        panelBotones.add(btnConsultarAsistencia);
        panelBotones.add(btnReportes);
        panelBotones.add(btnJustificarInasistencia);

        if (rolActual.equals("admin")) {
            panelBotones.add(btnAdminUsuarios);
        } else {
            panelBotones.add(new JLabel());
        }

        panelBotones.add(btnCerrarSesion);

        btnRegistrarAsistencia.addActionListener(e -> registrarAsistencia());
        btnConsultarAsistencia.addActionListener(e -> consultarAsistencia());
        btnReportes.addActionListener(e -> generarReportes());
        btnJustificarInasistencia.addActionListener(e -> justificarInasistencia());
        btnAdminUsuarios.addActionListener(e -> administrarUsuarios());
        btnCerrarSesion.addActionListener(e -> {
            frame.dispose();
            mostrarLogin();
        });

        panel.add(panelBotones, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }

    private static void registrarAsistencia() {
        JFrame frame = new JFrame("Registrar Asistencia");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblMetodo = new JLabel("Método de registro:");
        ButtonGroup grupoMetodos = new ButtonGroup();
        JRadioButton rbCodigo = new JRadioButton("Código único");
        JRadioButton rbHuella = new JRadioButton("Huella digital");
        JRadioButton rbFacial = new JRadioButton("Reconocimiento facial");
        grupoMetodos.add(rbCodigo);
        grupoMetodos.add(rbHuella);
        grupoMetodos.add(rbFacial);

        JLabel lblTipo = new JLabel("Tipo de registro:");
        ButtonGroup grupoTipos = new ButtonGroup();
        JRadioButton rbEntrada = new JRadioButton("Entrada");
        JRadioButton rbSalida = new JRadioButton("Salida");
        grupoTipos.add(rbEntrada);
        grupoTipos.add(rbSalida);

        JTextField txtCodigo = new JTextField();
        JButton btnRegistrar = new JButton("Registrar");

        panel.add(lblMetodo);
        JPanel panelMetodos = new JPanel(new GridLayout(1, 3));
        panelMetodos.add(rbCodigo);
        panelMetodos.add(rbHuella);
        panelMetodos.add(rbFacial);
        panel.add(panelMetodos);

        panel.add(lblTipo);
        JPanel panelTipos = new JPanel(new GridLayout(1, 2));
        panelTipos.add(rbEntrada);
        panelTipos.add(rbSalida);
        panel.add(panelTipos);

        panel.add(txtCodigo);
        panel.add(btnRegistrar);

        btnRegistrar.addActionListener(e -> {
            try {
                String metodo = "";
                if (rbCodigo.isSelected()) metodo = "codigo";
                else if (rbHuella.isSelected()) metodo = "huella";
                else if (rbFacial.isSelected()) metodo = "facial";
                else {
                    JOptionPane.showMessageDialog(frame, "Seleccione un método de registro");
                    return;
                }

                String tipo = rbEntrada.isSelected() ? "entrada" :
                        rbSalida.isSelected() ? "salida" : "";
                if (tipo.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Seleccione un tipo de registro");
                    return;
                }

                // Proceso especial para reconocimiento facial
                if (metodo.equals("facial")) {
                    ReconocimientoFacial rf = new ReconocimientoFacial();
                    String usuarioReconocido = rf.reconocerRostro();

                    if (usuarioReconocido == null) {
                        JOptionPane.showMessageDialog(frame, "No se pudo reconocer al usuario");
                        return;
                    }

                    // Verificar que el usuario reconocido coincida con el usuario actual
                    if (!usuarioReconocido.equals(usuarioActual)) {
                        JOptionPane.showMessageDialog(frame, "El usuario reconocido no coincide con la sesión actual");
                        return;
                    }
                }

                // Obtener el ID del usuario actual
                String sqlUsuario = "SELECT id FROM usuarios WHERE username = ?";
                PreparedStatement stmtUsuario = conexion.prepareStatement(sqlUsuario);
                stmtUsuario.setString(1, usuarioActual);
                ResultSet rsUsuario = stmtUsuario.executeQuery();

                if (rsUsuario.next()) {
                    int usuarioId = rsUsuario.getInt("id");

                    // Registrar la asistencia
                    String sql = "INSERT INTO asistencias (usuario_id, fecha, hora, tipo, metodo) VALUES (?, CURDATE(), CURTIME(), ?, ?)";
                    PreparedStatement stmt = conexion.prepareStatement(sql);
                    stmt.setInt(1, usuarioId);
                    stmt.setString(2, tipo);
                    stmt.setString(3, metodo);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "Asistencia registrada exitosamente");
                    frame.dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al registrar asistencia: " + ex.getMessage());
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void consultarAsistencia() {
        JFrame frame = new JFrame("Consultar Asistencia");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelFiltros = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblUsuario = new JLabel("Usuario:");
        JComboBox<String> cmbUsuarios = new JComboBox<>();
        JLabel lblFecha = new JLabel("Fecha (YYYY-MM-DD):");
        JTextField txtFecha = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");

        // Llenar combo de usuarios
        try {
            String sql = "SELECT username FROM usuarios";
            if (!rolActual.equals("admin")) {
                sql += " WHERE username = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, usuarioActual);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    cmbUsuarios.addItem(rs.getString("username"));
                }
            } else {
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    cmbUsuarios.addItem(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar usuarios: " + e.getMessage());
        }

        panelFiltros.add(lblUsuario);
        panelFiltros.add(cmbUsuarios);
        panelFiltros.add(lblFecha);
        panelFiltros.add(txtFecha);

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnBuscar);
        panelBotones.add(btnLimpiar);

        JTextArea txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtResultados);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(panelBotones, BorderLayout.CENTER);
        panel.add(scroll, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> {
            try {
                String usuario = cmbUsuarios.getSelectedItem().toString();
                String fecha = txtFecha.getText();

                String sql = "SELECT a.fecha, a.hora, a.tipo, a.metodo, u.nombre " +
                        "FROM asistencias a JOIN usuarios u ON a.usuario_id = u.id " +
                        "WHERE u.username = ? ";

                if (!fecha.isEmpty()) {
                    sql += "AND a.fecha = ? ";
                }

                sql += "ORDER BY a.fecha DESC, a.hora DESC";

                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, usuario);

                if (!fecha.isEmpty()) {
                    stmt.setString(2, fecha);
                }

                ResultSet rs = stmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("Registros de asistencia para ").append(usuario).append(":\n\n");
                sb.append(String.format("%-12s %-10s %-10s %-15s\n", "Fecha", "Hora", "Tipo", "Método"));
                sb.append("------------------------------------------------\n");

                while (rs.next()) {
                    sb.append(String.format("%-12s %-10s %-10s %-15s\n",
                            rs.getString("fecha"),
                            rs.getString("hora"),
                            rs.getString("tipo"),
                            rs.getString("metodo")));
                }

                txtResultados.setText(sb.toString());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al consultar asistencia: " + ex.getMessage());
            }
        });

        btnLimpiar.addActionListener(e -> {
            txtFecha.setText("");
            txtResultados.setText("");
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void generarReportes() {
        if (!rolActual.equals("admin")) {
            JOptionPane.showMessageDialog(null, "Solo los administradores pueden generar reportes");
            return;
        }

        JFrame frame = new JFrame("Generar Reportes");
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblFechaInicio = new JLabel("Fecha inicio (YYYY-MM-DD):");
        JTextField txtFechaInicio = new JTextField();
        JLabel lblFechaFin = new JLabel("Fecha fin (YYYY-MM-DD):");
        JTextField txtFechaFin = new JTextField();
        JButton btnGenerar = new JButton("Generar Reporte");
        JButton btnExportar = new JButton("Exportar a CSV");

        panel.add(lblFechaInicio);
        panel.add(txtFechaInicio);
        panel.add(lblFechaFin);
        panel.add(txtFechaFin);
        panel.add(btnGenerar);
        panel.add(btnExportar);

        JTextArea txtReporte = new JTextArea();
        txtReporte.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtReporte);

        panel.add(scroll);

        btnGenerar.addActionListener(e -> {
            try {
                String fechaInicio = txtFechaInicio.getText();
                String fechaFin = txtFechaFin.getText();

                String sql = "SELECT u.username, u.nombre, COUNT(a.id) as total_asistencias, " +
                        "SUM(CASE WHEN a.tipo = 'entrada' THEN 1 ELSE 0 END) as entradas, " +
                        "SUM(CASE WHEN a.tipo = 'salida' THEN 1 ELSE 0 END) as salidas " +
                        "FROM usuarios u LEFT JOIN asistencias a ON u.id = a.usuario_id " +
                        "AND a.fecha BETWEEN ? AND ? " +
                        "GROUP BY u.id, u.username, u.nombre " +
                        "ORDER BY u.nombre";

                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, fechaInicio);
                stmt.setString(2, fechaFin);
                ResultSet rs = stmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("Reporte de asistencias del ").append(fechaInicio).append(" al ").append(fechaFin).append("\n\n");
                sb.append(String.format("%-15s %-20s %-10s %-10s %-10s\n",
                        "Usuario", "Nombre", "Total", "Entradas", "Salidas"));
                sb.append("------------------------------------------------------------\n");

                while (rs.next()) {
                    sb.append(String.format("%-15s %-20s %-10d %-10d %-10d\n",
                            rs.getString("username"),
                            rs.getString("nombre"),
                            rs.getInt("total_asistencias"),
                            rs.getInt("entradas"),
                            rs.getInt("salidas")));
                }

                txtReporte.setText(sb.toString());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al generar reporte: " + ex.getMessage());
            }
        });

        btnExportar.addActionListener(e -> {
            String reporte = txtReporte.getText();
            if (reporte.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Genere un reporte primero");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar reporte como CSV");
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".csv")) {
                        file = new java.io.File(file.getAbsolutePath() + ".csv");
                    }

                    java.io.PrintWriter writer = new java.io.PrintWriter(file);
                    writer.println("Usuario,Nombre,Total,Entradas,Salidas");

                    // Convertir el texto del reporte a formato CSV
                    String[] lineas = reporte.split("\n");
                    for (String linea : lineas) {
                        if (linea.contains("|") || linea.contains("-")) continue;
                        String[] partes = linea.trim().split("\\s{2,}");
                        if (partes.length >= 5) {
                            writer.println(String.join(",", partes));
                        }
                    }

                    writer.close();
                    JOptionPane.showMessageDialog(frame, "Reporte exportado exitosamente");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error al exportar reporte: " + ex.getMessage());
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void justificarInasistencia() {
        JFrame frame = new JFrame("Justificar Inasistencia");
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblFecha = new JLabel("Fecha de inasistencia (YYYY-MM-DD):");
        JTextField txtFecha = new JTextField();
        JLabel lblMotivo = new JLabel("Motivo:");
        JTextArea txtMotivo = new JTextArea();
        txtMotivo.setLineWrap(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        JButton btnJustificar = new JButton("Justificar");

        panelForm.add(lblFecha);
        panelForm.add(txtFecha);
        panelForm.add(lblMotivo);
        panelForm.add(scrollMotivo);

        panel.add(panelForm, BorderLayout.CENTER);
        panel.add(btnJustificar, BorderLayout.SOUTH);

        btnJustificar.addActionListener(e -> {
            try {
                String fecha = txtFecha.getText();
                String motivo = txtMotivo.getText();

                if (fecha.isEmpty() || motivo.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Complete todos los campos");
                    return;
                }

                // Obtener el ID del usuario actual
                String sqlUsuario = "SELECT id FROM usuarios WHERE username = ?";
                PreparedStatement stmtUsuario = conexion.prepareStatement(sqlUsuario);
                stmtUsuario.setString(1, usuarioActual);
                ResultSet rsUsuario = stmtUsuario.executeQuery();

                if (rsUsuario.next()) {
                    int usuarioId = rsUsuario.getInt("id");

                    // Verificar si ya existe una justificación para esa fecha
                    String sqlVerificar = "SELECT id FROM inasistencias WHERE usuario_id = ? AND fecha = ?";
                    PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
                    stmtVerificar.setInt(1, usuarioId);
                    stmtVerificar.setString(2, fecha);
                    ResultSet rsVerificar = stmtVerificar.executeQuery();

                    if (rsVerificar.next()) {
                        // Actualizar justificación existente
                        String sqlActualizar = "UPDATE inasistencias SET justificada = TRUE, motivo = ? WHERE id = ?";
                        PreparedStatement stmtActualizar = conexion.prepareStatement(sqlActualizar);
                        stmtActualizar.setString(1, motivo);
                        stmtActualizar.setInt(2, rsVerificar.getInt("id"));
                        stmtActualizar.executeUpdate();
                    } else {
                        // Insertar nueva justificación
                        String sqlInsertar = "INSERT INTO inasistencias (usuario_id, fecha, justificada, motivo) VALUES (?, ?, TRUE, ?)";
                        PreparedStatement stmtInsertar = conexion.prepareStatement(sqlInsertar);
                        stmtInsertar.setInt(1, usuarioId);
                        stmtInsertar.setString(2, fecha);
                        stmtInsertar.setString(3, motivo);
                        stmtInsertar.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(frame, "Inasistencia justificada exitosamente");
                    frame.dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al justificar inasistencia: " + ex.getMessage());
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void administrarUsuarios() {
        if (!rolActual.equals("admin")) {
            JOptionPane.showMessageDialog(null, "Solo los administradores pueden administrar usuarios");
            return;
        }

        JFrame frame = new JFrame("Administrar Usuarios");
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnAgregar = new JButton("Agregar Usuario");
        JButton btnEditar = new JButton("Editar Usuario");
        JButton btnEliminar = new JButton("Eliminar Usuario");
        JButton btnActualizar = new JButton("Actualizar Lista");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);

        // Tabla de usuarios
        String[] columnas = {"ID", "Usuario", "Nombre", "Rol", "Código Único", "Fecha Creación"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        // Cargar datos iniciales
        cargarUsuarios(modelo);

        panel.add(panelBotones, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarUsuario(modelo));
        btnEditar.addActionListener(e -> editarUsuario(tabla, modelo));
        btnEliminar.addActionListener(e -> eliminarUsuario(tabla, modelo));
        btnActualizar.addActionListener(e -> cargarUsuarios(modelo));

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void cargarUsuarios(DefaultTableModel modelo) {
        modelo.setRowCount(0); // Limpiar tabla

        try {
            String sql = "SELECT * FROM usuarios";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] fila = {
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("nombre"),
                        rs.getString("rol"),
                        rs.getString("codigo_unico"),
                        rs.getTimestamp("fecha_creacion")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar usuarios: " + e.getMessage());
        }
    }

    private static void agregarUsuario(DefaultTableModel modelo) {
        JFrame frame = new JFrame("Agregar Usuario");
        frame.setSize(450, 400);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Componentes
        JLabel lblUsuario = new JLabel("Usuario:");
        JTextField txtUsuario = new JTextField();
        JLabel lblPassword = new JLabel("Contraseña:");
        JPasswordField txtPassword = new JPasswordField();
        JLabel lblNombre = new JLabel("Nombre completo:");
        JTextField txtNombre = new JTextField();
        JLabel lblRol = new JLabel("Rol:");
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"admin", "usuario"});
        JLabel lblCodigo = new JLabel("Código único:");
        JTextField txtCodigo = new JTextField();
        JLabel lblEmail = new JLabel("Email:");
        JTextField txtEmail = new JTextField();

        // Componentes para registro facial
        JButton btnRegistrarRostro = new JButton("Registrar Rostro");
        JLabel lblRostro = new JLabel("Registro Facial:");

        // Botones de acción
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Agregar componentes al panel
        panel.add(lblUsuario);
        panel.add(txtUsuario);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblRol);
        panel.add(cmbRol);
        panel.add(lblCodigo);
        panel.add(txtCodigo);
        panel.add(lblEmail);
        panel.add(txtEmail);
        panel.add(lblRostro);
        panel.add(btnRegistrarRostro);
        panel.add(btnGuardar);
        panel.add(btnCancelar);

        // Usamos un array para el estado del rostro (solución para la variable en lambda)
        final boolean[] rostroRegistrado = {false};

        // Acción para el botón de registro facial
        btnRegistrarRostro.addActionListener(e -> {
            String usuario = txtUsuario.getText();
            if (usuario.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ingrese primero un nombre de usuario");
                return;
            }

            if (txtNombre.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Complete el nombre completo primero");
                return;
            }

            try {
                ReconocimientoFacial rf = new ReconocimientoFacial();
                rf.registrarRostro(usuario);
                rostroRegistrado[0] = true;
                btnRegistrarRostro.setBackground(Color.GREEN);
                btnRegistrarRostro.setText("Rostro Registrado");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error al registrar rostro: " + ex.getMessage());
            }
        });

        // Acción para el botón Guardar
        btnGuardar.addActionListener(e -> {
            try {
                String usuario = txtUsuario.getText();
                String password = new String(txtPassword.getPassword());
                String nombre = txtNombre.getText();
                String rol = cmbRol.getSelectedItem().toString();
                String codigo = txtCodigo.getText();
                String email = txtEmail.getText();

                // Validaciones
                if (usuario.isEmpty() || password.isEmpty() || nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Complete los campos obligatorios");
                    return;
                }

                // Verificar si el usuario ya existe
                String sqlVerificar = "SELECT id FROM usuarios WHERE username = ?";
                PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
                stmtVerificar.setString(1, usuario);
                ResultSet rsVerificar = stmtVerificar.executeQuery();

                if (rsVerificar.next()) {
                    JOptionPane.showMessageDialog(frame, "El usuario ya existe");
                    return;
                }

                // Insertar nuevo usuario
                String sql = "INSERT INTO usuarios (username, password, nombre, rol, codigo_unico, email) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, ?, ?)";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, usuario);
                stmt.setString(2, password);
                stmt.setString(3, nombre);
                stmt.setString(4, rol);
                stmt.setString(5, codigo.isEmpty() ? null : codigo);
                stmt.setString(6, email.isEmpty() ? null : email);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Usuario agregado exitosamente");

                // Actualizar la tabla de usuarios
                cargarUsuarios(modelo);

                // Cerrar la ventana
                frame.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al agregar usuario: " + ex.getMessage());
            }
        });

        // Acción para el botón Cancelar
        btnCancelar.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void editarUsuario(JTable tabla, DefaultTableModel modelo) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un usuario para editar");
            return;
        }

        int idUsuario = (int) modelo.getValueAt(filaSeleccionada, 0);
        String usuarioActual = (String) modelo.getValueAt(filaSeleccionada, 1);

        JFrame frame = new JFrame("Editar Usuario");
        frame.setSize(400, 350);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblUsuario = new JLabel("Usuario:");
        JTextField txtUsuario = new JTextField(usuarioActual);
        JLabel lblPassword = new JLabel("Contraseña (dejar en blanco para no cambiar):");
        JPasswordField txtPassword = new JPasswordField();
        JLabel lblNombre = new JLabel("Nombre completo:");
        JTextField txtNombre = new JTextField((String) modelo.getValueAt(filaSeleccionada, 2));
        JLabel lblRol = new JLabel("Rol:");
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"admin", "usuario"});
        cmbRol.setSelectedItem(modelo.getValueAt(filaSeleccionada, 3));
        JLabel lblCodigo = new JLabel("Código único:");
        JTextField txtCodigo = new JTextField((String) modelo.getValueAt(filaSeleccionada, 4));
        JLabel lblEmail = new JLabel("Email:");
        JTextField txtEmail = new JTextField();
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Obtener email de la base de datos
        try {
            String sql = "SELECT email FROM usuarios WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtEmail.setText(rs.getString("email"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar datos del usuario: " + e.getMessage());
        }

        panel.add(lblUsuario);
        panel.add(txtUsuario);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblRol);
        panel.add(cmbRol);
        panel.add(lblCodigo);
        panel.add(txtCodigo);
        panel.add(lblEmail);
        panel.add(txtEmail);
        panel.add(btnGuardar);
        panel.add(btnCancelar);

        btnGuardar.addActionListener(e -> {
            try {
                String usuario = txtUsuario.getText();
                String password = new String(txtPassword.getPassword());
                String nombre = txtNombre.getText();
                String rol = cmbRol.getSelectedItem().toString();
                String codigo = txtCodigo.getText();
                String email = txtEmail.getText();

                if (usuario.isEmpty() || nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Complete los campos obligatorios");
                    return;
                }

                String sql;
                if (password.isEmpty()) {
                    sql = "UPDATE usuarios SET username = ?, nombre = ?, rol = ?, codigo_unico = ?, email = ? WHERE id = ?";
                    PreparedStatement stmt = conexion.prepareStatement(sql);
                    stmt.setString(1, usuario);
                    stmt.setString(2, nombre);
                    stmt.setString(3, rol);
                    stmt.setString(4, codigo.isEmpty() ? null : codigo);
                    stmt.setString(5, email.isEmpty() ? null : email);
                    stmt.setInt(6, idUsuario);
                    stmt.executeUpdate();
                } else {
                    sql = "UPDATE usuarios SET username = ?, password = SHA2(?, 256), nombre = ?, rol = ?, codigo_unico = ?, email = ? WHERE id = ?";
                    PreparedStatement stmt = conexion.prepareStatement(sql);
                    stmt.setString(1, usuario);
                    stmt.setString(2, password);
                    stmt.setString(3, nombre);
                    stmt.setString(4, rol);
                    stmt.setString(5, codigo.isEmpty() ? null : codigo);
                    stmt.setString(6, email.isEmpty() ? null : email);
                    stmt.setInt(7, idUsuario);
                    stmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(frame, "Usuario actualizado exitosamente");
                cargarUsuarios(modelo);
                frame.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al actualizar usuario: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void eliminarUsuario(JTable tabla, DefaultTableModel modelo) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un usuario para eliminar");
            return;
        }

        String usuario = (String) modelo.getValueAt(filaSeleccionada, 1);
        if (usuario.equals("admin")) {
            JOptionPane.showMessageDialog(null, "No se puede eliminar al usuario admin");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de eliminar al usuario " + usuario + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM usuarios WHERE username = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, usuario);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Usuario eliminado exitosamente");
                cargarUsuarios(modelo);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al eliminar usuario: " + e.getMessage());
            }
        }
    }
}