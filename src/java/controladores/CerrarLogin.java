/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.mvc.Controller;

/**
 *
 * @author Jesús Aragón
 */
@Controller
public class CerrarLogin {
//El ModelAndView inicio del controlador Homepage.java es lo que hace que cargue la primera vista(mismo procedimiento que en Homepage.java/inicio).
//Este mv (cerrarLogin) está vinculado al botón Log Out en la vista infouser.jsp.
//En dicha vista, se aplica una function de JavaScript llamada logout(), que
//contiene:   document.location.href = "<c:url value="/cerrarLogin.htm"/>", y esto es lo que hace que redireccione al inicio).
    @RequestMapping(value="/cerrarLogin")
    public ModelAndView cerrarLogin(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
            ModelAndView mv =  new ModelAndView("redirect:/userform.htm?opcion=inicio");
        
            HttpSession sesion = hsr.getSession(false);
            sesion.invalidate();
            return mv;
    }

}
