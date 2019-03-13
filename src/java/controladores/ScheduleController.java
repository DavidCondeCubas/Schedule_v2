/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Algoritmo;
import dataManage.Consultas;
import dataManage.Exceptions;
import dataManage.Restrictions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.InputStream;
import javax.servlet.http.HttpSession;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 *
 * @author Chema
 */
@Controller
public class ScheduleController   {
//Este es el schedule que está actualmente en uso(local):
    @RequestMapping("/schedule/renweb.htm")
    public ModelAndView scheduleEduweb(HttpServletRequest hsr, HttpServletResponse hsr1) {
//Se cogen los parámetros del redirect y se guarda en diferentes variables para poder implementarlas posteriormente.
//id, x e y se implementan en el mv para las cabeceras de columnas y filas:
        ModelAndView mv = new ModelAndView("homepage");
        
//APLICAR TRY DESDE AQUI:
try{
        String tempid = hsr.getParameter("tempid");
        String xs = hsr.getParameter("cols");
        String ys = hsr.getParameter("rows");
    
//String schoolName = hsr.getParameter("schoolName");
        String schoolCode = hsr.getParameter("schoolcode");
        String shuffle = hsr.getParameter("shuffle");
        String actvRoom = hsr.getParameter("actvRoom");
        String roomgroup = hsr.getParameter("grouproom");
// String posSelectTemplate = hsr.getParameter("posSelectTemplate");
// int roommode = Integer.parseInt(hsr.getParameter("roommode"));
        int id = Integer.parseInt(hsr.getParameter("id"));
        String yearid = hsr.getParameter("yearid");
        int x = Integer.parseInt(xs);
        int y = Integer.parseInt(ys);
//Los siguientes objetos implementan en el mv los datos de las cabeceras de filas y columnas: 
//El getRowHeader contiene las horas de cada clase, de tamaño 10 (es la primera columna, que se sitúa a la izquiera del horario):
        mv.addObject("hFilas", Consultas.getRowHeader(id, y)); //4sg
//El getColReader contiene el nombre de los días de la semana, de tamaño 5 (es la primera fila, se sitúa en la parte superior del horario):        
        mv.addObject("hcols", Consultas.getColHeader(id, x)); //2sg
        
//Después se instancian objetos de algoritmo y restricciones: 

        Algoritmo algo = new Algoritmo(x, y);
     
//Se aplican las restricciones en funcion de los datos introducidos en el menu previo:        
      Restrictions r = new Restrictions(yearid, tempid, roomgroup, 1,schoolCode);
        //r.syncOwnDB();
//Shuffle y ActiveRoom se capturan del redirect que se hace desde Homepage.menu, que a su vez se captura de menu.jsp:
//En la vista del menu, aparecen los valores de 0=disabled y 1=enabled.
//Los dos siguientes métodos capuran un boolean, que si vale 1 es = true, pero si es diferente se mantiene en false, ya que
//así ha sido declarado en el constructor de Restrictions:
//Básicamente lo que hacen los métodos, es que activan el Shuffle y el ActiveRoom si en el menú se les ha indicado esa opción:

       r.updateShuffleRosters(shuffle);
      r.updateActiveRooms(actvRoom);
 
//Se aplica el algoritmo en función de las restricciones, el schoolCode, el yearid y el tempid, y se retorna el mv correspondiente:    

        algo.algo(mv, r,schoolCode,yearid,tempid);
       String json = r.teachersJSON();
       
       
}catch (Exception e){
   e.getMessage();
    
}
                
        return mv;
    }

  
    @RequestMapping("/schedule/teacherMasterSchedule.htm")
    public String masterSchedule(HttpServletRequest hsr, HttpServletResponse hsr1) {

        return "";
    }
}
