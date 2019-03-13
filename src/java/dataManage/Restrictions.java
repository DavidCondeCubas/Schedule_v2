/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Algoritmo;
import model.Course;
import model.DBConnect;
import model.Room;
import model.Seccion;
import model.Student;
import model.Teacher;

/**
 *
 * @author Chema
 */
public class Restrictions {

    public Consultas cs;
    public ArrayList<Integer> idCourses;
    public HashMap<Integer, ArrayList<Integer>> studentsCourse;
    public HashMap<Integer, Student> students;
    public HashMap<Integer, Teacher> hashTeachers;
    public HashMap<Integer, Room> rooms;
    public ArrayList<Course> courses;
    public HashMap<Integer,ArrayList<Seccion>> mapSecciones;
    public ArrayList<Teacher> teachers;
    public static HashMap<String,ArrayList<Integer>> groupRooms;
    public String tempid;
    public ArrayList<ArrayList<Boolean>> totalBlocks;
    public HashMap<String, Course> linkedCourses = new HashMap<>();
    public boolean shuffleRosters;
    public boolean activeRooms;
    
    public Restrictions(String yearid, String tempid, String groupofrooms) {
        this.tempid = tempid;
        this.cs = new Consultas(tempid);
        this.totalBlocks = this.cs.getTotalBlocksStart();
        this.idCourses = new ArrayList();
        this.students = new HashMap<>();
        shuffleRosters = false;
        activeRooms = false;
    }
//Aquí se define la estructura inicial de las restricciones para luego poder manejarlas:
    public Restrictions(String yearid, String tempid, String groupofrooms, int mode,String schoolCode) throws Exception {

      try{
  
        this.tempid = tempid;
//Se crea el objeto cs para instanciar la estructura que se va a llevar a cabo al aplicar las restricciones posteriores(cs es un objeto de Consultas):
//Se capturan los datos de Consultas en función del tempid que ha devuelto el redirect, que a su vez ha devuelto el menu:
//En cs se guarda la estructura para poder establecer datos como profesores, estudiantes, nombres, apellidos, sexo...
        this.cs = new Consultas(tempid);
        this.hashTeachers = new HashMap<>();
        this.idCourses = new ArrayList();
  //Con este método se cogen las rooms por defecto de school
       this.groupRooms = cs.roomsGroup(groupofrooms,tempid) ; 
//1º--> se recogen las restricciones de los estudiantes. Pra ello lo primero se crea un array de tipo clase estudiante: st       
        ArrayList<Student> st = new ArrayList();
        ArrayList<Course> cr = new ArrayList();
        int[] tempInfo =  cs.templateInfo(tempid);
      //  this.studentsCourse = Consultas.getCoursesGroups(st, idCourses, yearid, tempid); //5sg
        this.studentsCourse = new HashMap<>();
        this.students = new HashMap<>();

//En st se añaden las restricciones de estudiantes obtenidas de la clase Consultas(el array Conjuntos se ocupa de añadir el contenido de
//cs.restriccionesStudent en st):
        st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses, studentsCourse, yearid,(Integer.parseInt(tempid)),schoolCode));
   
        //chargeHashStudents();
//Aquí se carga el array students de todas las restricciones de los estudiantes en función de su id:        
        for (Student s : st) {
            this.students.put(s.getId(), s);
        }
//Con el método getTotalBlocksStart se obtienen las restricciones de inicio de bloques(el nº de bloques con el que se inicia)
//, obtenidas en función del metodo TotalBlocksStart:
        this.totalBlocks = this.cs.getTotalBlocksStart();
//        
        this.linkedCourses = this.cs.getLinkedCourses();
 //En getRooms se obtienen todos los datos de las rooms: ids, nombres, a la school que pertenecen...      
        
        this.rooms = cs.getRooms();

//2º-->        
//Obtencion de las restricciones de los cursos de la clase consultas:      
//Se guardan todas las restricciones recogidas a través de los hash de consultas en el array courses declarado al principio de esta clase:  
        this.courses = cs.getRestriccionesCourses(Consultas.convertIntegers(idCourses), tempInfo,tempid,this.groupRooms);//aqui es donde tarda mucho
        try{
//Se priorizan unos cursos sobre otros (a través de los rangos) una vez aplicadas las restricciones anteriores:       
        this.courses.sort(new Restrictions.CompCoursesRank());
        }
        catch(Exception e){
            e.getMessage();
        }
//Se guarda la lista de profesores con sus restricciones obtenidas de cada uno en teachers:            
        this.teachers = cs.teachersList(tempid,tempInfo);
//Se agregan los datos de teachers al hashTeachers, con identificador el id de Teacher. Esto se hace para todos:       
        for (Teacher teacher : this.teachers) {
            this.hashTeachers.put(teacher.getIdTeacher(), teacher);
        }
//Se añaden los nombres de los cursos en función de los ids que hay en courses(nota: es Static):      
        cs.fillHashCourses(this.courses);
//Se añaden las secciones(aulas) en función de los estudiantes, los profesores, rooms, cursos, año, template:        
        this.mapSecciones = cs.getDataSections(this.students,this.hashTeachers,this.rooms,this.courses,yearid,tempid,linkedCourses,schoolCode, studentsCourse);
        
//Se establece el Shuffle y el ActiveRooms disabled por defecto:

        shuffleRosters = false;
        activeRooms = false;
        }catch(Exception e){
          e.getMessage();
      }
    }


//Con este método se comparan los rangos de los cursos (estos son necesarios para algunas asignaturas se generen antes y tengan más patrones.
//disponibles y rosters más completos):
    private class CompCoursesRank implements Comparator<Course> {

        @Override
        public int compare(Course e1, Course e2) {
            if (e1.getRank() < e2.getRank()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public String studentsJSON() {
        if (this.students == null) {
            return "ejecuta el algoritmo";
        } else {
            return (new Gson()).toJson(this.students);
        }
    }

    public String teachersJSON() {
        if (this.teachers == null) {
            return "ejecuta el algoritmo";
        } else {
            return (new Gson()).toJson(this.teachers);
        }
    }

    public String coursesJSON() {
        if (this.courses == null) {
            return "ejecuta el algoritmo";
        } else {
            return (new Gson()).toJson(this.courses);
        }
    }

    public HashMap<String, Course> getLinkedCourses() {
        return linkedCourses;
    }

    public void setLinkedCourses(HashMap<String, Course> linkedCourses) {
        this.linkedCourses = linkedCourses;
    }
    public void updateShuffleRosters(String booleanString){
        if(booleanString.equals("1")) 
            this.shuffleRosters = true;
    }

    public boolean isShuffleRosters() {
        return shuffleRosters;
    }
    
    public void setShuffleRosters(boolean shuffleRosters) {
        this.shuffleRosters = shuffleRosters;
    }

    public void updateActiveRooms(String booleanString){
        if(booleanString.equals("1")) 
            this.activeRooms = true;
    }

    public boolean isActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(boolean activeRooms) {
        this.activeRooms = activeRooms;
    }
    
    
    
}
