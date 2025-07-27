package Loogin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL= "jdbc:mysql://localhost:3306/login";
    private  static final String USER= "root";
    private static final String PASSWORD = "Dios";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
}
