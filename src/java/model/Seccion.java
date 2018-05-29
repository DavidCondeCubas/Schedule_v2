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
    int idTeacher;
    boolean lockSchedule;
    boolean lockEnrollment;
    int classId;

    public Seccion(Teacher currentT, int numStudents, ArrayList<Tupla> patron) {
        this.teacher = currentT;
        this.numStudents = numStudents;
        this.patronUsado = patron;
        this.gender = "mixto";
        this.lockEnrollment = false;
        this.lockSchedule = false;
    }

    public Seccion(Teacher currentT, int numStudents, ArrayList<Tupla> patron, String gender, ArrayList<Integer> ids, int idPatron, int numS, boolean lockEnr, boolean lockSche,int cID) {
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

    }

    public Seccion(Seccion s) {
        this.teacher = s.teacher;
        this.idTeacher = s.idTeacher;
        this.numStudents = s.numStudents;
        this.patronUsado = s.patronUsado;
        this.gender = s.gender;
        this.idStudents = s.idStudents;
        this.indicePatronUsado = s.indicePatronUsado;
        this.numSeccion = s.numSeccion;
        this.lockEnrollment = s.lockEnrollment;
        this.lockSchedule = s.lockSchedule;
        this.classId = s.classId;
    }

    public Seccion() {
        this.idStudents = new ArrayList<>();
        this.teacher = new Teacher();
        this.patronUsado = new ArrayList<>();
        this.lockEnrollment = false;
        this.lockSchedule = false;
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

    public void setLockEnrollment(int lockEnrollmentInt) {
        if (lockEnrollmentInt == 1) {
            this.lockEnrollment = true;
        } else {
            this.lockEnrollment = false;
        }
    }
    public void copiarIdsStudents(ArrayList<Integer> a, HashMap<Integer,Student> students,Course c){
        this.idStudents = new ArrayList<>();
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
    
}
