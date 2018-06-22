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
        url = url;
        loginName = lName;
        password = pass;
    }
    
     private Object getBean(String nombrebean, ServletContext servlet) {
        ApplicationContext contexto = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet);
        Object beanobject = contexto.getBean(nombrebean);
        return beanobject;
    }
     
    private void fillDataConnection(String districtCode, HttpServletRequest hsr){
        String consulta = "select * from schoolsdata where districtcode='" +districtCode+"'";
        try {
           
            DriverManagerDataSource dataSource2 = (DriverManagerDataSource) this.getBean("dataSource", hsr.getServletContext());
            Connection cn = null;
            cn = dataSource2.getConnection();
            Statement s = cn.createStatement();
            ResultSet rs = s.executeQuery(consulta);
            while (rs.next()) {
                url = rs.getString("url");
                loginName = rs.getString("loginname");
                password= rs.getString("password");
            }
        } catch (Exception e) {
            System.err.println("");
        }
    }
    
    public static Connection SQLConnection() throws SQLException {
        System.out.println("database.SQLMicrosoft.SQLConnection()");
      /*  String url = "jdbc:sqlserver://ca-pan.odbc.renweb.com\\ca_pan:1433;databaseName=ca_pan";
        String loginName = "CA_PAN_CUST";
        String password = "RansomSigma+339";*/
      
        DriverManager.registerDriver(new SQLServerDriver());
        Connection cn = null;
        try {
            cn = DriverManager.getConnection(url, loginName, password);
        } catch (SQLException ex) {
            System.out.println("No se puede conectar con el Motor");
            System.err.println(ex.getMessage());
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

    public User consultUserDB(String user,String password) throws Exception {
        User u = null;
       //user = 'shahad' and pswd = 'shahad1234' group = Spring
        String query = "select * from Person where username = '"+user+"' and pswd = HASHBYTES('MD5', CONVERT(nvarchar(4000),'"+password+"'));";
      //      String query = "select * from Person where username = '"+user+"';";

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
    public int getSecurityGroupID(String name) throws SQLException{
        int sgid = 0;
        String query ="select groupid from SecurityGroups where Name like '"+name+"'";
         ResultSet rs = SQLQuery(query);
            while(rs.next()){
                sgid = rs.getInt(1);
            }
        return sgid;
    }
    
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
