package com.example.thibault.openggl.model;

import android.widget.Switch;

import com.example.thibault.openggl.model.utils.Color;

/**
 * Created by thibault on 27/04/17.
 */

public class Bubblee {

    //Ce sont les messages des pouvoir de chaque bubblee en fonction de la regle du jeu
    private static final String MESSAGE_RED_POWER = "SWITCH ADJACENT BUBBLEE of your opponent";
    private static final String MESSAGE_PURPLE_POWER = "SELECT A BUBBLE TO SEND to your opponent";
    private static final String MESSAGE_BLUE_POWER = "SELECT A BUBBLE IN THE SKY for your opponent";
    private static final String MESSAGE_GREEN_POWER = "SWITCH ADJACENT BUBBLE on your planet";
    private static final String MESSAGE_YELLOW_POWER = "SELECT A BUBBLEE TO OBTAIN";

    private String color;

    public Bubblee() {
        this.color = Color.EMPTY.toString();
    }

    public Bubblee(Color color) {
        this.color = color.toString();
    }

    /**
     * Renvoi le message de la couleur selectionné par color
     * @param color couleur seclectionné
     * @return le message de la couleur selectionné
     */
    public static String getMessagePower(String color){
        if (color.equals(Color.RED.toString())){
            return MESSAGE_RED_POWER;
        }else if (color.equals(Color.GREEN.toString())){
            return MESSAGE_GREEN_POWER;
        }else if (color.equals(Color.BLUE.toString())){
            return MESSAGE_BLUE_POWER;
        }else if (color.equals(Color.PURPLE.toString())){
            return MESSAGE_PURPLE_POWER;
        }else if (color.equals(Color.YELLOW.toString())){
            return MESSAGE_YELLOW_POWER;
        }else{
            return "";
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color.toString();
    }

    public void setColor(String color) {
        this.color = color.toString();
    }

    @Override
    public String toString() {
        return color;
    }
}
