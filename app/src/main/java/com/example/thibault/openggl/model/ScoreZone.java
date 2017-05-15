package com.example.thibault.openggl.model;

import java.util.Stack;

/**
 * Created by thibault on 29/04/17.
 */

public class ScoreZone {

    private static final int NUMBER_OF_BUBBLEE_TO_SHOW = 6;

    private Stack<Bubblee> score;

    public ScoreZone() {
        this.score = new Stack<>();
    }

    public void increment(Bubblee bubblee){
        this.score.push(bubblee);
    }

    public void clear(){
        this.score.clear();
    }

    public int getScore(){
        return score.size();
    }

    public static int getNumberOfBubbleeToShow() {
        return NUMBER_OF_BUBBLEE_TO_SHOW;
    }

    /**
     * Retourne un tableau contenant les NUMBER_OF_BUBBLEE_TO_SHOW dernière bubblee de la zone de score
     * @return tableau des NUMBER_OF_BUBBLEE_TO_SHOW dernière bubblee gagnés
     */
    public Bubblee[] getLastBubbleesWin() {
        Stack<Bubblee> tmp = score;
        int size = tmp.size();
        if (!this.score.isEmpty()) {
            Bubblee[] bubblees = new Bubblee[NUMBER_OF_BUBBLEE_TO_SHOW];
            for (int i = 0; i < bubblees.length; i++) {
                try {
                    bubblees[i] = tmp.get(size - i -1);
                }catch (ArrayIndexOutOfBoundsException e){
                    bubblees[i] = new Bubblee();
                }
            }
            return bubblees;
        }else{
            return null;
        }
    }
}
