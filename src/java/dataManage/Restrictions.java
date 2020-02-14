/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
    public HashMap<String,ArrayList<Integer>> groupRooms;
    public String tempid;
    public ArrayList<ArrayList<Boolean>> totalBlocks;
    public HashMap<String, Course> linkedCourses = new HashMap<>();
    public boolean shuffleRosters;
    public boolean activeRooms;
    public Exceptions aviso = new Exceptions();

    
    public Restrictions(String yearid, String tempid, int x, int y) {
        this.tempid = tempid;
        this.cs = new Consultas(tempid, x, y);
        this.totalBlocks = this.cs.getTotalBlocksStart();
        this.idCourses = new ArrayList();
        this.students = new HashMap<>();
        shuffleRosters = false;
        activeRooms = false;
    }
//Aquí se define la estructura inicial de las restricciones para luego poder manejarlas:
    public Restrictions(String yearid, String tempid,String schoolCode, int x, int y) throws Exception {

      try{
        
        this.tempid = tempid;
//Se crea el objeto cs para instanciar la estructura que se va a llevar a cabo al aplicar las restricciones posteriores(cs es un objeto de Consultas):
//Se capturan los datos de Consultas en función del tempid que ha devuelto el redirect, que a su vez ha devuelto el menu:
//En cs se guarda la estructura para poder establecer datos como profesores, estudiantes, nombres, apellidos, sexo... (datos por defecto de consultas):
        this.cs = new Consultas(tempid, x, y);
        this.teachers = new ArrayList<>();
        this.hashTeachers = new HashMap<>();
        this.idCourses = new ArrayList();
  //Con este método se cogen las rooms por defecto de school (hash: key->id template, value-> array de rooms asociadas):
       this.groupRooms = cs.roomsGroup(tempid); 
       
//1º--> se recogen las restricciones de los estudiantes. Para ello lo primero se crea un array de tipo clase estudiante: st       
        ArrayList<Student> st = new ArrayList();
//Con este método se recoge un array de 4 posiciones para las diferentes escuelas, 1 es que true y 0 false.
//Para el ejemplo de RWI-SPAIN, serían todos a 0, menos HS que sería 1:            
        int[] tempInfo =  cs.templateInfo(tempid);
      //  this.studentsCourse = Consultas.getCoursesGroups(st, idCourses, yearid, tempid); //5sg
        this.studentsCourse = new HashMap<>();
        this.students = new HashMap<>();

//En st se añaden las restricciones de estudiantes obtenidas de la clase Consultas(el array Conjuntos se ocupa de añadir el contenido de
//cs.restriccionesStudent en st):
        st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses, studentsCourse, yearid,(Integer.parseInt(tempid)),schoolCode, aviso));
   
        //chargeHashStudents();
//Aquí se carga el array students de todas las restricciones de los estudiantes en función de su id:        
        for (Student s : st) {
            this.students.put(s.getId(), s);
        }
 
//Con el método getTotalBlocksStart se obtienen las restricciones de inicio de bloques(el nº de bloques con el que se inicia)
//, obtenidas en función del metodo TotalBlocksStart:
        this.totalBlocks = this.cs.getTotalBlocksStart();
//Devuelve un hash (key:curso padre, value:curso hijo):        
        this.linkedCourses = this.cs.getLinkedCourses();
 //En getRooms se obtienen todos los datos de las rooms: ids, nombres, a la school que pertenecen...             
        this.rooms = cs.getRooms();

//2º-->Obtencion de las restricciones de los cursos y teachers de la clase consultas:      
//Se guardan todas las restricciones recogidas de cada curso a través de los hash de consultas en el array courses declarado al principio de esta clase:
//También se guardan en el Array teachers los ids de todos los profesores de los cursos (de User Defined y Default)
        this.courses = cs.getRestriccionesCourses(Consultas.convertIntegers(idCourses),tempid,this.groupRooms,schoolCode, aviso,x,y);
//Se priorizan unos cursos sobre otros (a través de los rangos) una vez aplicadas las restricciones anteriores: 
        
        //this.courses.sort(new Restrictions.CompCoursesRank());

        Collections.sort(this.courses, new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                Integer w = new Integer(o1.getRank()); 
                Integer z = new Integer(o2.getRank()); 

                // as 15 is greater than 8, Output will be a value greater than zero 
             //   System.out.println(w.compareTo(z)); 
                
                return w.compareTo(z);
            }
        });
        
//Se guarda la lista de profesores con sus restricciones obtenidas de cada uno en teachers:            
        this.hashTeachers = cs.teachersList(tempid,tempInfo, x, y);
//Se agregan los datos de Hashteachers al teachers, con identificador el id de Teacher. Esto se hace para todos:
        for (HashMap.Entry<Integer, Teacher> entry : hashTeachers.entrySet()) {
            teachers.add(entry.getValue());
}
//Se añaden los nombres de los cursos en función de los ids que hay en courses:      
        cs.fillHashCourses(this.courses);
//3º: Se añaden las secciones(aulas) en función de los estudiantes, los profesores, rooms, cursos, año, template:
//El resultado es un hashmap con key=id curso, value= array de secciones
        this.mapSecciones = cs.getDataSections(this.students,this.hashTeachers,this.rooms,this.courses,yearid,tempid,linkedCourses,schoolCode, studentsCourse, aviso);

//Se establece el Shuffle y el ActiveRooms disabled por defecto (luego se actualiza en ScheduleController si el usuario los ha activado):
        shuffleRosters = false;
        activeRooms = false;
        }catch(Exception e){
          e.getMessage();
      }
    }


//Con este método se comparan los rangos de los cursos (estos son necesarios para que algunos cursos se generen antes y tengan más patrones
//disponibles y rosters más completos):
    private class CompCoursesRank implements Comparator<Course> {

        @Override
        public int compare(Course e1, Course e2) {
            try{
                if (e1.getRank() < e2.getRank()) {
                    return -1;
                } else {
                    return 1;
                }
            }catch(Exception e){
                System.err.println("gfg");
            }
            return -1;
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
//Get y Set de activacion/desactivacion de Active Rooms y Shuffle Rosters, y LinkedCourses:
    
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
