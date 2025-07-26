package Esstudiante;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


public class Calificaciones extends JFrame {
    private JPanel calificacionPanel;
    private JComboBox comboAsignatura;
    private JButton verCalificacionesButton;
    private JTable table1;
    private JButton correoButton;
    private JButton volverAlMenúButton;
    private int estudianteId;

    public Calificaciones(int estudianteId) {
        this.estudianteId = estudianteId;

        setTitle("Calificaciones del Estudiante");
        setContentPane(calificacionPanel);
        setSize(600,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        cargarAsignaturasMatriculadas();

        verCalificacionesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarCalificaciones();

            }
        });
        correoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String asignaturaSeleccionada = (String) comboAsignatura.getSelectedItem();
                if (asignaturaSeleccionada == null){
                    JOptionPane.showMessageDialog(null,"Selecciona una asignatura");
                    return;
                }
                try(Connection conn = ConexionBD.getConnection()){
                    String sql = "SELECT e.correo, e.nombre, e.apellido, c.nota1, c.nota2 " +
                            "FROM calificacion c " +
                            "JOIN estudiante e ON c.estudiante_id = e.id " +
                            "JOIN asignaturas a ON c.asignatura_id = a.id " +
                            "WHERE e.id = ? AND a.nombre = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, estudianteId);
                    stmt.setString(2,  asignaturaSeleccionada);
                    ResultSet rs =stmt.executeQuery();

                    if (rs.next()){
                        String correo = rs.getString("correo");
                        String nombre= rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        double nota1 = rs.getDouble("nota1");
                        double nota2 = rs.getDouble("nota2");

                        String asunto = "Calificaciones de " + asignaturaSeleccionada;
                        String cuerpo = "Hola "+ nombre + " " + apellido + ", \n\n" +
                                "Estas son tus calificaciones: \n"+
                                "-Asignatura: " + asignaturaSeleccionada + "\n" +
                                "- Nota 1: " + nota1 + "\n"+
                                "- Nota 2: " + nota2 +"\n\n" +
                                "Saludos, \nTu institución";

                        enviarCorreo(correo, asunto, cuerpo);
                    }else {
                        JOptionPane.showMessageDialog(null,"No se encontro información");
                    }

                } catch (SQLException ex){
                    JOptionPane.showMessageDialog(null,"Error al enviar correo " + ex.getMessage());
                }
            }
        });
        volverAlMenúButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Estudiantes(estudianteId).setVisible(true);
                dispose();
            }
        });
    }
    private void cargarAsignaturasMatriculadas(){
        if (comboAsignatura.getItemCount() > 0) {
            return;
        }
        try (Connection conn = ConexionBD.getConnection()) {
             String sql = "SELECT a.id, a.nombre FROM asignaturas a " +
                    "JOIN matricula m ON a.id = m.asignatura_id " +
                    "WHERE m.estudiante_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();

            comboAsignatura.removeAllItems();
            while (rs.next()){
                comboAsignatura.addItem(rs.getString("nombre"));
            }
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,"Error al cargar materias: "+ ex.getMessage());
        }
    }
    private void cargarCalificaciones(){

        String asignaturaSeleccionada = (String) comboAsignatura.getSelectedItem();
        if (asignaturaSeleccionada ==null){
            JOptionPane.showMessageDialog(null, "Selecciona una asignatura primero.");
            return;
        }
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Nombre");
        modelo.addColumn("Apellido");
        modelo.addColumn("Curso");
        modelo.addColumn("Nota1");
        modelo.addColumn("Nota2");

        try(Connection conn = ConexionBD.getConnection()){
            String sql = "SELECT e.nombre, e.apellido, e.curso, c.nota1, c.nota2 " +
                    "FROM calificacion c " +
                    "JOIN estudiante e ON c.estudiante_id = e.id " +
                    "JOIN asignaturas a ON c.asignatura_id = a.id " +
                    "WHERE e.id = ? AND a.nombre = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, estudianteId);
            stmt.setString(2, asignaturaSeleccionada);

            ResultSet rs= stmt.executeQuery();

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("curso"),
                        rs.getDouble("nota1"),
                        rs.getDouble("nota2")
                });
            }
            table1.setModel(modelo);

            }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,"Error al cargar calificaciones" + ex.getMessage());
        }

        }
        private void  enviarCorreo(String destinatario, String asunto, String cuerpo) {
            final String remitente = "anabelayo2017@gmail.com";
            final String clave = "pyfz hxru towk wssk";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(remitente, clave);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(remitente));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                message.setSubject(asunto);
                message.setText(cuerpo);

                Transport.send(message);
                JOptionPane.showMessageDialog(null,"Las notas fueron enviados al correo correctamente :)");

            }catch (MessagingException e){
                JOptionPane.showMessageDialog(null,"Error enviando correo: "+ e.getMessage());
            }
        }
}



