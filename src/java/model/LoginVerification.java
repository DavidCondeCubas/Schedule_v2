/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author nmohamed
 */

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.support.WebApplicationContextUtils;
public class LoginVerification {
    static String url;
    static String loginName;
    static String password;

    
    public LoginVerification(String districtCode,HttpServletRequest hsr){
        url = "";
        loginName ="";
        password="";
        fillDataConnection(districtCode,hsr);
    }
    public LoginVerification(String url, String lName,String pass){     
        this.url = url;
        this.loginName = lName;
        this.password = pass;
    }
 //-->A través de getBean se obtiene la información almacenada en el fichero applicationContext.
 //applicationContext es un fichero que guarda los datos de las credenciales necesarias para entrar en la aplicación web.
 //Si al aplicar el LoginVerification, los datos coinciden con los de la applicationContext.xml, dicha aplicación permite acceder al usuario.
     private Object getBean(String nombrebean, ServletContext servlet) {
//AplicationContext es una interfaz que proporciona configuración a aplicaciones, en este caso servlet.         
//WebApplicationContextUtils.getRequiredWebApplicationContext(servlet)-->Encuentre la raíz WebApplicationContext para esta aplicación web.
        ApplicationContext contexto = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet);
//Mediante getBean devuelve una instancia si coincide con el tipo de dato especificado. Esto se guarda en el objeto beanobject y se retorna.        
        Object beanobject = contexto.getBean(nombrebean);
        return beanobject;
    }
     
    private void fillDataConnection(String districtCode, HttpServletRequest hsr){
 //Guarda consultas de la BBDD cuyo districtCode viene dado de antemano:
        String consulta = "select * from schoolsdata where districtcode='" +districtCode+"'";
//AQUÍ SE CAMBIA LA ESTRUCTURA A LA CONEXIÓN DE BBDD PARA PODER CERRARLA HAYA FALLOS O NO
//Se capturan excepciones de tipo SQL para mayor depuración:
        Connection cn = null;
        Statement s = null;
        ResultSet rs = null;
        try {
//Para coger datos de la bean de aplicationContext cuyo Id es"dataSource",
//a través de la URL específica del colegio, con su loginName y password:           
            DriverManagerDataSource dataSource2 = (DriverManagerDataSource) this.getBean("dataSource", hsr.getServletContext());

            cn = dataSource2.getConnection();
            s = cn.createStatement();
            rs = s.executeQuery(consulta);
            while (rs.next()) {
                url = rs.getString("url");
                loginName = rs.getString("loginname");
                password= rs.getString("password");
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        finally{
             try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
               
            }
            try {
                if (s != null) {
                    s.close();
                }
            } catch (SQLException e) {
                
            }
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException e) {
                e.getMessage();
            }
        }
    }
//Los siguientes 3 métodos: SQLConnection, Query y SQLQuery son necesarios para obtener el query y posteriormente 
//En ConsultUserDB se comprueba si el usuario está en la BBDD (a través de la comprobación de username, passoword y el ID PERSONAL)
    public static Connection SQLConnection() throws SQLException {
      
        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {
            cn = DriverManager.getConnection(url, loginName, password);
        } catch (SQLException ex) {
            ex.getMessage();
        }
 
        return cn;
    }

    public static ResultSet Query(Connection conn, String queryString) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
        rs = stmt.executeQuery(queryString);
        //stmt.close();
        //conn.close();
        return rs;
    }

    public static ResultSet SQLQuery(String queryString) throws SQLException {
        return Query(SQLConnection(), queryString);
    }
//Comprobación de username, passoword y el ID PERSONAL:
    public User consultUserDB(String user,String password) throws Exception {
        User u = null;
        String query = "select * from Person where username = '"+user+"' and pswd = HASHBYTES('MD5', CONVERT(nvarchar(4000),'"+password+"'));";

         ResultSet rs = SQLQuery(query);
         if(!rs.next()) 
         {u=new User();
                 u.setId(0);}
         else{
             rs.beforeFirst();
            while(rs.next()){
                u = new User();
                u.setName(rs.getString("username"));
                u.setPassword(password);
                u.setId(rs.getInt("PersonID"));

            }}
        return u;
    }
//El último paso para la verificación del login, después de verificar los datos con el query,
//es comprobar si el usuario está dentro del grupo EWSchedule a través del ID de grupo.
    
//Con este siguiente método se obtiene el ID de grupo del usuario:
    public int getSecurityGroupID(String name) throws SQLException{
        int sgid = 0;
        String query ="select groupid from SecurityGroups where Name like '"+name+"'";
         ResultSet rs = SQLQuery(query);
            while(rs.next()){
                sgid = rs.getInt(1);
            }
        return sgid;
    }
//Con el siguiente método se verifica si el usuario pertenece al grupo o no (en caso afirmativo devuelve aux=true)
//    a través del groupId y el staffId que obtiene previamente de la BBDD.    
    public boolean fromGroup(int groupid, int staffid) throws SQLException{
        boolean aux  = false;
        String query = "select * from SecurityGroupMembership where groupid = "+groupid+" and StaffID = " + staffid;
        ResultSet rs = SQLQuery(query);
            while(rs.next()){
                aux = true;
            }
      
        return aux;
    }
   
    
}
