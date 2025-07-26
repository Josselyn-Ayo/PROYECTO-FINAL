package Esstudiante;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.io.FileOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class Certificado extends JFrame {
    private JButton DescargarButton;
    private JButton VolverButton;
    private JPanel CertificadoPanel;
    private int estudianteId;

    public Certificado(int estudianteId) {
        setTitle("Calificaciones del Estudiante");
        setContentPane(CertificadoPanel);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        this.estudianteId = estudianteId;
        DescargarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarCertificado(estudianteId);
            }
        });
        VolverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Estudiantes(estudianteId).setVisible(true);
                dispose();

            }
        });
    }
    private void generarCertificado(int estudianteId){
        Document documento = new Document();

        try (Connection conn = ConexionBD.getConnection()) {

            PreparedStatement psEst = conn.prepareStatement("SELECT nombre, apellido, curso FROM estudiante WHERE id = ?");
            psEst.setInt(1, estudianteId);
            ResultSet rsEst = psEst.executeQuery();

            if (!rsEst.next()) {
                JOptionPane.showMessageDialog(null, "Estudiante no encontrado.");
                return;
            }

            String nombre = rsEst.getString("nombre");
            String apellido = rsEst.getString("apellido");
            String curso = rsEst.getString("curso");
            //Crear PDF
            String archivoNombre = "Certificado_" + nombre + "_" + apellido + ".pdf";
            PdfWriter.getInstance(documento, new FileOutputStream(archivoNombre));
            documento.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("CERTIFICADO DE CALIFICACIONES", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("\n"));

            documento.add(new Paragraph("Estudiante: " + nombre + " " + apellido));
            documento.add(new Paragraph("Curso: " + curso));
            documento.add(new Paragraph("Fecha de emisión: " + java.time.LocalDate.now()));
            documento.add(new Paragraph("\nCalificaciones:\n"));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidths(new float[]{4, 1});
            tabla.addCell("Asignatura");
            tabla.addCell("Promedio");

            PreparedStatement psNotas = conn.prepareStatement(
                    "SELECT a.nombre AS asignatura, c.nota1, c.nota2 " +
                            "FROM matricula m " +
                            "JOIN asignaturas a ON m.asignatura_id = a.id " +
                            "LEFT JOIN calificacion c ON c.estudiante_id = m.estudiante_id AND c.asignatura_id = m.asignatura_id " +
                            "WHERE m.estudiante_id = ?"
            );
            psNotas.setInt(1, estudianteId);
            ResultSet rsNotas = psNotas.executeQuery();

            while (rsNotas.next()) {
                String asignatura = rsNotas.getString("asignatura");
                double nota1 = rsNotas.getDouble("nota1");
                double nota2 = rsNotas.getDouble("nota2");

                boolean notasValidas = !(rsNotas.wasNull());

                String promedio;
                if (!rsNotas.wasNull() && nota1 >= 0 && nota2 >= 0) {
                    double promedioFinal = (nota1 + nota2) / 2;
                    promedio = String.format("%.2f", promedioFinal);
                } else {
                    promedio = "N/A";
                }

                tabla.addCell(asignatura);
                tabla.addCell(promedio);
            }

            documento.add(tabla);
            documento.add(new Paragraph("\nFirma del responsable: ___________________________"));

            documento.close();
            JOptionPane.showMessageDialog(null, "Certificado generado: " + archivoNombre);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al generar certificado: " + e.getMessage());
        }
    }
}