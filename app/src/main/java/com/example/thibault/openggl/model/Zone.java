package com.example.thibault.openggl.model;

import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.exceptions.SkyFullException;
import com.example.thibault.openggl.model.utils.Color;

/**
 * Created by thibault on 27/04/17.
 */

public abstract class Zone {
    protected Bubblee[] bubblees;
    protected int nbBubblee;
    protected static final int NUMBER_OF_BUBBLEE_PER_LINE = 5;

    private static boolean IS_POWER_ACTIVATED = false;

    public abstract void Initialize(Pick pick);

    public Zone(int size) {
        this.bubblees = new Bubblee[size];
        this.nbBubblee = size;

        for (int i=0; i<this.nbBubblee; i++){
            this.bubblees[i] = new Bubblee(Color.EMPTY);
        }
    }

    public boolean isFull() {
        for (Bubblee b : this.bubblees) {
            if (b.getColor().equals(Color.EMPTY.toString()))
                return false;
        }
        return true;
    }


    public Bubblee[] getBubblees() {
        return bubblees;
    }

    public static int getNumberOfBubbleePerLine() {
        return NUMBER_OF_BUBBLEE_PER_LINE;
    }

    /**
     * rempli le ciel
     * @param pick la pioche
     * @throws NoMoreBubbleeForColorException si la pioche est vide
     * @throws SkyFullException si le ciel est plein
     */
    public void fill(Pick pick) throws NoMoreBubbleeForColorException, SkyFullException {
        if (this instanceof Sky){
            boolean full = true;
            for (int i = 0; i < this.bubblees.length; i++){
                if (this.bubblees[i].getColor().equals(Color.EMPTY.toString())){
                    this.bubblees[i] = pick.onPick();
                    full = false;
                }
            }
            if (full)
                throw new SkyFullException();
        }
    }

    /**
     * échange deux bubblee en fonction de leurs indexs
     * @param indexBubblee1 index du premier bubblee
     * @param indexBubblee2 index du second bubblee
     */
    public void swipe(int indexBubblee1, int indexBubblee2) {
        Bubblee tmp = this.bubblees[indexBubblee2];
        this.bubblees[indexBubblee2] = this.bubblees[indexBubblee1];
        this.bubblees[indexBubblee1] = tmp;
    }

    /**
     * envoi un bubblee sur la planet opposé à la planete courante en fonction de l'index du bubblee
     * touché.
     * @param indexTouched index du bubblee à envoyer
     * @param opponentPlanet planet opposé
     */
    public void sendBubblee(int indexTouched, Planet opponentPlanet) {
        Bubblee bubbleeToSend = this.bubblees[indexTouched];
        this.bubblees[indexTouched] = new Bubblee();
        int col = indexTouched%NUMBER_OF_BUBBLEE_PER_LINE;
        opponentPlanet.getBubblees()[col] = bubbleeToSend;
        opponentPlanet.applyGravityOnBubblee(0,col);
    }

    @Override
    public String toString() {
        String s = "[";
        for (int i = 0; i< this.nbBubblee; i++){
            s += this.bubblees[i];
            if (i < this.nbBubblee -1)
                s +=  ",";
        }
        s += "]";
        return s;
    }

    public static boolean isPowerActivated() {
        return IS_POWER_ACTIVATED;
    }

    public static void setIsPowerActivated(boolean isPowerActivated) {
        IS_POWER_ACTIVATED = isPowerActivated;
    }
}
