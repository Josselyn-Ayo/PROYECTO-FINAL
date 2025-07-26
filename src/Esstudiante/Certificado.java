package Esstudiante;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Certificado extends JFrame {
    private JButton DescargarButton;
    private JButton VolverButton;
    private int estudianteId;

    public Certificado(int estudianteId) {
        DescargarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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
}
