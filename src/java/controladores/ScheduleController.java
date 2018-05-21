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
import dataManage.Restrictions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Chema
 */
@Controller
public class ScheduleController {
    
    @RequestMapping("/schedule/renweb.htm")
    public ModelAndView scheduleEduweb(HttpServletRequest hsr, HttpServletResponse hsr1){
        ModelAndView mv = new ModelAndView("homepage");
        String tempid = hsr.getParameter("tempid");
        String xs = hsr.getParameter("cols");
        String ys = hsr.getParameter("rows");
        String roomgroup = hsr.getParameter("grouproom");
        String posSelectTemplate = hsr.getParameter("posSelectTemplate");
        int roommode = Integer.parseInt(hsr.getParameter("roommode"));
        int id = Integer.parseInt(hsr.getParameter("id"));
        String yearid = hsr.getParameter("yearid");
        int x = Integer.parseInt(xs);
        int y = Integer.parseInt(ys);
        mv.addObject("hFilas",Consultas.getRowHeader(id, y)); //4sg
        mv.addObject("hcols",Consultas.getColHeader(id, x)); //2sg
        Algoritmo algo = new Algoritmo(x,y);
        Restrictions r = new Restrictions(yearid,tempid,roomgroup,1);
        //r.syncOwnDB();
        algo.algo(mv,r,roommode);
        String json = r.teachersJSON();
        return mv;
    }
    
    /**
     * to do:
     * -Teneis que revisar si funciona todo correctamente y si no le faltan datos
     *  a la DB.
     * falta comprobar el funcionamiento
     * @param hsr
     * @param hsr1
     * @return 
     */
    @RequestMapping("/schedule/own.htm")
    public ModelAndView scheduleOwn(HttpServletRequest hsr, HttpServletResponse hsr1){
        ModelAndView mv = new ModelAndView("index");
        String tempid = hsr.getParameter("tempid");
        String xs = hsr.getParameter("cols");
        String ys = hsr.getParameter("rows");
        String roomgroup = hsr.getParameter("grouproom");
        int roommode = Integer.parseInt(hsr.getParameter("roommode")); //VERIFICAR
        int id = Integer.parseInt(hsr.getParameter("id"));
        String yearid = hsr.getParameter("yearid");
        int x = Integer.parseInt(xs);
        int y = Integer.parseInt(ys);
        mv.addObject("hFilas",Consultas.getRowHeader(id, y));
        mv.addObject("hcols",Consultas.getColHeader(id, x));
        Algoritmo algo = new Algoritmo(x,y);
        Restrictions r = new Restrictions(yearid,tempid,roomgroup);
        r.extraerDatosOwnDB();
        algo.algo(mv,r,roommode);
        return mv;
    }
    
    @RequestMapping("/schedule/teacherMasterSchedule.htm")
    public String masterSchedule(HttpServletRequest hsr, HttpServletResponse hsr1){
        
        return "";
    }
}
