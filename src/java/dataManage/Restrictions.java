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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Algoritmo;
import model.Course;
import model.DBConnect;
import model.Room;
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
    public HashMap<Integer, Room> rooms;
    public ArrayList<Course> courses;
    public ArrayList<Teacher> teachers;
    public ArrayList<Integer> groupRooms;
    public String tempid;
    public ArrayList<ArrayList<Boolean>> totalBlocks;

    public Restrictions(String yearid, String tempid, String groupofrooms) {
        this.tempid = tempid;
        this.cs = new Consultas();
        this.totalBlocks = this.cs.getTotalBlocksStart();
        this.idCourses = new ArrayList();
        this.students = new HashMap<>();
        this.groupRooms = cs.roomsGroup(groupofrooms);

     //   this.rooms = new HashMap();
       // this.courses = cs.getRestriccionesCourses(Consultas.convertIntegers(idCourses),cs.templateInfo(tempid));
       // System.out.println("dataManage.Restrictions.<init>()");
        //solo prueba
        /*  ArrayList<Student> st = new ArrayList();
         this.studentsCourse = Consultas.getCoursesGroups(st,idCourses,yearid,tempid); //20sg
           st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses,studentsCourse,yearid));  //1min 20 sg
           */
    }

    public Restrictions(String yearid, String tempid, String groupofrooms, int mode) {
        this.tempid = tempid;
        this.cs = new Consultas();
        this.idCourses = new ArrayList();
        this.groupRooms = cs.roomsGroup(groupofrooms);
        ArrayList<Student> st = new ArrayList();
        this.studentsCourse = Consultas.getCoursesGroups(st, idCourses, yearid, tempid); //5sg
        this.students = new HashMap<>();
        st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses, studentsCourse, yearid));  
        for (Student s : st) {
            this.students.put(s.getId(), s);
        }

        this.totalBlocks = this.cs.getTotalBlocksStart();
        this.rooms = cs.getRooms();
        
        /*ArrayList<Integer> auxPrueba = new ArrayList<>();
        auxPrueba.add(idCourses.get(0));*/
        this.courses = cs.getRestriccionesCourses(Consultas.convertIntegers(idCourses), cs.templateInfo(tempid));//aqui es donde tarda mucho
        this.courses.sort(new Restrictions.CompCoursesRank());
        this.teachers = cs.teachersList(tempid);
        cs.fillHashCourses(this.courses);
    }

    /**
     * Realiza consultas en nuestra base de datos para sacar todas las
     * restricciones
     */
    public void extraerDatosOwnDB() {
        this.courses = cs.getCoursesOwnDB();
        
        this.students = cs.getStudnetsOwnDB();
        this.rooms = cs.getRoomsOwnDB();
        this.teachers = cs.getTeachersOwnDB();
        this.studentsCourse = cs.getStudentsCourseOwnDB();

        //this.courses = cs.getRestriccionesCourses(Consultas.convertIntegers(idCourses), cs.templateInfo(tempid));

        cs.fillHashCourses(this.courses);
    }

    /**
     * Sincroniza los datos de renweb con nuestra base de datos
     */
    public void syncOwnDB() {
        for (Teacher t : teachers) {
            t.insertarOActualizarDB();
        }
        for (Course c : courses) {
            c.insertarOActualizarCurso();
        }
        for (Map.Entry<Integer, Student> entry : students.entrySet()) {
            entry.getValue().insertarOActualizarDB();
        }
        for (Map.Entry<Integer, Room> entry : rooms.entrySet()) {
            entry.getValue().insertarOActualizarDB();
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : studentsCourse.entrySet()) {
            for (Integer id : entry.getValue()) { //TARDA ***
                String consulta = "insert into students_course values(" + entry.getKey() + "," + id + ",false)";
                try {
                    DBConnect.own.executeUpdate(consulta);
                } catch (SQLException ex) {
                    Logger.getLogger(Restrictions.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

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

}
