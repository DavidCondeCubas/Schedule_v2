/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Conjuntos;
import dataManage.Tupla;
import dataManage.Consultas;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.AALOAD;
import static dataManage.Consultas.teachersCOURSE;
import dataManage.Exceptions;
import dataManage.Restrictions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Norhan
 */
public class Algoritmo {

    public static int TAMX = 3;
    public static int TAMY = 11;
    public static int NUMERO_MAX_GENERO = 2;
    public static HashMap<Integer, String> NumNomSection = new HashMap<>();
    public final static int CHILDSPERSECTION = 20;
    private ArrayList<String> Log;
    private Conjuntos<Integer> conjuntos;

    public Algoritmo() {
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }

    public Algoritmo(int x, int y) {
        TAMX = x;
        TAMY = y;
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }

    private Boolean containsValueInLinkedCourse(Restrictions r, int courseID) {
        for (HashMap.Entry<String, Course> entry : r.linkedCourses.entrySet()) {
            if (entry.getValue().getIdCourse() == courseID) {
                return true;
            }
        }
        return false;
    }

    /**
     * algoritmo
     *
     * @param mv
     * @param r
     * @param schoolCode
     * @param yearId
     * @param roommode
     * @param templateId
     */
    //mv lleva en este momento el esquema de filas y columnas (filas para una posición y columnas para otra):
    //r lleva todas las restricciones contempladas previamente:
    //schoolCode,yearId,templateId: nombre del distrito, yearId y templateId escogidos previamente:
    //ESTRUCTURA GENERAL DEL METODO Algoritmo.algo (sin LinkedCourses):
    /* for de cada curso{
            for de cada seccion para rellenar con estudiantes{
                for de opciones totales para la seccion{
                    for estudiantes (se comprueba si cada opcion es compatible)
            }
        }
    }*/
    public void algo(ModelAndView mv, Restrictions r, String schoolCode, String yearId, String templateId) {

        int vueltas = 0;
//r.courses lleva las restricciones de los cursos que se han contemplado previamente(en este caso son 2)
//se va a recorrer cada uno con course:   

        for (Course course : r.courses) {

            if (course.getIdCourse() == 307) {
                System.err.println("");
            }
            if (course.getIdCourse() == 4) {
                System.err.println("");
            }
            if (course.getIdCourse() == 36) {
                System.err.println("");
            }
                HashMap<Integer, Integer> teachers_numSections = new HashMap<>();
            HashMap<Integer, Integer> rooms_numSections = new HashMap<>();

            vueltas++;
//Si el id del curso capturado no contiene valores LinkedCourse, se añade ArraySecciones a course(con las restricciones de las secciones por curso)
//Por ejemplo : con el id de curso 1245, se han añadido 5 posiciones de una seccion por posición, todas con id de curso 1245:       

            if (!containsValueInLinkedCourse(r, course.getIdCourse())) {

                course.setArraySecciones(chargeArraySections(r, course));
                if (course.getArraySecciones().isEmpty()) {
                    r.aviso.addCourseWithoutSections(course);
                }

                int maxSections;
//---CAMBIO---Si el apartado maximo de secciones de curso y school esta vacío(en RenWeb, primero analiza si está vacío el de curso,
//y si lo está coge el valor de school. En caso de que el de school también estuviera vacío, iría al if para calcularlo, si hay algún dato
//en curso o en school va al else, para asignar este dato a la variable maxSections:):                
                if (course.getMaxSections() == null || course.getMaxSections().equals("")) {
//(Ya no entra aqí porque se ha asignado un valor por defecto en school)El máximo de secciones se establece por la division del número de estudiantes por curso dividido entre el máximo nº de niños por sección(aula):
//En el caso del curso con id=1245, hay hay 56 alumnos en este curso, y hay una restricción de MáxChildPerSection de 20,
//por lo que se asignarán un máximo de secciones de 2 (se trunca el dato final). De los 16 estudiantes restantes, se añade en una nueva sección más
//adelante con la instrucción que recoge el resto y añade un maxSections manualmente(las secciones máximas se quedan guardadas en la variable
//maxSections). Total: maxSections = 3:

                    maxSections = r.studentsCourse.get(course.getIdCourse()).size() / course.getMaxChildPerSection();
                    if (r.studentsCourse.get(course.getIdCourse()).size() % course.getMaxChildPerSection() != 0) {
                        maxSections++;
                    }
                } //Si el apartado de cada curso de secciones no está vacío, se añaden a la variable maxSections                
                else {
                    maxSections = Integer.parseInt(course.getMaxSections());
                }
//Se comparan las secciones que ha recogido el ArraySecciones (esto captura las secciones que están añadidas en la página RenWeb/Academics/Classes, en el apartado
//Class Sections del curso en concreto. Por ejemplo en el caso de ENG1 (id=1245) hay 5 secciones añadidas) con la variable maxSections(esta última calculada previamente).
//El arraySecciones añade el numSeccion a cada seccion que hay contemplada (en el caso de id course=1245 hay 5 secciones con num: 1,2,5,3,4)
//Con el siguiente if se consigue que si ArraySecciones es menor que maxSections, se igualen:
                if (course.getArraySecciones().size() < maxSections) {
                    int i = course.getArraySecciones().size();
                    while (i < maxSections) {
                        Seccion auxSeccion = new Seccion();
                        auxSeccion.setNumSeccion(i + 1);
                        auxSeccion.setNameSeccion(String.valueOf(i + 1));
                        auxSeccion.setCourseID(course.getIdCourse());
                        course.addSeccion(new Seccion(auxSeccion));
                        NumNomSection.put(course.getIdCourse() * 100 + (i + 1), String.valueOf(i + 1));
                        i++;
                    }
                }

                ArrayList<ArrayList<Tupla>> opciones = new ArrayList<>();
//El siguiente if tiene por condición si las opciones de PatternGroup no existen aún, y si existe necesidad de generación de opciones en el curso
//(el needGenerateOptions se basa en si el lockSchedule está desactivado, entonces en este caso será true la necesidad):
//Esto es así porque si el lockschedule está activado se bloquea la posibilidad de añadir o quitar patrones de grupo(se bloquean los bloques),
//por lo que no hay necesidad de generar opciones. Sólo habrá necesidad si el lockSchedule está desactivado y no se encuentra ningún patrón de grupo
//generado previamente.
//Si se cumplen estas dos premisas, se rellena opciones en base a los totalBlocks de restricciones y log:
                if (course.getOpcionesPatternGroup().isEmpty() && needGenerateOptions(course)) { //totalBlocks es un array bidimensional con todos los bloques asignados:
                     //**************************************//
                   //****************************************//
                    opciones = course.opciones(r.totalBlocks, Log, r.aviso);
                    //CAMBIO DE OPCIONES
                    //opciones = course.opciones(r.totalBlocks);
                    //**************************************//
                   //****************************************//
                } //Si ya hay opciones en el PatternGroup, se capturan las opciones que ya existen en base a dicho PatternGroup(no se generan nuevos patterngroup ni opciones)
                //Se cogen las patternGroup definidas en RenWeb:                
                else if (!course.getOpcionesPatternGroup().isEmpty()) {
                    opciones = course.getOpcionesPatternGroup();
                }
                ArrayList<Integer> noAsign = new ArrayList<>();
//Se pegan en el array noAsign los ids de todos los estudiantes del curso en cuestión:
                if (r.studentsCourse.containsKey(course.getIdCourse())) {
                    noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
                    if (r.isShuffleRosters()) {
//Si está elegida la opción de ShuffleRosters en restricciones:
//Combinacion de estudiantes(barajea). shuffle es un método que tiene la clase Collections para barajear datos:
                        Collections.shuffle(noAsign);
                    }

                }

//Se analiza en las condiciones si las secciones del curso están activados o no los lockSchedule y lockEnrollment:                
//En el caso del idcourse= 1245, va a la opción de lockSchedule=false y lockEnrollment=false(al segundo else if para la primera seccion(i=0)):     
//La priemra opcion y la tercera tienen el mismo cuerpo porque no afecta que esté activado o no el lockEnrollment si previamente está desactivado 
//el lockSchedule(si está desactivado se asignan previamente la cantidad de estudiantes y el lockEnrollment no afecta).
//(está estructurado así para que se entienda que hay 4 opciones posibles).
//todas las opciones):
//Con estos métodos se se asignan los estudiantes de una forma u otra, y el noAsign puede quedar vacío si se han asignado todos.
//Con opciones 1 y 3: generan patrones, con opcion 2: se llenan las secciones porque el lockSchedule está activado.
//Con opcion 4: se llenan secciones teniendo en cuenta que el lockSchedule y el lockEnrollment están activados (no hace falta generar patrón ni usar el método fillSection).
                for (int i = 0; i < course.getArraySecciones().size(); i++) {
//Nota: las opciones generadas solo se usan si el lockSchedule está desactivado (si no no hace falta)
//Por lo que la preferencia sería:
//-Si está desactivado el lockSchedule: coger las opciones del patterngroup del curso. Si está vacío, generar opciones.
//En cualquiera de los dos casos se genera un patrón específico para la sección en base a las opciones.
//-Si está activado el lockSchedule: no hace falta tener en cuenta opciones, simplemente se rellena las secciones con el patrón
//específico que ya se ha indicado.
                    if (course.getIdCourse() == 1363) {
                        System.out.println("check");
                    }
                    if (course.getArraySecciones().get(i).patronUsado.isEmpty() && course.getArraySecciones().get(i).lockSchedule == true) {
                        r.aviso.addAvisoWithoutPatterns(course.getNameCourse(), course.getArraySecciones().get(i).getNameSeccion());
                        course.getArraySecciones().get(i).setLockSchedule(false);
                        if (opciones.isEmpty()) {
                            //**************************************//
                   //****************************************//
                    opciones = course.opciones(r.totalBlocks, Log, r.aviso);
                    //CAMBIO DE OPCIONES
                    //opciones = course.opciones(r.totalBlocks);
                    //**************************************//
                   //****************************************//
                        }
                    }
                    if (course.getArraySecciones().get(i).idStudents.isEmpty() && course.getArraySecciones().get(i).lockEnrollment == true) {
                        course.getArraySecciones().get(i).setLockEnrollment(false);
                    }
                    if (!course.getArraySecciones().get(i).lockSchedule && course.getArraySecciones().get(i).lockEnrollment) {
                        noAsign = generatePattern_Section(r, r.teachers, r.rooms, course, opciones,
                                noAsign, r.students, course.getArraySecciones().get(i), teachers_numSections, rooms_numSections, templateId, r.aviso, schoolCode);
                    } else if (course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment) {
                        noAsign = fillSection(course.getArraySecciones().get(i), r, course, noAsign, r.students, r.teachers, templateId, r.rooms, r.aviso, schoolCode);
                        course.getArraySecciones().get(i).setPatternRenWeb(1);
                    } else if (!course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment) {
                        noAsign = generatePattern_Section(r, r.teachers, r.rooms, course, opciones,
                                noAsign, r.students, course.getArraySecciones().get(i), teachers_numSections, rooms_numSections, templateId, r.aviso, schoolCode);
                    } else { // lockSchedule && lockEnrollment==true:
                        ArrayList<Teacher> teachersForCourse = new ArrayList<>();
                        //  teachersForCourse = TeachersCourse(r.teachers, teachersForCourse, course);
                        teachersForCourse = CompararTeachers(course, teachersForCourse, r, i);
                        try {
                            if (course.isBalanceTeachers() && teachersForCourse.size() > 1) {
                                Collections.sort(teachersForCourse, new CustomComparatorTeacher());
                            }
                        } catch (Exception e) {
                            e.getMessage();
                        }
                        for (int j = 0; j < course.getArraySecciones().get(i).getIdStudents().size(); j++) {

//Con este for se comprueba si las ids de los estudiantes de la seccion que se esta evaluando corresponden con los ids de estudiantes que hay guardados
//en el hash students(es decir, de los capturados en las restricciones).
//Aquí se hace lo mismo que en el if en el lockSchedule está activado (opción 2, primer else if):
                            if (r.students.containsKey(course.getArraySecciones().get(i).getIdStudents().get(j))) {
//                                if(r.students.get(course.getArraySecciones().get(i).getIdStudents().get(j)).patronCompatible(course.getArraySecciones().get(i).getPatronUsado()))
                                r.students.get(course.getArraySecciones().get(i).getIdStudents().get(j)).ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(i).getNumSeccion());

                            }

                        }
//Método ocuparHueco: se refiere a las marcas de los bloques                        
                        course.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getArraySecciones().get(i).getNumSeccion());
//En el caso de que entre en if de LockSchedule true y lockEnrollment true, se cogen las secciones que cumplen estas condiciones:                        
                        course.getArraySecciones().get(i).setLockSchedule(true);
                        course.getArraySecciones().get(i).setLockEnrollment(true);
                        course.getArraySecciones().get(i).setPatternRenWeb(1);
                        Teacher t_Aux = r.hashTeachers.get(course.getArraySecciones().get(i).getIdTeacher());
                        if (t_Aux != null) {
                            t_Aux.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(i).getNumSeccion());
                            t_Aux.incrementarNumSecciones();
                            course.getArraySecciones().get(i).setTeacher(t_Aux);
                        } else if (teachersForCourse.size() > 0) {
                            t_Aux = teachersForCourse.get(0);
                        }
                        if (r.isActiveRooms()) {
                            ArrayList<Room> roomsForCourse = new ArrayList<>();
                            roomsForCourse = CompararRooms(r.rooms, roomsForCourse, course, course.getArraySecciones().get(i), templateId, r.groupRooms, r.aviso, schoolCode);
                            if (roomsForCourse.size() > 0) {
                                Room r_Aux = roomsForCourse.get(0);
                                r.rooms.get(r_Aux.getRoomid()).ocuparHueco(course.getIdCourse(), course.getArraySecciones().get(i).getNumSeccion(), course.getArraySecciones().get(i).getPatronUsado());
                                r.rooms.get(r_Aux.getRoomid()).incrementarNumSecciones();
                                course.getArraySecciones().get(i).setRoom(r.rooms.get(r_Aux.getRoomid()));

                            }

                        }
                        noAsign = conjuntos.diferencia(noAsign, course.getArraySecciones().get(i).getIdStudents());

                        if (course.getArraySecciones().get(i).getIdStudents().size() < course.getMinChildPerSection()) {
                            r.aviso.addAvisoMinStudents(course.getNameCourse(), course.getArraySecciones().get(i).getNameSeccion());
                        }
                    }

                }
                ArrayList<Boolean> marcasAlumnos = new ArrayList<>();
//Todos los alumnos que no han quedado asignados se recorren en este for para almacenarlos en el array marcasAlumnos, y se les asigna el valor false:
                for (int j = 0; j < noAsign.size(); j++) {
                    marcasAlumnos.add(false);
                }
//Se ordenan las secciones del curso en concreto:

                try {
                    Collections.sort(course.getArraySecciones(), new CompSeccionesStudents());
                } catch (Exception e) {
                    e.getMessage();
                }

//A PARTIR DE AQUÍ SE AÑADEN ESTUDIANTES A LAS SECCIONES QUE YA TIENEN LOS MÍNIMOS DE ESTUDIANTES, ES DECIR, 
//EN LOS 4 IF DE ARRIBA SE RELLENAN LOS MÍNIMOS DE ESTUDIANTES POR SECCIÓN, Y AHORA SE TERMINAN DE RELLENAR LAS SECCIONES (PRIMERO SE RELLENAN SERIALMENTE LOS MÍNIMOS DE ESTUDIANTES
//DE TODAS LAS SECCIONES, Y SI QUEDAN ESTUDIANTES, SE SIGUEN RELLENANDO SERIALMENTE HASTA LLEGAR A LOS MÁXIMOS )
//Este for entra en cada seccion del curso concreto:
                for (int j = 0; j < course.getArraySecciones().size(); j++) {
//Si la seccion en concreto no tiene asignado lockEnrollment:
//Aquí se van añadiendo las marcasAlumnos a true (de todos los estudiantes):
                    if (!course.getArraySecciones().get(j).lockEnrollment) {
                        for (int k = 0; k < noAsign.size(); k++) {
                            if (!marcasAlumnos.get(k) && course.getArraySecciones().get(j).getIdStudents().size() < course.getMaxChildPerSection()) {
                                if (r.students.get(noAsign.get(k)).patronCompatible(course.getArraySecciones().get(j).getPatronUsado())) {
                                    if (!course.isGR()) {
//Este es el mismo array por el cual se ocupaban huecos de fillSection, es decir, se añade en los huecos que coincida, el id de seccion+curso:                                    
                                        r.students.get(noAsign.get(k)).ocuparHueco(course.getArraySecciones().get(j).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(j).getNumSeccion());
                                        course.getArraySecciones().get(j).addStudent(noAsign.get(k));
                                        marcasAlumnos.set(k, true);
                                    } else {
                                        if (course.getArraySecciones().get(j).getGender() == null) {
                                            if (course.getArraySecciones().get(j).idStudents.size() > 0) {
                                                course.getArraySecciones().get(j).setGender(r.students.get(course.getArraySecciones().get(j).idStudents.get(0)).getGenero());
                                            } else {
                                                course.getArraySecciones().get(j).setGender("Male");
                                            }
                                        }
                                        if (course.getArraySecciones().get(j).getGender().equals(r.students.get(noAsign.get(k)).getGenero())) {
                                            r.students.get(noAsign.get(k)).ocuparHueco(course.getArraySecciones().get(j).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(j).getNumSeccion());
                                            course.getArraySecciones().get(j).IncrNumStudents();
                                            course.getArraySecciones().get(j).addStudent(noAsign.get(k));

                                            marcasAlumnos.set(k, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ArrayList<Integer> auxNoAsign = new ArrayList<>();
                for (int i = 0; i < noAsign.size(); i++) {
                    if (!marcasAlumnos.get(i)) {
                        auxNoAsign.add(noAsign.get(i));
                    }
                }
                //Se actualiza el noAsign con los últimos students asignados:                
                noAsign = auxNoAsign;

                if (!noAsign.isEmpty()) {
                    int numAlumnos = course.getMaxChildPerSection();
                    if (numAlumnos == 0) {
                        numAlumnos = CHILDSPERSECTION; // POR DEFECTO
                    }
                    int sectionsNoEnroled = noAsign.size() / numAlumnos;
                    if (sectionsNoEnroled == 0) {
                        sectionsNoEnroled = 1;
                    }
                    course.setStudentsNoAsignados(noAsign);
                    course.setSectionsNoEnrolled(sectionsNoEnroled);
                    double noasignsize = noAsign.size();
                    double studentscoursesize = r.studentsCourse.get(course.getIdCourse()).size();
                    double percent = 100 - (noasignsize / studentscoursesize) * 100;
                    course.setPercentEnrolled(percent);
                } else {
                    course.setSectionsNoEnrolled(0);
                    course.setPercentEnrolled(100);
                }
            }

//Hasta aquí todos los cursos que no tienen linked Courses, y a partir de aquí los que si:
//---------------------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------
//Con este if, se comprueba primero de cada curso si tiene LinkedCourses.
//Si es así, se cambia el valor del boolean encontrado a true.
            if (r.getLinkedCourses().containsKey("" + course.getIdCourse())) {
                int k = 0;
                boolean encontrado = false;
//Luego se sigue el bucle hasta que se recorran todos los cursos o hasta que se encuentre el linked course.  
//Es decir, se busca el curso hijo en el has
                while (k < r.courses.size() && !encontrado) {
                    if (r.courses.get(k).getIdCourse() == r.getLinkedCourses().get("" + course.getIdCourse()).getIdCourse()) {
                        encontrado = true;
                    } else {
                        k++;
                    }

                }
//Si encontrado==true, se copia el objeto del curso hijo (linkeado) a courseAsociado              
                if (encontrado) {
                    Course courseAsociado = r.courses.get(k);
                    ArrayList<Integer> seccionesHabilitadas = new ArrayList<>();
//En seccionesHabilitadas se capturan las secciones que se han linkeado en consultas (los números que están enntre el curso padre y el curso hijo).                    
                    seccionesHabilitadas = r.getLinkedCourses().get("" + course.getIdCourse()).getSectionsLinkeadas();
                    ArrayList<ArrayList<Tupla>> opcionesAsoc = new ArrayList<>();
//Con el if siguiente se guardan en courseAsociado (curso hijo) las secciones (a través de los datos de DataSections de Restrictions):
                    if (courseAsociado.getArraySecciones() == null || courseAsociado.getArraySecciones().isEmpty()) {
                        courseAsociado.setArraySecciones(chargeArraySections(r, courseAsociado));
                    }
                    if (courseAsociado.getArraySecciones().isEmpty()) {
                        r.aviso.addCourseWithoutSections(courseAsociado);
                    }
                    // courseAsociado.setArraySecciones(course.getArraySecciones());

//Si no encuentra patterngroup (en el curso) y hay necesidad de generar optionslinked (que se basa en si hay menos secciones en el curso hijo que en el padre
//o si el curso hijo no tiene un patron usado(se refiere al Pattern del Schedule de cada seccion)): en ese caso genera opciones
//de la misma forma que en el apartado de cursos no linkeados:
                    if (courseAsociado.getOpcionesPatternGroup().isEmpty() && needGenerateOptionsLinked(courseAsociado, course)) {
                           //**************************************//
                   //****************************************//
                    opcionesAsoc = courseAsociado.opciones(r.totalBlocks, Log, r.aviso);
                    //CAMBIO DE OPCIONES
                    //opcionesAsoc = courseAsociado.opciones(r.totalBlocks);
                    //**************************************//
                   //****************************************//
                        
                    } //Si no hay necesidad de generar opciones pero existe un patterngroup del curso padre: se vincula ese patrón al curso hijo:                    
                    else if (!course.getOpcionesPatternGroup().isEmpty()) {
                        opcionesAsoc = courseAsociado.getOpcionesPatternGroup();
                    }
                    for (int i = 0; i < course.getArraySecciones().size(); i++) {
                        if (courseAsociado.getIdCourse() == 1301) {
                            System.err.println("    ");
                        }
//Para cada seción                        
//1º if: si hay más número de secciones en el curso padre que en el hijo: se añade una seccion al curso hijo y se le añade un patrón:
                        if (courseAsociado.getArraySecciones().size() < i) {
                            courseAsociado.addSeccionWithoutPattern(course.getArraySecciones().get(i));
                            assignPatternToSection(r, course, course.getArraySecciones().get(i), opcionesAsoc, courseAsociado);

                        } //2º else if: si no hay secciones linkeadas (esto es en el caso de que no se hubieran indicado las secciones (solo se indico curso padre, curso hijo)):
                        else if (seccionesHabilitadas.isEmpty()) { // todos

                            int ind = 0;
                            boolean exito = false;

                            while (ind < courseAsociado.getArraySecciones().size() && !exito) {
//Se irían añadiendo (según va dando vueltas el for) los datos del curso padre al curso hijo, sección por sección: 
//(se busca que el número de sección del curso hijo coincida con el numero de seccion del curso padre):    
                                if (courseAsociado.getArraySecciones().get(ind).getNumSeccion() == course.getArraySecciones().get(i).getNumSeccion()) {
                                    courseAsociado.ocuparHueco(courseAsociado.getArraySecciones().get(ind).patronUsado, courseAsociado.getArraySecciones().get(ind).getNumSeccion());
                                    courseAsociado.getArraySecciones().get(ind).copiarIdsStudents(course.getArraySecciones().get(i).getIdStudents(), r.students, courseAsociado);
                                    courseAsociado.getArraySecciones().get(ind).setPatternRenWeb(course.getArraySecciones().get(i).getPatternRenWeb());
                                    exito = true;
                                    if (!courseAsociado.getArraySecciones().get(ind).isLockSchedule()) {
                                        assignPatternToSection(r, course, course.getArraySecciones().get(i), opcionesAsoc, courseAsociado);

                                    }
                                } else {
                                    ind++;
                                }

                            }
                        } //3º else:                         
                        else {
                            int ind = 0;
                            boolean exito = false;
                            while (ind < courseAsociado.getArraySecciones().size() && !exito) {
                                if (courseAsociado.getArraySecciones().get(ind).getNumSeccion() == course.getArraySecciones().get(i).getNumSeccion() && seccionesHabilitadas.contains(ind + 1)) {
//A diferencia del apartado 2º, aquí se ocupan los huecos buscando las marcas, en el anterior simplemente era un copypaste de todo
                                    courseAsociado.ocuparHueco(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).patronUsado, courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getNumSeccion());
                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).copiarIdsStudents(course.getArraySecciones().get(i).getIdStudents(), r.students, courseAsociado);
//Se asigna el teacher de la seccion del curso hijo:                                    
                                    Teacher t_Aux = new Teacher();
                                    if (r.hashTeachers.containsKey(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getIdTeacher())) {
                                        t_Aux = r.hashTeachers.get(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getIdTeacher());
                                    } else {
                                        t_Aux.setName("No found in template courses");
                                    }
                                    t_Aux.ocuparHueco(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getPatronUsado(), courseAsociado.getIdCourse() * 100 + courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getNumSeccion());
                                    t_Aux.incrementarNumSecciones();

                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).setTeacher(t_Aux);
                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).setPatternRenWeb(course.getArraySecciones().get(i).getPatternRenWeb());
                                    exito = true;
                                    if (!courseAsociado.getArraySecciones().get(ind).isLockSchedule()) {
                                        assignPatternToSection(r, course, course.getArraySecciones().get(i), opcionesAsoc, courseAsociado);

                                    }
                                } else {
                                    ind++;
                                }

                            }
                        }
                    }
                }
            }
        }

//Guardado de resultados en archivo y se devuelven a la vista:        
     //   saveXML_FTP(yearId, templateId, schoolCode, r);
        HashMap<Integer, String> persons = Consultas.getPersons();
        sortSectionsAndStudents(r.courses, persons);

        mv.addObject("TAMX", TAMX);
        mv.addObject("TAMY", TAMY);
        mv.addObject("persons", persons);

        ArrayList<Student> studentsOrdered = new ArrayList<>(r.students.values());
        try {
            sortStudentsPerGradeLevel(studentsOrdered, r.cs);
        } catch (Exception e) {

        }
        mv.addObject("students", r.students);
        mv.addObject("orderedStudents", studentsOrdered);

        try {
            sortCoursesPerAbbrev(r.courses, r.cs);
        } catch (Exception e) {

        }
        mv.addObject("Courses", r.courses);
        try {
            sortTeachersPerNames(r.teachers, r.cs);
        } catch (Exception e) {

        }
        mv.addObject("profesores", r.teachers);

        mv.addObject("cs", r.cs);
        mv.addObject("rooms", r.rooms);
        mv.addObject("hashTeachers", r.hashTeachers);
        mv.addObject("activeRoom", r.activeRooms);
        mv.addObject("log", Log);
        mv.addObject("avisos", r.aviso);
//Se añade este objeto para poder mostrar en la vista los cursos que no tienen estudiantes y que no podrán aparecer al crear el horario (se manda a homepage)

    }

    private boolean needGenerateOptions(Course c) {
        for (Seccion arraySeccion : c.getArraySecciones()) {
            if (!arraySeccion.lockSchedule) {
                return true;
            }
        }
        return false;
    }

    private boolean needGenerateOptionsLinked(Course cAsoc, Course c) {
        if (cAsoc.getArraySecciones().size() < c.getArraySecciones().size()) {
            return true;
        }

        for (int i = 0; i < cAsoc.getArraySecciones().size(); i++) {
            if (cAsoc.getArraySecciones().get(i).getPatronUsado().isEmpty()
                    || cAsoc.getArraySecciones().get(i).getPatronUsado() == null) {
                return true;
            }

        }
        return false;
    }

    private void sortStudentsPerGradeLevel(ArrayList<Student> t, Consultas cs) {
        // Sorting
        Collections.sort(t, new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                if (o1.getGradeLevel() == null || o2.getGradeLevel() == null) {
                    return -1;
                }
                return o1.getGradeLevel().compareTo(o2.getGradeLevel());
            }
        });
    }

    private void sortTeachersPerNames(ArrayList<Teacher> t, Consultas cs) {
        // Sorting
        Collections.sort(t, new Comparator<Teacher>() {
            @Override
            public int compare(Teacher o1, Teacher o2) {
                return cs.getNamePersons().get(o1.getIdTeacher()).compareTo(cs.getNamePersons().get(o2.getIdTeacher()));
            }
        });
    }

    private void sortCoursesPerAbbrev(ArrayList<Course> c, Consultas cs) {
        // Sorting
        Collections.sort(c, new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                return cs.getAbbrevCourses().get(o1.getIdCourse()).compareTo(cs.getAbbrevCourses().get(o2.getIdCourse()));
            }
        });
    }

    public class teacherComparator implements Comparator<Teacher> {

        HashMap<Integer, Integer> teachers_numS;

        public teacherComparator(HashMap<Integer, Integer> teachers_numS) {
            this.teachers_numS = teachers_numS;
        }

        @Override
        public int compare(Teacher o1, Teacher o2) {
            return this.teachers_numS.get(o1.getIdTeacher()) - this.teachers_numS.get(o2.getIdTeacher()); //To change body of generated methods, choose Tools | Templates.
        }
    }
//Este método es necesario para generar el patron a la hora de asignar estudiantes (de noAsign) por sección teniendo en cuenta que está desactivado el lockSchedule 
//(sirve tanto para si está activado o no el lockEnrollment, ya que dentro de este método hay un if que tiene en cuenta si está activado o no):

    private ArrayList<Integer> generatePattern_Section(Restrictions r, ArrayList<Teacher> teachers, HashMap<Integer, Room> rooms, Course c, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students, Seccion currentSec, HashMap<Integer, Integer> teachers_numSections, HashMap<Integer, Integer> rooms_numSections, String templateId, Exceptions aviso, String schoolCode) {
//Mínimo de estudiantes por sección(en el caso actual son 6, primera sección del curso 1245 eng1):
        int minStudentSection = c.getMinChildPerSection();

//ret es el array que se va a devolver(resultado de estudiantes no asignados después de aplicar la generación del patrón y hacer asignaciones)
        ArrayList<Integer> ret = new ArrayList<>();
//stids=(tupla de dos partes: 1ºids de opcion, 2º se va a almacenar posteriormente array estudiantes(ids, por eso es Integer) COMPATIBLES) La razón por la que no es un hash es que se va a necesitar
//una una búsqueda secuencial por ids:        
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();

        ArrayList<Integer> idsAsignados = new ArrayList<>();
//Se obtiene el id de los estudiantes de la sección concreta, para luego tenerlos en cuenta si está activado el lockEnrollment
//y para saber posteriormente si han podido ser asignados o no.
//Estos ids van siendo cada vez más según se van recorriendo las secciones (fuera del generatePattern). Así en la primera vuelta tiene 6 estudiantes,
//pero en las siguientes vueltas tiene 12, 18...
        //idsAsignados = c.getAllIds();
//Este hash almacena la cantidad de opciones válidas para cada estudiante(id: de cada estudiante, cantidad de opciones válidas):
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
//initHashStudents hace que se inicialicen a 0 los estudiantes para sumarlos de 1 en 1:        
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
        HashMap<Integer, String> genderStids = new HashMap<>();
//for de las opciones: dentro hay un for de estudiantes-->    
        for (int i = 0; i < sec.size(); i++) {
//Aquí se indica que stids se va a contabilizar a través de i (i=cada opción):            
            stids.add(new Tupla(i, new ArrayList<>()));
//stdMales y stdFemales también hace una separación de estudiantes masculinos y femeninos, pero este se tiene en cuenta con menor prioridad:            
            ArrayList<Integer> stdMales = new ArrayList<>();
            ArrayList<Integer> stdFemales = new ArrayList<>();
//-->for de estudiantes se comrpueba que cada estudiante puede encajar con la opción concreta(del anterior for):            
            for (Integer j : studentsCourse) {

//patronCompatible: en función de los huecos(bloques no asignados en la plantilla de un horario)
//students es un array que viene de fuera(restricciones), y que coge a todos los estudiantes de todos los cursos. Se necesita para obtener los datos de los estudiantes
                if (students.get(j).patronCompatible(sec.get(i))) {
//Si no se refiere a una seccion separada por restricciones de genero: se añade a stids la opción compatible(determinada por patronCompatible) en ese estudiante en concreto:
//hashStudents_cantPatrones se suma 1 si se cumple la condición finalmente de que el estudiante encaja con la opción:
                    if (!c.isGR()) {
                        stids.get(i).y.add(j);
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    } //Este caso es para añadir datos si se refiere a una seccion separada por restricciones de genero(el stids se calcula abajo):                   
                    else {// is GR
                        if (students.get(j).getGenero().equals("Female")) {
                            stdFemales.add(j);
                        } //Si es nulo el contenido, por defecto es "male":                        
                        else { //male or null
                            stdMales.add(j);
                        }
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
            }
//Si se refiere a una seccion separada por restricciones de genero: funcion igual que en !c.isGR pero teniendo en cuenta las restricciones de género, aunque se tiene en cuenta
//después del recorrido de todos los estudiantes:            
            if (c.isGR()) {
                if (stdMales.size() >= stdFemales.size()) {
                    // stids.get(i).y. = new ArrayList<>();
                    stids.set(i, new Tupla(i, new ArrayList<>(stdMales)));
                    genderStids.put(i, "Male");
                } else {
                    stids.set(i, new Tupla(i, new ArrayList<>(stdFemales)));
                    genderStids.put(i, "Female");
                }
            } else {
                genderStids.put(i, "");
            }
        }
        int idCourse = c.getIdCourse();

        //En el caso de que la seccion no sea separada por restricciones de género, se aplica un equilibrado en género masculino y femenino:        
        if (!c.isGR()) {
            equilibrarGender(stids, students);
        }
        //1ª ORDENACIÓN de stids-->Ordena la lista de conjuntos por numero de estudiantes de mayor a menor(es decir, las opciones compatibles con mayor número de estudiantes serán las primeras):        
        stids = SortStids(stids);

        //2ª ORDENACIÓN de stids: Después de aplicar la ordenación previa se ordena por prioridad de preferedBlocks(es decir, que las opciones que tengan en cuenta bloques preferentes, se posicionarán primero,
        //incluso por encima de aquellas opciones que tengan mayor número de estudiantes pero no posean preferedBlocks(ver ejemplo de foto tomada)):
        sortStidsByPriority(stids, sec, c, r);

        //3ª ORDENACIÓN de stids: Después de aplicar la segunda ordenación se aplica el orden por estudiante(NO POR CANTIDAD DE ESTUDIANTE, sino por estudiante).
        //Es decir, no se cambia ya el orden de las opciones de stids, sino el orden de los estudiantes del array dentro de la tupla stids):
        sortStudentsByPatron(stids, hashStudents_cantPatrones);

        int lastTeacher = -1;
        int lastStudent = -1;
        int lastRoom = -1;
        int i = 0;

        //recorro la lista de conjuntos y la de profesores
        ArrayList<Teacher> teachersForCourse = new ArrayList<>();
        ArrayList<Room> roomForCourse = new ArrayList<>();
        //  teachersOrderByPriority = teachers;
//Ahora con profesores: en el caso de profesores sólo se cogen en función del curso y número de sección:
        // teachersForCourse = TeachersCourse(teachers, teachersForCourse, c);
        teachersForCourse = CompararTeachers(c, teachersForCourse, r, currentSec.getNumSeccion() - 1);
//Con lo siguiente se quitan los ; parentesis y otros caracteres, para almacenar todo en el hashmap roomsTemplate(
//con key: id de los template, y value: los ids de rooms que tienen asignado a determinado id de template )
        if (teachersForCourse.isEmpty()) {
            Consultas cs = new Consultas(templateId, TAMX, TAMY);
            teachersForCourse.add(cs.teacherDefault(TAMX, TAMY));
            teachersForCourse.get(0).setName("No found in template courses");
        }
        boolean exito = false;
        try {
            if (c.isBalanceTeachers() && teachersForCourse.size() > 1) {
                Collections.sort(teachersForCourse, new CustomComparatorTeacher());
            }
        } catch (Exception e) {
            e.getMessage();
        }

        for (Teacher t : teachersForCourse) {
//&& (currentSec.studentsCompatibles(stids.get(i).y) || currentSec.idStudents.isEmpty())
            if (!r.isActiveRooms()) {
//Recorridos en función de las opciones compatibles y una variable llamada exito que permite que cuando se han asignado los profesores, salga del bucle:       
                while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
                    if (!exito && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                            && t.patronCompatibleEB(sec.get(stids.get(i).x), t.getExcludeBlocks())) {
//Recorridos a todos los profesores: dentro se evalúan las restricciones de los profesores, las asignaturas que pueden cursar y si tienen un patrón compatible con las opciones disponibles:       
//teachersForCourse son los estudiantes que estan asignados al curso en concreto, mas el default (no incluye los profesores asignados a cada seccion):

//Si se dan esas condiciones para un profesor en concreto, primero se asigna a k el número de estudiantes de la sección actual (en el curso con id=1245, primera seccion
//hay 6 estudiantes)
                        int k = currentSec.getIdStudents().size();
                        lastTeacher = i;
                        //VAMOS A TENER QUE EN CUENTA CUANDO YA ESTAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
                        //EL PATTERN PARA LOS ESTUDIANTES YA MATRICULADOS CUMPLEN ESA RESTRICCION

//El siguiente for se aplica en el caso de que el lockEnrollment esté desactivado en la sección en concreto (cuando se estaban asignando los noAsign
//se podía acceder a generatePattern a través de la primera y tercera opción. El siguiente for es al que se entra si el lockEnrollment estaba desactivado):
//También tiene en cuenta los estudiantes que no han sido asignados todavía a ninguna sección:
//(No se aplica lo siguiente, se evalua el nº deestudiantes fuera del generate Pattern)Se definen variables temporales para guardar info de los estudiantes. Si llegan al minimo de estudiantes, se aplica en esta seccion posteriormente. Si no, se quedan no asignados para 
//manejarlos despues de los 4 if:
                        ArrayList<Integer> J = new ArrayList<>();
                        for (Integer j : stids.get(i).y) { // studiantes
                            if ((k < minStudentSection) && !idsAsignados.contains(j)
                                    && students.get(j).patronCompatible(sec.get(stids.get(i).x)) && !currentSec.lockEnrollment) {
                                J.add(j);

                                k++;
                                lastStudent = i;
                            }
                        }
                        //una vez que ya hay estudiantes asignados a esta seccion ocupamos el hueco en el teacher
                        //y añadimos la seccion a la tabla del curso.
//El siguiente if: k>0 se refiere a que si por lo menos hay un estudiante (si la sección se ha rellenado). En ese caso se cargan teachers en función de la sección actual:
//Si entra en este if, se sale del while finalmente(si i es mayor o igual que stids.size()), pero si no llega a entrar, da vueltas hasta que llegue a entrar (es decir, hasta que haya rellenado la sección con alumnos)
                        if (J.isEmpty() && k < minStudentSection) {

                        }
                        if (k >= minStudentSection || currentSec.lockEnrollment) { // se llena los huecos de ese profesor incluyendole la seccion:
                            for (Integer student : currentSec.idStudents) {
                                if (students.containsKey(student)) {
                                    students.get(student).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                }
                            }
                            for (Integer j : J) {
                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                currentSec.addStudent(j);
                            }
                            currentSec.setPatronUsado(sec.get(stids.get(i).x));

                            currentSec.setIdTeacher(t.getIdTeacher());
                            if (t.getIdTeacher() != 0) {
                                Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                t_Aux.ocuparHueco(currentSec.getPatronUsado(), c.getIdCourse() * 100 + currentSec.getNumSeccion());
                                t_Aux.incrementarNumSecciones();
                                if (teachers_numSections.containsKey(t.getIdTeacher())) {
                                    teachers_numSections.put(t.getIdTeacher(), teachers_numSections.get(t.getIdTeacher()) + 1);
                                }
                                currentSec.setTeacher(t_Aux);
                            }

                            currentSec.setGender(genderStids.get(stids.get(i).x));
                            c.ocuparHueco(currentSec.getPatronUsado(), currentSec.getNumSeccion());
                            currentSec.setLockSchedule(true);
                            if (k == c.getMaxChildPerSection()) {
                                currentSec.setLockEnrollment(true);
                            }
                            exito = true;
                        }

                    }
                    i++;
                }

            } //ESTE ELSE ES IGUAL QUE EL IF, PERO TENIENDO EN CUENTA LOS ACTIVE ROOM(SOLO SE AÑADE UN FOR DE ROOMS PARA TENERLO EN CUENTA AL GENERAR ALGORITMO)
            else {

                roomForCourse = CompararRooms(rooms, roomForCourse, c, currentSec, templateId, r.groupRooms, r.aviso, schoolCode);
                if (roomForCourse.isEmpty()) {
                    roomForCourse.add(new Room());
                    if (!r.rooms.containsKey(0)) {
                        r.rooms.put(0, new Room());
                    }

                }
                while (i < stids.size() && !exito) { // recorrido a los bloques disponibles

                    for (int contR = 0; contR < roomForCourse.size(); contR++) {
                        if (r.rooms.get(roomForCourse.get(contR).getRoomid()).patronCompatible(sec.get(stids.get(i).x))) {
//                   

                            if (!exito && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                                    && t.patronCompatible(sec.get(stids.get(i).x))) {
//Si se dan esas condiciones para un profesor en concreto, primero se asigna a k el número de estudiantes de la sección actual (en el curso con id=1245, primera seccion
//hay 6 estudiantes)
                                int k = currentSec.getIdStudents().size();
                                lastTeacher = i;
                                // VAMOS A TENER QUE EN CUENTA CUANDO YA ESTAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
                                //  EL PATTERN PARA LOS ESTUDIANTYES YA MATRICULADOS CUMPLEN ESA RESTRICCION

//El siguiente for se aplica en el caso de que el lockEnrollment esté desactivado en la sección en concreto (cuando se estaban asignando los noAsign
//se podía acceder a generatePattern a través de la primera y tercera opción. El siguiente for es al que se entra si el lockEnrollment estaba desactivado):
//También tiene en cuenta los estudiantes que no han sido asignados todavía a ninguna sección:
                                ArrayList<Integer> J = new ArrayList<>();
                                for (Integer j : stids.get(i).y) { // studiantes
                                    if ((k < minStudentSection) && !idsAsignados.contains(j)
                                            && students.get(j).patronCompatible(sec.get(stids.get(i).x)) && !currentSec.lockEnrollment) {
                                        J.add(j);

                                        k++;
                                        lastStudent = i;
                                    }
                                }

                                //una vez que ya hay estudiantes asignados a esta seccion ocupamos el hueco en el teacher
                                //y añadimos la seccion a la tabla del curso.
//El siguiente if: k>0 se refiere a que si por lo menos hay un estudiante (si la sección se ha rellenado). En ese caso se cargan teachers en función de la sección actual:
//Si entra en este if, se sale del while finalmente(si i es mayor o igual que stids.size()), pero si no llega a entrar, da vueltas hasta que llegue a entrar (es decir, hasta que haya rellenado la sección con alumnos)
                                if (k >= minStudentSection || currentSec.lockEnrollment) { // se llena los huecos de ese profesor y room incluyendole la seccion:
                                    for (Integer student : currentSec.idStudents) {
                                        if (students.containsKey(student)) {
                                            students.get(student).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                        }
                                    }
                                    for (Integer j : J) {
                                        idsAsignados.add(j);
                                        students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                        currentSec.addStudent(j);
                                    }

                                    currentSec.setPatronUsado(sec.get(stids.get(i).x));

                                    currentSec.setIdTeacher(t.getIdTeacher());
                                    if (t.getIdTeacher() != 0) {
                                        Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                        t_Aux.ocuparHueco(currentSec.getPatronUsado(), c.getIdCourse() * 100 + currentSec.getNumSeccion());
                                        t_Aux.incrementarNumSecciones();
                                        if (teachers_numSections.containsKey(t.getIdTeacher())) {
                                            teachers_numSections.put(t.getIdTeacher(), teachers_numSections.get(t.getIdTeacher()) + 1);
                                        }
                                        currentSec.setTeacher(t_Aux);
                                    } else {
                                        currentSec.setTeacher(t);
                                    }

                                    if (rooms_numSections.containsKey(roomForCourse.get(contR).getRoomid())) {
                                        rooms_numSections.put(roomForCourse.get(contR).getRoomid(), rooms_numSections.get(roomForCourse.get(contR).getRoomid()) + 1);
                                    }
                                    currentSec.setRoom(roomForCourse.get(contR));
                                    currentSec.setIdRoom(roomForCourse.get(contR).getRoomid());
                                    r.rooms.get(roomForCourse.get(contR).getRoomid()).ocuparHueco(c.getIdCourse(), currentSec.getNumSeccion(), currentSec.getPatronUsado());

                                    currentSec.setGender(genderStids.get(stids.get(i).x));
                                    c.ocuparHueco(currentSec.getPatronUsado(), currentSec.getNumSeccion());
                                    currentSec.setLockSchedule(true);
                                    if (k == c.getMaxChildPerSection()) {
                                        currentSec.setLockEnrollment(true);
                                    }
                                    exito = true;
                                }
                            }

                        }

                    }
                    i++;

                }
            }
        }
        if (currentSec.getIdStudents().size() < minStudentSection || exito == false) {
            aviso.addAvisoMinStudents(c.getNameCourse(), currentSec.getNameSeccion());
        }
        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.

//El siguiente if es por si no hay posibilidad de asignación de estudiantes(se devuelve el ret, que es el número de estudiantes noAsign que va a haber después de pasar las
//4 opciones de asignación en función de l lockSchedule y el lockEnrollment). SE TIENE EN CUENTA EL NÚMERO DE IDS ASIGNADOS TOTALES EN EL CURSO (ES ACUMULATIVO DE UNA SECCIÓN A OTRA):
//Este if también asigna los log(comentarios) que avisan de la no asignación (el log se pasaba como parámetro en el generatePattern)
//Por último el if también tiene en cuenta los profesores que no hay asignados al curso, y los que no tienen huecos compatibles. En caso de que no se cumpla ninguna de estas dos 
//posibilidades, es que no se han podido asignar estudiantes):
        if (idsAsignados.size() != studentsCourse.size()) {

            ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for (Integer teacher : c.getTrestricctions()) {
                tname += r.cs.fetchName(teacher) + " ;";
            }
            if (tname.length() > 2) {
                tname = tname.substring(0, tname.length() - 1);
            }
//Primero analiza si no profesores asignados al curso. En el caso de que si que haya, comprueba si los profesores tienen huecos compatibles o no (es decir, si no han entrado
//al for de teachers, ya que lastTeacher coge el último i de este for. La última opción es el else, en el que es al revés: los estudiantes no tienen secciones disponibles, y este
//es el dato que se va a retornar en ret. Si no se diera esta última opción, ret =0 por lo que se habrían asignado todos los estudiantes al curso).            
            if (c.getTrestricctions().isEmpty()) {
                Log.add("-No hay profesores asignados al curso:" + r.cs.nameCourse(c.getIdCourse()));
            } else if (lastTeacher <= lastStudent) {
                Log.add("-Los profesores " + tname + " asignados al curso:" + r.cs.nameCourse(c.getIdCourse()) + " no tienen disponible ningun hueco compatible");
            } else {//lastStudent<lastTeacher
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso " + r.cs.nameCourse(c.getIdCourse()) + ":");
            }
//Este es el for que recoge los estudiantes no asignados finalmente:            
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }

        }
        return ret;

    }

    private void assignPatternToSection(Restrictions r, Course c, Seccion seccionCourse, ArrayList<ArrayList<Tupla>> sec, Course courseAsoc) {

        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> studentsCourse = seccionCourse.getIdStudents();
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
        Seccion currentSec = courseAsoc.getLastSeccion();
        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            for (Integer j : studentsCourse) {
                if (r.students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                    hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);

                }
            }
        }

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());

        } catch (Exception e) { // esto da errores aveces solucionar comparador
            e.getMessage();
        }

        int lastTeacher = -1;
        int lastStudent = -1;
        int lastRoom = -1;
        int i = 0;

        //recorro la lista de conjuntos y la de profesores
        ArrayList<Teacher> teachersForCourse = new ArrayList<>(); 
        for (int y = 0; y < r.teachers.size(); y++) {
            if (c.getTrestricctions().contains(r.teachers.get(y).getIdTeacher())) {
                teachersForCourse.add(r.teachers.get(y));
            }
        }
        boolean exito = false;

        while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
            int contR = 0;
            while (!r.isActiveRooms() || contR < c.getRooms().size()) {
                if (!r.isActiveRooms() || (r.rooms.containsKey(c.getRooms().get(contR)) && r.rooms.get(c.getRooms().get(contR)).patronCompatible(sec.get(stids.get(i).x)))) {

                    for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                        if (!exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                                && t.patronCompatible(sec.get(stids.get(i).x))) { 
                            int k = currentSec.getIdStudents().size();
                            lastTeacher = i;
                            // VAMOS A TENER QUE EN CUENTA CUANDO YA ESTAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
                            //  EL PATTERN PARA LOS ESTUDIANTYES YA CMATRICULADOS CUMPLEN ESA RESTRICCION
                            for (Integer j : stids.get(i).y) { // studiantes

                                r.students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                k++;
                                lastStudent = i;

                            }
                            //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                            //y añadimos la seccion a la tabla del curso.
                            // se llena los huecos de ese profesor incluyendole la seccion

                            currentSec.setPatronUsado(sec.get(stids.get(i).x));

                            currentSec.setIdTeacher(t.getIdTeacher());
                            Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                            t_Aux.ocuparHueco(currentSec.getPatronUsado(), courseAsoc.getIdCourse() * 100 + currentSec.getNumSeccion());
                            t_Aux.incrementarNumSecciones();

                            currentSec.setTeacher(t_Aux);
                            currentSec.setGender(seccionCourse.getGender());
                            courseAsoc.ocuparHueco(currentSec.getPatronUsado(), currentSec.getNumSeccion());
                            currentSec.setLockSchedule(true);
                            if (k == c.getMaxChildPerSection()) {
                                currentSec.setLockEnrollment(true);
                            }
                            exito = true;

                        }
                    }
                }
            }
            i++;
        }
    }

    private ArrayList<Integer> getSortIdsStudent(ArrayList<Integer> idsStudents, HashMap<Integer, String> hashPersons) {
        ArrayList<Tupla> auxTupla = new ArrayList<>();
        ArrayList<Integer> idsSorted = new ArrayList<>();

        for (int i = 0; i < idsStudents.size(); i++) {
            auxTupla.add(new Tupla(idsStudents.get(i), hashPersons.get(idsStudents.get(i))));
        }
        Collections.sort(auxTupla, new Comparator<Tupla>() {
            @Override
            public int compare(Tupla o1, Tupla o2) {
                return ("" + o1.y).compareTo("" + o2.y);
            }
        });

        for (int i = 0; i < auxTupla.size(); i++) {
            idsSorted.add((Integer) auxTupla.get(i).x);
        }

        return idsSorted;
    }

    private void sortSections(ArrayList<Seccion> s, HashMap<Integer, String> hashPersons) {
        ArrayList<Seccion> auxSecciones = new ArrayList<>();
        ArrayList<Seccion> sAux = (ArrayList<Seccion>) s.clone();
        while (!sAux.isEmpty()) {
            int posMin = getMinSeccion(sAux);
            Seccion auxS = new Seccion(sAux.get(posMin));
            auxS.setIdStudents(getSortIdsStudent(auxS.getIdStudents(), hashPersons));
            auxSecciones.add(auxS);
            sAux.remove(posMin);
        }
        for (int i = 0; i < auxSecciones.size(); i++) {
            s.set(i, new Seccion(auxSecciones.get(i)));
        }
    }

    private int getMinSeccion(ArrayList<Seccion> s) {
        int pos = -1;
        int i = 0;
        int numMin = 99999;
        do {
            if (s.get(i).getNumSeccion() < numMin) {
                numMin = s.get(i).getNumSeccion();
                pos = i;
            }
            i++;
        } while (i < s.size());

        return pos;
    }

    private void sortSectionsAndStudents(ArrayList<Course> c, HashMap<Integer, String> hashPersons) {
        for (int i = 0; i < c.size(); i++) {
            sortSections(c.get(i).getArraySecciones(), hashPersons);
        }
    }
//Este método se carga en el algoritmo para la carga de restricciones de secciones en cada curso concreto:

    private ArrayList<Seccion> chargeArraySections(Restrictions r, Course course) {
        ArrayList<Seccion> aux = new ArrayList<>();
        if (r.mapSecciones.containsKey(course.getIdCourse())) {
            for (int i = 0; i < r.mapSecciones.get(course.getIdCourse()).size(); i++) {
                Seccion sAux = new Seccion(r.mapSecciones.get(course.getIdCourse()).get(i));
                aux.add(sAux);
            }
        }
        return aux;
    }
//ocuparHuecco--> (array(tupla),idcourse y seccion-->se multiplica por 100 y se añade el numero de seccion para que los ultimos dos ceros del numero sean la seccion
//ejemplo ficticio: id curso 500, numero de seccion 25: el proceso seria 500*100+25=50025, donde los 3 priemros digitos es del curso y los ultimos 2 del numero de seccion)
//Lo que hace este método es:
//para cada seccion conjuntamente con cada idStudents, se obtiene el patron usado(ya está asignado en RenWeb, por seccion) y el id de curso/seccion:
//Resumen: se rellena en un hueco concreto de cada estudiante: el id de curso+seccion(para luego en otro método obtener los dos por separado). Esto se carga así porque tiene en cuenta 
//todos los cursos:

    private void chargeStatusStudents(Restrictions r, Course c) {
        for (int i = 0; i < c.getArraySecciones().size(); i++) {
            for (int j = 0; j < c.getArraySecciones().get(i).getIdStudents().size(); j++) {

                Integer idStudent = c.getArraySecciones().get(i).getIdStudents().get(j);
                Integer secNum = c.getArraySecciones().get(i).getNumSeccion();
                r.students.get(idStudent).ocuparHueco(c.getArraySecciones().get(i).getPatronUsado(), c.getIdCourse() * 100 + secNum);
            }
        }
    }

//Aquí es donde entra cuando está bloqueado el lockSchedule pero está desactivado el lockEnrollment
//El patrón está asignado y bloqueado (no se puede usar otro), por lo que no hay necesidad de generación de patrones:   
    private ArrayList<Integer> fillSection(Seccion currentSec, Restrictions r, Course c, ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students, ArrayList<Teacher> teachers, String templateId, HashMap<Integer, Room> rooms, Exceptions aviso, String schoolCode) {

        int minStudentSection = c.getMinChildPerSection();

//Aquí al principio se declaran los mismos arrays que en generatePattern_Section:
//stids=(tupla de dos partes: 1ºids de seccion, 2º se va a almacenar posteriormente array estudiantes(ids, por eso es Integer) COMPATIBLES) La razón por la que no es un hash es que se va a necesitar
//una búsqueda secuencial por ids:      
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        ArrayList<Integer> ret = new ArrayList<>();
        HashMap<Integer, String> genderStids = new HashMap<>();
//Se obtiene el id de los estudiantes de la sección concreta.
//Estos ids van siendo cada vez más según se van recorriendo las secciones (fuera del fillSection). Ejemplo ficticio: en la primera vuelta tiene 6 estudiantes,
//pero en las siguientes vueltas tiene 12, 18...        
        idsAsignados = c.getAllIds();
//Se aplica el chargeStatusStudents(con restricciones y curso concretos):    
//Lo que hace este método es:
//para cada seccion conjuntamente con cada idStudents, se obtiene el patron usado(ya está asignado en RenWeb, por seccion) y el id de curso/seccion:
//Resumen: se aplica el patron usado (tupla de coordenadas) a cada estudiante asignado de cada seccion(tiene en cuenta los rosters), teniendo en cuenta restrcciones y curso.  
        chargeStatusStudents(r, c);

        ArrayList<Teacher> teachersForCourse = new ArrayList<Teacher>();
        ArrayList<Room> roomsForCourse = new ArrayList<Room>();
//Con el siguiente método se capturan los profesores que están asignados en el curso (incluyendo el default teacher)  y que están disponibles:
        //   teachersForCourse = TeachersCourse(teachers, teachersForCourse, c);

//Este hash almacena la cantidad de opciones válidas para cada estudiante(idstudiante, cantidad opciones válidas)
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
//initHashStudents hace que se inicialicen a 0 los estudiantes para sumarlos de 1 en 1:        
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
//Si el curso no es rellenado por restricciones de genero (luego se hace equilibrado):  

        if (!c.isGR()) {
            for (int i = 0; i < c.getArraySecciones().size(); i++) {
//Si hay estudiantes asignados en el roster de la seccion, se clonan desde auxStids a stids.                
                ArrayList<Integer> auxStids = c.getArraySecciones().get(i).getIdStudents();
                stids.add(new Tupla(i, auxStids.clone()));
                for (Integer j : studentsCourse) {

//Se van añadiendo estudiantes que no estén asignados y que sean compatibles con el patron que proviene de la seccion:
//A stids se añaden, para cada seccion, los estudiantes que son compatibles con el patrón de dicha seccion.
//A hashStudents_cantPatrones se añade en key: el id del student, en value: la cantidad de patrones compatibles que tiene el estudiante + 1 (el que se está asignando en este momento):
//Cuanta más cantidad de patrones tenga un estudiante, más posibilidades de ser asignado frente a otros, por lo que los que más tengan, deben ordenarse posteriormente de forma que sean los últimos en asignarse a secciones:
                    if (!idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(i).getPatronUsado())) {
                        stids.get(i).y.add(j);
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
            }

            equilibrarGender(stids, students);
        } //fillSection con restricciones de genero:        
        else {

            for (int i = 0; i < c.getArraySecciones().size(); i++) {
                ArrayList<Integer> stdMales = new ArrayList<>();
                ArrayList<Integer> stdFemales = new ArrayList<>();
                ArrayList<Integer> auxStids = c.getArraySecciones().get(i).getIdStudents();
                stids.add(new Tupla(i, auxStids.clone()));
                for (Integer j : studentsCourse) {
                    if (!idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(i).getPatronUsado())) {
                        if (students.get(j).getGenero().equals("Female")) {
                            stdFemales.add(j);
                        } else { //male or null
                            stdMales.add(j);
                        }
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
                if (stdMales.size() >= stdFemales.size()) {
                    stids.set(i, new Tupla(i, new ArrayList<>(stdMales)));
                    genderStids.put(i, "Male");
                } else {
                    stids.set(i, new Tupla(i, new ArrayList<>(stdFemales)));
                    genderStids.put(i, "Female");
                }
            }

        }

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        stids.sort(new CompConjuntos());
        //Y ordena por patrones compatibles por estudiantes:    
        sortStudentsByPatron(stids, hashStudents_cantPatrones);

        int i = 0;
        int lastTeacher = -1;
        int lastStudent = -1;
        boolean exito = false;

        teachersForCourse = CompararTeachers(c, teachersForCourse, r, currentSec.getNumSeccion() - 1);
        if (teachersForCourse.isEmpty()) {
            Teacher t = new Teacher();
            Consultas cs = new Consultas(templateId, TAMX, TAMY);
            t = cs.teacherDefault(TAMX, TAMY);
            teachersForCourse.add(t);
        }

        try {
            if (c.isBalanceTeachers() && teachersForCourse.size() > 1) {
                Collections.sort(teachersForCourse, new CustomComparatorTeacher());
            }
        } catch (Exception e) {
            e.getMessage();
        }

        for (Teacher t : teachersForCourse) {
            while (i < stids.size() && !exito) {
                if (!exito && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                        && t.patronCompatible(c.getArraySecciones().get(stids.get(i).x).getPatronUsado())) {
                    if (c.getArraySecciones().get(stids.get(i).x).getNumSeccion() == currentSec.getNumSeccion()) {
                        int k = c.getArraySecciones().get(stids.get(i).x).getIdStudents().size();
                        ArrayList<Integer> J = new ArrayList<>();
                        for (Integer j : stids.get(i).y) { // studiantes
                            if ((k < minStudentSection) && !idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(stids.get(i).x).getPatronUsado())) {
                                J.add(j);

                                k++;
                                lastStudent = i;
                            }
                        }

                        if (k >= minStudentSection) { // se llena los huecos de ese profesor incluyendole la seccion                
                            for (Integer j : J) {
                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                                c.getArraySecciones().get(stids.get(i).x).addStudent(j);
                            }
                            c.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                            c.getArraySecciones().get(stids.get(i).x).setLockSchedule(true);
                            if (k == c.getMaxChildPerSection()) {
                                c.getArraySecciones().get(stids.get(i).x).setLockEnrollment(true);
                            }

                            //Con este método se coge un teacher del curso si no hay asignado ninguno en la seccion:                                                         
                            t.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                            t.incrementarNumSecciones();
                            currentSec.setIdTeacher(t.getIdTeacher());
                            c.getArraySecciones().get(stids.get(i).x).setTeacher(t);

                            //Con este método se coge un room del curso si no hay asignado ninguna en la seccion:
                            Room r_Aux = new Room();
                            if (r.isActiveRooms()) {
                                roomsForCourse = CompararRooms(rooms, roomsForCourse, c, currentSec, templateId, r.groupRooms, r.aviso, schoolCode);
                                if (roomsForCourse.size() > 0) {
                                    r_Aux = roomsForCourse.get(0);
                                    r.rooms.get(r_Aux.getRoomid()).ocuparHueco(c.getIdCourse(), c.getArraySecciones().get(i).getNumSeccion(), c.getArraySecciones().get(i).getPatronUsado());
                                    r.rooms.get(r_Aux.getRoomid()).incrementarNumSecciones();
                                    currentSec.setIdRoom(r_Aux.getRoomid());
                                    c.getArraySecciones().get(stids.get(i).x).setRoom(r.rooms.get(r_Aux.getRoomid()));
                                }

                            }
                            exito = true;
                        }
                    }
                }
                i++;
            }
        }
        if (currentSec.getIdStudents().size() < minStudentSection || exito == false) {
            aviso.addAvisoMinStudents(c.getNameCourse(), currentSec.getNameSeccion());
        }
//Esto funciona igual que en generatePattern:
        if (idsAsignados.size() != studentsCourse.size()) {
            ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for (Integer teacher : c.getTrestricctions()) {
                tname += r.cs.fetchName(teacher) + " ,";
            }
            if (tname.length() > 2) {
                tname = tname.substring(0, tname.length() - 1);
            }
            if (c.getTrestricctions().isEmpty()) {
                Log.add("-No hay profesores asignados al curso:" + r.cs.nameCourse(c.getIdCourse()));
            } else if (lastTeacher <= lastStudent) {
                Log.add("-Los profesores " + tname + " asignados al curso:" + r.cs.nameCourse(c.getIdCourse()) + " no tienen disponible ningun hueco compatible");
            } else {
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso " + r.cs.nameCourse(c.getIdCourse()) + ":");
                String anadir = "";
                ArrayList<ArrayList<Tupla>> aux = null;
                for (Integer i2 : ret) {
                    anadir += students.get(i2).getName() + ",";
                    if (aux == null) {
                                 //**************************************//
                   //****************************************//
                    aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log, r.aviso));
                    //CAMBIO DE OPCIONES
                    //aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks));
                    //**************************************//
                   //****************************************// 
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);
                Log.add(anadir);
            }
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }
        }
        return ret;
    }

    private void saveXML_FTP(String yearId, String templateId, String schoolCode, Restrictions r) {
        String server = "192.168.1.36";
        int port = 21;
        String user = "david";
        String pass = "david";
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            String fecha = dateFormat.format(date);
            fecha = fecha.replace(" ", "_");
            fecha = fecha.replace("/", "_");
            fecha = fecha.replace(":", "_");
            String filename = yearId + "_" + templateId + "_" + fecha + ".xml";
            String rutaCarpeta = "/Schedules/" + schoolCode;

            if (!ftpClient.changeWorkingDirectory(rutaCarpeta));
            {
                ftpClient.changeWorkingDirectory("/Schedules");
                ftpClient.mkd(schoolCode);
                ftpClient.changeWorkingDirectory(schoolCode);
            }

            icBuilder = icFactory.newDocumentBuilder();
            Document doc = icBuilder.newDocument();
            Element mainRootElement = doc.createElementNS("http://eduwebgroup.ddns.net/ScheduleWeb/enviarmensaje.htm", "Horarios");
            doc.appendChild(mainRootElement);
            Element students = doc.createElement("Students");

            for (Course t : r.courses) {
                for (int j = 0; j < t.getArraySecciones().size(); j++) {
                    for (int k = 0; k < t.getArraySecciones().get(j).getIdStudents().size(); k++) {
                        students.appendChild(getStudent(doc, "" + t.getArraySecciones().get(j).getIdStudents().get(k), "" + t.getIdCourse(), "" + (j + 1), yearId, "" + t.getArraySecciones().get(j).getClassId()));
                    }
                }
            }

            Element cursos = doc.createElement("Courses");
            for (Course t : r.courses) {
                for (int j = 1; j < t.getArraySecciones().size(); j++) {
                    cursos.appendChild(getCursos(doc, "" + t.getIdCourse(), "" + j, "" + t.getArraySecciones().get(j).getIdTeacher(), yearId, "" + t.getArraySecciones().get(j).getClassId()));
                }
            }

            Element bloques = doc.createElement("Blocks");
            for (Course t : r.courses) {
                for (int i = 0; i < t.getArraySecciones().size(); i++) {
                    for (int j = 0; j < t.getArraySecciones().get(i).getPatronUsado().size(); j++) {
                        int col = (int) t.getArraySecciones().get(i).getPatronUsado().get(j).x + 1;
                        int row = (int) t.getArraySecciones().get(i).getPatronUsado().get(j).y + 1;

                        bloques.appendChild(getBloques(doc, "" + (col), "" + (row), templateId, "" + t.getIdCourse(), "" + t.getArraySecciones().get(i).getNumSeccion(), yearId, "" + t.getArraySecciones().get(i).getClassId()));
                    }
                }
            }

            mainRootElement.appendChild(students);
            mainRootElement.appendChild(cursos);
            mainRootElement.appendChild(bloques);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            ftpClient.storeFile(filename, is);
            ftpClient.logout();

        } catch (Exception ex) {
            ex.getMessage();
        }

    }

    private Node getStudent(Document doc, String name, String courseId, String section, String yearId, String classID) {
        Element company = doc.createElement("Student");
        company.appendChild(getCompanyElements(doc, company, "student_ID", name));
        company.appendChild(getCompanyElements(doc, company, "course_ID", courseId));
        company.appendChild(getCompanyElements(doc, company, "section", section));
        company.appendChild(getCompanyElements(doc, company, "YearID", yearId));
        company.appendChild(getCompanyElements(doc, company, "classID", classID));
        return company;
    }

    private Node getCursos(Document doc, String courseId, String section, String idTeacher, String yearId, String classID) {
        Element company = doc.createElement("Course");
        company.appendChild(getCompanyElements(doc, company, "course_ID", courseId));
        company.appendChild(getCompanyElements(doc, company, "section", section));
        company.appendChild(getCompanyElements(doc, company, "teacher_ID", idTeacher));
        company.appendChild(getCompanyElements(doc, company, "YearID", yearId));
        company.appendChild(getCompanyElements(doc, company, "classID", classID));
        return company;
    }

    private Node getBloques(Document doc, String day, String begin, String tempId, String courseId, String section, String yearId, String classID) {
        Element company = doc.createElement("Block");
        company.appendChild(getCompanyElements(doc, company, "day", day));
        company.appendChild(getCompanyElements(doc, company, "begin", begin));
        company.appendChild(getCompanyElements(doc, company, "template_ID", tempId));
        company.appendChild(getCompanyElements(doc, company, "course_ID", courseId));
        company.appendChild(getCompanyElements(doc, company, "section", section));
        company.appendChild(getCompanyElements(doc, company, "YearID", yearId));
        company.appendChild(getCompanyElements(doc, company, "classID", classID));

        return company;
    }

    private Node getCompanyElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

    private class CompConjuntos implements Comparator<Tupla<Integer, ArrayList<Integer>>> {

        @Override
        public int compare(Tupla<Integer, ArrayList<Integer>> e1, Tupla<Integer, ArrayList<Integer>> e2) {
            try {
                if (e1.y.size() < e2.y.size()) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception e) {
                e.getMessage();
                return -1;
            }

        }
    }

    private class CompSeccionesStudents implements Comparator<Seccion> {

        @Override
        public int compare(Seccion o1, Seccion o2) {
            return o1.getNumStudents() - o2.getNumStudents();
        }

    }

    private void deleteInStids(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, int pos) {
        for (int i = 0; i < stids.size(); i++) {
            if (stids.get(i).getX() == pos) {
                stids.remove(i);
            }
        }
    }

    private void updateStidsWithUserDefined(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<ArrayList<Boolean>> totalBlocks) { // se encarga de descartar las filas que han sido bloqueadas desde confiuration school
        int contador = 0;
        for (int i = 0; i < totalBlocks.size(); i++) {
            if (!totalBlocks.get(i).isEmpty()) {
                for (int j = 0; j < totalBlocks.get(i).size(); j++) {
                    if (!totalBlocks.get(i).get(j)) {
                        deleteInStids(stids, contador);
                    }
                    contador++;
                }
            }
        }
    }

    private int buscarPosBlock(int x, int y, ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<ArrayList<Tupla>> sec) {
        int maxAlumnos = stids.get(0).getY().size();

        for (int i = 0; i < stids.size(); i++) {
            for (int j = 0; j < sec.get(0).size(); j++) {
                if (((Integer) sec.get(stids.get(i).x).get(j).x == x && (Integer) sec.get(stids.get(i).x).get(j).y == y)
                        || ((Integer) sec.get(stids.get(i).x).get(j).x == y && (Integer) sec.get(stids.get(i).x).get(j).y == x)
                        && stids.get(i).y.size() >= maxAlumnos) {
                    return i;
                }
            }

        }

        return -1;
    }

    private void sortStidsByPriority(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<ArrayList<Tupla>> sec, Course c, Restrictions r) {

        if ((c.getSections() <= c.getPreferedBlocks().size()) && c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
            ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
            ArrayList<Integer> auxRes = new ArrayList<>();

            for (int i = 0; i < c.getPreferedBlocks().get(c.getSections() - 1).size(); i++) {
                Tupla<Integer, ArrayList<Integer>> tuplaAux = new Tupla(stids.get(i).x, stids.get(i).y);
                int res = buscarPosBlock((c.getPreferedBlocks().get(c.getSections() - 1).get(i).x) - 1, (c.getPreferedBlocks().get(c.getSections() - 1).get(i).y) - 1, stids, sec);
                if (res != -1) {
                    auxRes.add(res);
                    auxStids.add(tuplaAux);

                }

            }

            for (int i = 0; i < stids.size(); i++) {
                if (!auxRes.contains(i)) {
                    auxStids.add(stids.get(i));
                }
            }
            stids = auxStids;
        }
    }

    private void initHashStudents(HashMap<Integer, Integer> hashStudents, ArrayList<Integer> students) {
        for (Integer student : students) {
            hashStudents.put(student, 0);
        }
    }

    private ArrayList<Integer> studentSections_2(Restrictions r, ArrayList<Teacher> teachers, Course c, int minsections, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Integer> studentsCourseSection, HashMap<Integer, Student> students, ArrayList<Integer> rooms) {

        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();

        initHashStudents(hashStudents_cantPatrones, studentsCourse);
        //Crea una lista con conjuntos de estudiantes compatibles con cada seccion
        //disponible del curso.
        // aqui es donde se tendra que modificar por donde se comenzara a buscar las posiciones del patron
        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            for (Integer j : studentsCourse) {
                if (students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                    hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                }

            }
        }

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());
        } catch (Exception e) {
            e.getMessage();
        }
        sortStidsByPriority(stids, sec, c, r);

        ArrayList<Integer> diferencia;
        if (!stids.isEmpty()) {
            diferencia = stids.get(0).y;
        } else {
            diferencia = new ArrayList();
        }
        int lastTeacher = -1;
        int lastStudent = -1;
        int i = 0;
        int numSeccion = 0; // indicara numeros de seccion se iniciara en 0 hasta el n-1 seccion

        //Ordenar por prioridad los teachers si la restricción BalanceTeachers se ha activado para un curso en concreto:
        //(Ordena los profesores en función de los cursos y secciones ocupadas, y los más ocupados los deja en último lugar):
        ArrayList<Teacher> teachersOrderByPriority = new ArrayList<>();
        teachersOrderByPriority = teachers;
        if (c.isBalanceTeachers()) {
            teachersOrderByPriority = sortTeacherByPriorty(teachers, c.getTrestricctions(), c.getMinSections());
        }

        //Recorro la lista de conjuntos y la de profesores:
        while (i < stids.size()) { // recorrido a los bloques disponibles
            for (Teacher t : teachersOrderByPriority) { // recorrido a los teachers  totales

            }
            if (i + 1 < stids.size()) {
                diferencia = conjuntos.diferencia(stids.get(i + 1).y, stids.get(i).y);
            }
            i++;
        }

        c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes
        for (Integer st : idsAsignados) {
            students.get(st).addAsignado(c.getIdCourse());
        }

        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.
        if (idsAsignados.size() != studentsCourse.size()) {
            System.out.println("FAILURE");
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for (Integer teacher : c.getTrestricctions()) {
                tname += r.cs.fetchName(teacher) + " ,";
            }
            if (tname.length() > 2) {
                tname = tname.substring(0, tname.length() - 1);
            }
            if (c.getTrestricctions().isEmpty()) {
                Log.add("-No hay profesores asignados al curso:" + r.cs.nameCourse(c.getIdCourse()));
            } else if (lastTeacher <= lastStudent) {
                Log.add("-Los profesores " + tname + " asignados al curso:" + r.cs.nameCourse(c.getIdCourse()) + " no tienen disponible ningun hueco compatible");
            } else {
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso " + r.cs.nameCourse(c.getIdCourse()) + ":");
                String anadir = "";
                ArrayList<ArrayList<Tupla>> aux = null;
                for (Integer i2 : ret) {
                    anadir += students.get(i2).getName() + ",";
                    if (aux == null) {
                                  //**************************************//
                   //****************************************//
                    aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log, r.aviso));
                    //CAMBIO DE OPCIONES
                    //aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks));
                    //**************************************//
                   //****************************************// 
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                Log.add(anadir);
            }
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }
            return ret;
        }
        return null;
    }

    public class CustomComparatorStudent implements Comparator<Student> {

        @Override
        public int compare(Student o1, Student o2) {
            return o1.getNumPatrones() - o2.getNumPatrones();
        }
    }

    public class CustomComparatorTeacher implements Comparator<Teacher> {

        @Override
        public int compare(Teacher o1, Teacher o2) {

            return o1.getNumSecciones() - o2.getNumSecciones();
        }
    }

    private ArrayList<Student> sortByGender(ArrayList<Student> auxStudents) {
        ArrayList<Student> aux = new ArrayList<>();

        return aux;
    }

    private void sortStudentsByPatron(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Integer> hashStudents_cantPatrones) {
        for (int i = 0; i < stids.size(); i++) {
            ArrayList<Student> auxStudents = new ArrayList<>();
            for (int j = 0; j < stids.get(i).getY().size(); j++) {
                if (hashStudents_cantPatrones.containsKey(stids.get(i).getY().get(j))) {
                    auxStudents.add(new Student(stids.get(i).getY().get(j), hashStudents_cantPatrones.get(stids.get(i).getY().get(j))));
                }
            }
            Collections.sort(auxStudents, new CustomComparatorStudent());
            ArrayList<Integer> auxIds = new ArrayList<>();
            for (int j = 0; j < auxStudents.size(); j++) {
                auxIds.add(auxStudents.get(j).getId());
            }

            stids.set(i, new Tupla(stids.get(i).x, auxIds));

        }
    }

    private ArrayList<Tupla<Integer, ArrayList<Integer>>> dividirPatronesByGender(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Student> students) {
        ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
        ArrayList<Integer> auxStidsMale = new ArrayList<>();
        ArrayList<Integer> auxStidsFemale = new ArrayList<>();

        for (Tupla<Integer, ArrayList<Integer>> stid : stids) {
            auxStidsMale = new ArrayList<>();
            auxStidsFemale = new ArrayList<>();
            for (Integer y : stid.y) {
                if (students.containsKey(y)) {
                    if (students.get(y).getGenero().equals("Male")) {
                        auxStidsMale.add(y);
                    } else {//female
                        auxStidsFemale.add(y);
                    }
                }
            }

            auxStids.add(new Tupla(stid.x, auxStidsFemale.clone()));
            auxStids.add(new Tupla(stid.x, auxStidsMale.clone()));
        }
        return auxStids;
    }

    private void updateStids(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<Integer> idsAsignados, ArrayList<Integer> studentsCourse, Course c, Restrictions r) {
        for (int i = 0; i < stids.size(); i++) {
            ArrayList<Integer> auxStids = new ArrayList<>();
            for (int j = 0; j < stids.get(i).y.size(); j++) {
                if (!idsAsignados.contains(stids.get(i).y.get(j))) {
                    auxStids.add(stids.get(i).y.get(j));
                }
            }
            stids.set(i, new Tupla(stids.get(i).x, auxStids.clone()));
        }
        try {
            stids.sort(new CompConjuntos());

        } catch (Exception e) {
            e.getMessage();
        }

        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
        sortStudentsByPatron(stids, hashStudents_cantPatrones);

    }
//Equilibrado entre hombres y mujeres que se aplica en getPatrones en el caso de que sea una sección en el que no se apliquen restricciones de género:  

    private ArrayList<Tupla<Integer, ArrayList<Integer>>> equilibrarGender(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Student> students) {
        ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
        ArrayList<Integer> auxStidsMale = new ArrayList<>();
        ArrayList<Integer> auxStidsFemale = new ArrayList<>();

        for (Tupla<Integer, ArrayList<Integer>> stid : stids) {
            auxStidsMale = new ArrayList<>();
            auxStidsFemale = new ArrayList<>();
            for (Integer y : stid.y) {

                if (students.containsKey(y)) {
                    if (students.get(y).getGenero().equals("Male")) {
                        auxStidsMale.add(y);
                    } else if (students.get(y).getGenero().equals("Female")) {
                        auxStidsFemale.add(y);
                    } else {
                        auxStidsMale.add(y);
                    }
                }
            }
            int maxLength = Math.max(auxStidsFemale.size(), auxStidsMale.size());
            ArrayList<Integer> auxEquilibrado = new ArrayList<>();

            for (int i = 0; i < maxLength; i++) {
                if (i < auxStidsFemale.size()) {
                    auxEquilibrado.add(auxStidsFemale.get(i));
                }
                if (i < auxStidsMale.size()) {
                    auxEquilibrado.add(auxStidsMale.get(i));
                }
            }

            auxStids.add(new Tupla(stid.x, auxEquilibrado.clone()));
        }

        return auxStids;
    }

    private ArrayList<Integer> studentSections(Restrictions r, ArrayList<Teacher> teachers, Course c, int minSection, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Integer> studentsCourseSection, HashMap<Integer, Student> students, ArrayList<Integer> rooms) {

        int maxStudentSection = c.getMaxChildPerSection();

        if (maxStudentSection == 0) {
            maxStudentSection = CHILDSPERSECTION; // POR DEFECTO
        }

        int numMinStudents_Section = (int) Math.round(maxStudentSection * 0.70);
        ArrayList<Seccion> secciones = new ArrayList<>();
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);

        //Crea una lista con conjuntos de estudiantes compatibles con cada seccion
        //disponible del curso.
        // aqui es donde se tendra que modificar por donde se comenzara a buscar las posiciones del patron
        Boolean equilibrador = false;

        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            for (Integer j : studentsCourse) {
                if (students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                    hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);

                }
            }
        }

        equilibrarGender(stids, students);

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());

        } catch (Exception e) {
            e.getMessage();
        }

        sortStidsByPriority(stids, sec, c, r);
        sortStudentsByPatron(stids, hashStudents_cantPatrones);

        //Inicializo el conjunto de estudiantes seleccionables
        ArrayList<Integer> diferencia;
        if (!stids.isEmpty()) {
            diferencia = stids.get(0).y;
        } else {
            diferencia = new ArrayList();
        }
        int lastTeacher = -1;
        int lastStudent = -1;
        int i = 0;
        int numSeccion = 0; // indicara numeros de seccion se iniciara en 0 hasta el n-1 seccion

        //recorro la lista de conjuntos y la de profesores
        ArrayList<Integer> idsbySeccion = new ArrayList<>();
        ArrayList<Teacher> teachersForCourse = new ArrayList<>();

        for (int y = 0; y < teachers.size(); y++) {
            if (c.getTrestricctions().contains(teachers.get(y).getIdTeacher())) {
                teachersForCourse.add(teachers.get(y));
            }
        }

        while (i < stids.size()) { // recorrido a los bloques disponibles
            for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                if (c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                        && t.patronCompatible(sec.get(stids.get(i).x))
                        && c.getSections() <= c.getMinSections()) {

                    int k = 0;
                    lastTeacher = i;

                    for (Integer j : diferencia) { // studiantes
                        if (((k <= numMinStudents_Section) || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {

                            idsbySeccion.add(j);
                            idsAsignados.add(j);
                            students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                            k++;
                            lastStudent = i;
                        }
                    }
                    if (k < numMinStudents_Section) { // si no  se llena la seccion
                        // se entra aqui para meter los alumnos que no cumplian las restricciones ?? -- no se si es necesario
                        for (Integer j : stids.get(i).y) {
                            if ((k <= numMinStudents_Section || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                    && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {
                                idsAsignados.add(j);
                                idsbySeccion.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                k++;
                                lastStudent = i;
                            }
                        }
                    }
                    //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                    //y añadimos la seccion a la tabla del curso.
                    if (k > 0) { // se llena los huecos de ese profesor incluyendole la seccion
                        t.ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                        t.incrementarNumSecciones();

                        secciones.add(new Seccion(t, k, sec.get(stids.get(i).x), students.get(stids.get(i).y.get(0)).getGenero(), idsbySeccion, stids.get(i).x, c.getSections(), true, true, 0, c.getIdCourse()));
                        c.ocuparHueco(sec.get(stids.get(i).x));
                        idsbySeccion = new ArrayList<>();
                    }
                    if (idsAsignados.size() == studentsCourse.size()) { // se pudo organizar un horario con todos los alumnos
                        // de la clase
                        for (Integer st : idsAsignados) {
                            students.get(st).addAsignado(c.getIdCourse()); // aqui se agrega este curso en una lista la cual
                            // indica los cursos donde fue "matriculado" este alumno
                        }
                        c.setStudentsAsignados(idsAsignados); // se actualiza la lista de alumnos de ese curso
                        return null;
                    }
                }
            }
            if (i + 1 < stids.size()) {
                diferencia = conjuntos.diferencia(stids.get(i + 1).y, stids.get(i).y);
            }
            i++;
        }

        /*AQUI INTENTARE METER LOS ALUMNOS NO FUERON METIDOS EN NINGUNA SECCION
              SE METEN AQUI PARA INTENTAR EQUILIBRAR LAS SECCIONES*/
        ArrayList<Integer> alumnosNoAsignados = conjuntos.diferencia(studentsCourse, idsAsignados);
        ArrayList<Boolean> marcasAlumnos = new ArrayList<>();

        for (int j = 0; j < alumnosNoAsignados.size(); j++) {
            marcasAlumnos.add(false);
        }

        Collections.sort(secciones, new CompSeccionesStudents());

        for (int j = 0; j < secciones.size(); j++) {
            for (int k = 0; k < alumnosNoAsignados.size(); k++) {
                if (!marcasAlumnos.get(k) && secciones.get(j).getNumStudents() < maxStudentSection) {
                    if (students.get(alumnosNoAsignados.get(k)).patronCompatible(secciones.get(j).getPatronUsado())) {
                        if (!c.isGR()) {
                            idsAsignados.add(alumnosNoAsignados.get(k));
                            students.get(alumnosNoAsignados.get(k)).ocuparHueco(secciones.get(j).getPatronUsado(), c.getIdCourse() * 100 + (j + 1));
                            secciones.get(j).IncrNumStudents();
                            secciones.get(j).addStudent(alumnosNoAsignados.get(k));
                            marcasAlumnos.set(k, true);
                        } else if (secciones.get(j).getGender().equals(students.get(alumnosNoAsignados.get(k)).getGenero())) {
                            idsAsignados.add(alumnosNoAsignados.get(k));
                            students.get(alumnosNoAsignados.get(k)).ocuparHueco(secciones.get(j).getPatronUsado(), c.getIdCourse() * 100 + (j + 1));
                            secciones.get(j).IncrNumStudents();
                            secciones.get(j).addStudent(alumnosNoAsignados.get(k));

                            marcasAlumnos.set(k, true);
                        }
                    }
                }
            }
        }
        c.setArraySecciones(secciones);
        c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes

        for (Integer st : idsAsignados) {
            students.get(st).addAsignado(c.getIdCourse());
        }

        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.
        if (idsAsignados.size() != studentsCourse.size()) {
            System.out.println("FAILURE");
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for (Integer teacher : c.getTrestricctions()) {
                tname += r.cs.fetchName(teacher) + " ,";
            }
            if (tname.length() > 2) {
                tname = tname.substring(0, tname.length() - 1);
            }
            if (c.getTrestricctions().isEmpty()) {
                Log.add("-No hay profesores asignados al curso:" + r.cs.nameCourse(c.getIdCourse()));
            } else if (lastTeacher <= lastStudent) {
                Log.add("-Los profesores " + tname + " asignados al curso:" + r.cs.nameCourse(c.getIdCourse()) + " no tienen disponible ningun hueco compatible");
            } else {
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso " + r.cs.nameCourse(c.getIdCourse()) + ":");
                String anadir = "";
                ArrayList<ArrayList<Tupla>> aux = null;
                for (Integer i2 : ret) {
                    anadir += students.get(i2).getName() + ",";
                    if (aux == null) {
                                  //**************************************//
                   //****************************************//
                    aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log, r.aviso));
                    //CAMBIO DE OPCIONES
                    //aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks));
                    //**************************************//
                   //****************************************// 
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                Log.add(anadir);
            }
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }

            return ret;
        }
        return null;
    }

    private ArrayList<Integer> studentSectionsBackTracking(Restrictions r, ArrayList<Teacher> teachers, Course c, int minsections, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Integer> studentsCourseSection, HashMap<Integer, Student> students, ArrayList<Integer> rooms) {

        ArrayList<Tupla<Integer, ArrayList<Integer>>> patronesStudents = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();

        //Crea una lista con conjuntos de estudiantes compatibles con cada seccion
        //disponible del curso.
        // aqui es donde se tendra que modificar por donde se comenzara a buscar las posiciones del patron
        for (int i = 0; i < sec.size(); i++) {
            patronesStudents.add(new Tupla(i, new ArrayList<>()));
            for (Integer j : studentsCourse) {
                if (students.get(j).patronCompatible(sec.get(i))) {
                    patronesStudents.get(i).y.add(j);
                }
            }
        }

        try {
            patronesStudents.sort(new CompConjuntos());
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            e.getMessage();
        }
        sortStidsByPriority(patronesStudents, sec, c, r);

        ArrayList<Seccion> arraySeccion = new ArrayList<>(minsections);
        ArrayList<Seccion> mejorArraySeccion = new ArrayList<>(minsections);
        int numAlumnosTotal = r.studentsCourse.get(c.getIdCourse()).size();
        int maxStudentSeccion = c.getMaxChildPerSection();
        if (maxStudentSeccion == 0) {
            maxStudentSeccion = CHILDSPERSECTION; // POR DEFECTO
        }

        ArrayList<ArrayList<Boolean>> marcasTeacherPatron = new ArrayList<>();

        inicializarSols(arraySeccion, mejorArraySeccion, minsections);
        inicializarMarcas(marcasTeacherPatron, teachers.size(), patronesStudents.size());
        int numAlumnosAsignados = 0;
        int sectionsAsignadas = 0;
        int mejorNumAlumnosAsignados = 0;

        backTrackingSchedule(patronesStudents, 0, arraySeccion, mejorArraySeccion, teachers, sectionsAsignadas, minsections, maxStudentSeccion,
                numAlumnosAsignados, mejorNumAlumnosAsignados, numAlumnosTotal, marcasTeacherPatron);
        return null;
    }
//Ordena los teachers por prioridad (también depende de la cantidad de secciones disponibles de cada profesor):

    private ArrayList<Teacher> sortTeacherByPriorty(ArrayList<Teacher> teachers, ArrayList<Integer> preferedTeachers, int numSections) {
        ArrayList<Teacher> aux = new ArrayList<>();
        if (preferedTeachers.isEmpty()) {
            return aux;
        }

        for (int i = 0; i < teachers.size(); i++) {
            Teacher teacher = findTeacher(teachers, preferedTeachers.get(i % preferedTeachers.size()));
            aux.add(teacher);
        }
        return aux;
    }

    private Teacher findTeacher(ArrayList<Teacher> teachers, int idTeacher) {
        for (int i = 0; i < teachers.size(); i++) {
            if (teachers.get(i).getIdTeacher() == idTeacher) {
                return teachers.get(i);
            }
        }
        return null;
    }

    private void backTrackingSchedule(ArrayList<Tupla<Integer, ArrayList<Integer>>> patronStudents, int k, ArrayList<Seccion> sol, ArrayList<Seccion> mejorSol, ArrayList<Teacher> arrayTeachers,
            int sectionsAsignadas, int numSeccionesMax, int maxStudentSeccion, int numAlumnos, int mejorNumAlumnos, int numStudentTotal, ArrayList<ArrayList<Boolean>> marcas) {

    }

    private void actualizarPatronStudents(ArrayList<Tupla<Integer, ArrayList<Integer>>> patronStudents, ArrayList<Integer> studentsAsignados) {
        for (int i = 0; i < patronStudents.size(); i++) {
            patronStudents.set(i, new Tupla(patronStudents.get(i).getX(), this.conjuntos.diferencia(patronStudents.get(i).getY(), studentsAsignados)));
        }
    }

    private void inicializarSols(ArrayList<Seccion> sol, ArrayList<Seccion> mejorSol, int tam) { // inicializa a false
        for (int i = 0; i < tam; i++) {
            sol.add(new Seccion());
            mejorSol.add(new Seccion());
        }
    }

    private void inicializarMarcas(ArrayList<ArrayList<Boolean>> m, int filas, int cols) { // inicializa a false
        for (int i = 0; i < filas; i++) {
            ArrayList<Boolean> arrayAux = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                arrayAux.add(false);
            }
            m.add((ArrayList<Boolean>) arrayAux.clone());
        }
    }

    private String getGenero(int idGenero) {
        switch (idGenero) {
            case 0:
                return "Female";
            case 1:
                return "Male";
            default:
                return "Male";
        }
    }

    private Boolean esValido() {
        return false;
    }

    private ArrayList<Teacher> CompararTeachers(Course c, ArrayList<Teacher> teachersForCourse, Restrictions r, int i) {
        if (c.getArraySecciones().get(i).getIdTeacher() != 0) {
            r.aviso.getTeacherSection(r.hashTeachers, teachersForCourse, c, c.getArraySecciones().get(i));
        } else if (!c.getTrestricctions().isEmpty()) {
            r.aviso.addAvisosSectionTeachersAvailable(c, c.getArraySecciones().get(i));
            r.aviso.getTeacherCourse(r.hashTeachers, teachersForCourse, c, c.getArraySecciones().get(i));
        } else {
            r.aviso.addAvisosSectionTeachersAvailable(c, c.getArraySecciones().get(i));
            r.aviso.addAvisosCourseTeachersAvailable(c);
        }
        return teachersForCourse;
    }

//Con este método se decide que room coger (de section, course o school en función de si están definidos en RenWeb(available), si su tamaño es adecuado(size) o de si tienen disponibilidad(full)):
    private ArrayList<Room> CompararRooms(HashMap<Integer, Room> rooms, ArrayList<Room> roomForCourse, Course c, Seccion currentSec, String templateId, HashMap<String, ArrayList<Integer>> groupRooms, Exceptions aviso, String schoolCode) {

        //Se pone distinto de 0, porque al cargar una seccion en consultas, se inicializan los ids de rooms a 0 siempre por defecto si no encuentra ninguna room:       
        if (currentSec.getIdRoom() != 0) {
            aviso.getRoomSection(rooms, roomForCourse, c, currentSec, templateId, groupRooms, schoolCode);

        } else if (!c.getRooms().isEmpty()) {
            aviso.addAvisosSectionRoomsAvailable(c, currentSec);
            aviso.getRoomCourse(rooms, roomForCourse, c, currentSec, templateId, groupRooms, schoolCode);

        } else if (groupRooms.containsKey(templateId)) {
            aviso.addAvisosSectionRoomsAvailable(c, currentSec);
            aviso.addAvisosCourseRoomsAvailable(c);
            aviso.getRoomSchool(rooms, roomForCourse, c, currentSec, templateId, groupRooms, schoolCode);
        } else {
            aviso.addAvisosSchoolRoomsAvailable(schoolCode);
            aviso.addAvisosSectionRoomsAvailable(c, currentSec);
            aviso.addAvisosCourseRoomsAvailable(c);
        }

        return roomForCourse;
    }
//CORREGIR ESTA FUNCION LA DE ORDENACION ESTA TOMANDO DEMASIADO TIEMPO LO QUE HACE QUE SE RETRASE TODA LA TAREA.
    public ArrayList<Tupla<Integer, ArrayList<Integer>>> SortStids(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids) {
        ArrayList<Tupla<Integer, ArrayList<Integer>>> e2 = new ArrayList();
        ArrayList<Tupla<Integer, ArrayList<Integer>>> aux = stids;
        try {
            
            //ArrayList<Tupla<Integer, ArrayList<Integer>>> aux = stids;

            Collections.sort(aux, new Comparator<Tupla>() { 
                 public int compare(Tupla o1, Tupla o2) { 
                     return (((ArrayList)(o2.getY())).size()) >= ((ArrayList)(o1.getY())).size() ? -1 : 0;
                 }
            });

          /*  
            while (!aux.isEmpty()) {
                Tupla<Integer, ArrayList<Integer>> max = null;
                Integer idMax=0;
                for (Tupla<Integer, ArrayList<Integer>> e1 : aux) {
                    if (max == null) {
                        max = e1;
                    } else if (max.y.size() < e1.y.size()) {
                        max = e1;
                    }

                }
                e2.add(aux.get(idMax));
                aux.remove(idMax);
            }
*/
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
        return aux;
    }

    
}
