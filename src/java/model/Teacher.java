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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Teacher {

    private int[][] huecos;
    private int[] blocksPerDay;
    private int idTeacher;
    private int MaxSections; // maxima secciones
    private int secsComplete;
    private int Preps;//maximo asignaturas
    private ArrayList<Integer> prepsComplete;
    private int MaxBxD;//max blocksperday
    private ArrayList<Tupla> excludeBlocks;
    private boolean ocupado;
    private String name;
    private HashMap<Integer, Integer> secciones;
    private ArrayList<Integer> excludeCols;
    private ArrayList<Integer> excludeRows;
    private int numSecciones;

    public int getNumSecciones() {
        return numSecciones;
    }

    public void setNumSecciones(int numSecciones) {
        this.numSecciones = numSecciones;
    }

    public void incrementarNumSecciones() {
        this.numSecciones++;
    }

    public Teacher() {
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        blocksPerDay = new int[Algoritmo.TAMY];
        for (int i = 0; i < blocksPerDay.length; i++) {
            blocksPerDay[i] = 0;
        }
        secsComplete = 0;
        prepsComplete = new ArrayList<>();
        excludeBlocks = new ArrayList<>();
        secciones = new HashMap<>();
        numSecciones = 0;
    }

    /**
     * Esta funcion devuelve todas las tuplas ocupadas en la cuadricula del profesor.
     *
     * @return
     */
    public ArrayList<Tupla<Integer, Integer>> getAllPosiciones() {
        ArrayList<Tupla<Integer, Integer>> ret = new ArrayList<>();
        for (int i = 0; i < Algoritmo.TAMX; i++) {
            for (int j = 0; j < Algoritmo.TAMY; j++) {
                if (huecos[i][j] != 0) {
                    ret.add(new Tupla(i, j));
                }
            }
        }
        return ret;
    }

    /**
     * Muestra la cuadricula en la terminal
     */
    public void mostrarHuecos() {
        for (int i = 0; i < Algoritmo.TAMY; i++) {
            for (int j = 0; j < Algoritmo.TAMX; j++) {
                System.out.print(" " + huecos[j][i] + " ");
            }
            System.out.println("");
        }
    }

    /**
     * Ocupa una seccion del profesor
     *
     * @param ar
     * @param id
     */
    public void ocuparHueco(ArrayList<Tupla> ar, int id) {
        for (Tupla t : ar) {
            huecos[(Integer) t.x][(Integer) t.y] = id;
            blocksPerDay[(Integer) t.y]++;
        }
        if (!prepsComplete.contains(id / 100)) {
            prepsComplete.add(id / 100);
        }
        secsComplete++;
        if (secsComplete >= MaxSections) {
            ocupado = true;
        }
        if (secciones.containsKey(id / 100)) {
            int aux = secciones.get(id / 100) + 1;
            secciones.replace(id / 100, aux);
        } else {
            secciones.put(id / 100, 1);
        }
    }

    /**
     * Comprueba si el profesor puede cursar una nueva asignatura
     *
     * @param id
     * @return
     */
    public boolean asignaturaCursable(int id) {
        if (this.Preps == 0 && this.MaxSections == 0) {
            return true;
        } else if (ocupado || (!prepsComplete.contains(id) && prepsComplete.size() >= Preps)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Comprueba si una seccion en concreto es compatible con el profesor.
     *
     * @param ar
     * @return
     */
    public boolean patronCompatible(ArrayList<Tupla> ar) {

        for (Tupla t : ar) {
            if (huecos[(Integer) t.x][(Integer) t.y] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean patronCompatibleEB(ArrayList<Tupla> ar, ArrayList<Tupla> ExcludeBlocks) {

        for (Tupla t : ar) {
            int col = t.get_x_Int();
            int fila = t.get_y_Int();
            Tupla t2 = new Tupla(col + 1, fila + 1);
            if (huecos[(Integer) t.x][(Integer) t.y] != 0 || ExcludeBlocks.contains(t2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Devuelve el numero de secciones disponibles que tiene el profesor
     *
     * @return
     */
    //Aquí se accede en la vista: el primer if resta las secciones totales que tiene el profesor - las secciones ocupadas.
    //El else establece por defecto (si no se ha configurado) secciones disponibles 
    //sobre el total de bloques - las secciones ocupadas:
    public int seccionesDisponibles(int totalBlocks) {
        if (MaxSections > 0) {
            return MaxSections - secsComplete;
        } else {
            return totalBlocks - secsComplete;
        }
    }

    /**
     * Devuelve el numero de asignaturas que se le pueden asignar al profesor.
     *
     * @return
     */
    //Aquí se accede en la vista: el primer if resta los cursos totales que tiene el profesor - los cursos ocupados.
    //El else establece por defecto (si no se ha configurado) cursos disponibles:
    //totales de bloques entre 3 (para establecer de media 3 secciones por curso) - los cursos ocupados. 
    public int prepsDisponibles(int totalBlocks) {
        if (Preps > 0) {
            return Preps - prepsComplete.size();
        } else {
            return (totalBlocks / 3) - prepsComplete.size();
        }
    }

    //---------------------------------
    //-------GETTERS AND SETTERS-------
    //---------------------------------
    public HashMap<Integer, Integer> getSecciones() {
        return secciones;
    }

    public void setSecciones(HashMap<Integer, Integer> secciones) {
        this.secciones = secciones;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSecsComplete() {
        return secsComplete;
    }

    public int getMaxSections() {
        return MaxSections;
    }

    public int getIdTeacher() {
        return idTeacher;
    }

    public int[] getBlocksPerDay() {
        return blocksPerDay;
    }

    public void setBlocksPerDay(int[] blocksPerDay) {
        this.blocksPerDay = blocksPerDay;
    }

    public int getPreps() {
        return Preps;
    }

    public void setPreps(int Preps) {
        this.Preps = Preps;
    }

    public int getMaxBxD() {
        return MaxBxD;
    }

    public void setMaxBxD(int MaxBxD) {
        this.MaxBxD = MaxBxD;
    }

    public void setHuecos(int[][] huecos) {
        this.huecos = huecos;
    }

    public void setIdTeacher(int idTeacher) {
        this.idTeacher = idTeacher;
    }

    public void setMaxSections(int MaxSections) {
        this.MaxSections = MaxSections;
    }

    public void setSecsComplete(int secsComplete) {
        this.secsComplete = secsComplete;
    }

    public void setPrepsComplete(ArrayList<Integer> prepsComplete) {
        this.prepsComplete = prepsComplete;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    public ArrayList<Tupla> getExcludeBlocks() {
        return excludeBlocks;
    }

    public void addExcludeBlock(Tupla t) {
        if (!excludeBlocks.contains(t)) {
            excludeBlocks.add(t);
        }
    }

    public String getExcludeBlocksString() {
        String ExcludeBlocksString = "";
        if (!this.excludeBlocks.isEmpty()) {
            String ExcludeBlocks = this.excludeBlocks.toString();
            ExcludeBlocks = ExcludeBlocks.substring(1, ExcludeBlocks.length() - 1);
            String[] arrays = ExcludeBlocks.split(",");
            for (String arr : arrays) {
                String[] array = arr.split("-");
                array[0] = array[0].replace("x", "");
                array[0] = array[0].replace(":", "");
                array[0] = array[0].trim();
                int row = Integer.parseInt(array[0]);
                array[1] = array[1].replace("y", "");
                array[1] = array[1].replace(":", "");
                array[1] = array[1].trim();
                int col = Integer.parseInt(array[1]);
                ExcludeBlocksString += row + "," + col + ";";
            }
        }

        return ExcludeBlocksString;
    }

    public String toString() {
        return idTeacher + " sections: " + MaxSections + " preps: " + Preps + " maxbxd: " + MaxBxD + " exclude: " + excludeBlocks;
    }

    public void setExcludeBlocks(String excludeBlocks, int x, int y) {
        String[] s = excludeBlocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        if (!s[0].equals("")) {
            for (String s2 : s) {
                if (s2.contains(",")) {
                    elem = s2.split(",");
                    String col = "-1";
                    String row = "-1";
                    int c = Integer.parseInt(col);
                    int r = Integer.parseInt(row);
                    if (!s2.contains("*")) {
                        try {

                            c = Integer.parseInt(elem[0]);
                        } catch (Exception e) {
                        }
                        try {

                            r = Integer.parseInt(elem[1]);
                        } catch (Exception e) {
                        }
                        if (c != -1 && r != -1) {
                            this.excludeBlocks.add(new Tupla(c, r));
                        }
                    } else if (elem[0].contains("*")) {
                        r = Integer.parseInt(elem[1]);
                        for (int i = 1; i <= x; i++) {
                            this.excludeBlocks.add(new Tupla(i, r));
                        }
                    } else {
                        c = Integer.parseInt(elem[0]);
                        for (int j = 1; j <= y; j++) {
                            this.excludeBlocks.add(new Tupla(c, j));
                        }

                    }
                }
            }
        }
    }

    public int[][] getHuecos() {
        return huecos;
    }

    public ArrayList<Integer> getPrepsComplete() {
        return prepsComplete;
    }

}
