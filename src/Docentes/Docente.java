package Docentes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Docente  extends JFrame{
    private JButton VERESTUDIANTESButton;
    private JButton CALIFICACIONESButton;
    private JPanel DocentePanel;

    public Docente() {
        setTitle("Menú de los docentes");
        setContentPane(DocentePanel);
        setSize(300,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JOptionPane.showMessageDialog(null,"Abriendo el menú :)" );
        setVisible(true);
        VERESTUDIANTESButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


            }
        });
        CALIFICACIONESButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
}
