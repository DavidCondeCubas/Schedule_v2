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
import dataManage.Restrictions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Norhan
 */
public class Algoritmo {

    public static int TAMX = 3;
    public static int TAMY = 11;
    public final static int CHILDSPERSECTION = 25;
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

    /**
     * algoritmo
     *
     * @param mv
     * @param r
     * @param roommode
     */
    public void algo(ModelAndView mv, Restrictions r, int roommode) {
        for (Course course : r.courses) {
            int minsections = 1; //falla al dividir entre 0
            try {
                int numAlumnos = course.getMaxChildPerSection();
                if (numAlumnos == 0) {
                    numAlumnos = CHILDSPERSECTION; // POR DEFECTO
                }
                //MINIMO DE SECCIONES NECESARIAS PARA TODOS LOS ESTUDIANTES (TENIENDO EN CUENTA EL NUMERO MAX  DE ALUMNOS POR AULA)
                //solo prueba
                if (r.studentsCourse.get(course.getIdCourse()).size() % numAlumnos == 0) {
                    minsections = (r.studentsCourse.get(course.getIdCourse()).size() / numAlumnos);
                } else {
                    minsections = 1 + (r.studentsCourse.get(course.getIdCourse()).size() / numAlumnos);
                }
                //pruebas 
                //if(minsections == 0) minsections = 4;
            } catch (ArithmeticException e) {
                //e.printStackTrace();
                System.out.println("id:" + course.getIdCourse() + " name: " + r.cs.nameCourse(course.getIdCourse()));
            }
            course.setMinSections(minsections);
            ArrayList<Integer> noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
            HashMap<Integer, Integer> noAsignSection = new HashMap<Integer, Integer>(); // indicara seccion de cada alumno de la clase
            // esto rellena las secciones de manera equitativa tutilizando el random

            for (int i = 0; i < noAsign.size(); i++) { // secciones 0,1,2,3,...
                noAsignSection.put(noAsign.get(i), i % minsections);
            }
            //id:695  science
            if (course.opciones(r.totalBlocks, Log).size() > 0) {
                //Segun el modo de gestion de clases cambia como rellenas la parte de studentsSections
                switch (roommode) {
                    case 0:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(r.totalBlocks, Log), noAsign, noAsignSection, r.students, null);
                        break;
                    case 1:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(r.totalBlocks, Log), noAsign, noAsignSection, r.students, course.getRooms());
                        break;
                    case 2:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(r.totalBlocks, Log), noAsign, noAsignSection, r.students, r.groupRooms);
                        break;
                    case 3:
                        if (course.getRooms().isEmpty()) {
                            noAsign = studentSections(r, r.teachers, course, minsections,
                                    course.opciones(r.totalBlocks, Log), noAsign, noAsignSection, r.students, r.groupRooms);
                        } else {
                            noAsign = studentSections(r, r.teachers, course, minsections,
                                    course.opciones(r.totalBlocks, Log), noAsign, noAsignSection, r.students, course.getRooms());
                        }
                        break;
                    default:
                        break;
                }

                //si no asign es distinto de null quiere decir que no se han podido 
                //asignar todos los estudiantes al curso
                if (noAsign != null) {
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
            } else {
                System.out.println("FAILURE: " + course.getIdCourse());
            }
        }
//        XMLWriterDOM.xmlCreate(trst, retst);
        mv.addObject("TAMX", TAMX);
        mv.addObject("TAMY", TAMY);
        mv.addObject("profesores", r.teachers);
        mv.addObject("students", r.students);
        mv.addObject("Courses", r.courses);
        mv.addObject("cs", r.cs);
        mv.addObject("rooms", r.rooms);
        mv.addObject("grouprooms", r.groupRooms);
        mv.addObject("log", Log);
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

        if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
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

    private void sortStudentsByPatron(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, HashMap<Integer, Integer> hashStudents_cantPatrones) {
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
                Room compatibleRoom = null;
                //compruebo que el profesor puede impartir esta clase
                /*  if (c.getTrestricctions().contains(t.getIdTeacher()) //comprobar que el profesor puede dar ese curso
                        && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                        && t.patronCompatible(sec.get(stids.get(i).x))
                        && c.getSections() <= c.getMinSections()) {*/
                if (c.getTrestricctions().contains(t.getIdTeacher()) && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                        && t.patronCompatible(sec.get(stids.get(i).x))
                        && c.getSections() <= c.getMinSections()) {
                    //si el schedule por rooms esta activado comprueba si las rooms disponibles 
                    //tienen la seccion elegida disponible -- NO REVISADA
                    if (rooms != null) {
                        for (Integer room : rooms) {
                            if (r.rooms.get(room).patronCompatible(sec.get(stids.get(i).x))) {
                                compatibleRoom = r.rooms.get(room);
                                break;
                            }
                        }
                    }

                    //si hay una room compatible o no el schedule por rooms esta desactivado
                    //entonces ya procedemos a ocupar los huecos de los estudiantes con la seccion elegida
                    if (compatibleRoom != null || rooms == null) {
                        int k = 0;
                        lastTeacher = i;
                        int studentsBySection = studentsCourse.size() / c.getMinSections(); // alumnos por seccion
                        if (studentsCourse.size() % c.getMinSections() == 0) {
                            studentsBySection++; // esto comprueba si no es multiplo es cuando debe incrementarse 1
                        }

                        for (Integer j : diferencia) { // studiantes
                            /*if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
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
                            //    if ((k < studentsCourse.size() / c.getMinSections() + 1 || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                            if (((k <= numMinStudents_Section) || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                    && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {

                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                k++;
                                lastStudent = i;
                            }
                        }
                        if (k < numMinStudents_Section) { // si no  se llena la seccion
                            // se entra aqui para meter los alumnos que no cumplian las restricciones ?? -- no se si es necesario
                            for (Integer j : stids.get(i).y) {
                                //     if ((k < studentsCourse.size() / c.getMinSections() + 1 || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                if ((k <= numMinStudents_Section || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                        && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {
                                    idsAsignados.add(j);
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
                            //    public Seccion(Teacher currentT,int numStudents,ArrayList<Tupla> patron){
                            secciones.add(new Seccion(t, k, sec.get(stids.get(i).x)));
                            //**/ esto funcionaria para el balanceado
                            if (c.isBalanceTeachers()) {
                                //   teachersOrderByPriority.remove(t);
                                ArrayList<Teacher> teachersOrderByPriorityAux = (ArrayList<Teacher>) teachersOrderByPriority.clone();
                                teachersOrderByPriorityAux.remove(t);
                                teachersOrderByPriority = teachersOrderByPriorityAux;
                            }

                            c.ocuparHueco(sec.get(stids.get(i).x));
                            if (compatibleRoom != null) {
                                compatibleRoom.ocuparHueco(c.getIdCourse() * 100 + c.getSections(), sec.get(stids.get(i).x));
                            }
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
                    } else {
                        Log.add("-No hay rooms compatibles con el curso:" + r.cs.nameCourse(c.getIdCourse()));
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
                if (!marcasAlumnos.get(k) && secciones.get(j).getNumStudents() <= maxStudentSection) {       
                    if (students.get(alumnosNoAsignados.get(k)).patronCompatible(secciones.get(j).getPatronUsado())) {
                        idsAsignados.add(alumnosNoAsignados.get(k));
                        students.get(alumnosNoAsignados.get(k)).ocuparHueco(secciones.get(j).getPatronUsado(), c.getIdCourse() * 100 + (j+1));
                        secciones.get(j).IncrNumStudents();
                        marcasAlumnos.set(k,true);
                    }       
                }
            }
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

    private Boolean esValido() {
        return false;
    }
}
