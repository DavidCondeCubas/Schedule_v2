/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

import java.util.ArrayList;

/**
 *
 * @author Chema
 */
public class Conjuntos <T>{ // CLASE ENCARGADA DE COMPARAR ARRAYLIST (UNION, INTERSECCION, COMPARACION)

    public ArrayList<T> union(ArrayList<T> primero, ArrayList<T> segundo) {
        ArrayList<T> retVal = new ArrayList<T>(primero);
        for (T worte : segundo) {
            if (!primero.contains(worte)) {
                retVal.add(worte);
            }
        }
        return retVal;
    }

    public ArrayList<T> interseccion(ArrayList<T> a, ArrayList<T> b) {
        ArrayList<T> c = new ArrayList<T>();
        ArrayList<T> iter = a.size() > b.size() ? a : b;
        for (T elem : iter) {
            if (a.contains(elem) && b.contains(elem)) {
                c.add(elem);
            }
        }
        return c;
    }

    public ArrayList<T> diferencia(ArrayList<T> a, ArrayList<T> b) {
        ArrayList<T> c = new ArrayList<T>();
        for (T elem : a) {
            if (!b.contains(elem)) {
                c.add(elem);
            }
        }
        return c;
    }
}
