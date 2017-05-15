package com.example.thibault.openggl.model.utils;

import java.io.Serializable;

/**
 * Created by thibault on 07/01/17.
 */

/**
 * Classe générique d'une paire d'element
 * @param <E> element droite de la pair de type E
 * @param <F> element fauche de la pair de type F
 */
public class Pair<E,F> implements Serializable{
    private E first;
    private F second;

    public Pair() {
    }

    public Pair(E first, F second){
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public F getSecond() {
        return second;
    }

    public void setSecond(F second) {
        this.second = second;
    }
}
