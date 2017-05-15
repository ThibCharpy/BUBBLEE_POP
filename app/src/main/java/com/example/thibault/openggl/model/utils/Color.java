package com.example.thibault.openggl.model.utils;

/**
 * Created by thibault on 27/04/17.
 */

/**
 * Classe qui contient les couleurs utilisé dans le jeu
 */
public enum Color {

    BLACK("BLACK"),RED("RED"),GREEN("GREEN"),BLUE("BLUE"),PURPLE("PURPLE"),YELLOW("YELLOW"),EMPTY("EMPTY");

    private String currentColor;

    Color(String currentColor) {
        this.currentColor = currentColor;
    }

    @Override
    public String toString() {
        return this.currentColor;
    }
}
