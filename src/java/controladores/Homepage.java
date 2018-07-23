/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

/**
 *
 * @author nmohamed
 */
import atg.taglib.json.util.JSONException;
import atg.taglib.json.util.JSONObject;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.LoginVerification;
import model.User;
import javax.servlet.http.HttpSession;
import dataManage.Consultas;
import model.DBConnect;
import model.Student;
import model.Teacher;
import model.Template;
import dataManage.Tupla;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import dataManage.XMLReaderDOM;

@RequestMapping("/")
public class Homepage extends MultiActionController {

    private Object getBean(String nombrebean, ServletContext servlet) {
        ApplicationContext contexto = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet);
        Object beanobject = contexto.getBean(nombrebean);
        return beanobject;
    }

    public ModelAndView inicio(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        
        //    return menu(hsr,hsr1);
        return new ModelAndView("userform");
    }

    private class CompString implements Comparator<Tupla<String, String>> {

        @Override
        public int compare(Tupla<String, String> e1, Tupla<String, String> e2) {
            return e2.y.compareTo(e1.y);
        }
    }
    
    private class Comp implements Comparator<Tupla<Integer, String>> {

        @Override
        public int compare(Tupla<Integer, String> e1, Tupla<Integer, String> e2) {
            return e2.y.compareTo(e1.y);
        }
    }
    

    @RequestMapping("/menu.htm")
    public ModelAndView menu(HttpServletRequest hsr, HttpServletResponse hsr1, String districtCode) {
        ModelAndView mv = new ModelAndView("menu");
        ArrayList<Tupla<String, String>> schools = Consultas.getSchools(districtCode);
    //    ArrayList<Tupla<Integer, String>> ar = Consultas.getYears(districtCode);

       // ar.sort(new Comp());
        schools.sort(new CompString());

       // mv.addObject("years", ar);
      
        mv.addObject("schools", schools);
        return mv;
    }

    @RequestMapping("/menu/create.htm") // DATA ES ID DE TEMPLATE
    public ModelAndView create(HttpServletRequest hsr, HttpServletResponse hsr1) {
        String data = hsr.getParameter("templateInfo");

        String posSelectTemplate = data.split("#")[1];
        data = data.split("#")[0];
        HttpSession session = hsr.getSession();

        posSelectTemplate = posSelectTemplate.split(" ")[0];
        String yearid = hsr.getParameter("yearid");
        String roomMode = hsr.getParameter("rooms");
        String groupRoom = hsr.getParameter("groupofrooms");
        String schoolcode = hsr.getParameter("schoolcode");
        String schoolName = hsr.getParameter("schoolName");
        session.setAttribute("schoolName", schoolName);
        
        String shuffle = hsr.getParameter("suffleCheck");
        String actvRoom = hsr.getParameter("roomsCheck");
        String[] datost = data.split("-");
        ModelAndView mv = new ModelAndView("redirect:/schedule/renweb.htm?actvRoom=" + actvRoom + "&schoolcode=" + schoolcode + "&shuffle=" + shuffle +  "&grouproom=" + groupRoom + "&roommode=" + roomMode + "&tempid=" + datost[0] + "&posSelectTemplate=" + posSelectTemplate + "&yearid=" + yearid + "&id=" + datost[0] + "&rows=" + datost[1] + "&cols="
                + datost[2]);
        ArrayList<Tupla<Integer, String>> ar = Consultas.getYears("IS-PAN");
        ar.sort(new Comp());
        mv.addObject("years", ar);
        return mv;
    }

    @RequestMapping("/menu/temp.htm")
    @ResponseBody
    public String getTemplates(HttpServletRequest hsr, HttpServletResponse hsr1) throws JSONException {
        String id = hsr.getParameter("id");
        ArrayList<Template> tmps = Consultas.getTemplates(id);
        return (new Gson()).toJson(tmps);
    }

    @RequestMapping("/menu/years.htm")
    @ResponseBody
    public String getYears(HttpServletRequest hsr, HttpServletResponse hsr1) throws JSONException {
        String id = hsr.getParameter("id");
        ArrayList<Tupla<Integer,String>> tmps = Consultas.getYears(id);
        tmps.sort(new Comp());
        return (new Gson()).toJson(tmps);
    }
    
    public static ModelAndView checklogin(HttpServletRequest hsr) {
        if (hsr.getSession().getAttribute("user") == null) {
            return new ModelAndView("redirect:/");
        }
        return null;
    }

    @RequestMapping
    public ModelAndView login(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        HttpSession session = hsr.getSession();
      
        User user = new User();//cambiar
        int scgrpid = 0;
        boolean result = false;
        
        ModelAndView mv = new ModelAndView();

//            setTipo(user);//borrar
//            session.setAttribute("user", user); //borrar
        String txtusuario = hsr.getParameter("txtusuario");
        String districtCode = hsr.getParameter("selectDistrictCode");
        
        LoginVerification login = new LoginVerification(districtCode,hsr);
        DBConnect db = new DBConnect(hsr);
        // menu(hsr,hsr1,schoolCode);
      

        if(txtusuario==null){
               return new ModelAndView("userform");
            }else{
            
               user = login.consultUserDB(hsr.getParameter("txtusuario"), hsr.getParameter("txtpassword"));
               // if the username or password incorrect
               if(user.getId()==0){
                    mv = new ModelAndView("userform");
                    String message = "Username or password incorrect";
                    mv.addObject("message", message);
                    return mv;
                }
                //if the user is not part of the group
                else{
                    scgrpid = login.getSecurityGroupID("EWSchedule");
                    result = login.fromGroup(scgrpid, user.getId());
                    
                    if (result == true){
//                       user.setId(10393);//padre
//                       user.setId(10332);//profe
                        setTipo(user);
                        session.setAttribute("user", user);
                        session.setAttribute("schoolName", "");
                        return menu(hsr, hsr1, districtCode);
                    }
                    else{
                        mv = new ModelAndView("userform");
                        String message = "Username or Password incorrect";
                        mv.addObject("message", message);
                        return mv;
                    }
                }
             }
        // return mv;
    }

    //user.setId(10333);
    //user.setId(10366);
    public void setTipo(User user) {
        boolean padre = false, profesor = false;
        try {
            String consulta = "SELECT count(*) AS cuenta FROM Staff where Faculty = 1 and StaffID =" + user.getId();
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            if (rs.next()) {
                profesor = rs.getInt("cuenta") > 0;
            }
            consulta = "SELECT count(*) AS cuenta FROM Parent_Student where ParentID =" + user.getId();
            ResultSet rs2 = DBConnect.renweb.executeQuery(consulta);
            if (rs2.next()) {
                padre = rs2.getInt("cuenta") > 0;
            }
        } catch (SQLException ex) {
            System.out.println("error");
        }
        if (padre && profesor) {
            user.setType(0);
        } else if (padre) {
            user.setType(1);
        } else if (profesor) {
            user.setType(2);
        } else {
            user.setType(3);
        }
    }

}
