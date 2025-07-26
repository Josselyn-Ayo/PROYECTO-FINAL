package Esstudiante;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Estudiantes extends  JFrame {
    private JButton CALIFICACIONESButton;
    private JButton CERTIFICADOButton;
    private JPanel EstudiaPanel;
    private int estudianteId;

    public Estudiantes(int estudianteId) {
        this.estudianteId =estudianteId;

        setTitle("Estudiantes");
        setContentPane(EstudiaPanel);
        setSize(300,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JOptionPane.showMessageDialog(null,"Abriendo el menú :)" );
        setVisible(true);
        CALIFICACIONESButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Calificaciones(estudianteId).setVisible(true);
                dispose();

            }
        });
        CERTIFICADOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Certificado(estudianteId).setVisible(true);
                dispose();

            }
        });
    }
}

