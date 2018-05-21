/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Tupla;
import java.util.ArrayList;

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
    
    public Seccion(Teacher currentT,int numStudents,ArrayList<Tupla> patron){
        this.teacher = currentT;
        this.numStudents = numStudents;
        this.patronUsado = patron;
    }
     public Seccion(){
        this.idStudents = new ArrayList<>();
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
    public void IncrNumStudents(){
        this.numStudents++;
    }
    
    
}
