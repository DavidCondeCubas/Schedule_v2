/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

/**
 *
 * @author Jesús Aragón
 */
import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.support.RequestContextUtils;


public class DatosUsuarioControlador implements Controller {
//Gracias a este mv se puede cargar el idioma correspondiente en userform.jsp, al elegirlo con uno de los vínculos que hay
//(English, Español o عربي) en la vista. Al accionar uno de los vínculos, se carga el valor obtenido en el parametro lenguaje (ver userform.jsp).
//Entonces se vuelve a cargar la página de inicio con el idioma correspondiente.    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            RequestContextUtils.getLocaleResolver(request).setLocale(request, response, new Locale(request.getParameter("lenguaje")));
        } catch (Exception ex) {
            ex.getMessage();
        }

        return new ModelAndView("userform");
    }

}
