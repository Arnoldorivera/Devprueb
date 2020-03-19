package Conexion;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;




public class Conectar {
    
    
    private Connection CONEXION;
    private String host, puerto, BD, usuario, clave;
    

    public boolean conectar() {// aqui se validan los parametros para la conexion con la base de datos
        try {
            CONEXION = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + puerto + "/" + BD, usuario, clave);
            return  true;
        } catch (SQLException ex) {
            System.err.println("Error de conexion " + ex);
            return  false;
        }

    }

    public ResultSet query(String SQL) {//aqui se ejecutan las consultas
        if (CONEXION != null) {
            System.out.println(SQL);
            try {
                return CONEXION.createStatement().executeQuery(SQL);
            } catch (SQLException ex) {
                System.err.println("Error query " + ex);
                
                return null;
            }
        }
            return null;
    }

    public int update(String SQL) {//aqui se ejecutan los update
        if (CONEXION != null) {
            System.out.println(SQL);
            try {
                return CONEXION.createStatement().executeUpdate(SQL);
            } catch (SQLException ex) {
                System.err.println("Error update " + ex);
                return 0;
            }
        }
            return 0;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public void setBD(String BD) {
        this.BD = BD;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Connection getCONEXION() {
        return CONEXION;
    }

}
