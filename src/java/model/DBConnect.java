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
   
    private Connection cn;
    public static Statement renweb;


    public DBConnect(HttpServletRequest hsr) {
        
        try {  
            cn = SQLConnection();
            renweb = cn.createStatement();
            
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static Connection SQLConnection() throws SQLException {


        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {
            cn = DriverManager.getConnection(LoginVerification.url,LoginVerification.loginName,LoginVerification.password);
        } catch (SQLException ex) {

                ex.getMessage();
        }

        return cn;
    }

}
