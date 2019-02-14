/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controladores.Homepage;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Norhan
 */
//ESTA CLASE SE USA PARA UTILIZAR LA CONEXION A RENWEB
public class DBConnect {
//OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU:
    
    private Connection cn;
//  private Connection cn2;
    public static Statement renweb;
//   public static Statement own;

    public DBConnect(HttpServletRequest hsr) {
        try {
            
//OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU:
            cn = SQLConnection();
     //       cn2 = SQLConnection2(hsr);
            renweb = cn.createStatement();
     //      own = cn2.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//El getBean se quita porque no se utiliza en DBConnect (este método era necesario para comprobación de credenciales en LoginVerification)
    
    /*private Object getBean(String nombrebean, ServletContext servlet) {
        ApplicationContext contexto = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet);
        Object beanobject = contexto.getBean(nombrebean);
        return beanobject;
    }
   */

    public static Connection SQLConnection() throws SQLException {
        /*System.out.println("database.SQLMicrosoft.SQLConnection()");
        String url = "jdbc:sqlserver://is-pan.odbc.renweb.com:1433;databaseName=is_pan";
        String loginName = "IS_PAN_CUST";
        String password = "HotelBravo+943";*/

        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {
            cn = DriverManager.getConnection(LoginVerification.url,LoginVerification.loginName,LoginVerification.password);
        } catch (SQLException ex) {
            System.out.println("No se puede conectar con el Motor");
            System.err.println(ex.getMessage());
        }

        return cn;
    }
    
//OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU:
    
   /*public Connection SQLConnection2(HttpServletRequest hsr) throws SQLException {
        System.out.println("database schedule conexion");
        DriverManagerDataSource dataSource2 = (DriverManagerDataSource) this.getBean("dataSource", hsr.getServletContext());

        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {
            cn = dataSource2.getConnection();
        } catch (SQLException ex) {
            System.out.println("No se puede conectar con el Motor");
            System.err.println(ex.getMessage());
        }

        return cn;
    }*/

}
