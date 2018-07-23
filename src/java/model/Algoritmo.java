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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    public void algo(ModelAndView mv, Restrictions r, String schoolCode,String yearId, String templateId ) {

        int vueltas =0;
        for (Course course : r.courses) {
            
            HashMap<Integer,Integer> teachers_numSections = new HashMap<>();
                           
            vueltas++;
            if(!containsValueInLinkedCourse(r,course.getIdCourse())){
                course.setArraySecciones(chargeArraySections(r,course));

                int maxSections ;
                if(course.getMaxSections() == null || course.getMaxSections().equals("")){
                 /*   maxSections = r.studentsCourse.get(course.getIdCourse()).size() / course.getMaxChildPerSection();
                    if(r.studentsCourse.get(course.getIdCourse()).size() % course.getMaxChildPerSection() !=0)
                        maxSections++;

                */
                    maxSections = 2;
                }
                else{
                    maxSections = Integer.parseInt(course.getMaxSections());
                }
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

             if(course.getOpcionesPatternGroup().isEmpty() && needGenerateOptions(course))
                    opciones = course.opciones(r.totalBlocks,Log);
                else if(!course.getOpcionesPatternGroup().isEmpty() ){
                    opciones = course.getOpcionesPatternGroup();
               }
                                                        ArrayList<Integer> noAsign = new ArrayList<>();

                if(r.studentsCourse.containsKey(course.getIdCourse())){
                    noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
                    if(r.isShuffleRosters()){
                        Collections.shuffle(noAsign);
                    }

                }

                if(course.getIdCourse() == 1245){
                    System.out.println("model.Algoritmo.algo()");
                }
                // FALTA ACABAR LOS LOGS DE LAS FUNCIONES MODIFICADAS
                for (int i = 0; i < course.getArraySecciones().size(); i++) {
                    if(!course.getArraySecciones().get(i).lockSchedule && course.getArraySecciones().get(i).lockEnrollment){
                        noAsign = generatePattern_Section(r,r.teachers,course,opciones,
                              noAsign,r.students,course.getArraySecciones().get(i),teachers_numSections);
                    }
                    else if(course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment){
                        noAsign = fillSection(course.getArraySecciones().get(i),r, course,noAsign, r.students);
                        course.getArraySecciones().get(i).setPatternRenWeb(1);
                    }
                    else if(!course.getArraySecciones().get(i).lockSchedule && !course.getArraySecciones().get(i).lockEnrollment){
                       noAsign = generatePattern_Section(r,r.teachers,course,opciones,
                               noAsign,r.students,course.getArraySecciones().get(i),teachers_numSections);
                    }
                    else{ // lockSchedule && lockEnrollment
                        for (int j = 0; j < course.getArraySecciones().get(i).getIdStudents().size(); j++) {
                            r.students.get(course.getArraySecciones().get(i).getIdStudents().get(j)).ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(), course.getIdCourse() * 100 + course.getArraySecciones().get(i).getNumSeccion());
                        }
                        course.ocuparHueco(course.getArraySecciones().get(i).getPatronUsado(),course.getArraySecciones().get(i).getNumSeccion());
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

                for (int j = 0; j < noAsign.size(); j++) {
                    marcasAlumnos.add(false);
                }

                Collections.sort(course.getArraySecciones(), new CompSeccionesStudents());

                //     students_Section.add(-1);
                /* for (int j = 0; j < students_Section.get(i).y; j++) {
                    alumnosNoAsignados.ge
                }*/

             //   c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes


                for (int j = 0; j < course.getArraySecciones().size(); j++) {
                    if(!course.getArraySecciones().get(j).lockEnrollment){
                    for (int k = 0; k < noAsign.size(); k++) {
                        if (!marcasAlumnos.get(k) && course.getArraySecciones().get(j).getIdStudents().size() < course.getMaxChildPerSection()) {
                            if (r.students.get(noAsign.get(k)).patronCompatible(course.getArraySecciones().get(j).getPatronUsado())) {
                                if(!course.isGR()){
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
                if (encontrado) {
                    Course courseAsociado = r.courses.get(k);
                    ArrayList<Integer> seccionesHabilitadas = new ArrayList<>();
                    seccionesHabilitadas = r.getLinkedCourses().get("" + course.getIdCourse()).getSectionsLinkeadas();
                        ArrayList<ArrayList<Tupla>> opcionesAsoc = new ArrayList<>();

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
        //mv.addObject("rooms", r.rooms);
      
        mv.addObject("hashTeachers",r.hashTeachers);
       // mv.addObject("grouprooms", r.groupRooms);
        mv.addObject("log", Log);
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
    private ArrayList<Integer> generatePattern_Section(Restrictions r, ArrayList<Teacher> teachers, Course c, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students,Seccion currentSec,HashMap<Integer,Integer> teachers_numSections) {

        int minStudentSection = c.getMaxChildPerSection();
        minStudentSection = (int) Math.round(minStudentSection*0.7);

        ArrayList<Integer> ret = new ArrayList<>();
        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
         idsAsignados = c.getAllIds();
        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
        HashMap<Integer,String> genderStids = new HashMap<>();


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
        int idCourse = c.getIdCourse();
        if(idCourse == 806){
            System.err.println("");
        }
        if(!c.isGR())
            equilibrarGender(stids,students);

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());

        } catch (Exception e) { // esto da errores aveces solucionar comparador
            //return null;
        }

        sortStidsByPriority(stids, sec, c, r);
        sortStudentsByPatron(stids, hashStudents_cantPatrones);


        int lastTeacher = -1;
        int lastStudent = -1;
        int i = 0;

        //recorro la lista de conjuntos y la de profesores

        ArrayList<Teacher> teachersForCourse = new ArrayList<>();

       //  teachersOrderByPriority = teachers;
        
            
        for(int y =0;y< teachers.size();y++){
            if(c.getTrestricctions().contains(teachers.get(y).getIdTeacher())){
                teachersForCourse.add(teachers.get(y));
                if(!teachers_numSections.containsKey(teachers.get(y).getIdTeacher()))
                    teachers_numSections.put(teachers.get(y).getIdTeacher(),0);
            }
        }
        /*if(c.isBalanceTeachers()){
            Collections.sort(teachersForCourse, new teacherComparator(teachers_numSections));
        }
        */
        boolean exito = false;
        // HAY QUE REHACER ESTAQ PARTE YA QUE BASTARIA CON CREA R LA PRIMERA SECCION
        // Y COPIAR LA LISTA DE ESTUDIANTES SI NO LOS TIENE BLOQUEADO
        // SI LO TUVIERA BLOQUEADO CAMBIAR LOS NSTUDENTS COURSE QUE ENTRAN
        // POR PARAMETRO, ESTOS DEBERIAN CAMBIAR.
       //while (i < stids.size() && secciones.size() < c.getMinSections())   {
        while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
            //int contR = 0;
          //  while(!exito && (!r.isActiveRooms() || contR < c.getRooms().size())){
               // if(!r.isActiveRooms() || (r.rooms.containsKey(c.getRooms().get(contR)) && r.rooms.get(c.getRooms().get(contR)).patronCompatible(sec.get(stids.get(i).x)))){
                    for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                         if ( !exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                                 && t.patronCompatible(sec.get(stids.get(i).x))) {

                                 int k = currentSec.getIdStudents().size();
                                 lastTeacher = i;
     // VAMOS A TENER QUE EN CUENTA CUANDO YA E3STAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
     //  EL PATTERN PARA LOS ESTUDIANTYES YA CMATRICULADOS CUMPLEN ESA RESTRICCION
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
                                 //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                                 //y añadimos la seccion a la tabla del curso.
                                 if (k > 0) { // se llena los huecos de ese profesor incluyendole la seccion

                                     currentSec.setPatronUsado(sec.get(stids.get(i).x));

                                     currentSec.setIdTeacher(t.getIdTeacher());
                                     Teacher t_Aux = r.hashTeachers.get(t.getIdTeacher());
                                  // Teacher t_Aux = new Teacher();
                                     t_Aux.ocuparHueco( currentSec.getPatronUsado(), c.getIdCourse() * 100 +  currentSec.getNumSeccion());
                                     t_Aux.incrementarNumSecciones();
                                     if(teachers_numSections.containsKey(t.getIdTeacher())){
                                       // teachers_numSections.get[t.getIdTeacher()++;
                                        teachers_numSections.put(t.getIdTeacher(), teachers_numSections.get(t.getIdTeacher()) + 1);
                                    }
                                 //    r.rooms.get(c.getRooms().get(contR)).ocuparHueco(c.getIdCourse(), currentSec.getNumSeccion(), currentSec.getPatronUsado());
                                     
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
                     }/*
                    
                }
               contR++;*/
            //}
            i++;
        }

        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.

        if (idsAsignados.size() != studentsCourse.size()) {
            System.out.println("FAILURE");
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
        while (i < stids.size() && !exito) { // recorrido a los bloques disponibles
          /*  int contR = 0;
            while(!r.isActiveRooms() || contR < c.getRooms().size()){
                if(!r.isActiveRooms() || (r.rooms.containsKey(c.getRooms().get(contR)) && r.rooms.get(c.getRooms().get(contR)).patronCompatible(sec.get(stids.get(i).x)))){
              */
                    for (Teacher t : teachersForCourse) { // recorrido a los teachers  totales
                         if ( !exito && c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                                 && t.patronCompatible(sec.get(stids.get(i).x))) {

                                 int k = currentSec.getIdStudents().size();
                                 lastTeacher = i;
     // VAMOS A TENER QUE EN CUENTA CUANDO YA E3STAN MATRICULADOS Y NO TENEMOS ASIGNADO UN PATTERN EL CUAL DEBERA COMPROBAR QUE
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
                   /* }
                }*/
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
  ///
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
     private ArrayList<Integer> fillSection(Seccion currentSec,Restrictions r, Course c,ArrayList<Integer> studentsCourse, HashMap<Integer, Student> students) {

        //int maxStudentSection = c.getMinChildPerSection();
        /*if (maxStudentSection == 0) {
            maxStudentSection = (int) Math.round(c.getMaxChildPerSection() * 0.7); // POR DEFECTO
        }*/
        int minStudentSection = c.getMaxChildPerSection();
        minStudentSection = (int) Math.round(minStudentSection*0.7);

        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
          ArrayList<Integer> ret = new ArrayList<>();
             HashMap<Integer,String> genderStids = new HashMap<>();
        idsAsignados = c.getAllIds();
        chargeStatusStudents(r,c);

        int idCourse = c.getIdCourse();
        ArrayList<Seccion> auxGenderSecciones = new ArrayList<Seccion>();

        HashMap<Integer, Integer> hashStudents_cantPatrones = new HashMap<>();
        initHashStudents(hashStudents_cantPatrones, studentsCourse);
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

        if (idsAsignados.size() != studentsCourse.size()) {
            System.out.println("FAILURE");
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
