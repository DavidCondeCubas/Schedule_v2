/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Tupla;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author David
 */
public class Seccion {

    Teacher teacher;
    ArrayList<Integer> idStudents;
    int idRoom;
    int numStudents;
    ArrayList<Tupla> patronUsado;
    String gender;
    int indicePatronUsado;
    int numSeccion;
    String nameSeccion;
    int idTeacher;
    boolean lockSchedule;
    boolean lockEnrollment;
    int classId;
    int courseID;
    int patternRenWeb;

    public Seccion(Teacher currentT, int numStudents, ArrayList<Tupla> patron) {
        this.teacher = currentT;
        this.numStudents = numStudents;
        this.patronUsado = patron;
        this.gender = "mixto";
        this.lockEnrollment = false;
        this.lockSchedule = false;
        this.patternRenWeb = 0;
        this.idRoom = 0;
    }

    public Seccion(Teacher currentT, int numStudents, ArrayList<Tupla> patron, String gender, ArrayList<Integer> ids, int idPatron, int numS, boolean lockEnr, boolean lockSche, int cID, int courseID) {
        this.teacher = currentT;
        this.numStudents = numStudents;
        this.patronUsado = patron;
        this.gender = gender;
        this.idStudents = ids;
        this.indicePatronUsado = idPatron;
        this.numSeccion = numS;
        this.lockEnrollment = lockEnr;
        this.lockSchedule = lockSche;
        this.classId = cID;
        this.courseID = courseID;
        this.patternRenWeb = 0;
        this.idRoom = 0;
        this.nameSeccion = ""+numS;
    }

    public Seccion(Seccion s) {
        this.teacher = s.teacher;
        this.idTeacher = s.idTeacher;
        this.numStudents = s.numStudents;
        this.patronUsado = s.patronUsado;
        this.gender = s.gender;
        this.idRoom = s.getIdRoom();
        this.idStudents = s.idStudents;
        this.indicePatronUsado = s.indicePatronUsado;
        this.numSeccion = s.numSeccion;
        this.lockEnrollment = s.lockEnrollment;
        this.lockSchedule = s.lockSchedule;
        this.classId = s.classId;
        this.courseID = s.courseID;
        this.patternRenWeb = s.patternRenWeb;
        this.nameSeccion = s.getNameSeccion();
    }

    public Seccion() {
        this.idStudents = new ArrayList<>();
        this.teacher = new Teacher();
        this.patronUsado = new ArrayList<>();
        this.lockEnrollment = false;
        this.lockSchedule = false;
        this.patternRenWeb = 0;
        this.idRoom = 0;
        this.nameSeccion = "";
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher t) {
        this.teacher = t;
    }

    public ArrayList<Integer> getIdStudents() {
        return idStudents;
    }

    public void setIdStudents(ArrayList<Integer> idStudents) {
        this.idStudents = idStudents;
    }

    public int getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(int idRoom) {
        this.idRoom = idRoom;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public ArrayList<Tupla> getPatronUsado() {
        return patronUsado;
    }

    public void setPatronUsado(ArrayList<Tupla> patronUsado) {
        this.patronUsado = patronUsado;
    }

    public void IncrNumStudents() {
        this.numStudents++;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void addStudent(int id) {
        this.idStudents.add(id);
    }

    public int getIndicePatronUsado() {
        return indicePatronUsado;
    }

    public void setIndicePatronUsado(int indicePatronUsado) {
        this.indicePatronUsado = indicePatronUsado;
    }

    public int getNumSeccion() {
        return numSeccion;
    }

    public void setNumSeccion(int numSeccion) {
        this.numSeccion = numSeccion;
    }

    public int getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(int idTeacher) {
        this.idTeacher = idTeacher;
    }

    public void addTuplaPatron(Tupla t) {
        this.patronUsado.add(t);
    }

    public boolean isLockSchedule() {
        return lockSchedule;
    }

    public void setLockSchedule(boolean lockSchedule) {
        this.lockSchedule = lockSchedule;
    }

    public boolean isLockEnrollment() {
        return lockEnrollment;
    }

    public void setLockEnrollment(boolean lockEnrollment) {
        this.lockEnrollment = lockEnrollment;
    }

    public void setLockSchedule(int lockScheduleInt) {
        if (lockScheduleInt == 1) {
            this.lockSchedule = true;
        } else {
            this.lockSchedule = false;
        }

    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public void setLockEnrollment(int lockEnrollmentInt) {
        if (lockEnrollmentInt == 1) {
            this.lockEnrollment = true;
        } else {
            this.lockEnrollment = false;
        }
    }

    public void copiarIdsStudents(ArrayList<Integer> a, HashMap<Integer, Student> students, Course c) {
        // this.idStudents = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            this.idStudents.add(a.get(i));
            students.get(a.get(i)).ocuparHueco(this.patronUsado, c.getIdCourse() * 100 + this.numSeccion);
        }
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public boolean patronCompatible(ArrayList<Tupla> ar) {
        for (Tupla t : ar) {
            for (int i = 0; i < this.patronUsado.size(); i++) {
                if (this.patronUsado.get(i).x == t.x && this.patronUsado.get(i).y == t.y) {
                    return false;
                }
            }

        }
        return true;
    }

    public int getPatternRenWeb() {
        return patternRenWeb;
    }

    public void setPatternRenWeb(int patternRenWeb) {
        this.patternRenWeb = patternRenWeb;
    }

    public String getNameSeccion() {
        return nameSeccion;
    }

    public void setNameSeccion(String nameSeccion) {
        this.nameSeccion = nameSeccion;
    }
    
    
}
