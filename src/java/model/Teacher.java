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
public class Teacher{
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
    private HashMap<Integer,Integer> secciones;
    private ArrayList excludeCols;
    private ArrayList excludeRows;
    private int numSecciones;

    public int getNumSecciones() {
        return numSecciones;
    }

    public void setNumSecciones(int numSecciones) {
        this.numSecciones = numSecciones;
    }
    public void incrementarNumSecciones(){
        this.numSecciones++;
    }
    
    
    public void insertarOActualizarDB(){
        String consulta="select * from teachers where id="+idTeacher;
        boolean actualizar = false;
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while(rs.next()){
                actualizar = true;
            }
            if(!actualizar){
                consulta="insert into teachers values("+idTeacher+","+MaxSections
                        + ","+Preps+","+MaxBxD+",'"+excludeBlocks.toString()+"','"+name+"')";
                DBConnect.own.executeUpdate(consulta);
            }else{
                //to do: UPDATE
            }
        } catch (SQLException ex) {
            Logger.getLogger(Teacher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Teacher(){
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        blocksPerDay = new int[Algoritmo.TAMY];
        for(int i = 0; i<blocksPerDay.length;i++){
            blocksPerDay[i] = 0;
        }
        secsComplete = 0;
        prepsComplete = new ArrayList<>();
        excludeBlocks = new ArrayList();
        secciones = new HashMap<>();
        numSecciones=0;
    }
    
    /**
     * Esta funcion devuelve todas las tuplas ocupadas 
     * en la cuadricula del profesor.
     * @return 
     */
    public ArrayList<Tupla<Integer,Integer>> getAllPosiciones(){
        ArrayList<Tupla<Integer,Integer>> ret= new ArrayList<>();
        for(int i = 0; i < Algoritmo.TAMX;i++)
            for(int j = 0; j < Algoritmo.TAMY;j++)
                    if(huecos[i][j] != 0)
                        ret.add(new Tupla(i,j));
        return ret;
    }
    
    /**
     * Muestra la cuadricula en la terminal
     */
    public void mostrarHuecos(){
        for(int i = 0; i < Algoritmo.TAMY;i++){
            for(int j = 0; j < Algoritmo.TAMX;j++){
                System.out.print(" "+huecos[j][i]+" ");
            }
            System.out.println("");
        }
    }
    
    /**
     * Ocupa una seccion del profesor
     * @param ar
     * @param id 
     */
    public void ocuparHueco(ArrayList<Tupla> ar,int id){
        for(Tupla t:ar){
            huecos[(Integer)t.x][(Integer)t.y]=id;
            blocksPerDay[(Integer)t.y]++;
        }
        if(!prepsComplete.contains(id/100))
            prepsComplete.add(id/100);
        secsComplete++;
        if(secsComplete >= MaxSections)
            ocupado = true;
        if(secciones.containsKey(id/100)){
            int aux = secciones.get(id/100)+1;
            secciones.replace(id/100, aux);
        }else{
            secciones.put(id/100,1);
        }
    }
    
    /**
     * Comprueba si el profesor puede cursar una nueva asignatura
     * @param id
     * @return 
     */
    public boolean asignaturaCursable(int id){
        if(this.Preps == 0 && this.MaxSections ==0)
            return true;
        else if(ocupado || (!prepsComplete.contains(id) && prepsComplete.size()>=Preps))
            return false;
        else
            return true;
    }
    
    /**
     * Comprueba si una seccion en concreto
     * es compatible con el profesor.
     * @param ar
     * @return 
     */
    public boolean patronCompatible(ArrayList<Tupla> ar){
        /*
        TO DO:
            -Revisar la parte del if que esta comentada. 
        */
        for(Tupla t:ar)
            if(huecos[(Integer)t.x][(Integer)t.y]!=0) //|| !excludeBlocks.contains(t) || (blocksPerDay[(Integer)t.y] >= MaxBxD && MaxBxD>0))
                return false;
        return true;
    }
    
    /**
     * Devuelve el numero de secciones disponibles que tiene el profesor
     * @return 
     */
    public int seccionesDisponibles(int totalBlocks){
        if(MaxSections>0)
            return MaxSections - secsComplete;
        else
            return totalBlocks-secsComplete;
    }
    
    /**
     * Devuelve el numero de asignaturas que se le 
     * pueden asignar al profesor.
     * @return 
     */
    public int prepsDisponibles(int totalBlocks){
        if(Preps>0)
            return Preps - prepsComplete.size();
        else
            return (totalBlocks/3)-prepsComplete.size();
    }
    
        
    //---------------------------------
    //-------GETTERS AND SETTERS-------
    //---------------------------------
    
    
    
    public HashMap<Integer,Integer> getSecciones() {
        return secciones;
    }

    public void setSecciones(HashMap<Integer,Integer> secciones) {
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
    
    public void addExcludeBlock(Tupla t){
        if(!excludeBlocks.contains(t))
            excludeBlocks.add(t);
    } 
    
    public String toString(){
        return idTeacher +" sections: "+ MaxSections+" preps: "+ Preps+" maxbxd: "+ MaxBxD +" exclude: "+excludeBlocks;
    }
    
    public void setExcludeBlocks(String excludeBlocks) {
        String [] s = excludeBlocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        if(!s[0].equals("")){
            for(String s2 : s){
                elem = s2.split(",");
                int row = -1;
                int col = -1;
                try{
                    row = Integer.parseInt(elem[0]);
                }catch(Exception e){}
                try{
                    col = Integer.parseInt(elem[1]);
                }catch(Exception e){}
                if(row!=-1 && col!=-1){
                    this.excludeBlocks.add(new Tupla(row,col));
                }
            }
        }
    }
    
    public int[][] getHuecos(){
        return huecos;
    }
    
    public ArrayList<Integer> getPrepsComplete() {
        return prepsComplete;
    }
    
}
