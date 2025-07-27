package Esstudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
public class Matriculas extends JFrame {
    private JPanel MatriculaPanel;
    private JComboBox<String> comboAsignatura;
    private JButton agregarButton;
    private JButton cancelarButton;
    private JTable table1;
    private JButton volverAlMenúButton;
    private DefaultTableModel tableModel;
    private int estudianteId;

    public Matriculas(int estudianteId) {
        this.estudianteId = estudianteId;
        setContentPane(MatriculaPanel);
        setTitle("Gestionar matrícula");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID Matrícula", "Asignatura", "Paralelo", "Día", "Hora Inicio", "Hora Fin", "Aula"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setModel(tableModel);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cargarAsignaturas();
        comboAsignatura.addActionListener(e -> cargarMatriculasPorAsignatura());
        agregarButton.addActionListener(e -> agregarMatricula());
        cancelarButton.addActionListener(e -> eliminarMatricula());
        volverAlMenúButton.addActionListener(e -> {
            new Estudiantes(estudianteId).setVisible(true);
            dispose();
        });
        if (comboAsignatura.getItemCount() > 0) {
            comboAsignatura.setSelectedIndex(0);
            cargarMatriculasPorAsignatura();
        }
    }
    private Connection getConnection() throws SQLException {
        return ConexionBD.getConnection();
    }
    private void cargarAsignaturas() {
        String sql = "SELECT DISTINCT id, nombre FROM asignaturas";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            comboAsignatura.removeAllItems();
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                comboAsignatura.addItem(id + " - " + nombre);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar asignaturas: " + ex.getMessage());
        }
    }
    private void cargarMatriculasPorAsignatura() {
        String itemSeleccionado = (String) comboAsignatura.getSelectedItem();
        if (itemSeleccionado == null || itemSeleccionado.isEmpty()) {
            tableModel.setRowCount(0);
            return;
        }
        String[] partes = itemSeleccionado.split(" - ");
        int idAsignatura = Integer.parseInt(partes[0]);

        String sql = "SELECT m.id as matricula_id, a.nombre, p.nombre AS paralelo, h.dia, h.hora_inicio, h.hora_fin, au.nombre AS aula " +
                "FROM matricula m " +
                "JOIN asignaturas a ON m.asignatura_id = a.id " +
                "JOIN paralelo p ON m.paralelo_id = p.id " +
                "JOIN horario h ON h.asignatura_id = m.asignatura_id AND h.paralelo_id = m.paralelo_id " +
                "JOIN aula au ON h.aula_id = au.id " +
                "WHERE m.estudiante_id = ? AND m.asignatura_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, estudianteId);
            ps.setInt(2, idAsignatura);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                int matriculaId = rs.getInt("matricula_id");
                String nombre = rs.getString("nombre");
                String paralelo = rs.getString("paralelo");
                String dia = rs.getString("dia");
                String horaInicio = rs.getString("hora_inicio");
                String horaFin = rs.getString("hora_fin");
                String aula = rs.getString("aula");
                tableModel.addRow(new Object[]{matriculaId, nombre, paralelo, dia, horaInicio, horaFin, aula});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar matrículas: " + ex.getMessage());
        }
    }
    private void agregarMatricula() {
        String itemSeleccionado = (String) comboAsignatura.getSelectedItem();
        if (itemSeleccionado == null || itemSeleccionado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una asignatura para matricular.");
            return;
        }
        String[] partes = itemSeleccionado.split(" - ");
        int asignaturaId = Integer.parseInt(partes[0]);
        try (Connection conn = getConnection()) {
            String paraleloQuery = "SELECT DISTINCT paralelo_id FROM horario WHERE asignatura_id = ?";
            PreparedStatement psParalelo = conn.prepareStatement(paraleloQuery);
            psParalelo.setInt(1, asignaturaId);
            ResultSet rsParalelos = psParalelo.executeQuery();
            DefaultComboBoxModel<String> paraleloModel = new DefaultComboBoxModel<>();
            while (rsParalelos.next()) {
                int paraleloId = rsParalelos.getInt("paralelo_id");
                String nombreParalelo = obtenerNombreParalelo(paraleloId);
                paraleloModel.addElement(paraleloId + " - " + nombreParalelo);
            }

            if (paraleloModel.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "No hay horarios para esta asignatura.");
                return;
            }
            String seleccionadoParalelo;
            if (paraleloModel.getSize() == 1) {
                seleccionadoParalelo = paraleloModel.getElementAt(0);
            } else {
                int size = paraleloModel.getSize();
                String[] opcionesParalelo = new String[size];
                for (int i = 0; i < size; i++) {
                    opcionesParalelo[i] = paraleloModel.getElementAt(i);
                }
                seleccionadoParalelo = (String) JOptionPane.showInputDialog(this,
                        "Selecciona el paralelo:",
                        "Paralelo",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcionesParalelo,
                        opcionesParalelo[0]);
                if (seleccionadoParalelo == null) return;
            }
            int paraleloIdSeleccionado = Integer.parseInt(seleccionadoParalelo.split(" - ")[0]);

            String checkSql = "SELECT COUNT(*) FROM matricula WHERE estudiante_id = ? AND asignatura_id = ? AND paralelo_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, estudianteId);
            checkStmt.setInt(2, asignaturaId);
            checkStmt.setInt(3, paraleloIdSeleccionado);
            ResultSet rsCheck = checkStmt.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Ya estás matriculado en esta asignatura y paralelo.");
                return;
            }
            String insertSql = "INSERT INTO matricula (estudiante_id, asignatura_id, paralelo_id) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, estudianteId);
            insertStmt.setInt(2, asignaturaId);
            insertStmt.setInt(3, paraleloIdSeleccionado);
            insertStmt.executeUpdate();
            String nombreAsignatura = obtenerNombreAsignatura(asignaturaId);
            String nombreParalelo = obtenerNombreParalelo(paraleloIdSeleccionado);
            String horarioSql = "SELECT h.dia, h.hora_inicio, h.hora_fin, a.nombre as aula " +
                    "FROM horario h JOIN aula a ON h.aula_id = a.id " +
                    "WHERE h.asignatura_id = ? AND h.paralelo_id = ?";
            PreparedStatement psHorario = conn.prepareStatement(horarioSql);
            psHorario.setInt(1, asignaturaId);
            psHorario.setInt(2, paraleloIdSeleccionado);
            ResultSet rsHorario = psHorario.executeQuery();
            StringBuilder detallesHorario = new StringBuilder();
            while (rsHorario.next()) {
                String dia = rsHorario.getString("dia");
                String horaInicio = rsHorario.getString("hora_inicio");
                String horaFin = rsHorario.getString("hora_fin");
                String aula = rsHorario.getString("aula");
                detallesHorario.append(String.format("Día: %s, Hora: %s - %s, Aula: %s%n", dia, horaInicio, horaFin, aula));
            }
            String mensajeNotificacion = String.format(
                    "¡Te has matriculado exitosamente en %s (Paralelo %s)!\nHorarios:\n%s",
                    nombreAsignatura, nombreParalelo, detallesHorario.toString());
            guardarNotificacion(estudianteId, mensajeNotificacion);
            mostrarMensaje(mensajeNotificacion);
            enviarCorreoMatricula(estudianteId, mensajeNotificacion);

            cargarMatriculasPorAsignatura();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar matrícula: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private String obtenerNombreParalelo(int paraleloId) {
        String nombre = "";
        String sql = "SELECT nombre FROM paralelo WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paraleloId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("nombre");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nombre;
    }
    private String obtenerNombreAsignatura(int asignaturaId) {
        String nombre = "";
        String sql = "SELECT nombre FROM asignaturas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, asignaturaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("nombre");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nombre;
    }
    private void guardarNotificacion(int estudianteId, String mensaje) {
        String sql = "INSERT INTO notificaciones (estudiante_id, mensaje) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, estudianteId);
            ps.setString(2, mensaje);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Notificación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void enviarCorreoMatricula(int estudianteId, String mensaje) {
        String correo = obtenerCorreoEstudiante(estudianteId);
        if (correo == null || correo.isEmpty()) {
            System.out.println("Correo no encontrado para estudiante ID: " + estudianteId);
            return;
        }
        final String remitente = "anabelayo2017@gmail.com";
        final String clave = "pyfz hxru towk wssk";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(remitente, clave);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correo));
            message.setSubject("Notificación de Matrícula");
            message.setText(mensaje);
            Transport.send(message);
            System.out.println("Correo enviado a " + correo);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private String obtenerCorreoEstudiante(int estudianteId) {
        String correo = "";
        String sql = "SELECT correo FROM estudiante WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, estudianteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                correo = rs.getString("correo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return correo;
    }

    private void eliminarMatricula() {
        int filaSeleccionada = table1.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una matrícula para cancelar.");
            return;
        }
        int idMatricula = (int) tableModel.getValueAt(filaSeleccionada, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres cancelar esta matrícula?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM matricula WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMatricula);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Matrícula cancelada correctamente.");
            cargarMatriculasPorAsignatura();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cancelar matrícula: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}


