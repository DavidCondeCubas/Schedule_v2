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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Course {

    private String[][] huecos; // cuadricula
    private int idCourse; // id del curso
    private int blocksWeek; // bloques por semana
    private String maxSections; // maximo numero de grupos
    private String minGapBlocks; // espacio minimo entre bloques
    private int minSections;
    private int minGapDays; //cada cuantos dias entre bloques
    private int rank; // prioridad
    private boolean GR; //
    private boolean balanceTeachers;
    private ArrayList<Integer> excludeRows; // bloques que no se pueden usar
    private ArrayList<Integer> excludeCols;
    private ArrayList<ArrayList<Tupla<Integer, Integer>>> preferedBlocks;
    private ArrayList<Tupla<Integer, Integer>> excludeBlocks;
    private int maxBlocksPerDay;
    private int sections;
    private int sectionsNoEnrolled;
    private double percentEnrolled;
    private ArrayList<Integer> studentsNoAsignados;
    private ArrayList<Integer> studentsAsignados;
    private ArrayList<ArrayList<Tupla>> patronesStudents;
    private int maxChildPerSection;
    private ArrayList<Integer> rooms;
    private ArrayList<Integer> trestricctions;
    private String preferedBlockString;
    private String mandatoryBlockRange;

    public Course(int idCourse) {
        this.idCourse = idCourse;
        this.rank = Integer.MAX_VALUE;
        huecos = new String[Algoritmo.TAMX][Algoritmo.TAMY];
        for (int i = 0; i < Algoritmo.TAMX; i++) {
            for (int j = 0; j < Algoritmo.TAMY; j++) {
                huecos[i][j] = "0";
            }
        }
        maxBlocksPerDay = 1;
        sections = 1;
        studentsNoAsignados = new ArrayList<>();
        patronesStudents = new ArrayList<>();
        trestricctions = new ArrayList();
        rooms = new ArrayList();

        maxSections = "";
        balanceTeachers = false;
        preferedBlockString = "";
        mandatoryBlockRange = "";

    }

    public String getMandatoryBlockRange() {
        return mandatoryBlockRange;
    }

    public void setMandatoryBlockRange(String mandatoryBlockRange) {
        this.mandatoryBlockRange = mandatoryBlockRange;
    }

    public void addRoom(int id) {
        rooms.add(id);
    }

    /**
     * Actualiza el nnumero de alumnos que no se han podido matricular.
     *
     * @param sectionsEnrolled
     */
    public void setSectionsNoEnrolled(int sectionsEnrolled) {
        this.sectionsNoEnrolled = sectionsEnrolled;
    }

    /**
     * Ocupa un hueco en una seccion
     *
     * @param list
     */
    public void ocuparHueco(ArrayList<Tupla> list) {
        if (list != null && !list.isEmpty()) {
            if (huecos[(Integer) list.get(0).x][(Integer) list.get(0).y].equals("0")) {
                for (Tupla<Integer, Integer> t : list) {
                    huecos[t.x][t.y] = "" + sections;
                }
            } else {
                for (Tupla<Integer, Integer> t : list) {
                    huecos[t.x][t.y] += " and " + sections;
                }
            }
            sections++;
        }
    }

    /**
     * Devuelve los huecos disponibles en los estudiantes no asignados al curso
     *
     * @return
     */
    public int[][] huecosStudents() {
        int[][] ret = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        for (ArrayList<Tupla> ar : this.patronesStudents) {
            for (Tupla<Integer, Integer> t : ar) {
                ret[t.x][t.y] = 1;
            }
        }
        return ret;
    }

    private void calcularTuplas(ArrayList<String> log, ArrayList<Integer> colsHabilitadas, ArrayList<Integer> rowsHabilitadas, ArrayList<Tupla> tuplasHabilitadas) {
        if (this.mandatoryBlockRange != null && !this.mandatoryBlockRange.equals("") && !this.mandatoryBlockRange.equals("*,*;")) {
            String[] parts = this.mandatoryBlockRange.split(";");
            for (int i = 0; i < parts.length; i++) {
                try {
                    String[] partsMand = parts[i].split(",");
                    if ("*".equals(partsMand[0])) {
                        rowsHabilitadas.add(Integer.parseInt(partsMand[1]) - 1);
                        for (int k = 0; k < Algoritmo.TAMX; k++) {
                            if (!colsHabilitadas.contains(k)) {
                                colsHabilitadas.add(k);
                            }
                        }
                    } else if ("*".equals(partsMand[1])) {
                        colsHabilitadas.add(Integer.parseInt(partsMand[0]) - 1);
                        for (int j = 0; j < Algoritmo.TAMY; j++) {
                            if (!rowsHabilitadas.contains(j)) {
                                rowsHabilitadas.add(j);
                            }
                        }

                    } else {
                        tuplasHabilitadas.add(new Tupla(Integer.parseInt(partsMand[0]) - 1, Integer.parseInt(partsMand[1]) - 1));
                    }
                } catch (Exception e) {
                    log.add("Problemas en  el mandatory block en el course con id= " + this.idCourse);
                }
            }
        } else {
            for (int i = 0; i < Algoritmo.TAMX; i++) {
                if (!colsHabilitadas.contains(i)) {
                    colsHabilitadas.add(i);
                }
                for (int j = 0; j < Algoritmo.TAMY; j++) {
                    if (!rowsHabilitadas.contains(j)) {
                        rowsHabilitadas.add(j);
                    }
                    tuplasHabilitadas.add(new Tupla(i, j));
                }
            }
        }
    }

    ArrayList<ArrayList<Tupla>> opciones(ArrayList<ArrayList<Boolean>> totalBlocks, ArrayList<String> log) {

        //AQUI HAY QUE LIMITAR LA CANTIDAD DE OCPIONES SI QUERERMOS QUE TENGAN BLOQUES OBLIGATORIOS ASIGNADOS.
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        try {
            if (maxSections == null && Integer.parseInt(maxSections) == 0
                    && Integer.parseInt(maxSections) <= sections) {
                return ret;
            }
        } catch (Exception e) {
            System.out.println("model.Course.opciones()");
        }
        ArrayList<Integer> colsHabilitadas = new ArrayList<>();
        ArrayList<Integer> rowsHabilitadas = new ArrayList<>();
        ArrayList<Tupla> tuplasHabilitadas = new ArrayList<>();

        calcularTuplas(log, colsHabilitadas, rowsHabilitadas, tuplasHabilitadas);
     
        int k, bloqueados;
        int gd = this.minGapDays;
        
        //RECORRE LAS TUPLAS DEFINIDAS COMO MANDATORY
        /*for (int ind = 0; ind < tuplasHabilitadas.size(); ind++) {
            ArrayList<Tupla> t = new ArrayList<>();
            k = this.blocksWeek;
            Tupla taux = new Tupla(tuplasHabilitadas.get(ind).x, tuplasHabilitadas.get(ind).y);

            if (colsHabilitadas.contains(tuplasHabilitadas.get(ind).x) && !t.contains(taux) && !this.excludeBlocks.contains(taux)
                    && !this.excludeCols.contains(tuplasHabilitadas.get(ind).x)
                    && totalBlocks.get((int) tuplasHabilitadas.get(ind).y).get((int) tuplasHabilitadas.get(ind).x)) {

                t.add(taux);
                k--;
                if (k <= 0) {
                    ret.add((ArrayList<Tupla>) t.clone());
                    t.remove(t.size() - 1);
                } else {
                    //FORMA CORRECTA SERIA VUELTA ATRAS PERO SE CONSIGUE ENCONTRAR LA MAYORIA DE RESULTADOS RECORRIENDO BIDIRECCIONAL
                    for (int l = 0; l < Algoritmo.TAMX; l++) { //cambiar algoritmo para q funcione teniendo en cuenta el maxbxd
                        if (Math.abs((int)tuplasHabilitadas.get(ind).x - l) >= gd) {
                            for (int m = 0; m < Algoritmo.TAMY; m++) {
                                Tupla taux2 = new Tupla(l, m);
                                if (((int)tuplasHabilitadas.get(ind).y != m && (int)tuplasHabilitadas.get(ind).x != l) && comprobarBxD(t, taux2) && !t.contains(taux2) && !this.excludeBlocks.contains(taux2)
                                        && !this.excludeCols.contains(l)
                                        && totalBlocks.get(m).get(l)) {
                                    t.add(taux2);
                                    k--;
                                    if (k <= 0 && !ret.contains(t)) {
                                        ret.add((ArrayList<Tupla>) t.clone());
                                        t.remove(t.size() - 1);
                                        k++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }*/
        // RECORRE LAS COLUMNAS Y FILAS HABILITADAS 
        for (int j = 0; j < Algoritmo.TAMY; j++) { // j son las filas  11 en MS
            if (rowsHabilitadas.contains(j) && (excludeRows == null && excludeCols == null && excludeBlocks == null) || !excludeRows.contains(j + 1)) {

                /*if (gd == 0) {
                    gd++;
                }
                int sumC = gd; //falta insertar lo del gd*/
                for (int i = 0; i < Algoritmo.TAMX; i++) { //i son las cols 3 en MS 
                    ArrayList<Tupla> t = new ArrayList<>();
                    k = this.blocksWeek;
                    Tupla taux = new Tupla(i, j);

                    if (colsHabilitadas.contains(i) && !t.contains(taux) && !this.excludeBlocks.contains(taux)
                            && !this.excludeCols.contains(i)
                            && totalBlocks.get(j).get(i)) {

                        t.add(taux);
                        k--;
                        if (k <= 0) {
                            ret.add((ArrayList<Tupla>) t.clone());
                            t.remove(t.size() - 1);
                        } else {
                            /*int antK =k;
                            ArrayList<Tupla> antT = (ArrayList<Tupla>) t.clone();
                             */
                            //FORMA CORRECTA SERIA VUELTA ATRAS PERO SE CONSIGUE ENCONTRAR LA MAYORIA DE RESULTADOS RECORRIENDO BIDIRECCIONAL
                            for (int l = 0; l < Algoritmo.TAMX; l++) { //cambiar algoritmo para q funcione teniendo en cuenta el maxbxd
                                if (Math.abs(i - l) >= gd) {
                                    for (int m = 0; m < Algoritmo.TAMY; m++) {
                                        Tupla taux2 = new Tupla(l, m);
                                        if ((j != m && i != l) && comprobarBxD(t, taux2) && !t.contains(taux2) && !this.excludeBlocks.contains(taux2)
                                                && !this.excludeCols.contains(l)
                                                && totalBlocks.get(m).get(l)) {
                                            t.add(taux2);
                                            k--;
                                            if (k <= 0 && !ret.contains(t)) {
                                                ret.add((ArrayList<Tupla>) t.clone());
                                                t.remove(t.size() - 1);
                                                k++;
                                            }
                                        }
                                    }
                                }
                            }
                            /*k = antK;
                            t = antT;
                            for (int l = Algoritmo.TAMX-1; l >=  0; l--) { //cambiar algoritmo para q funcione teniendo en cuenta el maxbxd
                                for (int m = Algoritmo.TAMY-1; m >=0 ; m--) {
                                    Tupla taux2 = new Tupla(l, m);
                                    if ((j != m && i != l) && comprobarBxD(t, taux2) && !t.contains(taux2) && !this.excludeBlocks.contains(taux2)
                                            && !this.excludeCols.contains(l)
                                            && totalBlocks.get(m).get(l)) {
                                        t.add(taux2);
                                        k--;
                                        if (k <= 0 && !ret.contains(t)) {
                                            ret.add((ArrayList<Tupla>) t.clone());
                                            t.remove(t.size() - 1);
                                            k++;
                                        }
                                    }

                                }
                            }*/

                        }
                    }
                }

            }
        }

        return ret;
    }

    private boolean comprobarBxD(ArrayList<Tupla> t, Tupla taux2) {
        int numBlocksWeek = 0;
        for (int i = 0; i < t.size(); i++) {
            if (t.get(i).x == taux2.x) {
                numBlocksWeek++;
            }
        }
        return this.maxBlocksPerDay > numBlocksWeek;
    }

    ArrayList<ArrayList<Tupla>> opciones(ArrayList<ArrayList<Boolean>> totalBlocks) {

        //AQUI HAY QUE LIMITAR LA CANTIDAD DE OCPIONES SI QUERERMOS QUE TENGAN BLOQUES OBLIGATORIOS ASIGNADOS.
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        try {
            if (maxSections == null && Integer.parseInt(maxSections) == 0
                    && Integer.parseInt(maxSections) <= sections) {
                return ret;
            }
        } catch (Exception e) {
            System.out.println("model.Course.opciones()");
        }

        //int mandatoryCols =Algoritmo.TAMX,mandatoryRows = Algoritmo.TAMY;
        ArrayList<Integer> cols = new ArrayList<>();
        ArrayList<Integer> rows = new ArrayList<>();

        //  calcularBlocksRange(cols, rows);
        //int[][] matrizTotal = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        for (int j = 0; j < Algoritmo.TAMY; j++) { // j son las filas  11 en MS
            if ((excludeRows == null && excludeCols == null && excludeBlocks == null)
                    || !excludeRows.contains(j + 1)) {
                int k, bloqueados;
                int gd = this.minGapDays;
                if (gd == 0) {
                    gd++;
                }
                int sum = gd;
                for (int i = 0; i < Algoritmo.TAMX; i++) { //i son las cols 3 en MS 
                    ArrayList<Tupla> t = new ArrayList<>();
                    k = this.blocksWeek;
                    Tupla taux = new Tupla(i, j);

                    if (!t.contains(taux) && !this.excludeBlocks.contains(taux)
                            && !this.excludeCols.contains(i)
                            && totalBlocks.get(j).get(i)) {

                        t.add(taux);
                        k--;
                        /*nuevo*/
                        if (k <= 0) {
                            ret.add((ArrayList<Tupla>) t.clone());
                            t.remove(t.size() - 1);
                        } else {
                            for (int l = i + 1; l < Algoritmo.TAMX; l++) { //cambiar algoritmo para q funcione teniendo en cuenta el maxbxd
                                for (int m = 0; m < Algoritmo.TAMY; m++) {
                                    Tupla taux2 = new Tupla(l, m);
                                    if (!t.contains(taux2) && !this.excludeBlocks.contains(taux2)
                                            && !this.excludeCols.contains(l)
                                            && totalBlocks.get(m).get(l)) {
                                        t.add(taux2);
                                        k--;
                                        if (k <= 0) {
                                            ret.add((ArrayList<Tupla>) t.clone());
                                            t.remove(t.size() - 1);
                                            k++;
                                        }
                                    }
                                }
                            }
                        }

                        /*
                        int cont = 0;
                        while (i+sumC < Algoritmo.TAMX && cont < Algoritmo.TAMY) {
                            Tupla taux2 = new Tupla(i+sumC, cont);
                            if (!t.contains(taux2) && !this.excludeBlocks.contains(taux2)
                                    && !this.excludeCols.contains(i+sumC)
                                    && totalBlocks.get(cont).get(i+sumC)) {
                                t.add(taux2);
                                k--;
                                if (k <= 0/* && !ret.contains(t)) {
                                    ret.add(t);
                                    t.remove(t.size()-1);
                                    k++;
                                }
                            } 
                            cont++;
                            sumC++;
                        }*/
                    }
                }

            }
        }

        /*for (int j = 0; j < Algoritmo.TAMY; j++) { // j son las filas  11 en MS
            if ((excludeRows == null && excludeCols == null && excludeBlocks == null)
                    || !excludeRows.contains(j + 1)) {
                int k, bloqueados;
                int gd = this.minGapDays;
                if (gd == 0) {
                    gd++;
                }
                for (int i = 0; i < Algoritmo.TAMX; i++) { //i son las cols 3 en MS
                    ArrayList<Tupla> t = new ArrayList<>();
                    int sum = 0;
                    bloqueados = 0;
                    k = this.blocksWeek;

                    while (k > 0) {
                        Tupla taux = new Tupla((i + sum) % Algoritmo.TAMX, j);
                        
                        if (!t.contains(taux) && !this.excludeBlocks.contains(taux)
                                && !this.excludeCols.contains((i + sum) % Algoritmo.TAMX)
                                && totalBlocks.get(j).get(i)
                                && totalBlocks.get(j).get((i + sum) % Algoritmo.TAMX)) {
                            t.add(taux);
                            k--;
                        } 
                        else {
                            bloqueados++;
                        }
                        
                        if (bloqueados > Algoritmo.TAMY) {
                            break;
                        }
                        sum += gd;                      
                    }

                    if (k <= 0 && !ret.contains(t)) {
                        ret.add(t);
                    }
                }
            }
        }*/
        return ret;
    }

    /**
     * Devuelve los huecos donde se puede colocar una seccion
     *
     * @return
     */
    /*public ArrayList<ArrayList<Tupla>> opciones() {
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        try {
            if (maxSections == null && Integer.parseInt(maxSections) == 0
                    && Integer.parseInt(maxSections) <= sections) {
                return ret;
            }
        } catch (Exception e) {
        }
        for (int j = 0; j < Algoritmo.TAMY; j++) { // j son las filas  11 en MS
            if ((excludeRows == null && excludeCols == null && excludeBlocks == null)
                    || !excludeRows.contains(j + 1)) {
                int k, bloqueados;
                int gd = this.minGapDays;
                if (gd == 0) {
                    gd++;
                }
                for (int i = 0; i < Algoritmo.TAMX; i++) { //  i son las filas 11 en MS
                    ArrayList<Tupla> t = new ArrayList<>();
                    int sum = 0;
                    bloqueados = 0;
                    k = this.blocksWeek;
                    while (k > 0) {
                        Tupla taux = new Tupla((i + sum) % Algoritmo.TAMX, j);
                        if (!t.contains(taux) && !this.excludeBlocks.contains(taux)
                                && !this.excludeCols.contains((i + sum) % Algoritmo.TAMX)) {
                            t.add(taux);
                            k--;
                        } else {
                            bloqueados++;
                        }
                        if (bloqueados > Algoritmo.TAMY) {
                            break;
                        }
                        sum += gd;
                    }
                    if (k <= 0 && !ret.contains(t)) {
                        ret.add(t);
                    }
                }
            }
        }
        return ret;
    }*/
    public ArrayList<ArrayList<Boolean>> opcionesStart() { // AQUI ES DONDE SE LIMUTAN LOS HUECOS DISPONIBLES INICIO
        ArrayList<ArrayList<Boolean>> ret = new ArrayList<>();
        for (int j = 1; j <= Algoritmo.TAMY; j++) { // AQUI ES EL FALLO COMPARA LA HORA CON EL DIA TENIENDO EN CUENTA QUE LAS Y SON LAS HORAS Y LAS X LOS DIAS
            if (!excludeCols.contains(j)) {
                ArrayList<Boolean> t = new ArrayList<>();
                for (int i = 1; i <= Algoritmo.TAMX; i++) { // cambiar!  
                    int bloqueados = 0;
                    Tupla taux = new Tupla(i, j);
                    if (!this.excludeBlocks.contains(taux) && !this.excludeRows.contains(i)) {
                        t.add(true);
                    } else {
                        bloqueados++;
                        t.add(false);
                    }
                }
                ret.add(t);
            } else {
                ArrayList<Boolean> auxFalse = new ArrayList();

                for (int i = 1; i <= Algoritmo.TAMX; i++) {
                    auxFalse.add(false);
                }

                ret.add(auxFalse);
            }
        }
        return ret;
    }
    //---------------------------------
    //-------GETTERS AND SETTERS-------
    //---------------------------------

    public ArrayList<Integer> getRooms() {
        return rooms;
    }

    public void setRooms(String rooms) {
        String rparse1 = rooms.substring(1, rooms.length() - 1);
        String[] rparse2 = rparse1.split(",");
        for (String s : rparse2) {
            try {
                this.rooms.add(Integer.parseInt(s));
            } catch (Exception e) {
            }
        }
    }

    public int getMinSections() {
        return minSections;
    }

    public void setMinSections(int minSections) {
        this.minSections = minSections;
    }

    public ArrayList<ArrayList<Tupla>> getPatronesStudents() {
        return patronesStudents;
    }

    public void setPatronesStudents(ArrayList<ArrayList<Tupla>> patronesStudents) {
        if (patronesStudents != null) {
            this.patronesStudents = patronesStudents;
        }
    }

    public int getSectionsNoEnrolled() {
        return sectionsNoEnrolled;
    }

    public int getMaxChildPerSection() {
        return maxChildPerSection;
    }

    public void setMaxChildPerSection(int maxChildPerSection) {
        this.maxChildPerSection = maxChildPerSection;
    }

    public double getPercentEnrolled() {
        return percentEnrolled;
    }

    public void setPercentEnrolled(double percentEnrolled) {
        this.percentEnrolled = percentEnrolled;
    }

    public ArrayList<Integer> getStudentsAsignados() {
        return studentsAsignados;
    }

    public void setStudentsAsignados(ArrayList<Integer> studentsAsignados) {
        this.studentsAsignados = studentsAsignados;
    }

    public ArrayList<Integer> getStudentsNoAsignados() {
        return studentsNoAsignados;
    }

    public void setStudentsNoAsignados(ArrayList<Integer> studentsNoAsignados) {
        this.studentsNoAsignados = studentsNoAsignados;
    }

    public int getSections() {
        return sections;
    }

    public int getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(int idCourse) {
        this.idCourse = idCourse;
    }

    public int getBlocksWeek() {
        return blocksWeek;
    }

    public void setBlocksWeek(int blocksWeek) {
        this.blocksWeek = blocksWeek;
    }

    public int getMaxSections() {
        return Integer.parseInt(maxSections);
    }

    public void setMaxSections(String maxSections) {
        if (maxSections != null) {
            this.maxSections = maxSections;
        }
    }

    public String getMinGapBlocks() {
        return minGapBlocks;
    }

    public void setMinGapBlocks(String minGapBlocks) {
        this.minGapBlocks = minGapBlocks;
    }

    public int getMinGapDays() {
        return minGapDays;
    }

    public void setMinGapDays(int minGapDays) {
        this.minGapDays = minGapDays;
    }

    public boolean isBalanceTeachers() {
        return balanceTeachers;
    }

    public void setBalanceTeachers(boolean balanceTeachers) {
        this.balanceTeachers = balanceTeachers;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isGR() {
        return GR;
    }

    public void setGR(boolean GR) {
        this.GR = GR;
    }

    public int getMaxBlocksPerDay() {
        return maxBlocksPerDay;
    }

    public void setMaxBlocksPerDay(int maxBlocksPerDay) {
        this.maxBlocksPerDay = maxBlocksPerDay;
    }

    public void setExcludeBlocks(String excludeBlocks) {
        String[] s = excludeBlocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        this.excludeCols = new ArrayList();
        this.excludeRows = new ArrayList();
        if (!s[0].equals("")) {
            for (String s2 : s) {
                elem = s2.split(",");
                int row = -1;
                int col = -1;
                try {
                    row = Integer.parseInt(elem[0]);
                } catch (Exception e) {
                }
                try {
                    col = Integer.parseInt(elem[1]);
                } catch (Exception e) {
                }
                if (row == -1) {
                    this.excludeCols.add(col);
                } else if (col == -1) {
                    this.excludeRows.add(row);
                } else {
                    this.excludeBlocks.add(new Tupla(row, col));
                }
            }
        }
    }

    public ArrayList<Integer> getTrestricctions() {
        return trestricctions;
    }

    public void setTrestricctions(ArrayList<Integer> trestricctions) {
        this.trestricctions = trestricctions;
    }

    public String[][] getHuecos() {
        return huecos;
    }

    private String excludeBlocksToString() {
        String ret = "";
        for (Tupla t : this.excludeBlocks) {
            ret += t.x.toString() + "," + t.y.toString() + ";";
        }
        return ret;
    }

    public void setExcludeBlocksOwnDB(String excludeblocks) {
        String[] s = excludeblocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        if (!s[0].equals("")) {
            for (String s2 : s) {
                elem = s2.split(",");
                int row = -1;
                int col = -1;
                try {
                    row = Integer.parseInt(elem[0]);
                } catch (Exception e) {
                }
                try {
                    col = Integer.parseInt(elem[1]);
                } catch (Exception e) {
                }
                if (row != -1 && col != -1) {
                    this.excludeBlocks.add(new Tupla(row, col));
                }
            }
        }
    }

    public void setPreferedBlocks(String s) {
        this.preferedBlockString = s;
        if (s != null && s != "") {
            this.preferedBlocks = new ArrayList();
            //falta terminar
            String[] restric_sec = s.split("]"), restric_bloq, restric_elem;

            for (String restric_sec1 : restric_sec) {
                String aux = restric_sec1.substring(1, restric_sec1.length());
                restric_bloq = aux.split(";");
                ArrayList<Tupla<Integer, Integer>> arrayAux = new ArrayList();
                for (String restric_bloq1 : restric_bloq) {
                    restric_elem = restric_bloq1.split(",");
                    int row = -1;
                    int col = -1;
                    try {
                        row = Integer.parseInt(restric_elem[0]);
                        col = Integer.parseInt(restric_elem[1]);
                    } catch (Exception e) {
                        System.out.println("Problemas model.Course.setPreferedBlocks()");
                    }
                    if (row != -1 && col != -1) {
                        arrayAux.add(new Tupla(row, col));
                    }
                }
                this.preferedBlocks.add(arrayAux);
            }
        }
    }

    public ArrayList<ArrayList<Tupla<Integer, Integer>>> getPreferedBlocks() {
        return preferedBlocks;
    }

    public void setPreferedBlocks(ArrayList<ArrayList<Tupla<Integer, Integer>>> preferedBlocks) {
        this.preferedBlocks = preferedBlocks;
    }

    public void setExcludeCols(String cols) {
        String[] s = cols.split(",");
        String[] elem;
        this.excludeCols = new ArrayList();
        if (!s[0].equals("")) {
            for (String s2 : s) {
                int col = -1;
                try {
                    col = Integer.parseInt(s2);
                } catch (Exception e) {
                }
                if (col != -1) {
                    this.excludeCols.add(col);
                }
            }
        }
    }

    public void setExcludeRows(String rows) {
        String[] s = rows.split(",");
        String[] elem;
        this.excludeRows = new ArrayList();
        if (!s[0].equals("")) {
            for (String s2 : s) {
                int row = -1;
                try {
                    row = Integer.parseInt(s2);
                } catch (Exception e) {
                }
                if (row != -1) {
                    this.excludeRows.add(row);
                }
            }
        }
    }

    /**
     * inserta o actualiza si ya existe ,el curso en nuestra base de datos.
     */
    public void insertarOActualizarCurso() {
        String consulta = "select * from courses where id=" + this.idCourse;
        boolean actualizar = false;
        try {
            int maxsec = 0, mingapblocks = 0;
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                actualizar = true;
            }
            if (!actualizar) {

                try {
                    maxsec = Integer.parseInt(this.maxSections);
                } catch (Exception e) {
                }

                try {
                    mingapblocks = Integer.parseInt(this.minGapBlocks);
                } catch (Exception e) {
                }

                consulta = "insert into courses values(" + this.idCourse + "," + this.blocksWeek + ","
                        + maxsec + "," + mingapblocks + ","
                        + this.minGapDays + "," + this.rank + "," + this.GR + ",'"
                        + excludeBlocksToString() + "'," + this.maxBlocksPerDay + ",'"
                        + this.rooms.toString() + "','" + this.excludeCols.toString()
                        + "','" + this.excludeRows.toString() + "','" + this.trestricctions.toString()
                        + "','" + this.balanceTeachers + "','" + this.preferedBlockString + "')";
                DBConnect.own.executeUpdate(consulta);
            } else {// to do
                /*   consulta = "UPDATE courses SET blocksperweek= "+this.blocksWeek+" ,maxsections= "+maxsec+" ,mingapblocks= "
                        +mingapblocks+ " ,mingapdays= "+this.minGapDays+ " ,rank= "+this.rank+" ,gender= "+this.GR
                        +" ,excludeblocks= "+excludeBlocksToString()+" ,maxblocksperday= "+this.maxBlocksPerDay
                        +" ,rooms= "+this.rooms.toString()+" ,excludecols="+this.excludeCols.toString()
                        +" ,excluderows="+this.excludeRows.toString()+" ,teachers="+ this.trestricctions.toString()
                        +" ,balanceteacher="+this.balanceTeachers + " , preferedblocks="+ this.preferedBlockString + "where ";
                 */

            }
        } catch (SQLException ex) {
            Logger.getLogger(Course.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean equals(Object c) {
        return this.idCourse == ((Course) c).idCourse;
    }

    public Teacher getTeacher(List<Teacher> aTeacher, int idCourse, int section) {
        int idCod = (idCourse * 100) + section;
        for (Teacher aTeacherElem : aTeacher) {
            for (int i = 0; i < aTeacherElem.getHuecos().length; i++) {
                for (int j = 0; j < aTeacherElem.getHuecos()[0].length; j++) {
                    if (aTeacherElem.getHuecos()[i][j] == idCod) {
                        return aTeacherElem;
                    }
                }
            }
        }
        return null;
    }
}
