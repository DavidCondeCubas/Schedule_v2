/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Consultas;
import dataManage.Tupla;
import static java.lang.Integer.max;
import static java.lang.Math.random;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Course {

    public static int MUESTRA = 1000000;
    public static int MAX_INTENTOS = 5000000;
    private String[][] huecos; // cuadricula
    private int idCourse; // id del curso
    private String nameCourse;
    private int blocksWeek; // bloques por semana
    private String maxSections; // maximo numero de grupos
    private int minGapBlocks; // espacio minimo entre bloques
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
    private HashMap<String, ArrayList<Tupla<Integer, Integer>>> priorityPattern;
    private String patternGroup;
    private ArrayList<Seccion> arraySecciones;
    private ArrayList<Integer> sectionsLinkeadas;
    private ArrayList<ArrayList<Tupla>> opcionesPatternGroup;
    private int minChildPerSection;

    public Course(Course c) {
        this.huecos = c.getHuecos(); // cuadricula
        this.idCourse = c.getIdCourse(); // id del curso
        this.blocksWeek = c.getBlocksWeek(); // bloques por semana
        this.maxSections = "" + c.getMaxSections(); // maximo numero de grupos
        this.minGapBlocks = c.getMinGapBlocks(); // espacio minimo entre bloques
        this.minSections = c.getMinSections();
        this.minGapDays = c.getMinGapDays(); //cada cuantos dias entre bloques
        this.rank = c.getRank(); // prioridad
        this.GR = c.isGR(); //
        this.balanceTeachers = c.isBalanceTeachers();
        this.excludeRows = c.getExcludeRows(); // bloques que no se pueden usar
        this.excludeCols = c.getExcludeCols();
        this.preferedBlocks = c.getPreferedBlocks();
        this.excludeBlocks = c.getExcludeBlocks();
        this.maxBlocksPerDay = c.getMaxBlocksPerDay();
        this.sections = c.getSections();
        this.sectionsNoEnrolled = c.getSectionsNoEnrolled();
        this.percentEnrolled = c.getPercentEnrolled();
        this.studentsNoAsignados = c.getStudentsNoAsignados();
        this.studentsAsignados = c.getStudentsAsignados();
        this.patronesStudents = c.getPatronesStudents();
        this.maxChildPerSection = c.getMaxChildPerSection();
        this.rooms = c.getRooms();
        this.trestricctions = c.getTrestricctions();
        this.preferedBlockString = c.getPreferedBlockString();
        this.mandatoryBlockRange = c.getMandatoryBlockRange();
        this.sectionsLinkeadas = c.getSectionsLinkeadas();
        this.minChildPerSection = c.getMinChildPerSection();
        this.opcionesPatternGroup = c.getOpcionesPatternGroup();
    }

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
        minGapBlocks = 0; // espacio minimo entre bloques
        minGapDays =1; //cada cuantos dias entre bloques  
        sections = 1;
        studentsNoAsignados = new ArrayList<>();
        patronesStudents = new ArrayList<>();
        trestricctions = new ArrayList();
        rooms = new ArrayList();
        preferedBlocks = new ArrayList<>();
        maxSections = "";
        balanceTeachers = false;
        preferedBlockString = "";
        mandatoryBlockRange = "";
        this.patternGroup = "";
        this.arraySecciones = new ArrayList<>();
        this.sectionsLinkeadas = new ArrayList<>();
        this.opcionesPatternGroup =new ArrayList<>();
    }

    public String getNameCourse() {
        return nameCourse;
    }

    public void setNameCourse(String nameCourse) {
        this.nameCourse = nameCourse;
    }

    public ArrayList<Integer> getSectionsLinkeadas() {
        return sectionsLinkeadas;
    }

    public void setSectionsLinkeadas(ArrayList<Integer> sectionsLinkeadas) {
        this.sectionsLinkeadas = sectionsLinkeadas;
    }

    public void fillPriorityPattern() {
        this.priorityPattern = new HashMap<>();
        /*
        SELECT * FROM (SchedulePatterns inner join 
                              SchedulePatternsTimeTable 
                        on(SchedulePatterns.PatternNumber = SchedulePatternsTimeTable.PatternNumber
                           and
                            SchedulePatterns.TemplateID = SchedulePatternsTimeTable.TemplateID)) where PatternGroup = '06_Grade';

--SELECT PatternGroup FROM IS_PAN.dbo.Courses where CourseID=669;

         */

        String consulta = "SELECT PatternGroup FROM Courses where CourseID= '" + this.idCourse + "'";
        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                this.patternGroup = rs.getString(1);
            }

            if (!this.patternGroup.equals("")) {
                consulta = "SELECT *  FROM (SchedulePatterns inner join "
                        + "                              SchedulePatternsTimeTable"
                        + "                        on(SchedulePatterns.PatternNumber = SchedulePatternsTimeTable.PatternNumber"
                        + "                           and"
                        + "                            SchedulePatterns.TemplateID = SchedulePatternsTimeTable.TemplateID)) where PatternGroup = '" + this.patternGroup + "'";

                rs = DBConnect.renweb.executeQuery(consulta);
                String name = "";
                int row = -1, col = -1;

                while (rs.next()) {
                    name = rs.getString("Name");
                    row = rs.getInt("Row");
                    col = rs.getInt("Col");
                    if (!this.priorityPattern.containsKey(name)) {
                        this.priorityPattern.put(name, new ArrayList<>());
                        //  this.priorityPattern.get(name).add(new Tupla(col, row));
                        //  this.priorityPattern.get(name).add(new Tupla(col,row));
                    }
                    /* else{
                     this.priorityPattern.put(name, new ArrayList<>());
                      this.priorityPattern.get(name).add(new Tupla(col,row));
                }*/
                    this.priorityPattern.get(name).add(new Tupla(col, row));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public void ocuparHueco(ArrayList<Tupla> list, Integer numSeccion) {
        if (list != null && !list.isEmpty()) {
            /*           
            if (huecos[(Integer) list.get(0).x][(Integer) list.get(0).y].equals("0")) {
                for (Tupla<Integer, Integer> t : list) {
                    huecos[t.x][t.y] = "" + numSeccion;
                }
            } else {
                for (Tupla<Integer, Integer> t : list) {
                    huecos[t.x][t.y] += " and " + numSeccion;
                }
            }*/
            for (Tupla<Integer, Integer> t : list) {
                if (huecos[t.x][t.y].equals("0")) {
                    huecos[t.x][t.y] = "" + numSeccion;
                } else {
                    huecos[t.x][t.y] += " and " + numSeccion;
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

    ArrayList<ArrayList<Tupla>> opcionesLinkeados(ArrayList<ArrayList<Tupla>> retAsociado, ArrayList<ArrayList<Boolean>> totalBlocks, ArrayList<String> log) {
        // ESTO PRACTICAMENTE SE METERAN A PELO PARA EL IS-PAN MD EN ESPECIFICO
        //AQUI HAY QUE LIMITAR LA CANTIDAD DE OCPIONES SI QUERERMOS QUE TENGAN BLOQUES OBLIGATORIOS ASIGNADOS.
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();

        ArrayList<String> auxPatterns = getNamesPatternsPosibles();

        for (int i = 0; i < auxPatterns.size(); i++) {
            ArrayList<Tupla> aux = new ArrayList<>();
            ArrayList<Tupla> aux2 = new ArrayList<>();

            for (int j = 0; j < this.priorityPattern.get(auxPatterns.get(i)).size(); j++) {
                int col = this.priorityPattern.get(auxPatterns.get(i)).get(j).x;
                int row = this.priorityPattern.get(auxPatterns.get(i)).get(j).y;
                aux.add(new Tupla(col - 1, row - 1));

                col = this.priorityPattern.get(tuplaAsociada(auxPatterns.get(i))).get(j).x;
                row = this.priorityPattern.get(tuplaAsociada(auxPatterns.get(i))).get(j).y;
                aux2.add(new Tupla(col - 1, row - 1));
            }
            retAsociado.add(aux2);
            ret.add(aux);
        }

        return ret;
    }

    private String tuplaAsociada(String s) {
        switch (s) {
            case "6A":
                return "6B";
            case "6B":
                return "6A";
            case "6C":
                return "6D";
            case "6D":
                return "6C";
            case "6F":
                return "6G";
            case "6G":
                return "6F";
        }
        return "";
    }

    public ArrayList<Tupla<Integer, Integer>> getTuplaLinked(int col, int row) {
        /*for (Map.Entry<String, ArrayList<Tupla<Integer, Integer>>> entry : this.priorityPattern.entrySet()) {
            String key = entry.getKey();
            ArrayList<Tupla<Integer, Integer>> value = entry.getValue();
            int pos = value.indexOf(new Tupla(col, row));
            if (pos != -1) {
                if (pos == 0) {
                    return new Tupla(value.get(1).x, value.get(1).y);
                } else {
                    return new Tupla(value.get(0).x, value.get(0).y);
                }
            }
        }*/
        return null;
    }

    private ArrayList<String> getNamesPatternsPosibles() {
        switch (this.patternGroup) {
            case "06_Grade":
                ArrayList<String> aux = new ArrayList<>();
                aux.add("6A");
                aux.add("6B");
                aux.add("6C");
                aux.add("6D");
                aux.add("6F");
                aux.add("6G");
                return aux;
        }
        return null;
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


        Tupla[] sol = new Tupla[this.blocksWeek];
        boolean[][] marcas = new boolean[Algoritmo.TAMY][Algoritmo.TAMX];

        for (int i = 0; i < Algoritmo.TAMY; i++) {
            for (int j = 0; j < Algoritmo.TAMX; j++) {
                marcas[i][j] = false;
            }
        }
        int maxVueltas = 0;
        
        recurOpciones( maxVueltas,ret, Algoritmo.TAMX, Algoritmo.TAMY, sol, 0, marcas,  totalBlocks);

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
        /*   for (int j = 0; j < Algoritmo.TAMY; j++) { // j son las filas  11 en MS
            if (rowsHabilitadas.contains(j) && (excludeRows == null && excludeCols == null && excludeBlocks == null) || !excludeRows.contains(j + 1)) {
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

                        }
                    }
                }

            }
        }*/
      //  return takeNelements(MUESTRA, ret);
      return ret;
    }

    private ArrayList<ArrayList<Tupla>> takeNelements(int n, ArrayList<ArrayList<Tupla>> ret) {
        Collections.shuffle(ret);
        int maxLongitud = Math.min(n, ret.size());
        ArrayList<ArrayList<Tupla>> aux = new ArrayList<>();
        for (int i = 0; i < maxLongitud; i++) {
            aux.add(ret.get(i));
        }
        return aux;
    }

    private void recurOpciones(int vueltas,ArrayList<ArrayList<Tupla>> solucionFinal, int tamX, int tamY,
            Tupla[] sol, int k,
            boolean[][] marcas,  ArrayList<ArrayList<Boolean>> totalBlocks) {

     
         vueltas ++;
        if(vueltas >= MAX_INTENTOS) return;
        
        for (int j = 0; j < tamY; j++) {            
            for (int i = 0; i < tamX; i++) {
                
                i = (int)(Math.random() * tamX); 
                j = (int)(Math.random() * tamY); 
                
                sol[k] = new Tupla(i, j);
                if ( solucionFinal.size() < MUESTRA && esValida(sol, k, marcas[j][i], j, i, totalBlocks)) {
                    marcas[j][i] = true;
                    sol[k] = new Tupla(i, j);
                    if (k == this.blocksWeek - 1) {
                        //solucionFinal.add(sol);

                        ArrayList<Tupla> aList = new ArrayList<>(Arrays.asList(sol));
                        solucionFinal.add(aList);
                    } else {
                        k = k + 1;
                        recurOpciones(vueltas,solucionFinal, tamX, tamY, sol, k, marcas, totalBlocks);
                        k = k - 1;
                    }
                    marcas[j][i] = false;
                }
                // k = antK;            
            }
        }
    }

    private boolean esValida(Tupla[] sol, int k, boolean marca, int y, int x,    ArrayList<ArrayList<Boolean>> totalBlocks) {

        if (x == 3 && y == 3) { //solo pruebas
            System.out.println("model.Course.esValida()");
        }
                            boolean boolExcludes = this.excludeCols.contains(x + 1) || this.excludeRows.contains(y + 1) || this.excludeBlocks.contains(new Tupla(y + 1, x + 1));

        int i = 0;
        while (i < k) {
            if (Math.abs((Integer) sol[i].x - x) < this.minGapDays && Math.abs((Integer) sol[i].y - y) < this.minGapBlocks) {
                return false;
            }
            i++;
        }
        
        return !boolExcludes && !marca  && totalBlocks.get(y).get(x);
        /*if (rowsHabilitadas.contains(j) && (excludeRows == null && excludeCols == null && excludeBlocks == null) || !excludeRows.contains(j + 1)) {
                for (int i = 0; i < Algoritmo.TAMX; i++) { //i son las cols 3 en MS 
                    ArrayList<Tupla> t = new ArrayList<>();
                    k = this.blocksWeek;
                    Tupla taux = new Tupla(i, j);

                    if (colsHabilitadas.contains(i) && !t.contains(taux) && !this.excludeBlocks.contains(taux)
                            && !this.excludeCols.contains(i)
                            && totalBlocks.get(j).get(i)) {
                        
                    }*/

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

    public String getMaxSections() {
        return maxSections;
    }

    public void setMaxSections(String maxSections) {
        if (maxSections != null) {
            this.maxSections = maxSections;
        }
    }

    public int getMinGapBlocks() {
        return minGapBlocks;
    }

    public void setMinGapBlocks(int minGapBlocks) {
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
                   // mingapblocks = Integer.parseInt(this.minGapBlocks);
                   mingapblocks =  this.minGapBlocks;
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
            Logger.getLogger(Course.class
                    .getName()).log(Level.SEVERE, null, ex);
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

    public ArrayList<Integer> getExcludeRows() {
        return excludeRows;
    }

    public void setExcludeRows(ArrayList<Integer> excludeRows) {
        this.excludeRows = excludeRows;
    }

    public ArrayList<Integer> getExcludeCols() {
        return excludeCols;
    }

    public void setExcludeCols(ArrayList<Integer> excludeCols) {
        this.excludeCols = excludeCols;
    }

    public ArrayList<Tupla<Integer, Integer>> getExcludeBlocks() {
        return excludeBlocks;
    }

    public void setExcludeBlocks(ArrayList<Tupla<Integer, Integer>> excludeBlocks) {
        this.excludeBlocks = excludeBlocks;
    }

    public String getPreferedBlockString() {
        return preferedBlockString;
    }

    public void setPreferedBlockString(String preferedBlockString) {
        this.preferedBlockString = preferedBlockString;
    }

    public ArrayList<Seccion> getArraySecciones() {
        return arraySecciones;
    }

    public void setArraySecciones(ArrayList<Seccion> arraySecciones) {
        this.arraySecciones = arraySecciones;
    }

    public void addSeccion(Seccion e) {
        this.arraySecciones.add(e);
    }
    
    public void addOption(ArrayList<Tupla> e) {
        this.opcionesPatternGroup.add(e);
    }
    
    public ArrayList<Integer> getAllIds() {
        ArrayList<Integer> auxStudents = new ArrayList<>();

        for (int i = 0; i < this.arraySecciones.size(); i++) {
            for (int j = 0; j < this.arraySecciones.get(i).getIdStudents().size(); j++) {
                auxStudents.add(this.arraySecciones.get(i).getIdStudents().get(j));
            }
        }

        return auxStudents;
    }

    public void setSectionsLinkeadas(String[] secLinks) {
        for (int i = 0; i < secLinks.length; i++) {
            this.sectionsLinkeadas.add(Integer.parseInt(secLinks[i]));
        }
    }

    public HashMap<String, ArrayList<Tupla<Integer, Integer>>> getPriorityPattern() {
        return priorityPattern;
    }

    public void setPriorityPattern(HashMap<String, ArrayList<Tupla<Integer, Integer>>> priorityPattern) {
        this.priorityPattern = priorityPattern;
    }

    public String getPatternGroup() {
        return patternGroup;
    }

    public void setPatternGroup(String patternGroup) {
        this.patternGroup = patternGroup;
    }

    public int getMinChildPerSection() {
        return minChildPerSection;
    }

    public void setMinChildPerSection(int minChildPerSection) {
        this.minChildPerSection = minChildPerSection;
    }

    public ArrayList<ArrayList<Tupla>> getOpcionesPatternGroup() {
        return opcionesPatternGroup;
    }

    public void setOpcionesPatternGroup(ArrayList<ArrayList<Tupla>> opcionesPatternGroup) {
        this.opcionesPatternGroup = opcionesPatternGroup;
    }
    
    //solo testing
    public boolean hayEstudiantes(){
        for (int i = 0; i < this.getArraySecciones().size(); i++) {
            if(this.getArraySecciones().get(i).getIdStudents().size() > 0) return true;
        }
        return false;
    }
    
}
