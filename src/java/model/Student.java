/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Tupla;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private int numPatrones;

    public int getNumPatrones() {
        return numPatrones;
    }

    public void setNumPatrones(int numPatrones) {
        this.numPatrones = numPatrones;
    }

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

    public Student(int id, int numPatrones) {
        this.id = id;
        this.numPatrones = numPatrones;
    }

    public Student(int id) {
        this.cursosNoAsignados = new ArrayList<>();
        this.cursosAsignados = new ArrayList<>();
        this.id = id;
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
    }

    public Student(int id, String name, String genero) {
        this.cursosNoAsignados = new ArrayList<>();
        this.cursosAsignados = new ArrayList<>();
        this.id = id;
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        this.name = name;
        this.genero = genero;
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
        if (ar == null) {
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
}
