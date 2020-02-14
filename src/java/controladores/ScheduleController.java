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
public class ScheduleController {

    @RequestMapping("/schedule/renweb.htm")
    public ModelAndView scheduleEduweb(HttpServletRequest hsr, HttpServletResponse hsr1) {

//Se crea el mv que devolverá  homepage.jsp, que, al ejecutar el programa, se accederá desde el botón Create Schedule de la vista menu.jsp,
//Pero antes hay que cargar los datos de las restricciones y del algoritmo, teniendo en cuenta también los datos recogidos desde la redirección de Homepage.create:
        ModelAndView mv = new ModelAndView("homepage");

        try {
//Se cogen los parámetros del redirect y se guarda en diferentes variables para poder implementarlas posteriormente.
//id, x e y se implementan en el mv para las cabeceras de columnas y filas:            
            String tempid = hsr.getParameter("tempid");
            String schoolCode = hsr.getParameter("schoolcode");
            String shuffle = hsr.getParameter("shuffle");
            String actvRoom = hsr.getParameter("actvRoom");
            int id = Integer.parseInt(tempid);
            String yearid = hsr.getParameter("yearid");
//x son las columnas e y son las filas (está al revés, tenerlo en cuenta, para los ExcludeBlocks por ejemplo):            
            int x = Integer.parseInt(hsr.getParameter("cols"));
            int y = Integer.parseInt(hsr.getParameter("rows"));

//Los siguientes objetos implementan en el mv los datos de las cabeceras de filas y columnas: 
//El getRowHeader contiene las horas de cada clase, de tamaño 10 por ejemplo (es la primera columna, que se sitúa a la izquiera del horario):
            mv.addObject("hFilas", Consultas.getRowHeader(id)); //4sg
//El getColReader contiene el nombre de los días de la semana, de tamaño 5 por ejemplo (es la primera fila, se sitúa en la parte superior del horario):        
            mv.addObject("hcols", Consultas.getColHeader(id)); //2sg
            mv.addObject("templateText", Consultas.getTemplateText(id, x, y));

//Después se instancian objetos de algoritmo y restricciones: 
            Algoritmo algo = new Algoritmo(x, y);

//Se aplican las restricciones en funcion de los datos introducidos en el menu previo:        
            Restrictions r = new Restrictions(yearid, tempid, schoolCode, x, y);

//Shuffle y ActiveRoom se capturan del redirect que se hace desde Homepage.menu, que a su vez se captura de menu.jsp:
//En la vista del menu, aparecen los valores de 0=disabled y 1=enabled.
//Los dos siguientes métodos capuran un boolean,si vale 1 es = true, pero si es diferente se mantiene en false, ya que
//así ha sido declarado en el constructor de Restrictions:
            r.updateShuffleRosters(shuffle);
            r.updateActiveRooms(actvRoom);

//Se aplica el algoritmo en función de las restricciones, el schoolCode, el yearid y el tempid, y se retorna el mv correspondiente:    
            algo.algo(mv, r, schoolCode, yearid, tempid);

        } catch (Exception e) {
                e.getMessage();

        }

        return mv;
    }

    @RequestMapping("/schedule/teacherMasterSchedule.htm")
    public String masterSchedule(HttpServletRequest hsr, HttpServletResponse hsr1) {

        return "";
    }
}
