package Loogin;

import Esstudiante.Estudiantes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.*;

public class Login extends JFrame {
    private JTextField usuarioField;
    private JPasswordField passwordField1;
    private JComboBox comboBox1;
    private JButton ingresarButton;
    private JPanel PanelLogin;

    public Login() {
        setTitle("Login");
        setContentPane(PanelLogin);
        setSize(300,300);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ingresarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = usuarioField.getText();
                String contrasena = new String(passwordField1.getPassword());
                String rolSeleccionado = comboBox1.getSelectedItem().toString();
                ResultSet rs = null;
                PreparedStatement stmt = null;

                try(Connection conn = Conexion.getConnection()){
                    stmt = conn.prepareStatement(
                    "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ? AND rol = ?");

                    stmt.setString(1,usuario);
                    stmt.setString(2,contrasena);
                    stmt.setString(3, rolSeleccionado);

                    rs = stmt.executeQuery();

                    if (rs.next()){
                        String rol = rs.getString("rol");
                        JOptionPane.showMessageDialog(null,"Bienvenidos  "+ rol);

                        if (rol.equalsIgnoreCase("estudiante")){
                            int estudianteId = rs.getInt("estudiante_id");
                              new Estudiantes(estudianteId).setVisible(true);
                            dispose();
                        }else{
                            JOptionPane.showMessageDialog(null, "Rol no autorizado :( ");
                        }
                    }else {
                        JOptionPane.showMessageDialog(null,"Usuario y contraseña incorrecta :(");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null,"Error en la conexión"+ ex.getMessage());
                } finally {
                    try{
                        if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                    }catch (SQLException ex){
                        ex.printStackTrace();
                    }

                }

            }
        });

    }


}

