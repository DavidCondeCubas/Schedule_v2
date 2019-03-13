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

    public ModelAndView inicio(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        
        return new ModelAndView("userform");
    }
//Comparadores de datos (en tuplas):
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
//Se define una tupla en la que se están guardando los datos de las schools correspondientes al districtCode elegido:
//Por ejemplo, si se escoge el district code RWI-SPAIN, se añaden Elementary School, Middle School y High School
        ArrayList<Tupla<String, String>> schools = Consultas.getSchools(districtCode);

//Método sort sirve para ordenar alfabeticamente los objetos recogidos en la Tupla(por ejemplo para RWI-SPAIN: primero pone Middle, luego High y luego Elementary):        
        schools.sort(new CompString());

//Aquí se añaden los datos de la tupla en el mv, para enviarlo a menu.jsp:      
        mv.addObject("schools", schools);
    
        return mv;
    }

    @RequestMapping("/menu/create.htm") 
    public ModelAndView create(HttpServletRequest hsr, HttpServletResponse hsr1) {
//Se guarda en data el parametro recogido de templateInfo (que está en menu.jsp)--> es la ID de template:        
        String data = hsr.getParameter("templateInfo");

        String posSelectTemplate = data.split("#")[1];
        data = data.split("#")[0];
        HttpSession session = hsr.getSession();

        posSelectTemplate = posSelectTemplate.split(" ")[0];
//Se obtienen los parámetros del Request para posteriormente redireccionar a schedule/renweb con esos parámetros:      
        String yearid = hsr.getParameter("yearid");
        String roomMode = hsr.getParameter("rooms");
        String groupRoom = hsr.getParameter("groupofrooms");
        String schoolcode = hsr.getParameter("schoolcode");
        String schoolName = hsr.getParameter("schoolName");
//Aquí se establece el nombre de la escuela a la sesión (p. ej: high school):        
        session.setAttribute("schoolName", schoolName);
        
        String shuffle = hsr.getParameter("suffleCheck");
        String actvRoom = hsr.getParameter("roomsCheck");
        String[] datost = data.split("-");
        ModelAndView mv = new ModelAndView("redirect:/schedule/renweb.htm?actvRoom=" + actvRoom + "&schoolcode=" + schoolcode + "&shuffle=" + shuffle +  "&grouproom=" + groupRoom + "&roommode=" + roomMode + "&tempid=" + datost[0] + "&posSelectTemplate=" + posSelectTemplate + "&yearid=" + yearid + "&id=" + datost[0] + "&rows=" + datost[1] + "&cols="
                + datost[2]);
//Se obtienen los datos del año en función de la escuela que se introduzca(en estecaso IS-PAN). Lo obtiene del método getYears de Consultas:        
        ArrayList<Tupla<Integer, String>> ar = Consultas.getYears("IS-PAN");
//Se ordena alfabéticamente:        
        ar.sort(new Comp());
//Se introducen los datos (en model del mv), y se mandan a "years", en menu.jsp:        
        mv.addObject("years", ar);
        return mv;
    }
//Aquí se accede a través de la funcion templates, que se accede a su vez con el Select Year (al inicio de create).
//Se obtienen los datos de Consultas.getTemplates(proporcionando el id correspondiente), y se devuelve un array con los datos:
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
//Se accede a login a través de:
//1º: userform.jsp --> userform=login
//2º: declarando en dispatcher una id para la clase controlador homepage(<bean class="controladores.Homepage" id="homepage">,<prop key="userform.htm">homepage</prop>).
    @RequestMapping
//HttpServletRequest hsr: petición.
//HttpServletResponse: respuesta.
    public ModelAndView login(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        HttpSession session = hsr.getSession();
//Se declara un objeto de la clase User: para utilizar getters y setters y aplicarlos posteriormente:      
        User user;
        int scgrpid;
        boolean result;
        
        ModelAndView mv;


//Mediante getParameter podemos obtener los valores de los campos del formulario de userform.
        String txtusuario = hsr.getParameter("txtusuario");
        String districtCode = hsr.getParameter("selectDistrictCode");
//Se hace el LoginVerification: con esto permite comprobar si los datos del usuario personales y de grupo coinciden con las credenciales:
//Para ello se hace una conexión a la BD y se cierra al comprobar todos los datos.
        LoginVerification login = new LoginVerification(districtCode,hsr);
        DBConnect db = new DBConnect(hsr);
      
//Verificación de los datos introducidos por el usuario, gracias a la clase LoginVerification:
//Primero comprueba si el campo del usuario está vacío. Si es así devuelve un mensaje y refreca la página.
//En caso de que se haya rellenado, pasa a comprobar si el usuario y contraseña son correctos. Si son incorrectos, 
//devuelve de nuevo un mensaje. Si son correctos, pasa a comprobar si la ID de grupo es correcta. Si es incorrecta
//devuelve otro mensaje. Si finalmente la ID de grupo es correcta entra en el Menu:
        if(txtusuario==null){
               mv = new ModelAndView("userform");
               String message="Username text field is empty, try again please.";  
               mv.addObject("message",message);
               return mv;
            }else{
//consultUserDB se declaraba en LoginVerification y servía para:comprobación de username, password y el ID PERSONAL
               user = login.consultUserDB(hsr.getParameter("txtusuario"), hsr.getParameter("txtpassword"));             
               
//Si el username o el password es incorrecto(es decir, no obtiene ninguna Id del usuario):               
               if(user.getId()==0){
                    mv = new ModelAndView("userform");
                    String message = "Username or password incorrect, try again please.";
                    mv.addObject("message", message);
                    return mv;
                }
//Comprobación de si pertenece al grupo a través de la GroupID(EWSchedule es el grupo al que debe pertenecer):
                else{
                    scgrpid = login.getSecurityGroupID("EWSchedule");
                    result = login.fromGroup(scgrpid, user.getId());
// Entra en este if si todos los campos son correctos: 
                
                    if (result == true){
                        setTipo(user);
                        session.setAttribute("user", user);
                        session.setAttribute("schoolName", "");
//Aquí devuelve el método menu con los datos de la navegación y el districtCode(ver menu más arriba):                        
                        return menu(hsr, hsr1, districtCode);
                    }
                    else{
                        mv = new ModelAndView("userform");
                        String message = "GroupID incorrect, try again please.";
                        mv.addObject("message", message);
                        return mv;
                    }
                }
             }
    }

//En este método se definen los tipos de usuarios. Se va a comprobar si el usuario es un profesor, es un padre, o es un profesor y padre a la vez:
//En función de esto, se definirá un tipo de usuario u otro(user.setType(...))    
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
            ex.getMessage();
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
