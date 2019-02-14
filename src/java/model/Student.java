/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Consultas;
import dataManage.Tupla;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Student {

    private int huecos[][];
    private int id;
    private String genero;
    private String name;
    private int numSection;
    private ArrayList<Integer> cursosNoAsignados;
    private ArrayList<Integer> cursosAsignados;
    private ArrayList<Integer> seccionesAsignadas;
    private int numPatrones;
    private ArrayList<Seccion> seccionesFromRenWeb;
    private String gradeLevel;

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    
    public int getNumPatrones() {
        return numPatrones;
    }

    public void setNumPatrones(int numPatrones) {
        this.numPatrones = numPatrones;
    }
//OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU: 
    //En renweb no se modificar BD
 /*   
    public void insertarOActualizarDB() {
        String consulta = "select * from students where id=" + id;
        boolean actualizar = false;
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                actualizar = true;
            }
            if (!actualizar) {
                name = name.replace("\'", "\'\'");
                name = name.replace("\"", "\"\"");
                genero = genero.replace("\'", "\'\'");
                genero = genero.replace("\"", "\"\"");
                consulta = "insert into students values(" + id + ",'" + genero
                        + "','" + name.replace("'", "''") + "')";
                DBConnect.own.executeUpdate(consulta);
            } else {
                //to do: UPDATE
            }
        } catch (Exception ex) {
            Logger.getLogger(Teacher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
*/
    public Student(int id, int numPatrones) {
        this.id = id;
        this.numPatrones = numPatrones;

    }

    public Student(int id) {
        this.cursosNoAsignados = new ArrayList<>();
        this.cursosAsignados = new ArrayList<>();
        this.id = id;
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        this.seccionesAsignadas = new ArrayList<>();
        this.seccionesFromRenWeb = new ArrayList<>();
    }

    public Student(int id, String name, String genero) {
        this.cursosNoAsignados = new ArrayList<>();
        this.cursosAsignados = new ArrayList<>();
        this.id = id;
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        this.name = name;
        this.genero = genero;
        this.seccionesAsignadas = new ArrayList<>();
        this.seccionesFromRenWeb = new ArrayList<>();

    }

    public ArrayList<ArrayList<Tupla>> listPatronesCompatibles(ArrayList<ArrayList<Tupla>> ar) {
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        boolean compatible;
        ret.add(new ArrayList());
        for (ArrayList<Tupla> a : ar) {
            for (Tupla t : a) {
                if (huecos[(Integer) t.x][(Integer) t.y] == 0) {
                    ret.get(0).add(t);
                }
            }
        }
        return ret;
    }

    public ArrayList<Tupla<Integer, Integer>> posicionesOcupadas() {
        ArrayList<Tupla<Integer, Integer>> ret = new ArrayList();
        for (int i = 0; i < Algoritmo.TAMX; i++) {
            for (int j = 0; j < Algoritmo.TAMY; j++) {
                if (this.huecos[i][j] != 0) {
                    ret.add(new Tupla(i, j));
                }
            }
        }
        return ret;
    }

    public ArrayList<Seccion> getSeccionesFromRenWeb() {
        return seccionesFromRenWeb;
    }

    public void setSeccionesFromRenWeb(ArrayList<Seccion> seccionesFromRenWeb) {
        this.seccionesFromRenWeb = seccionesFromRenWeb;
    }

    public void addSeccionFromrenweb(Seccion s) {
        this.seccionesFromRenWeb.add(s);
    }

    public void addNoAsignado(Integer i) {
        cursosNoAsignados.add(i);
    }

    public void addAsignado(Integer i) {
        cursosAsignados.add(i);
    }

    public ArrayList<Integer> getCursosAsignados() {
        return cursosAsignados;
    }

    public ArrayList<Integer> getCursosNoAsignados() {
        return cursosNoAsignados;
    }

    public void setCursosNoAsignados(ArrayList<Integer> cursosNoAsignados) {
        this.cursosNoAsignados = cursosNoAsignados;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getId() {
        return id;
    }

    public int getNumSection() {
        return numSection;
    }

    public void setNumSection(int numSection) {
        this.numSection = numSection;
    }

    public void ocuparHueco(ArrayList<Tupla> ar, int id) {
        for (Tupla t : ar) {
            huecos[(Integer) t.x][(Integer) t.y] = id;
        }
    }

    public boolean patronCompatible(ArrayList<Tupla> ar) {
        if (ar == null || ar.isEmpty()) {
            return false;
        }
        for (Tupla t : ar) {
            if (huecos[(Integer) t.x][(Integer) t.y] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean patronCompatible2(Tupla<Integer, Integer> ar) {
        if (ar == null) {
            return false;
        }
        return huecos[ar.x][ar.y] == 0;
    }

    public void mostrarHuecos() {
        for (int i = 0; i < Algoritmo.TAMY; i++) {
            for (int j = 0; j < Algoritmo.TAMX; j++) {
                System.out.print(" " + huecos[j][i] + " ");
            }
            System.out.println("");
        }
    }

    public int[][] getHuecos() {
        return huecos;
    }

    @Override
    public boolean equals(Object st) {
        return ((Student) st).id == this.id;
    }

    public int getNumSectionByCourse(int idCourse) {
        for (int i = 0; i < Algoritmo.TAMX; i++) {
            for (int j = 0; j < Algoritmo.TAMY; j++) {
                if ((this.huecos[i][j] / 100) == idCourse) {
                    return this.huecos[i][j] % 100;
                }
            }
        }
        return -1;
    }

    public ArrayList<Integer> getSeccionesAsignadas() {
        return seccionesAsignadas;
    }

    public void setSeccionesAsignadas(ArrayList<Integer> seccionesAsignadas) {
        this.seccionesAsignadas = seccionesAsignadas;
    }

    public void addSeccion(int numSeccion) {
        this.seccionesAsignadas.add(numSeccion);
    }

    public boolean estaEnSeccion(int numSeccion) { // indicara si esta alumno ya ha sido matriculado en la misma seccion en otras asignatura
        return this.seccionesAsignadas.contains(numSeccion);
    }

    public String getSolapamientoSeccionesFromRenWeb() {
        // this.seccionesFromRenWeb.size()

        String s = "";
        for (int i = 0; i < this.seccionesFromRenWeb.size(); i++) {
            for (int j = i + 1; j < this.seccionesFromRenWeb.size(); j++) {
                // hay problemas de solapamiento
                if (!this.seccionesFromRenWeb.get(i).patronCompatible(this.seccionesFromRenWeb.get(j).getPatronUsado())) {
                    s += "<strong>" + Consultas.courseName.get(this.seccionesFromRenWeb.get(i).getCourseID()) + "</strong>" + " se solapa con <strong>" + Consultas.courseName.get(this.seccionesFromRenWeb.get(j).getCourseID())
                            + "</strong> revisar configuracion en RenWeb <br>";
                }
            }
        }
        return s;
    }

    public String checkSolapamiento(int id) {
        int idc = 0;
        if (id != 0) {
            idc = id / 100;
        } 
        String solapada ="";
        for (int i = 0; i < this.seccionesFromRenWeb.size(); i++) {
            for (int j = i + 1; j < this.seccionesFromRenWeb.size(); j++) {
                // hay problemas de solapamiento
                if(this.seccionesFromRenWeb.get(i).courseID == idc && !this.seccionesFromRenWeb.get(i).patronCompatible(this.seccionesFromRenWeb.get(j).getPatronUsado())) {
                    solapada = Consultas.courseName.get(this.seccionesFromRenWeb.get(j).getCourseID()) +" Section: "+this.seccionesFromRenWeb.get(j).getNumSeccion();
                    return solapada;
                }
                else if(this.seccionesFromRenWeb.get(j).courseID == idc && !this.seccionesFromRenWeb.get(i).patronCompatible(this.seccionesFromRenWeb.get(j).getPatronUsado())) {
                    solapada = Consultas.courseName.get(this.seccionesFromRenWeb.get(i).getCourseID()) +" Section: "+this.seccionesFromRenWeb.get(i).getNumSeccion();
                    return solapada;
                }
            }
        }
        return solapada;
    }
    
    public String getCoursesUnenrolled() {
        String auxCoursesUnenrolled="";
        ArrayList<Integer> coursesEnrolled = new ArrayList<>();
        for (int i = 0; i < Algoritmo.TAMX; i++) {
            for (int j = 0; j < Algoritmo.TAMY; j++) {
                if(this.huecos[i][j] != 0)
                    coursesEnrolled.add(this.huecos[i][j]/100);
            }
        }
        
        for (Seccion seccionesFromRenWeb1 : this.seccionesFromRenWeb) {
            if(!coursesEnrolled.contains(seccionesFromRenWeb1.courseID)){
                auxCoursesUnenrolled += "<strong>" + Consultas.courseName.get(seccionesFromRenWeb1.courseID) + "</strong>. No se pudo matricular. <br>";    
            }
        }     
        return auxCoursesUnenrolled;
    }
}
