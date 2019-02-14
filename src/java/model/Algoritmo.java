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
    public static HashMap<Integer,String>NumNomSection = new HashMap<>();
    public final static int CHILDSPERSECTION = 20;
    private ArrayList<String> Log;
    private Conjuntos<Integer> conjuntos;
    public static boolean roomsActivation = false;

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

    private Boolean containsValueInLinkedCourse(Restrictions r,int courseID){
       for (HashMap.Entry<String, Course> entry : r.linkedCourses.entrySet()) {
		if(entry.getValue().getIdCourse() == courseID) return true;
	}
        return false;
    }
    /**
     * algoritmo
     *
     * @param mv
     * @param r
     * @param roommode
     */
    //mv lleva en este momento el esquema de filas y columnas (filas para una posición y columnas para otra):
    //r lleva todas las restricciones contempladas previamente:
    //schoolCode,yearId,templateId: nombre del distrito, yearId y templateId escogidos previamente:
    
    //ESTRUCTURA GENERAL DEL METODO Algoritmo.algo:
/* for de cada curso{
        for de cada seccion para rellenar con estudiantes{
            for de opciones totales para la seccion{
                for estudiantes (se comprueba si cada opcion es compatible)
            }
        }
    }*/
    
    public void algo(ModelAndView mv, Restrictions r, String schoolCode,String yearId, String templateId ) {

        int vueltas =0;
//r.courses lleva las restricciones de los cursos que se han contemplado previamente(en este caso son 2)
//se va a recorrer cada uno con course:        
        for (Course course : r.courses) {
            if(course.getIdCourse() == 1234){
                System.err.println("");
            }
            HashMap<Integer,Integer> teachers_numSections = new HashMap<>();
            HashMap<Integer,Integer> rooms_numSections = new HashMap<>();
            
                           
            vueltas++;
//Si el id del curso capturado no contiene valores LinkedCourse, se añade ArraySecciones a course(con las restricciones de las secciones por curso)
//Por ejemplo : con el id de curso 1245, se han añadido 5 posiciones de una seccion por posición, todas con id de curso 1245:            
            if(!containsValueInLinkedCourse(r,course.getIdCourse())){
                course.setArraySecciones(chargeArraySections(r,course));

                int maxSections ;
//---CAMBIO---Si el apartado maximo de secciones de curso y school esta vacío(en RenWeb, primero analiza si está vacío el de curso,
//y si lo está coge el valor de school. En caso de que el de school también estuviera vacío, iría al if para calcularlo, si hay algún dato
//en curso o en school va al else, para asignar este dato a la variable maxSections:):                
                if(course.getMaxSections() == null || course.getMaxSections().equals("")){
//(Ya no entra aqí porque se ha asignado un valor por defecto en school)El máximo de secciones se establece por la division del número de estudiantes por curso dividido entre el máximo nº de niños por sección(aula):
//En el caso del curso con id=1245, hay hay 56 alumnos en este curso, y hay una restricción de MáxChildPerSection de 20,
//por lo que se asignarán un máximo de secciones de 2 (se trunca el dato final). De los 16 estudiantes restantes, se añade en una nueva sección más
//adelante con la instrucción que recoge el resto y añade un maxSections manualmente(las secciones máximas se quedan guardadas en la variable
//maxSections). Total: maxSections = 3:

                   maxSections = r.studentsCourse.get(course.getIdCourse()).size() / course.getMaxChildPerSection();
                    if(r.studentsCourse.get(course.getIdCourse()).size() % course.getMaxChildPerSection() !=0)
                        maxSections++;
                }
//Si el apartado de cada curso de secciones no está vacío, se añaden a la variable maxSections                
                else{
                    maxSections = Integer.parseInt(course.getMaxSections());
                }
//Se comparan las secciones que ha recogido el ArraySecciones (esto captura las secciones que están añadidas en la página RenWeb/Academics/Classes, en el apartado
//Class Sections del curso en concreto. Por ejemplo en el caso de ENG1 (id=1245) hay 5 secciones añadidas) con la variable maxSections(esta última calculada previamente).
//El arraySecciones añade el numSeccion a cada seccion que hay contemplada (en el caso de id course=1245 hay 5 secciones con num: 1,2,5,3,4)
//Con el siguiente if se consigue que si ArraySecciones es menor que maxSections, se igualen:
                if(course.getArraySecciones().size() < maxSections){
                    int i = course.getArraySecciones().size();
                    while(i < maxSections){
                        Seccion auxSeccion = new Seccion();
                        auxSeccion.setNumSeccion(i+1);
                        course.addSeccion(new Seccion(auxSeccion));
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
                if(course.getOpcionesPatternGroup().isEmpty() && needGenerateOptions(course))
//totalBlocks es un array bidimensional con todos los bloques asignados:                    
                    opciones = course.opciones(r.totalBlocks,Log);
//Si ya hay opciones en el PatternGroup, se capturan las opciones que ya existen en base a dicho PatternGroup(no se generan nuevos patterngroup ni opciones)
//Se cogen las patternGroup definidas en RenWeb:                
                else if(!course.getOpcionesPatternGroup().isEmpty() ){
                    opciones = course.getOpcionesPatternGroup();
               }             
                ArrayList<Integer> noAsign = new ArrayList<>();
//Se pegan en el array noAsign los ids de todos los estudiantes del curso en cuestión:
                if(r.studentsCourse.containsKey(course.getIdCourse())){
                    noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
                    if(r.isShuffleRosters()){
//Si está elegida la opción de ShuffleRosters en restricciones:
//Combinacion de estudiantes(barajea). shuffle es un método que tiene la clase Collections para barajear datos:
                        Collections.shuffle(noAsign);
                    }

                }
//Punto de interrupción:
                if(course.getIdCourse() == 1245){
                    System.out.println("model.Algoritmo.algo()");
                }
                // FALTA ACABAR LOS LOGS DE LAS FUNCIONES MODIFICADAS
                
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
                    if(!course.getArraySecciones().get(i).lockSchedule && course.getArraySecciones().get(i).lockEnrollment){
                        noAsign = generatePattern_Section(r,r.teachers,r.rooms,course,opciones,
                              noAsign,r.students,course.getArraySecciones().get(i),teachers_numSections,rooms_numSections, templateId);
                    }
                    else if(course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment){
                        noAsign = fillSection(course.getArraySecciones().get(i),r, course,noAsign, r.students);
                        course.getArraySecciones().get(i).setPatternRenWeb(1);
                    }
                    else if(!course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment){
                       noAsign = generatePattern_Section(r,r.teachers,r.rooms,course,opciones,
                               noAsign,r.students,course.getArraySecciones().get(i),teachers_numSections,rooms_numSections, templateId);
                    }
                    else{ // lockSchedule && lockEnrollment==true:
                        for (int j = 0; j < course.getArraySecciones().get(i).getIdStudents().size(); j++) {
//Con este for se comprueba si las ids de los estudiantes de la seccion que se esta evaluando corresponden con los ids de estudiantes que hay guardados
//en el hash students(es decir, de los capturados en las restricciones).
//Aquí se hace lo mismo que en el si el lockSchedule está activado (opción 2, primer else if):
                            int idStudent = course.getArraySecciones().get(i).getIdStudents().get(j);
                            if(r.students.containsKey(course.getArraySecciones().get(i).getIdStudents().get(j))){
                              r.students.get(course.getArraySecciones().get(i).getIdStudents().get(j)).ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(i).getNumSeccion());

                            }
                            else{
                                System.err.println("");
                            }

                        }
//ocuparHueco: se refiere a las marcas de los bloques                        
                        course.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(),course.getArraySecciones().get(i).getNumSeccion());
//En el caso de que entre en if de LockSchedule true y lockEnrollment true, se cogen las secciones que cumplen estas condiciones:                        
                        course.getArraySecciones().get(i).setLockSchedule(true);
                        course.getArraySecciones().get(i).setLockEnrollment(true);
                        course.getArraySecciones().get(i).setPatternRenWeb(1);
                        Teacher t_Aux = r.hashTeachers.get(course.getArraySecciones().get(i).getIdTeacher());
                        if(t_Aux != null){
                            t_Aux.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(i).getNumSeccion());
                            t_Aux.incrementarNumSecciones();
                            course.getArraySecciones().get(i).setTeacher(t_Aux);
                        }
                    
                        noAsign = conjuntos.diferencia(noAsign, course.getArraySecciones().get(i).getIdStudents());
                    }
                }
                //aqui tengo que meter el algoritmo que intentara acabar de llenar todas las secciones con los alumnos que no
                //lograqron ser matriculados en estas secciones.
                /**
                 */
                ArrayList<Boolean> marcasAlumnos = new ArrayList<>();
//Todos los alumnos que no han quedado asignados se recorren en este for para almacenarlos en el array marcasAlumnos, y se les asigna el valor false:
                for (int j = 0; j < noAsign.size(); j++) {
                    marcasAlumnos.add(false);
                }
//Se ordenan las secciones del curso en concreto:
                Collections.sort(course.getArraySecciones(), new CompSeccionesStudents());

                //     students_Section.add(-1);
                /* for (int j = 0; j < students_Section.get(i).y; j++) {
                    alumnosNoAsignados.ge
                }*/

             //   c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes

//Este for entra en cada seccion del curso concreto:
                for (int j = 0; j < course.getArraySecciones().size(); j++) {
//Si la seccion en concreto no tiene asignado lockEnrollment:
//Aquí se van añadiendo las marcasAlumnos a true (de todos los estudiantes):
                    if(!course.getArraySecciones().get(j).lockEnrollment){
                    for (int k = 0; k < noAsign.size(); k++) {
                        if (!marcasAlumnos.get(k) && course.getArraySecciones().get(j).getIdStudents().size() < course.getMaxChildPerSection()) {
                            if (r.students.get(noAsign.get(k)).patronCompatible(course.getArraySecciones().get(j).getPatronUsado())) {
                                if(!course.isGR()){
//Este es el mismo array por el cual se ocupaban huecos de fillSection, es decir, se añade en los huecos que coincida, el id de seccion+curso:                                    
                                    r.students.get(noAsign.get(k)).ocuparHueco(course.getArraySecciones().get(j).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(j).getNumSeccion());
                                    //course.getArraySecciones().get(j).IncrNumStudents();
                                    course.getArraySecciones().get(j).addStudent(noAsign.get(k));
                                    marcasAlumnos.set(k,true);
                                }
                                else{
                                    if(course.getArraySecciones().get(j).getGender() == null){
                                        if(course.getArraySecciones().get(j).idStudents.size()>0){
                                            course.getArraySecciones().get(j).setGender(r.students.get(course.getArraySecciones().get(j).idStudents.get(0)).getGenero());
                                        }else{
                                            course.getArraySecciones().get(j).setGender("Male");
                                        }
                                    }
                                    if(course.getArraySecciones().get(j).getGender().equals(r.students.get(noAsign.get(k)).getGenero())){
                                        r.students.get(noAsign.get(k)).ocuparHueco(course.getArraySecciones().get(j).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(j).getNumSeccion());
                                        course.getArraySecciones().get(j).IncrNumStudents();
                                        course.getArraySecciones().get(j).addStudent(noAsign.get(k));

                                        marcasAlumnos.set(k,true);
                                    }
                                }
                            }
                        }
                    }
                    }
                }
                ArrayList<Integer> auxNoAsign = new ArrayList<>();
                for(int i = 0;i< noAsign.size();i++){
                    if(!marcasAlumnos.get(i)){
                        auxNoAsign.add(noAsign.get(i));
                    }
                }
                noAsign = auxNoAsign;
                //actualizamos el noASign

                 /*
                 **///
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
//Hasta aquí todos los cursos que no tienen linked Courses, y a partir de aquí los que sí:



//Con este if, se comprueba primero de cada curso si tiene LinkedCourses.
//Si es así, se cambia el valor del boolean encontrado a true.
            if (r.getLinkedCourses().containsKey("" + course.getIdCourse())) {
                int k = 0;
                boolean encontrado = false;
                while (k < r.courses.size() && !encontrado) {
                    if (r.courses.get(k).getIdCourse() == r.getLinkedCourses().get("" + course.getIdCourse()).getIdCourse()) {
                        encontrado = true;
                    }
                    else {
                        k++;
                    }

                }
//Si encontrado==true, se asigna el id del curso asociado.                
                if (encontrado) {
                    Course courseAsociado = r.courses.get(k);
                    ArrayList<Integer> seccionesHabilitadas = new ArrayList<>();
                    seccionesHabilitadas = r.getLinkedCourses().get("" + course.getIdCourse()).getSectionsLinkeadas();
                        ArrayList<ArrayList<Tupla>> opcionesAsoc = new ArrayList<>();
//Se va aplicando el algoritmo de las secciones del curso asociado en función de la aplicación al curso principal(es una copia):
                    if (courseAsociado.getArraySecciones() == null || courseAsociado.getArraySecciones().isEmpty()) {
                        courseAsociado.setArraySecciones(chargeArraySections(r, courseAsociado));
                    }
                    // courseAsociado.setArraySecciones(course.getArraySecciones());
                    if(courseAsociado.getOpcionesPatternGroup().isEmpty() && needGenerateOptionsLinked(courseAsociado,course)){
                        opcionesAsoc = courseAsociado.opciones(r.totalBlocks,Log);
                    }
                    else if(!course.getOpcionesPatternGroup().isEmpty() ){
                        opcionesAsoc = courseAsociado.getOpcionesPatternGroup();
                    }
                    for (int i = 0; i < course.getArraySecciones().size(); i++) {
                        if(courseAsociado.getIdCourse() == 1301){
                            System.err.println("    ");
                        }
                        if (courseAsociado.getArraySecciones().size() <= i) {
                            courseAsociado.addSeccionWithoutPattern(course.getArraySecciones().get(i));
                            
                            assignPatternToSection(r,course,course.getArraySecciones().get(i),opcionesAsoc,courseAsociado);
                            
                          //  courseAsociado.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(),course.getArraySecciones().get(i).getNumSeccion());
                            System.err.println("");
                        }
                        else if (seccionesHabilitadas.isEmpty()) { // todos

                            int ind = 0;
                            boolean exito = false;

                            while (ind < courseAsociado.getArraySecciones().size() && !exito) {

                                if (courseAsociado.getArraySecciones().get(ind).getNumSeccion() == course.getArraySecciones().get(i).getNumSeccion()) {
                                    courseAsociado.ocuparHueco(courseAsociado.getArraySecciones().get(ind).patronUsado, courseAsociado.getArraySecciones().get(ind).getNumSeccion());
                                    courseAsociado.getArraySecciones().get(ind).copiarIdsStudents(course.getArraySecciones().get(i).getIdStudents(), r.students, courseAsociado);
                                    courseAsociado.getArraySecciones().get(ind).setPatternRenWeb(course.getArraySecciones().get(i).getPatternRenWeb());
                                    exito = true;
                                } else {
                                    ind++;
                                }
                                if(!courseAsociado.getArraySecciones().get(ind).isLockSchedule()){
                                   assignPatternToSection(r,course,course.getArraySecciones().get(i),opcionesAsoc,courseAsociado);

                                }
                            }
                        }
                        else {
                            int ind = 0;
                            boolean exito = false;
                            while (ind < courseAsociado.getArraySecciones().size() && !exito) {
                                if (courseAsociado.getArraySecciones().get(ind).getNumSeccion() == course.getArraySecciones().get(i).getNumSeccion()) {

                                    courseAsociado.ocuparHueco(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).patronUsado, courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getNumSeccion());
                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).copiarIdsStudents(course.getArraySecciones().get(i).getIdStudents(), r.students, courseAsociado);
                                    //courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind)-1).setTeacher(r.hashTeachers.get(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind)-1).getIdTeacher()));
                                    Teacher t_Aux = new Teacher();
                                    if(r.hashTeachers.containsKey(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getIdTeacher()))
                                        t_Aux = r.hashTeachers.get(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getIdTeacher());
                                    else{
                                        t_Aux.setName("No found in template courses");
                                    }
                                    t_Aux.ocuparHueco(courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getPatronUsado(), courseAsociado.getIdCourse() * 100 + courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).getNumSeccion());
                                    t_Aux.incrementarNumSecciones();


                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).setTeacher(t_Aux);
                                    courseAsociado.getArraySecciones().get(seccionesHabilitadas.get(ind) - 1).setPatternRenWeb(course.getArraySecciones().get(i).getPatternRenWeb());
                                    exito = true;
                                }
                                else {
                                    ind++;
                                }
                                 if(!courseAsociado.getArraySecciones().get(ind).isLockSchedule()){
                                   assignPatternToSection(r,course,course.getArraySecciones().get(i),opcionesAsoc,courseAsociado);

                                }
                            }
                        }
                    }
                }
            }
        }
//Guardado de resultados en archivo y se devuelven a la vista:        
        saveXML_FTP(yearId,templateId,schoolCode,r);
        HashMap<Integer,String> persons = Consultas.getPersons();
        sortSectionsAndStudents(r.courses,persons);

        mv.addObject("TAMX", TAMX);
        mv.addObject("TAMY", TAMY);
        mv.addObject("persons", persons);


        ArrayList<Student> studentsOrdered = new ArrayList<>(r.students.values());
try{
    sortStudentsPerGradeLevel(studentsOrdered,r.cs);
}
catch(Exception e){
    
}
        mv.addObject("students",r.students);
        mv.addObject("orderedStudents",studentsOrdered);


        try{
        sortCoursesPerAbbrev(r.courses,r.cs);
}
catch(Exception e){
    
}
        mv.addObject("Courses", r.courses);
try{
        sortTeachersPerNames(r.teachers,r.cs);}
catch(Exception e){
    
}
        mv.addObject("profesores", r.teachers);

        mv.addObject("cs", r.cs);
        mv.addObject("rooms", r.rooms);
      
        mv.addObject("hashTeachers",r.hashTeachers);
        mv.addObject("grouprooms", r.groupRooms);
        mv.addObject("log", Log);
//Se añade este objeto para poder mostrar en la vista los cursos que no tienen estudiantes y que no podrán aparecer al crear el horario (se manda a homepage)
       // mv.addObject("cursosSinEstudiantes",Consultas.CoursesWithoutStudents);
     // mv.addObject("cursosSinEstudiantes",Consultas.cursosSin1);
        System.out.println("");
       
    }
    private boolean needGenerateOptions(Course c){
        for (Seccion arraySeccione : c.getArraySecciones()) {
            if(!arraySeccione.lockSchedule)
                return true;
        }
        return false;
    }

    private boolean needGenerateOptionsLinked(Course cAsoc,Course c){
        if(cAsoc.getArraySecciones().size() < c.getArraySecciones().size()) return true;
        
        for (int i = 0; i < cAsoc.getArraySecciones().size(); i++) {
            if(cAsoc.getArraySecciones().get(i).getPatronUsado().isEmpty() ||
               cAsoc.getArraySecciones().get(i).getPatronUsado() == null) return true;
            
        }
        return false;
    }
    
    private void sortStudentsPerGradeLevel(ArrayList<Student> t,Consultas cs){
        // Sorting
        Collections.sort(t, new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                if(o1.getGradeLevel() == null || o2.getGradeLevel() == null)
                    return -1;
                return  o1.getGradeLevel().compareTo(o2.getGradeLevel());
            }
        });
    }

    private void sortTeachersPerNames(ArrayList<Teacher> t,Consultas cs){
        // Sorting
        Collections.sort(t, new Comparator<Teacher>() {
            @Override
            public int compare(Teacher o1, Teacher o2) {
                /*if(!cs.getNamePersons().containsKey(o1.getIdTeacher()) && !cs.getNamePersons().containsKey(o2.getIdTeacher()))
                    return -1;*/
                return  cs.getNamePersons().get(o1.getIdTeacher()).compareTo(cs.getNamePersons().get(o2.getIdTeacher()));
            }
        });
    }

    private void sortCoursesPerAbbrev(ArrayList<Course> c,Consultas cs){
        // Sorting
        Collections.sort(c, new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                return  cs.getAbbrevCourses().get(o1.getIdCourse()).compareTo(cs.getAbbrevCourses().get(o2.getIdCourse()));
            }
        });
    }
    public class teacherComparator implements Comparator<Teacher> {
        HashMap<Integer,Integer> teachers_numS;
        
        public teacherComparator(HashMap<Integer,Integer> teachers_numS){
            this.teachers_numS = teachers_numS;
        }
        @Override
        public int compare(Teacher o1, Teacher o2) {
            return this.teachers_numS.get(o1.getIdTeacher()) - this.teachers_numS.get(o2.getIdTeacher()); //To change body of generated methods, choose Tools | Templates.
        }
    }
//Este método es necesario para generar el patron a la hora de asignar estudiantes (de noAsign) por sección teniendo en cuenta que está desactivado el lockSchedule 
    //(sirve tanto para si está activado o no el lockEnrollment, ya que dentro de este método hay un if que tiene en cuenta si está activado o no):
    private ArrayList<Integer> generatePattern_Section(Restrictions r, ArrayList<Teacher> teachers,HashMap <Integer,Room> rooms, Course c, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students,Seccion currentSec,HashMap<Integer,Integer> teachers_numSections,HashMap<Integer,Integer> rooms_numSections, String templateId) {
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
         idsAsignados = c.getAllIds();
//Este hash almacena la cantidad de opciones válidas para cada estudiante(id: de cada estudiante, cantidad de opciones válidas):
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
//initHashStudents hace que se inicialicen a 0 los estudiantes para sumarlos de 1 en 1:        
        initHashStudents(hashStudents_cantPatrones, studentsCourse);     
        HashMap<Integer,String> genderStids = new HashMap<>();
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
                    if(!c.isGR()){
                        stids.get(i).y.add(j);
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
//Este caso es para añadir datos si se refiere a una seccion separada por restricciones de genero(el stids se calcula abajo):                   
                    else{// is GR
                        if(students.get(j).getGenero().equals("Female")){
                            stdFemales.add(j);
                        }
    //Si es nulo el contenido, por defecto es "male":                        
                        else{ //male or null
                            stdMales.add(j);
                        }
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
            }
//Si se refiere a una seccion separada por restricciones de genero: funcion igual que en !c.isGR pero teniendo en cuenta las restricciones de género, aunque se tiene en cuenta
//después del recorrido de todos los estudiantes:            
            if(c.isGR()){
                if(stdMales.size() >= stdFemales.size()){
                   // stids.get(i).y. = new ArrayList<>();
                    stids.set(i, new Tupla(i, new ArrayList<>(stdMales)));
                    genderStids.put(i, "Male");
                }
                else{
                    stids.set(i, new Tupla(i, new ArrayList<>(stdFemales)));
                    genderStids.put(i, "Female");
                }
            }
            else{
                genderStids.put(i, "");
            }
        }
        int idCourse = c.getIdCourse();
        if(idCourse == 806){
            System.err.println("");
        }
//En el caso de que la seccion no sea separada por restricciones de género, se aplica un equilibrado en género masculino y femenino:        
        if(!c.isGR())
            equilibrarGender(stids,students);

//1ª ORDENACIÓN de stids-->Ordena la lista de conjuntos por numero de estudiantes de mayor a menor(es decir, las opciones compatibles con mayor número de estudiantes serán las primeras):
        try {
            stids.sort(new CompConjuntos());

        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }
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
           // 
           
           if(currentSec.getIdTeacher()!=0){
                for(int y =0;y< teachers.size();y++){            
            if(teachers.get(y).getIdTeacher()==currentSec.getIdTeacher()){
                teachersForCourse.add(teachers.get(y));
                currentSec.setTeacher(teachers.get(y));
                if(!teachers_numSections.containsKey(teachers.get(y).getIdTeacher()))
                    teachers_numSections.put(teachers.get(y).getIdTeacher(),0);
            }
        }
           }
           else{
               for(int y =0;y< teachers.size();y++){
                   if(c.getTrestricctions().contains(teachers.get(y).getIdTeacher())){
                       teachersForCourse.add(teachers.get(y));
                       currentSec.setTeacher(teachers.get(y));
                       currentSec.setIdTeacher(teachers.get(y).getIdTeacher());
                       if(!teachers_numSections.containsKey(teachers.get(y).getIdTeacher()))
                       teachers_numSections.put(teachers.get(y).getIdTeacher(),0);
                   }
               }
           }
       

/*ArrayList<Integer> idsrooms = new ArrayList<>();
for (int z=0;z<roomsArray.size();z++){
    idsrooms.add(Integer.parseInt(roomsArray.get(z).getName()));
}*/

//Con lo siguiente se quitan los ; parentesis y otros caracteres, para almacenar todo en el hashmap roomsTemplate(
//con key: id de los template, y value: los ids de rooms que tienen asignado a determinado id de template )


/*Consultas cons = new Consultas(templateId);
HashMap<Integer,Room>roomsU = cons.getRooms();

roomsU.containsValue(c.getRooms());
for (Entry<Integer, Room> entry : roomsU.entrySet()) {
    Integer clave = entry.getKey();
    Integer valor = entry.getValue().getRoomid();
    System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
}
    

       for(int y =0;y< rooms.size();y++){
    
           
            if(rooms.get(y).getRoomid().contains(c.getRooms()){
                
                roomForCourse.add(rooms.get(y));
                if(!rooms_numSections.containsKey(rooms.get(y).getRoomid()))
                    rooms_numSections.put(rooms.get(y).getRoomid(), 0);
                
            }
      
        }*/
        /*if(c.isBalanceTeachers()){
            Collections.sort(teachersForCourse, new teacherComparator(teachers_numSections));
        }
        */
       // roomForCourse.addAll(c.getRooms());
        
        


        boolean exito = false;
        // HAY QUE REHACER ESTAQ PARTE YA QUE BASTARIA CON CREA R LA PRIMERA SECCION
        // Y COPIAR LA LISTA DE ESTUDIANTES SI NO LOS TIENE BLOQUEADO
        // SI LO TUVIERA BLOQUEADO CAMBIAR LOS NSTUDENTS COURSE QUE ENTRAN
        // POR PARAMETRO, ESTOS DEBERIAN CAMBIAR.
       //while (i < stids.size() && secciones.size() < c.getMinSections())   {
       
       
        if(!r.isActiveRooms()){            
//Recorridos en función de las opciones compatibles y una variable llamada exito que permite que cuando se han asignado los profesores, salga del bucle:       
        while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
        int contR = 0;
         // while(!exito && (!r.isActiveRooms() || contR < c.getRooms().size())){
             // if(!r.isActiveRooms() || (r.rooms.containsKey(c.getRooms().get(contR)) && r.rooms.get(c.getRooms().get(contR)).patronCompatible(sec.get(stids.get(i).x)))){
               
               
//Recorridos a todos los profesores: dentro se evalúan las restricciones de los profesores, las asignaturas que pueden cursar y si tienen un patrón compatible con las opciones disponibles:       
//teachersForCourse son los estudiantes que estan asignados al curso en concreto, mas el default (no incluye los profesores asignados a cada seccion):


                    for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                         if ( !exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
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

                                 for (Integer j : stids.get(i).y) { // studiantes
                                     if ((k < minStudentSection) && !idsAsignados.contains(j)
                                             && students.get(j).patronCompatible(sec.get(stids.get(i).x)) && !currentSec.lockEnrollment) {

                                         idsAsignados.add(j);
                                         students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                         currentSec.addStudent(j);
                                         k++;
                                         lastStudent = i;
                                     }
                                 }
                                 //una vez que ya hay estudiantes asignados a esta seccion ocupamos el hueco en el teacher
                                 //y añadimos la seccion a la tabla del curso.
//El siguiente if: k>0 se refiere a que si por lo menos hay un estudiante (si la sección se ha rellenado). En ese caso se cargan teachers en función de la sección actual:
//Si entra en este if, se sale del while finalmente(si i es mayor o igual que stids.size()), pero si no llega a entrar, da vueltas hasta que llegue a entrar (es decir, hasta que haya rellenado la sección con alumnos)
                                 

                                    if (k > 0 || currentSec.lockEnrollment) { // se llena los huecos de ese profesor incluyendole la seccion:

                                     currentSec.setPatronUsado(sec.get(stids.get(i).x));

                                     currentSec.setIdTeacher(t.getIdTeacher());
                                     Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                     t_Aux.ocuparHueco( currentSec.getPatronUsado(), c.getIdCourse() * 100 +  currentSec.getNumSeccion());
                                     t_Aux.incrementarNumSecciones();
                                     if(teachers_numSections.containsKey(t.getIdTeacher())){
                                        teachers_numSections.put(t.getIdTeacher(), teachers_numSections.get(t.getIdTeacher()) + 1);
                                    }

                                     
                                    currentSec.setTeacher(t_Aux);
                                   //  currentSec.setIdRoom(c.getRooms().get(contR));
                                     //currentSec.copiarIdsStudents(idsAsignados, students, c);
                                     currentSec.setGender(genderStids.get(stids.get(i).x));
                                     c.ocuparHueco(currentSec.getPatronUsado(),currentSec.getNumSeccion());
                                     currentSec.setLockSchedule(true);
                                     if(k == c.getMaxChildPerSection()){
                                         currentSec.setLockEnrollment(true);
                                     }
                                     exito =true;
                                 }
                         }
                     }
                     i++; 
                }
            //   contR++;
       //     }
          
     }
        //ESTE ELSE ES IGUAL QUE EL IF, PERO TENIENDO EN CUENTA LOS ACTIVE ROOM(SOLO SE AÑADE UN FOR DE ROOMS PARA TENERLO EN CUENTA AL GENERAR ALGORITMO)
      else{
            roomsActivation=true;
            ArrayList<Room> roomsArray = new ArrayList<>(rooms.values());
            //Se pone distinto de 0, porque al cargar una seccion en consultas, se inicializan los ids de rooms a 0 siempre por defecto si no encuentra ninguna room:
    if(currentSec.getIdRoom()!=0){
        for(int y =0;y< roomsArray.size();y++){
            if(roomsArray.get(y).getRoomid()==currentSec.getIdRoom()){
                roomForCourse.add(roomsArray.get(y));
                currentSec.setRoom(roomsArray.get(y));
                    if(!rooms_numSections.containsKey(roomsArray.get(y).getRoomid()))
                        rooms_numSections.put(roomsArray.get(y).getRoomid(), 0);
                }
                }
        }
    else{
                
        for(int y =0;y< roomsArray.size();y++){
            if(c.getRooms().contains(roomsArray.get(y).getRoomid())){
                roomForCourse.add(roomsArray.get(y));
                currentSec.setRoom(roomsArray.get(y));
                currentSec.setIdRoom(roomsArray.get(y).getRoomid());
                if(!rooms_numSections.containsKey(roomsArray.get(y).getRoomid()))
                    rooms_numSections.put(roomsArray.get(y).getRoomid(), 0);
                
            }
        }
            }
            while (i < stids.size() && !exito) { // recorrido a los bloques disponibles

                
                for(int contR=0;contR<roomForCourse.size();contR++){
                    //Este if se pone en codigo muerto porque cambiaria las rooms del curso para todas las secciones cuando no se necesita esto
//                         if(!c.getRooms().contains(currentSec.getIdRoom())){
//                            c.setRooms(currentSec.getIdRoom().toString());
//                        }
//                    System.out.println(c.getRooms().get(contR));
//                    System.out.println(roomForCourse.get(contR).getRoomid());
//                    System.out.println(currentSec.getRoom().getRoomid());
//                    System.out.println(currentSec.getIdRoom());
//               //     System.out.println(currentSec.);
//                    System.out.println("");
                    if (r.rooms.containsKey(roomForCourse.get(contR).getRoomid()) && r.rooms.get(roomForCourse.get(contR).getRoomid()).patronCompatible(sec.get(stids.get(i).x))){
//                   
                        for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                  
                            if ( !exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
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
                                 for (Integer j : stids.get(i).y) { // studiantes
                                     if ((k < minStudentSection) && !idsAsignados.contains(j)
                                             && students.get(j).patronCompatible(sec.get(stids.get(i).x)) && !currentSec.lockEnrollment) {

                                         idsAsignados.add(j);
                                         students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                         currentSec.addStudent(j);
                                         k++;
                                         lastStudent = i;
                                     }
                                 }
                                 //una vez que ya hay estudiantes asignados a esta seccion ocupamos el hueco en el teacher
                                 //y añadimos la seccion a la tabla del curso.
//El siguiente if: k>0 se refiere a que si por lo menos hay un estudiante (si la sección se ha rellenado). En ese caso se cargan teachers en función de la sección actual:
//Si entra en este if, se sale del while finalmente(si i es mayor o igual que stids.size()), pero si no llega a entrar, da vueltas hasta que llegue a entrar (es decir, hasta que haya rellenado la sección con alumnos)
                                 if (k > 0 || currentSec.lockEnrollment) { // se llena los huecos de ese profesor incluyendole la seccion:

                                     currentSec.setPatronUsado(sec.get(stids.get(i).x));

                                     currentSec.setIdTeacher(t.getIdTeacher());
                                     Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                     t_Aux.ocuparHueco( currentSec.getPatronUsado(), c.getIdCourse() * 100 +  currentSec.getNumSeccion());
                                     t_Aux.incrementarNumSecciones();
                                     if(teachers_numSections.containsKey(t.getIdTeacher())){
                                        teachers_numSections.put(t.getIdTeacher(), teachers_numSections.get(t.getIdTeacher()) + 1);
                                    }
                                    if(rooms_numSections.containsKey(roomForCourse.get(contR).getRoomid())){
                                        rooms_numSections.put(roomForCourse.get(contR).getRoomid(), rooms_numSections.get(roomForCourse.get(contR).getRoomid())+1);
                                    } 
                                    
                                    r.rooms.get(roomForCourse.get(contR).getRoomid()).ocuparHueco(c.getIdCourse(), currentSec.getNumSeccion(), currentSec.getPatronUsado());
                                
                                    
                                     currentSec.setTeacher(t_Aux);
                                     //currentSec.setIdRoom(c.getRooms().get(contR));
                                     //currentSec.copiarIdsStudents(idsAsignados, students, c);
                                     currentSec.setGender(genderStids.get(stids.get(i).x));
                                     c.ocuparHueco(currentSec.getPatronUsado(),currentSec.getNumSeccion());
                                     currentSec.setLockSchedule(true);
                                     if(k == c.getMaxChildPerSection()){
                                         currentSec.setLockEnrollment(true);
                                     }
                                     exito =true;
                                 }
                         }
                     }
                    }
                      
                }
                i++;
          
        }
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
              /*  String anadir = "";
                ArrayList<ArrayList<Tupla>> aux = null;
                for (Integer i2 : ret) {
                    anadir += students.get(i2).getName() + ",";
                    if (aux == null) {
                        aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log));
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                //anadir = anadir.substring(0, anadir.length() - 2) + ".";
                Log.add(anadir);*/
            }
//este es el for que recoge los estudiantes no asignados finalmente:            
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }

        }
        return ret;
    
    }      

    private void assignPatternToSection(Restrictions r,Course c, Seccion seccionCourse, ArrayList<ArrayList<Tupla>> sec,Course courseAsoc) {

      
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
            //return null;
        }

       
       // sortStudentsByPatron(stids, hashStudents_cantPatrones);


        int lastTeacher = -1;
        int lastStudent = -1;
        int lastRoom = -1;
        int i = 0;

        //recorro la lista de conjuntos y la de profesores

        ArrayList<Teacher> teachersForCourse = new ArrayList<>();

        for(int y =0;y< r.teachers.size();y++){
            if(c.getTrestricctions().contains(r.teachers.get(y).getIdTeacher()))
                teachersForCourse.add(r.teachers.get(y));
        }
        boolean exito = false;
        // HAY QUE REHACER ESTAQ PARTE YA QUE BASTARIA CON CREA R LA PRIMERA SECCION
        // Y COPIAR LA LISTA DE ESTUDIANTES SI NO LOS TIENE BLOQUEADO
        // SI LO TUVIERA BLOQUEADO CAMBIAR LOS NSTUDENTS COURSE QUE ENTRAN
        // POR PARAMETRO, ESTOS DEBERIAN CAMBIAR.
       //while (i < stids.size() && secciones.size() < c.getMinSections())   {
       /*
       
       
       */
       
       
       
       
       
        while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
        int contR = 0;
            while(!r.isActiveRooms() || contR < c.getRooms().size()){
                if(!r.isActiveRooms() || (r.rooms.containsKey(c.getRooms().get(contR)) && r.rooms.get(c.getRooms().get(contR)).patronCompatible(sec.get(stids.get(i).x)))){
              
                    for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                         if ( !exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                                 && t.patronCompatible(sec.get(stids.get(i).x))) {

                                 int k = currentSec.getIdStudents().size();
                                 lastTeacher = i;
     // VAMOS A TENER QUE EN CUENTA CUANDO YA ESTAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
     //  EL PATTERN PARA LOS ESTUDIANTYES YA CMATRICULADOS CUMPLEN ESA RESTRICCION
                                 for (Integer j : stids.get(i).y) { // studiantes

                                         r.students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                        // currentSec.addStudent(j);
                                         k++;
                                         lastStudent = i;

                                 }
                                 //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                                 //y añadimos la seccion a la tabla del curso.
                                  // se llena los huecos de ese profesor incluyendole la seccion

                                     currentSec.setPatronUsado(sec.get(stids.get(i).x));

                                    currentSec.setIdTeacher(t.getIdTeacher());
                                     Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                  // Teacher t_Aux = new Teacher();
                                     t_Aux.ocuparHueco( currentSec.getPatronUsado(), courseAsoc.getIdCourse() * 100 +  currentSec.getNumSeccion());
                                     t_Aux.incrementarNumSecciones();

                               //      r.rooms.get(c.getRooms().get(contR)).ocuparHueco(courseAsoc.getIdCourse(), currentSec.getNumSeccion(), currentSec.getPatronUsado());
                                             
                                     currentSec.setTeacher(t_Aux);
                                     //currentSec.copiarIdsStudents(idsAsignados, students, c);
                                     //currentSec.setIdRoom(c.getRooms().get(contR));
                                     currentSec.setGender(seccionCourse.getGender());
                                     courseAsoc.ocuparHueco(currentSec.getPatronUsado(),currentSec.getNumSeccion());
                                     currentSec.setLockSchedule(true);
                                     if(k == c.getMaxChildPerSection()){
                                         currentSec.setLockEnrollment(true);
                                     }
                                     exito =true;

                         }
                    }
                }
            }
            i++;
        }  
    }

    
    private ArrayList<Integer> getSortIdsStudent(ArrayList<Integer> idsStudents,  HashMap<Integer,String> hashPersons){
        ArrayList<Tupla> auxTupla = new ArrayList<>();
        ArrayList<Integer> idsSorted = new ArrayList<>();

        for (int i = 0; i < idsStudents.size(); i++) {
            auxTupla.add(new Tupla(idsStudents.get(i),hashPersons.get(idsStudents.get(i))));
        }
        Collections.sort(auxTupla, new Comparator<Tupla>() {
            @Override
            public int compare(Tupla o1, Tupla o2) {
                return  (""+o1.y).compareTo(""+o2.y);
            }
        });

        for (int i = 0; i < auxTupla.size(); i++) {
            idsSorted.add((Integer) auxTupla.get(i).x);
        }

        return idsSorted;
    }
    private void sortSections(ArrayList<Seccion>  s, HashMap<Integer,String> hashPersons) {
        ArrayList<Seccion>  auxSecciones = new ArrayList<>();
 ArrayList<Seccion>  sAux = (ArrayList<Seccion>) s.clone();
        while(!sAux.isEmpty()){
            int posMin = getMinSeccion(sAux);
            Seccion auxS = new Seccion(sAux.get(posMin));
            auxS.setIdStudents(getSortIdsStudent(auxS.getIdStudents(),hashPersons));
            auxSecciones.add(auxS);
           sAux.remove(posMin);
        }
      //  s= new ArrayList<>();
        for (int i = 0; i < auxSecciones.size(); i++) {
            s.set(i,new Seccion(auxSecciones.get(i)));
        }
      //  s = new ArrayList<>(auxSecciones);
       /* for (int i = 1; i <= s.size(); i++) {
            boolean encontrado = false;
            int j = 0;
            
            while(!encontrado && j < s.size()) {
                if(i == s.get(j).getNumSeccion()){
                    s.get(j).setIdStudents(getSortIdsStudent(s.get(j).getIdStudents(),hashPersons));
                    auxSeccion.add(new Seccion(s.get(j)));
                    encontrado = true;
                }
                j++;
            }
         //   sortSections(c.get(i).getArraySecciones());
        }

        for (int i = 0; i < s.size(); i++) {
            s.set(i, new Seccion(auxSeccion.get(i)));
        }*/
    }
    
    private int getMinSeccion(ArrayList<Seccion> s){
        int pos = -1;
        int i = 0;
        int numMin = 99999;
        do{
            if(s.get(i).getNumSeccion() < numMin){
                numMin = s.get(i).getNumSeccion();
                pos = i;
            }
            i++;
        }while(i < s.size());
        
        return pos;
    }

    private void sortSectionsAndStudents(ArrayList<Course>  c, HashMap<Integer,String> hashPersons) {
        for (int i = 0; i < c.size(); i++) {
            sortSections(c.get(i).getArraySecciones(),hashPersons);
        }
    }
//Este método se carga en el algoritmo para la carga de restricciones de secciones en cada curso concreto:
    private ArrayList<Seccion> chargeArraySections(Restrictions r,Course course){
        ArrayList<Seccion> aux = new ArrayList<>();
        if(r.mapSecciones.containsKey(course.getIdCourse())){
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
    private void chargeStatusStudents(Restrictions r,Course c){
        for (int i = 0; i < c.getArraySecciones().size(); i++) {
            for (int j = 0; j < c.getArraySecciones().get(i).getIdStudents().size(); j++) {
                r.students.get(c.getArraySecciones().get(i).getIdStudents().get(j)).ocuparHueco(c.getArraySecciones().get(i).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(i).getNumSeccion());
            }
        }
    }
    /*
    private ArrayList<Integer> fillSections(Restrictions r, Course c,ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students) {

        int maxStudentSection = c.getMaxChildPerSection();
        if (maxStudentSection == 0) {
            maxStudentSection = CHILDSPERSECTION; // POR DEFECTO
        }
     //   int numMinStudents_Section = (int) Math.round(maxStudentSection * 0.70);
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();

        idsAsignados = c.getAllIds();
        chargeStatusStudents(r,c);
        int idCourse = c.getIdCourse();

        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);

        for (int i = 0; i < c.getArraySecciones().size(); i++) {
            ArrayList<Integer> auxStids = c.getArraySecciones().get(i).getIdStudents();
            stids.add(new Tupla(i, auxStids.clone()));

            if(!c.getArraySecciones().get(i).lockEnrollment){
                for (Integer j : studentsCourse) {
                    if (!idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(i).getPatronUsado())) {
                        stids.get(i).y.add(j);
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
            }
        }

        equilibrarGender(stids,students);

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());
            sortStudentsByPatron(stids, hashStudents_cantPatrones);
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }

        //inicializo el conjunto de estudiantes seleccionables
        ArrayList<Integer> diferencia;
        if (!stids.isEmpty()) {
            diferencia = stids.get(0).y;
        } else {
            diferencia = new ArrayList();
        }
        int i =0;
        int lastTeacher = -1;
        int lastStudent = -1;
        ArrayList<Integer> seccionesInsertadas = new ArrayList<>();
        while (i < stids.size())   {
            if(!c.getArraySecciones().get(stids.get(i).x).lockEnrollment){
            // while (i < stids.size()) { // recorrido a los secciones disponibles
                 int k = c.getArraySecciones().get(stids.get(i).x).getIdStudents().size();
                 for (Integer j : diferencia) { // studiantes
                     if ((k < maxStudentSection) && !idsAsignados.contains(j)
                             && students.get(j).patronCompatible(c.getArraySecciones().get(stids.get(i).x).getPatronUsado())) {
                         idsAsignados.add(j);
                         students.get(j).ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                         c.getArraySecciones().get(stids.get(i).x).addStudent(j);
                         k++;
                         lastStudent = i;
                     }
                 }

                 if (k  > 0){ // se llena los huecos de ese profesor incluyendole la seccion
                 //    secciones.add(new Seccion(t, k, sec.get(stids.get(i).x),students.get(stids.get(i).y.get(0)).getGenero(),idsbySeccion,stids.get(i).x,c.getSections(),true,true));
                     c.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(),c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                     c.getArraySecciones().get(stids.get(i).x).setLockSchedule(true);
                     c.getArraySecciones().get(stids.get(i).x).setLockEnrollment(true);
                     seccionesInsertadas.add(c.getArraySecciones().get(stids.get(i).x).getNumSeccion());

                     Teacher t_Aux = r.hashTeachers.get(c.getArraySecciones().get(stids.get(i).x).getIdTeacher());
                     t_Aux.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                     t_Aux.incrementarNumSecciones();

                     c.getArraySecciones().get(stids.get(i).x).setTeacher(t_Aux);
                     updateStids(stids,idsAsignados,studentsCourse,c,r);//fase prueba actualizara lista de estudiantes para que la lista stids se mantenga ordenada
                     i = 0 ;
                     //inicializo el conjunto de estudiantes seleccionables

                     if (!stids.isEmpty()) {
                         diferencia = stids.get(0).y;
                     } else {
                         diferencia = new ArrayList();
                     }
                     //  idsbySeccion = new ArrayList<>();

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
            else{
                int numSeccion = c.getArraySecciones().get(stids.get(i).x).getNumSeccion();
                if(!seccionesInsertadas.contains(c.getArraySecciones().get(stids.get(i).x).getNumSeccion())){
                    c.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(),c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                    c.getArraySecciones().get(stids.get(i).x).setLockSchedule(true);
                    c.getArraySecciones().get(stids.get(i).x).setLockEnrollment(true);
                    seccionesInsertadas.add(c.getArraySecciones().get(stids.get(i).x).getNumSeccion());


                    Teacher t_Aux = r.hashTeachers.get(c.getArraySecciones().get(stids.get(i).x).getIdTeacher());
                    if(t_Aux != null){
                        t_Aux.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                        t_Aux.incrementarNumSecciones();
                        c.getArraySecciones().get(stids.get(i).x).setTeacher(t_Aux);
                    }
                    if (!stids.isEmpty()) {
                           diferencia = stids.get(0).y;
                       }
                    else{
                           diferencia = new ArrayList();
                        }
                }
            }

            i++;
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
                        aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log));
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                //anadir = anadir.substring(0, anadir.length() - 2) + ".";
                Log.add(anadir);
            }
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }

            return ret;
        }
        return null;
    }
*/
//Aquí es donde entra cuando está bloqueado el lockSchedule pero está desactivado el lockEnrollment
//El patrón está asignado y bloqueado (no se puede usar otro), por lo que no hay necesidad de generación de patrones:   
//    
     private ArrayList<Integer> fillSection(Seccion currentSec,Restrictions r, Course c,ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students) {

        //int maxStudentSection = c.getMinChildPerSection();
        /*if (maxStudentSection == 0) {
            maxStudentSection = (int) Math.round(c.getMaxChildPerSection() * 0.7); // POR DEFECTO --> Ya no se coge esta opcion porque el mínimo de estudiantes por sección se puede personalizar el porcentaje y número.
        }*/
        int minStudentSection = c.getMinChildPerSection();

//Aquí al principio se declaran los mismos arrays que en generatePattern_Section:
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        ArrayList<Integer> ret = new ArrayList<>();
        HashMap<Integer,String> genderStids = new HashMap<>();
//Se obtiene el id de los estudiantes de la sección concreta.
//Estos ids van siendo cada vez más según se van recorriendo las secciones (fuera del fillSection). Ejemplo ficticio: en la primera vuelta tiene 6 estudiantes,
//pero en las siguientes vueltas tiene 12, 18...        
        idsAsignados = c.getAllIds();
//Se aplica el chargeStatusStudents(con restricciones y curso concretos):    
//Lo que hace este método es:
//para cada seccion conjuntamente con cada idStudents, se obtiene el patron usado(ya está asignado en RenWeb, por seccion) y el id de curso/seccion:
//Resumen: se aplica el patron usado (tupla de coordenadas) a cada estudiante de cada seccion, teniendo en cuenta restrcciones y curso.  
        chargeStatusStudents(r,c);

        int idCourse = c.getIdCourse();
        ArrayList<Seccion> auxGenderSecciones = new ArrayList<Seccion>();
//Este hash almacena la cantidad de opciones válidas para cada estudiante(idstudiante, cantidad opciones válidas)
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
//initHashStudents hace que se inicialicen a 0 los estudiantes para sumarlos de 1 en 1:        
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
//Si el curso no es rellenado por restricciones de genero (luego se hace equilibrado):        
        if(!c.isGR()){
            for (int i = 0; i < c.getArraySecciones().size(); i++) {
                ArrayList<Integer> auxStids = c.getArraySecciones().get(i).getIdStudents();
                stids.add(new Tupla(i, auxStids.clone()));
                    for (Integer j : studentsCourse) {
                        if (!idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(i).getPatronUsado())) {
                            stids.get(i).y.add(j);
                            hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                        }
                    }
            }


            /*
                SE PODRIA MODIFICAR EL ARRAY STIDS Y AGREGAR UN CAMPO MAS QUE INDICARA EL GENDER DE LA SECCION CUANDO SE INDIQUE QUE SE QUIERE SECICON SEPARADAS POR GENDER
                SE DUPLICARIAN ESTAS Y SE DARIA A CADA UNA DE LAS MITADAS EL GENDER
                - SE TIENEN QUE DUPLICAR PARA PODER HACER LA ORDENACION POR NUMERO DE PATRONES DE STUDENTS.
            */
            equilibrarGender(stids,students);
        }
//fillSection con restricciones de genero:        
        else{

            for (int i = 0; i < c.getArraySecciones().size(); i++) {
                ArrayList<Integer> stdMales = new ArrayList<>();
                ArrayList<Integer> stdFemales = new ArrayList<>();
                ArrayList<Integer> auxStids = c.getArraySecciones().get(i).getIdStudents();
                stids.add(new Tupla(i, auxStids.clone()));
                for (Integer j : studentsCourse) {
                    if (!idsAsignados.contains(j) && students.get(j).patronCompatible(c.getArraySecciones().get(i).getPatronUsado())) {
                         if(students.get(j).getGenero().equals("Female")){
                            stdFemales.add(j);
                        }
                        else{ //male or null
                            stdMales.add(j);
                        }
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
                 if(stdMales.size() >= stdFemales.size()){
                   // stids.get(i).y. = new ArrayList<>();
                    stids.set(i, new Tupla(i, new ArrayList<>(stdMales)));
                    genderStids.put(i, "Male");
                }
                else{
                    stids.set(i, new Tupla(i, new ArrayList<>(stdFemales)));
                    genderStids.put(i, "Female");
                }
            }


            /*

              for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            ArrayList<Integer> stdMales = new ArrayList<>();
            ArrayList<Integer> stdFemales = new ArrayList<>();
            for (Integer j : studentsCourse) {
                if (students.get(j).patronCompatible(sec.get(i))) {
                    if(!c.isGR()){
                        stids.get(i).y.add(j);
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                    else{// is GR
                        if(students.get(j).getGenero().equals("Female")){
                            stdFemales.add(j);
                        }
                        else{ //male or null
                            stdMales.add(j);
                        }
                        hashStudents_cantPatrones.put(j, hashStudents_cantPatrones.get(j) + 1);
                    }
                }
            }
            if(c.isGR()){
                if(stdMales.size() >= stdFemales.size()){
                   // stids.get(i).y. = new ArrayList<>();
                    stids.set(i, new Tupla(i, new ArrayList<>(stdMales)));
                    genderStids.put(i, "Male");
                }
                else{
                    stids.set(i, new Tupla(i, new ArrayList<>(stdFemales)));
                    genderStids.put(i, "Female");
                }
            }
            else{
                genderStids.put(i, "");
            }


        }






            */
        }
        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());
            sortStudentsByPatron(stids, hashStudents_cantPatrones);
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }


        int i =0;
        int lastTeacher = -1;
        int lastStudent = -1;
        boolean exito = false;
        while (i < stids.size() && !exito)   {
            if(c.getArraySecciones().get(stids.get(i).x).getNumSeccion() == currentSec.getNumSeccion()){
            // while (i < stids.size()) { // recorrido a los secciones disponibles
                 int k = c.getArraySecciones().get(stids.get(i).x).getIdStudents().size();

                 for (Integer j : stids.get(i).y){ // studiantes
                     if ((k < minStudentSection) && !idsAsignados.contains(j)
                             && students.get(j).patronCompatible(c.getArraySecciones().get(stids.get(i).x).getPatronUsado())) {
                         idsAsignados.add(j);
                         students.get(j).ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                         c.getArraySecciones().get(stids.get(i).x).addStudent(j);
                         k++;
                         lastStudent = i;
                     }
                 }

                 if (k  > 0){ // se llena los huecos de ese profesor incluyendole la seccion
                 //    secciones.add(new Seccion(t, k, sec.get(stids.get(i).x),students.get(stids.get(i).y.get(0)).getGenero(),idsbySeccion,stids.get(i).x,c.getSections(),true,true));
                     c.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(),c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                     c.getArraySecciones().get(stids.get(i).x).setLockSchedule(true);
                     if(k == c.getMaxChildPerSection())
                            c.getArraySecciones().get(stids.get(i).x).setLockEnrollment(true);

                     Teacher t_Aux  = new Teacher();
                     if(r.hashTeachers.containsKey(c.getArraySecciones().get(stids.get(i).x).getIdTeacher())) {
                      t_Aux = r.hashTeachers.get(c.getArraySecciones().get(stids.get(i).x).getIdTeacher());
                        }
                     else{
                         t_Aux.setName("No found in template courses");
                     }
                     t_Aux.ocuparHueco(c.getArraySecciones().get(stids.get(i).x).getPatronUsado(), c.getIdCourse() * 100 + c.getArraySecciones().get(stids.get(i).x).getNumSeccion());
                     t_Aux.incrementarNumSecciones();

                     c.getArraySecciones().get(stids.get(i).x).setTeacher(t_Aux);
                     exito = true;
                 }
            }
            i++;
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
                        aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log));
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

   private void saveXML_FTP(String yearId, String templateId, String schoolCode,Restrictions r) {
        //get the file chosen by the user
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
	//System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
            String fecha = dateFormat.format(date);
            fecha = fecha.replace(" ", "_");
            fecha = fecha.replace("/", "_");
            fecha = fecha.replace(":", "_");
            String filename = yearId + "_" + templateId+"_"+fecha+".xml";
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
            // append child elements to root element

            for (Course t : r.courses) {
                for (int j = 0; j < t.getArraySecciones().size(); j++) {
                    for (int k = 0; k < t.getArraySecciones().get(j).getIdStudents().size(); k++) {
                            students.appendChild(getStudent(doc,""+t.getArraySecciones().get(j).getIdStudents().get(k),""+t.getIdCourse(),""+(j+1),yearId,""+t.getArraySecciones().get(j).getClassId()));
                    }
                }
            }

            Element cursos = doc.createElement("Courses");
            for (Course t : r.courses) {
                for (int j = 1; j < t.getArraySecciones().size(); j++) {
                    //if()
                    cursos.appendChild(getCursos(doc,""+t.getIdCourse(),""+j,""+t.getArraySecciones().get(j).getIdTeacher(),yearId,""+t.getArraySecciones().get(j).getClassId()));
                }
            }

//private Node getBloques(Document doc, String day, String begin, String tempId, String courseId, String section) {

            Element bloques = doc.createElement("Blocks");
            for (Course t : r.courses) {
               /* for (int i = 0; i < TAMY; i++) {
                    for (int j = 0; j < TAMX; j++) {

                        if ( !t.getHuecos()[j][i].contains("0")) {
                           if(t.getHuecos()[j][i].contains("and")){
                               String[] partsSections = t.getHuecos()[j][i].split("and");
                               for (String partsSection : partsSections) {
                                   String seccionClean = partsSection.replace(" ", "");
                                   if(!seccionClean.equals("0"))bloques.appendChild(getBloques(doc,""+(j+1),""+(i+1),templateId,""+t.getIdCourse(),seccionClean,yearId));
                               }
                           }
                           else
                           bloques.appendChild(getBloques(doc,""+(j+1),""+(i+1),templateId,""+t.getIdCourse(),""+t.getHuecos()[j][i],yearId));
                        }
                    }
                }*/
                for (int i = 0; i < t.getArraySecciones().size(); i++) {
                    for (int j = 0; j < t.getArraySecciones().get(i).getPatronUsado().size(); j++) {
                        int col = (int) t.getArraySecciones().get(i).getPatronUsado().get(j).x +1;
                        int row = (int) t.getArraySecciones().get(i).getPatronUsado().get(j).y +1;

                        bloques.appendChild(getBloques(doc,""+(col),""+(row),templateId,""+t.getIdCourse(),""+t.getArraySecciones().get(i).getNumSeccion(),yearId,""+t.getArraySecciones().get(i).getClassId()));
                    }
                }
            }

           /* students.appendChild(getCompany(doc,  "Paypal", "Payment", "1000"));
            students.appendChild(getCompany(doc, "eBay", "Shopping", "2000"));
            students.appendChild(getCompany(doc, "Google", "Search", "3000"));*/

            mainRootElement.appendChild(students);
             mainRootElement.appendChild(cursos);
              mainRootElement.appendChild(bloques);
            // output DOM XML to console
            /*Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult console = new StreamResult(System.out);
            transformer.transform(source, console);
*/

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            ftpClient.storeFile(filename, is);
            ftpClient.logout();

        } catch (Exception ex) {
            System.err.println("");
        }

    }
    private Node getStudent(Document doc, String name, String courseId, String section,String yearId,String classID) {
        Element company = doc.createElement("Student");
        company.appendChild(getCompanyElements(doc, company, "student_ID", name));
        company.appendChild(getCompanyElements(doc, company, "course_ID", courseId));
        company.appendChild(getCompanyElements(doc, company, "section", section));
        company.appendChild(getCompanyElements(doc, company, "YearID", yearId));
        company.appendChild(getCompanyElements(doc, company, "classID", classID));
        return company;
    }

    private Node getCursos(Document doc, String courseId, String section, String idTeacher,String yearId,String classID) {
        Element company = doc.createElement("Course");
        company.appendChild(getCompanyElements(doc, company, "course_ID", courseId));
        company.appendChild(getCompanyElements(doc, company, "section", section));
        company.appendChild(getCompanyElements(doc, company, "teacher_ID", idTeacher));
        company.appendChild(getCompanyElements(doc, company, "YearID", yearId));
        company.appendChild(getCompanyElements(doc, company, "classID", classID));
        return company;
    }

    private Node getBloques(Document doc, String day, String begin, String tempId, String courseId, String section,String yearId,String classID) {
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

    // utility method to create text node
    private Node getCompanyElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

    private class CompConjuntos implements Comparator<Tupla<Integer, ArrayList<Integer>>> {
        @Override
        public int compare(Tupla<Integer, ArrayList<Integer>> e1, Tupla<Integer, ArrayList<Integer>> e2) {
            if (e1.y.size() < e2.y.size()) {
                return 1;
            } else {
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
        /*  for (int i = 0; i < stids.size(); i++){
            if(stids.get(i).x == pos) return i;
        }*/
        int maxAlumnos = stids.get(0).getY().size();

        for (int i = 0; i < stids.size(); i++) {
            for (int j = 0; j < sec.get(0).size(); j++) {
                if (((Integer) sec.get(stids.get(i).x).get(j).x == x && (Integer) sec.get(stids.get(i).x).get(j).y == y)
                        || ((Integer) sec.get(stids.get(i).x).get(j).x == y && (Integer) sec.get(stids.get(i).x).get(j).y == x)
                        && stids.get(i).y.size() >= maxAlumnos) {
                    return i;
                }
            }

            //sec.get(stids.get(i).x)
        }

        return -1;
    }

    private void sortStidsByPriority(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<ArrayList<Tupla>> sec, Course c, Restrictions r) {
        /* if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
            for (int h = 0; h < c.getPreferedBlocks().get(c.getSections() - 1).size(); h++) {
                ArrayList<Tupla> auxTupla = new ArrayList();
                auxTupla.add(new Tupla(c.getPreferedBlocks().get(c.getSections() - 1).get(h).x - 1, c.getPreferedBlocks().get(c.getSections() - 1).get(h).y - 1));
                if (!idsAsignados.contains(j) && students.get(j).patronCompatible(auxTupla)) {
                    idsAsignados.add(j);
                    students.get(j).ocuparHueco(auxTupla, c.getIdCourse() * 100 + c.getSections());
                    k++;
                    lastStudent = i;
                }
            }
        }*/

        if ((c.getSections() <= c.getPreferedBlocks().size()) && c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
            ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
            ArrayList<Integer> auxRes = new ArrayList<>();

            for (int i = 0; i < c.getPreferedBlocks().get(c.getSections() - 1).size(); i++) {
                Tupla<Integer, ArrayList<Integer>> tuplaAux = new Tupla(stids.get(i).x, stids.get(i).y);
                int res = buscarPosBlock((c.getPreferedBlocks().get(c.getSections() - 1).get(i).x) - 1, (c.getPreferedBlocks().get(c.getSections() - 1).get(i).y) - 1, stids, sec);
                if (res != -1) {
                   auxRes.add(res);
                   auxStids.add(tuplaAux);
                   /* stids.set(i, stids.get(res));
                    stids.set(res, tuplaAux);*/
                    /*
                        ESTO MODIFICARA EL STIDS PONIENDO DELANTE LAS PRIORITARIAS
                     */

                }

            }

            for (int i = 0; i < stids.size(); i++) {
                if(!auxRes.contains(i)){
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
            // Collections.sort(stids,new CompConjuntos());
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }
        sortStidsByPriority(stids, sec, c, r);

        //inicializo el conjunto de estudiantes seleccionables
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

        //ordenar por prioridad los teachers
        ArrayList<Teacher> teachersOrderByPriority = new ArrayList<>();
        teachersOrderByPriority = teachers;
        if (c.isBalanceTeachers()) {
            teachersOrderByPriority = sortTeacherByPriorty(teachers, c.getTrestricctions(), c.getMinSections());
            //teachers = teachersOrderByPriority;
        }
        // aqui meter un else que no los ordene pero si inserte elementos en teachersorderbypriority

        //recorro la lista de conjuntos y la de profesores
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
                        aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log));
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                //anadir = anadir.substring(0, anadir.length() - 2) + ".";
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
    private ArrayList<Student> sortByGender( ArrayList<Student> auxStudents){
        ArrayList<Student> aux = new ArrayList<>();


        return aux;
    }

   private void sortStudentsByPatron(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Integer> hashStudents_cantPatrones) {
        for (int i = 0; i < stids.size(); i++) {
            ArrayList<Student> auxStudents = new ArrayList<>();
            for (int j = 0; j < stids.get(i).getY().size(); j++) {
                auxStudents.add(new Student(stids.get(i).getY().get(j), hashStudents_cantPatrones.get(stids.get(i).getY().get(j))));
            }
            Collections.sort(auxStudents, new CustomComparatorStudent());
            /*if(GR){
                auxStudents = sortByGender(auxStudents);
            }*/
            ArrayList<Integer> auxIds = new ArrayList<>();
            for (int j = 0; j < auxStudents.size(); j++) {
                auxIds.add(auxStudents.get(j).getId());
            }

            stids.set(i, new Tupla(stids.get(i).x, auxIds));

        }
    }
    /*private void sortStudentsByPatron(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Integer> hashStudents_cantPatrones) {
        for (int i = 0; i < stids.size(); i++) {
            ArrayList<Student> auxStudents = new ArrayList<>();
            for (int j = 0; j < stids.get(i).getY().size(); j++) {
                auxStudents.add(new Student(stids.get(i).getY().get(j), hashStudents_cantPatrones.get(stids.get(i).getY().get(j))));
            }
            Collections.sort(auxStudents, new CustomComparatorStudent());
            ArrayList<Integer> auxIds = new ArrayList<>();
            for (int j = 0; j < auxStudents.size(); j++) {
                auxIds.add(auxStudents.get(j).getId());
            }
            stids.set(i, new Tupla(stids.get(i).x, auxIds));
        }
    }*/



  private ArrayList<Tupla<Integer, ArrayList<Integer>>> dividirPatronesByGender(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids,HashMap<Integer, Student>  students){
      ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
      ArrayList<Integer> auxStidsMale = new ArrayList<>();
      ArrayList<Integer> auxStidsFemale = new ArrayList<>();

        for (Tupla<Integer, ArrayList<Integer>> stid : stids) {
            auxStidsMale = new ArrayList<>();
            auxStidsFemale = new ArrayList<>();
            for (Integer y : stid.y) {
              if(students.containsKey(y)){
                  if(students.get(y).getGenero().equals("Male")){
                      auxStidsMale.add(y);
                  }
                  else{//female
                      auxStidsFemale.add(y);
                  }
              }
            }

          auxStids.add(new Tupla(stid.x,auxStidsFemale.clone()));
           auxStids.add(new Tupla(stid.x,auxStidsMale.clone()));
        }
      return auxStids;
  }
  private void updateStids(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids,ArrayList<Integer> idsAsignados,ArrayList<Integer> studentsCourse
  ,Course c, Restrictions r){
      for (int i = 0; i < stids.size(); i++) {
          ArrayList<Integer> auxStids = new ArrayList<>();
          for (int j = 0; j < stids.get(i).y.size(); j++) {
              if(!idsAsignados.contains(stids.get(i).y.get(j))){
                  auxStids.add(stids.get(i).y.get(j));
              }
          }
          stids.set(i, new Tupla(stids.get(i).x,auxStids.clone()));
      }
        try {
            stids.sort(new CompConjuntos());
            // Collections.sort(stids,new CompConjuntos());
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }

        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        //HashMap <Integer, String> hashSeccion_Gender = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);

        //sortStidsByPriority(stids, sec, c, r);
        sortStudentsByPatron(stids, hashStudents_cantPatrones);

  }
//Equilibrado entre hombres y mujeres que se aplica en getPatrones en el caso de que sea una sección en el que no se apliquen restricciones de género:  
  private ArrayList<Tupla<Integer, ArrayList<Integer>>> equilibrarGender(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Student> students){
      ArrayList<Tupla<Integer, ArrayList<Integer>>> auxStids = new ArrayList<>();
      ArrayList<Integer> auxStidsMale = new ArrayList<>();
      ArrayList<Integer> auxStidsFemale = new ArrayList<>();


        for (Tupla<Integer, ArrayList<Integer>> stid : stids) {
            auxStidsMale = new ArrayList<>();
            auxStidsFemale = new ArrayList<>();
            for (Integer y : stid.y) {
              if(students.containsKey(y)){
                  if(students.get(y).getGenero().equals("Male")){
                      auxStidsMale.add(y);
                  }
                  else{//female
                      auxStidsFemale.add(y);
                  }
              }
            }
            int maxLength = Math.max(auxStidsFemale.size(), auxStidsMale.size());
            ArrayList<Integer> auxEquilibrado = new ArrayList<>();

            for (int i = 0; i < maxLength; i++) {
                if(i < auxStidsFemale.size()){
                    auxEquilibrado.add(auxStidsFemale.get(i));
                }
                if(i < auxStidsMale.size()){
                    auxEquilibrado.add(auxStidsMale.get(i));
                }
            }
            //Collections.shuffle(auxEquilibrado);

            auxStids.add(new Tupla(stid.x,auxEquilibrado.clone()));
           /*auxStids.add(new Tupla(stid.x,auxStidsFemale.clone()));
           auxStids.add(new Tupla(stid.x,auxStidsMale.clone()));*/
        }


      return auxStids;
  }

    //FUNCIONANDO VERSION CLIENTE
    private ArrayList<Integer> studentSections(Restrictions r, ArrayList<Teacher> teachers, Course c, int minSection, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Integer> studentsCourseSection, HashMap<Integer, Student> students, ArrayList<Integer> rooms) {


        int maxStudentSection = c.getMaxChildPerSection();

        if (maxStudentSection == 0) {
            maxStudentSection = CHILDSPERSECTION; // POR DEFECTO
        }

        int numMinStudents_Section = (int) Math.round(maxStudentSection * 0.70);
      //  int numMinStudents_Section = (int) Math.round(maxStudentSection * 0.8);
        ArrayList<Seccion> secciones = new ArrayList<>();
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        //HashMap <Integer, String> hashSeccion_Gender = new HashMap<>();
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

        /*
        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));

            for (Integer j : studentsCourse) {
                ArrayList<Tupla<Integer, Integer>> arrayAux = c.getPreferedBlocks().get(studentsCourseSection.get(j));

                for (int k = 0; k < arrayAux.size(); k++) {
                    if(students.get(j).patronCompatible2(arrayAux.get(k))){
                        stids.add(new Tupla(k, new ArrayList<>()));

                        stids.get(i).y.add(j);
                    }
                }

                if (students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                }
            }
        }*/
        //  updateStidsWithUserDefined(stids, r.totalBlocks);

       /* if(c.isGR()){ //dividira cada patron por gender
            stids = dividirPatronesByGender(stids,students);
        }
        else{/*/
        equilibrarGender(stids,students);
        //}

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());
            // Collections.sort(stids,new CompConjuntos());
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }

        sortStidsByPriority(stids, sec, c, r);
       sortStudentsByPatron(stids, hashStudents_cantPatrones);


        //inicializo el conjunto de estudiantes seleccionables
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

        //ordenar por prioridad los teachers
        /*ArrayList<Teacher> teachersOrderByPriority = new ArrayList<>();
        teachersOrderByPriority = teachers;

        if (c.isBalanceTeachers()) {
            teachersOrderByPriority = sortTeacherByPriorty(teachers, c.getTrestricctions(), c.getMinSections());
            //teachers = teachersOrderByPriority;
        }*/
        // aqui meter un else que no los ordene pero si inserte elementos en teachersorderbypriority


        //recorro la lista de conjuntos y la de profesores
        ArrayList<Integer> idsbySeccion = new ArrayList<>();
        ArrayList<Teacher> teachersForCourse = new ArrayList<>();

        for(int y =0;y< teachers.size();y++){
            if(c.getTrestricctions().contains(teachers.get(y).getIdTeacher()))
                teachersForCourse.add(teachers.get(y));
        }

       //while (i < stids.size() && secciones.size() < c.getMinSections())   {
        while (i < stids.size()) { // recorrido a los bloques disponibles
            for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                if ( c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
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

                            secciones.add(new Seccion(t, k, sec.get(stids.get(i).x),students.get(stids.get(i).y.get(0)).getGenero(),idsbySeccion,stids.get(i).x,c.getSections(),true,true,0,c.getIdCourse()));
                            c.ocuparHueco(sec.get(stids.get(i).x));

                          //  updateStids(stids,idsAsignados,studentsCourse,sec,c,r);//fase prueba actualizara lista de estudiantes para que la lista stids se mantenga ordenada
                           // i = 0 ;
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
              //  c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes

        /*AQUI INTENTARE METER LOS ALUMNOS NO FUERON METIDOS EN NINGUNA SECCION
              SE METEN AQUI PARA INTENTAR EQUILIBRAR LAS SECCIONES*/

        ArrayList<Integer> alumnosNoAsignados = conjuntos.diferencia(studentsCourse, idsAsignados);
        ArrayList<Boolean> marcasAlumnos = new ArrayList<>();

        for (int j = 0; j < alumnosNoAsignados.size(); j++) {
            marcasAlumnos.add(false);
        }

        Collections.sort(secciones, new CompSeccionesStudents());

        //     students_Section.add(-1);
        /* for (int j = 0; j < students_Section.get(i).y; j++) {
            alumnosNoAsignados.ge
        }*/

     //   c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes

        for (int j = 0; j < secciones.size(); j++) {
            for (int k = 0; k < alumnosNoAsignados.size(); k++) {
                if (!marcasAlumnos.get(k) && secciones.get(j).getNumStudents() < maxStudentSection) {
                    if (students.get(alumnosNoAsignados.get(k)).patronCompatible(secciones.get(j).getPatronUsado())) {
                        if(!c.isGR()){
                            idsAsignados.add(alumnosNoAsignados.get(k));
                            students.get(alumnosNoAsignados.get(k)).ocuparHueco(secciones.get(j).getPatronUsado(), c.getIdCourse() * 100 + (j+1));
                            secciones.get(j).IncrNumStudents();
                            secciones.get(j).addStudent(alumnosNoAsignados.get(k));
                            marcasAlumnos.set(k,true);
                        }
                        else{
                            if(secciones.get(j).getGender().equals(students.get(alumnosNoAsignados.get(k)).getGenero())){
                                idsAsignados.add(alumnosNoAsignados.get(k));
                                students.get(alumnosNoAsignados.get(k)).ocuparHueco(secciones.get(j).getPatronUsado(), c.getIdCourse() * 100 + (j+1));
                                secciones.get(j).IncrNumStudents();
                                secciones.get(j).addStudent(alumnosNoAsignados.get(k));

                                marcasAlumnos.set(k,true);
                            }
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
                        aux = students.get(i2).listPatronesCompatibles(c.opciones(r.totalBlocks, Log));
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                //anadir = anadir.substring(0, anadir.length() - 2) + ".";
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
            // Collections.sort(stids,new CompConjuntos());
        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }
        sortStidsByPriority(patronesStudents, sec, c, r);

        ArrayList<Seccion> arraySeccion = new ArrayList<>(minsections);
        ArrayList<Seccion> mejorArraySeccion = new ArrayList<>(minsections);
        /*   ArrayList<Teacher> teachersOrderByPriority = new ArrayList<>();

        teachersOrderByPriority = teachers;
        if (c.isBalanceTeachers()) {
            teachersOrderByPriority = sortTeacherByPriorty(teachers, c.getTrestricctions(), c.getMinSections());
            //teachers = teachersOrderByPriority;
        }
         */
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

    private ArrayList<Teacher> sortTeacherByPriorty(ArrayList<Teacher> teachers, ArrayList<Integer> preferedTeachers, int numSections) {
        // ESTO TAMBIEN DEPENDERA DE LA CANTIDAD DE SECCIONES QUE TENGA CADA PROFESOR HASTA ESTE MOMENTO
        ArrayList<Teacher> aux = new ArrayList<>();
        if (preferedTeachers.isEmpty()) {
            return aux;
        }

        for (int i = 0; i < numSections; i++) {
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
        /*
        for (int i = 0; i < patronStudents.size(); i++) {
            for (int t = 0; t < arrayTeachers.size(); t++) {
                if (esValido()) {
                    marcas.get(t).set(i, true); // marco
                    int k_actual = k;
                    int numAlumnosActual = numAlumnos;
                    numAlumnos += patronStudents.get(i).getY().size() % maxStudentSeccion;

                    ArrayList<Tupla<Integer, ArrayList<Integer>>> patronStudentsActual = (ArrayList<Tupla<Integer, ArrayList<Integer>>>) patronStudents.clone();
                    actualizarPatronStudents(patronStudents, patronStudents.get(i).getY());

                    sol.set(k, new Seccion(patronStudents.get(i).getY(), arrayTeachers.get(t),
                            patronStudents.get(i).getY().size() % maxStudentSeccion));

                    if (sectionsAsignadas == numSeccionesMax) { //sol final

                        if (mejorNumAlumnos <= numAlumnos) { // mejor solucion
                            // copiarMejorSolucion();
                        }
                    } else {
                        backTrackingSchedule(patronStudents, k + 1, sol, mejorSol, arrayTeachers,
                                sectionsAsignadas, numSeccionesMax, maxStudentSeccion, numAlumnos, mejorNumAlumnos, numStudentTotal, marcas);
                    }
                    k = k_actual;
                    numAlumnos = numAlumnosActual;
                    patronStudents = patronStudentsActual;
                    marcas.get(t).set(i, false);
                }
            }
        }*/
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

    private String getGenero(int idGenero){
        switch (idGenero){
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
}
