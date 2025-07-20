package Esstudiante;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Estudiantes extends  JFrame {
    private JButton CALIFICACIONESButton;
    private JButton CERTIFICADOButton;
    private JPanel EstudiaPanel;

    public Estudiantes() {
        setTitle("Estudiantes");
        setContentPane(EstudiaPanel);
        setSize(300,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JOptionPane.showMessageDialog(null,"Abriendo la venta de estudiantes :)" );
        setVisible(true);
        CALIFICACIONESButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Calificaciones();

            }
        });
        CERTIFICADOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Certificado();

            }
        });
    }
}

