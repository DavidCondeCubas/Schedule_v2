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
//Gracias a este ModelAndView se carga la primera vista (porque se retorna ModelAndView("userform"), que se refiere a la primera vista, ya que:
//1º: En dispatcher:
//<bean class="controladores.Homepage" id="homepage">,<property name="paramName" value="opcion"/>  : 
//Se le da el id homepage al controlador y se le pasa un parametro llamado opcion (al que se le podrán pasar distintos valores (mv) como inicio o login).
//<prop key="userform.htm">homepage</prop> : Se redirecciona userform.htm en los mapeos con el nombre proporcionado por key(userform.htm).
//2º: Lo primero que carga el programa es redirect.jsp:
//En esta vista se indica lo siguiente: response.sendRedirect("userform.htm?opcion=inicio"), donde inicio se refiere a este mv)
//Carga la página por defecto en inglés:
//Para CerrarLogin.java, también se hace referencia a este mv inicio para poder redireccionar a la primera vista al darle al botón log out (gracias a un mapeo en el dispatcher).    
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
//Se accede a este mv en el ModelAndView login, si los datos del usuario se han introducido correctamente.
//Antes de devolver el mv menu.jsp, el cometido del ModelAndView menu es el de cargar los datos de las escuelas en función del districtCode que se haya seleccionado en la primera vista.
    public ModelAndView menu(HttpServletRequest hsr, HttpServletResponse hsr1, String districtCode) {
        ModelAndView mv = new ModelAndView("menu");
//Se define una tupla en la que se están guardando los datos de las schools correspondientes al districtCode elegido:
//Por ejemplo, si se ha escogido el districtCode RWI-SPAIN, se añaden Elementary School, Middle School y High School:
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
//data está compuesto por: id del template-filas del template-columnas del template#seleccion del template en menu.jsp
//Posteriormente se separan estos datos para poder redireccionarlos.
        String data = hsr.getParameter("templateInfo");
        data = data.split("#")[0];
        HttpSession session = hsr.getSession();

//Se obtienen los parámetros del Request para posteriormente redireccionar a schedule/renweb con esos parámetros (se redirecciona a través del mv):      
        String yearid = hsr.getParameter("yearid");
        String schoolcode = hsr.getParameter("schoolcode");
        String schoolName = hsr.getParameter("schoolName");
//Aquí se establece el nombre de la escuela a la sesión (p. ej: high school),
//ya que varía en función de la opción seleccionada:        
        session.setAttribute("schoolName", schoolName);        
        String shuffle = hsr.getParameter("suffleCheck");
        String actvRoom = hsr.getParameter("roomsCheck");
        String[] datost = data.split("-");
        //La redirección a renweb.htm con los diferentes parámetors, llevará al controlador ScheduleController directaamente, sin pasar por vista ni Dispatcher:
        ModelAndView mv = new ModelAndView("redirect:/schedule/renweb.htm?"
                + "actvRoom=" + actvRoom 
                + "&schoolcode=" + schoolcode 
                + "&shuffle=" + shuffle 
                + "&yearid=" + yearid 
                + "&tempid=" + datost[0] 
                + "&rows=" + datost[1] 
                + "&cols="+ datost[2]);
        return mv;
    }
//Aquí se accede a través de la funcion JavaScript templates() de menu.jsp, que se accede a su vez con el Select Year (al inicio del menu).
//Se obtienen los datos de Consultas.getTemplates(proporcionando el id correspondiente), y se devuelve un array con los datos:
//Es un mapeo de petición y respuesta (se devuelve un JSON con los datos de los templates a la vista)
    @RequestMapping("/menu/temp.htm")
    @ResponseBody
    public String getTemplates(HttpServletRequest hsr, HttpServletResponse hsr1) throws JSONException {
        String id = hsr.getParameter("id");
        ArrayList<Template> tmps = Consultas.getTemplates(id);
        return (new Gson()).toJson(tmps);
    }
//Con este mapeo y método, se obtienen los años para poder cargarlos en la vista del menú cuando se selecciona una escuela:  
//Es un mapeo de petición y respuesta (se devuelve un JSON con los datos de los years a la vista)
    @RequestMapping("/menu/years.htm")
    @ResponseBody
    public String getYears(HttpServletRequest hsr, HttpServletResponse hsr1) throws JSONException {
        String id = hsr.getParameter("id");
        ArrayList<Tupla<Integer,String>> tmps = Consultas.getYears(id);
        tmps.sort(new Comp());
        return (new Gson()).toJson(tmps);
    }
//Este método te redireciona a donde estés si no se introduce el usuario.    
    public static ModelAndView checklogin(HttpServletRequest hsr) {
        if (hsr.getSession().getAttribute("user") == null) {
            return new ModelAndView("redirect:/");
        }
        return null;
    }
//Se accede a login a través del siguiente proceso:
//1º: En el dispatcher se le da un id para la clase controlador Homepage(homepage) y
//y se le asgina un parámetro (opcion). Por otra parte al id se le asigna una key=userform.htm
//(<bean class="controladores.Homepage" id="homepage">,<property name="paramName" value="opcion"/>)
//(<prop key="userform.htm">homepage</prop>).
//2º:Lo declarado anteriormente se usa en userform.jsp: userform.htm?opcion=login, donde login se refiere al siguiente ModelAndView login, 
// y se llega a través del mapeo @RequestMapping: 
//El ModelAndView login permite comprobar si el usuario ha introducido bien los datos en la primera vista a través de una serie de comprobaciones.
//Si es así, captura el mv del ModelAndView menu para poder devolver este mv y cargar la página menu.jsp (la razón por la que ModelAndView está separado
//es porque carga aparte los datos en función del districtCode, y en login se comprueban los datos del usuario):    
    @RequestMapping
//HttpServletRequest hsr: petición.
//HttpServletResponse: respuesta.
    public ModelAndView login(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        HttpSession session = hsr.getSession();
//Se declara un objeto de la clase User: para utilizar getters y setters y aplicarlos posteriormente:      
        User user;
//Para poder guardar el securityGroupID:        
        int scgrpid;
//Y para verificar el resultado de si un usuario pertence a un grupo o no, se guarda en:        
        boolean result;
        
        ModelAndView mv;


//Mediante getParameter podemos obtener los valores de los campos del formulario de userform.
        String txtusuario = hsr.getParameter("txtusuario");
        String districtCode = hsr.getParameter("selectDistrictCode");
//Se hace el LoginVerification: con esto permite comprobar si los datos del usuario personales y de grupo coinciden con las credenciales:
//Para ello se hace una conexión a la BD proporcionada por applicationContext (la que guarda las credenciales de las distintas BBDD),
//pasando el parámetro districtCode, que permite conectarte a una BBDD o a otra en función de la opción que se elija en la primera vista
//y se cierra al comprobar todos los datos.
// Por ejemplo RWI-SPAIN es una BBDD de pruebas, y luego también se puede acceder
//a IS-PAN para ver BBDD de colegios de panamá.
//Por todo ello, primero
        LoginVerification login = new LoginVerification(districtCode,hsr);
//La conexión a una base de datos u otra depende de los datos obtenidos en LoginVerification        
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
//Con setTipo se establece el tipo de usuario (padre,profesor o ambos). Ver más abajo el método:                        
                        setTipo(user);
                        session.setAttribute("user", user);
                        session.setAttribute("schoolName", "");
//Aquí devuelve el método menu con los datos de la navegación y el districtCode(ver menu más arriba):                        
                        return menu(hsr, hsr1, districtCode);
                    }
//Aquí entra si se ha introducido mal el securityGroupID:                    
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
