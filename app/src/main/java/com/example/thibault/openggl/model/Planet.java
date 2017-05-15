package com.example.thibault.openggl.model;

import com.example.thibault.openggl.model.exceptions.ColumnFullException;
import com.example.thibault.openggl.model.utils.Color;
import com.example.thibault.openggl.model.utils.Pair;

/**
 * Created by thibault on 27/04/17.
 */

public class Planet extends Zone{

    private static final int NUMBER_OF_BUBBLEE_PER_PLANET = 20;
    private static final int NUMBER_OF_SAME_COLOR_FOR_REDUCE = 3;
    private static final int NUMBER_REDUCE_TO_UNLOCK_POWER = 2;

    private ScoreZone scoreZone;
    private int countToUnlockPower;

    public Planet() {
        super(NUMBER_OF_BUBBLEE_PER_PLANET);
        this.scoreZone = new ScoreZone();
        this.countToUnlockPower = NUMBER_REDUCE_TO_UNLOCK_POWER;
    }

    /**
     * initialise la planet en fonction du nombre de bubblee par ligne.
     * Place sur la derniere ligne les boules noirs.
     * @param pick la pioche
     */
    @Override
    public void Initialize(Pick pick) {
        for (int i=this.nbBubblee - NUMBER_OF_BUBBLEE_PER_LINE; i<this.nbBubblee; i++){
            if ( (i+1)%2 == 0 )
                this.getBubblees()[i] = pick.pickColor(Color.BLACK);
        }
    }

    public boolean isEmpty() {
        for (Bubblee bubblee : this.bubblees){
            if (!bubblee.getColor().equals(Color.EMPTY.toString()))
                return false;
        }
        return true;
    }

    /**
     * Verifie si la colonne peut acceuillir nbBubbleeToApply en fonction de l'index du bubblee touché
     * @param indexBubbleeTouched index du bubblee touché
     * @param nbBubbleeToApply nombre de bubblé à faire tomber
     * @return vrai si on peut faire tomber nbBubbleeToApply dans la planet faux sinon
     */
    public boolean planetColumnFreeForNbBubblee(int indexBubbleeTouched, int nbBubbleeToApply){
        int i = 0;
        int count = 0;
        int col = indexBubbleeTouched%NUMBER_OF_BUBBLEE_PER_LINE;
        int nb_row = this.nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        while(i<nb_row) {
            if (this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE+col].getColor().equals(Color.EMPTY.toString()))
                count++;
            i++;
        }
        return nbBubbleeToApply <= count;
    }

    /**
     * Applique la gravité au bubblee touché
     * @param indexBubbleeTouched index du bubblee touché
     * @param fallingBubblee le bubblee a faire tomber
     * @return renvoi la ligne ou le bubblee s'est arreté
     */
    public int applyGravity(int indexBubbleeTouched, Bubblee fallingBubblee) {
        int nb_row = this.nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        int realIndexBubbleeTouched = indexBubbleeTouched%Zone.getNumberOfBubbleePerLine();
        boolean falling = true;
        int i = 0;
        while(falling && i<nb_row){
            int indexTest = realIndexBubbleeTouched + i*NUMBER_OF_BUBBLEE_PER_LINE;
            if (!this.bubblees[indexTest].getColor().equals(Color.EMPTY.toString())){
                falling = false;
            }else{
                i++;
            }
        }
        if (0 != i || falling) {
            this.bubblees[realIndexBubbleeTouched + (i - 1) * NUMBER_OF_BUBBLEE_PER_LINE] = fallingBubblee;
            return i - 1;
        }
        return -1;
    }

    /**
     * Verfie si il y a une réduction. Ligne puis colonne.
     * @return renvoi une pair contenant la ligne/colonne et la couleur à reduire
     */
    public Pair<Integer,String> reducable() {
        int nb_row = nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        Bubblee currentBubblee;
        //REDUCTION PAR LIGNE
        for (int i = 0; i < nb_row; i++){
            for (int j = 0 ; j < NUMBER_OF_BUBBLEE_PER_LINE; j++){
                currentBubblee = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE+j];
                if (!currentBubblee.getColor().equals(Color.EMPTY.toString()) &&
                        !currentBubblee.getColor().equals(Color.BLACK.toString())) {
                    int k = 0;
                    int numberSameColorFollowing = 0;
                    boolean findReduce = false;
                    Bubblee bubbleeWatch;
                    while (k < NUMBER_OF_BUBBLEE_PER_LINE) {
                        bubbleeWatch = this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + k];
                        if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()) &&
                                !bubbleeWatch.getColor().equals(Color.BLACK.toString()) && !findReduce) {
                            if (bubbleeWatch == currentBubblee) {
                                numberSameColorFollowing++;
                            } else {
                                if (bubbleeWatch.getColor().equals(currentBubblee.getColor())) {
                                    numberSameColorFollowing++;
                                    if (NUMBER_OF_SAME_COLOR_FOR_REDUCE <= numberSameColorFollowing)
                                        findReduce = true;
                                } else
                                    numberSameColorFollowing = 0;
                            }
                        }else{
                            numberSameColorFollowing = 0;
                        }
                        k++;
                    }
                    if (findReduce) {
                        Pair<Integer,String> res = new Pair<>();
                        res.setFirst(i);
                        res.setSecond(currentBubblee.getColor());
                        return res;
                    }
                }
            }
        }
        //REDUCTION PAR COLONNE
        for (int i = 0; i<NUMBER_OF_BUBBLEE_PER_LINE; i++){
            for (int j = 0; j<nb_row; j++){
                currentBubblee = this.bubblees[j*NUMBER_OF_BUBBLEE_PER_LINE+i];
                if (!currentBubblee.getColor().equals(Color.EMPTY.toString()) &&
                        !currentBubblee.getColor().equals(Color.BLACK.toString())) {
                    int k = 0;
                    int numberSameColorFollowing = 0;
                    boolean findReduce = false;
                    Bubblee bubbleeWatch;
                    while (k < nb_row) {
                        bubbleeWatch = this.bubblees[k * NUMBER_OF_BUBBLEE_PER_LINE + i];
                        if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()) &&
                                !bubbleeWatch.getColor().equals(Color.BLACK.toString()) &&!findReduce) {
                            if (bubbleeWatch == currentBubblee) {
                                numberSameColorFollowing++;
                            } else {
                                if (bubbleeWatch.getColor().equals(currentBubblee.getColor())){
                                    numberSameColorFollowing++;
                                    if (NUMBER_OF_SAME_COLOR_FOR_REDUCE <= numberSameColorFollowing)
                                        findReduce = true;
                                } else
                                    numberSameColorFollowing = 0;
                            }
                        } else {
                            numberSameColorFollowing = 0;
                        }
                        k++;
                    }
                    if (findReduce) {
                        Pair<Integer,String> res = new Pair<>();
                        res.setFirst(i*-1);
                        res.setSecond(currentBubblee.getColor());
                        return res;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Applique une réduction sur une ligne en fonction de l'index de la ligne et de sa couleur
     * @param rowIndex index de la ligne à reduire
     * @param color couleur à reduire
     */
    public void applyReduceOnRow(int rowIndex, String color) {
        Bubblee currentBubblee;
        for (int i = 0 ; i < NUMBER_OF_BUBBLEE_PER_LINE; i++) {
            currentBubblee = this.bubblees[rowIndex * NUMBER_OF_BUBBLEE_PER_LINE + i];
            if (currentBubblee.getColor().equals(color)) {
                this.scoreZone.increment(currentBubblee);
                this.bubblees[rowIndex * NUMBER_OF_BUBBLEE_PER_LINE + i] = new Bubblee();
            }
        }
    }

    /**
     * Applique une réduction sur une colonne en fonction de l'index de la colonne et de sa couleur
     * @param colIndex index de la colonne à reduire
     * @param color couleur à réduire
     */
    public void applyReduceOnColumn(int colIndex, String color) {
        int nb_row = nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        Bubblee currentBubblee;
        for (int i = 0 ; i < nb_row; i++) {
            currentBubblee = this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + colIndex];
            if (currentBubblee.getColor().equals(color)) {
                this.scoreZone.increment(currentBubblee);
                this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + colIndex] = new Bubblee();
            }
        }
    }

    /**
     * Applique une gravité sur chaque ligne au dessus de celle donné en parametre
     * @param rowIndex index de la ligne sur laquelle appliquer la gravité
     */
    public void applyGravityOnRow(int rowIndex){
        for (int i = rowIndex - 1; 0 <= i; i--) {
            for (int j = 0; j < NUMBER_OF_BUBBLEE_PER_LINE; j++) {
                this.applyGravityOnBubblee(i, j);
            }
        }
    }

    /**
     * Applique une gravité sur la colonne donné en parametre
     * @param colIndex index de la colonne sur laquelle on applique la gravité
     */
    public void applyGravityOnColumn(int colIndex){
        int nb_row = nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        for (int i = nb_row - 1; 0 <= i; i--) {
            this.applyGravityOnBubblee(i,colIndex);
        }
    }

    /**
     * Tant que le bubblee du dessous est vide on fait descendre le bubblee courant
     * @param row index de la ligne du bubblee sur lequel on applique la gravité
     * @param col index de la colonne du bubblee sur lequel on applique la gravité
     */
    public void applyGravityOnBubblee(int row, int col) {
        Bubblee currentBubblee = this.bubblees[row * NUMBER_OF_BUBBLEE_PER_LINE + col];
        if (!currentBubblee.getColor().equals(Color.EMPTY.toString())) {
            int nb_row = nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
            boolean falling = true;
            int i = row + 1;
            Bubblee bubbleeBelow;
            while (falling) {
                if (nb_row <= i) {
                    this.swipe(row * NUMBER_OF_BUBBLEE_PER_LINE + col, (i - 1) * NUMBER_OF_BUBBLEE_PER_LINE + col);
                    falling = false;
                }else{
                    bubbleeBelow = this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + col];
                    if (bubbleeBelow.getColor().equals(Color.EMPTY.toString())) {
                        i++;
                    }else{
                        this.swipe(row * NUMBER_OF_BUBBLEE_PER_LINE + col, (i - 1) * NUMBER_OF_BUBBLEE_PER_LINE + col);
                        falling = false;
                    }
                }
            }
        }
    }

    /**
     * décrémente le compteur pour débloquer un pouvoir
     */
    public void decrementCountToUnlockPower(){
        this.countToUnlockPower--;
    }

    /**
     * réinitialize le compteur pour débloquer les pouvoir
     */
    public void resetCountToUnlockPower(){
        this.countToUnlockPower = NUMBER_REDUCE_TO_UNLOCK_POWER;
    }

    /**
     * Verifie si il existe un échange entre bubblee possible
     * @return vrai si un échange est possible faux sinon
     */
    public boolean gotSwipable() {
        int nb_row = nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE;
        Bubblee currentBubblee;
        for (int i = 0; i < nb_row; i++) {
            for (int j = 0; j < NUMBER_OF_BUBBLEE_PER_LINE; j++) {
                currentBubblee = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE+j];
                if (!currentBubblee.getColor().equals(Color.EMPTY.toString())){
                    if (0 < j){
                        if (j < (NUMBER_OF_BUBBLEE_PER_LINE -1)){
                            Bubblee bubbleeWatch1 = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE + (j-1)];
                            Bubblee bubbleeWatch2 = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE + (j+1)];
                            if (!bubbleeWatch1.getColor().equals(Color.EMPTY.toString()) || !bubbleeWatch2.getColor().equals(Color.EMPTY.toString()))
                                return true;
                        }else{
                            Bubblee bubbleeWatch = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE + (j-1)];
                            if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()))
                                return true;
                        }
                    }else{
                        Bubblee bubbleeWatch = this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE + (j+1)];
                        if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()))
                            return true;
                    }
                }
            }
        }
        for (int i = 0; i<NUMBER_OF_BUBBLEE_PER_LINE; i++) {
            for (int j = 0; j < nb_row; j++) {
                currentBubblee = this.bubblees[j * NUMBER_OF_BUBBLEE_PER_LINE + i];
                if (!currentBubblee.getColor().equals(Color.EMPTY.toString())){
                    if (0 < j){
                        if (j < (nb_row - 1)){
                            Bubblee bubbleeWatch1 = this.bubblees[(j-1)*NUMBER_OF_BUBBLEE_PER_LINE + i];
                            Bubblee bubbleeWatch2 = this.bubblees[(j+1)*NUMBER_OF_BUBBLEE_PER_LINE + i];
                            if (!bubbleeWatch1.getColor().equals(Color.EMPTY.toString()) || !bubbleeWatch2.getColor().equals(Color.EMPTY.toString()))
                                return true;
                        }else{
                            Bubblee bubbleeWatch = this.bubblees[(j-1)*NUMBER_OF_BUBBLEE_PER_LINE + i];
                            if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()))
                                return true;
                        }
                    }else{
                        Bubblee bubbleeWatch = this.bubblees[(j+1)*NUMBER_OF_BUBBLEE_PER_LINE + i];
                        if (!bubbleeWatch.getColor().equals(Color.EMPTY.toString()))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean columnFull(int columnIndex) {
        return !this.bubblees[columnIndex].getColor().equals(Color.EMPTY.toString());
    }

    /**
     * Vérifie si une réduction est possible entre le ciel et la planet, en horizontal en fonction de l'index de la
     * colonne passé en paramètre, de la couleur à réduire et de du nombre restant de bubblee à trouver
     * pour détecter une réduction
     * @param column index de la colonne
     * @param colorToReduce couleur à reduire
     * @param bubbleSameColorNeeded nombre de couleur identique restante pour réduire
     * @return vrai si une réduction est possible faux sinon
     */
    public boolean isReducePossibleWithSkyHorizontal(int column, String colorToReduce, int bubbleSameColorNeeded) {
        int nbRow = NUMBER_OF_BUBBLEE_PER_PLANET/NUMBER_OF_BUBBLEE_PER_LINE;
        int i = 0;
        while (i < nbRow && 0 < bubbleSameColorNeeded){
            if (this.bubblees[i*NUMBER_OF_BUBBLEE_PER_LINE + column].getColor().equals(colorToReduce)){
                bubbleSameColorNeeded--;
            }else{
                if (!this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + column].getColor().equals(Color.EMPTY.toString())){
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    /**
     * Vérifie si une réduction est possible entre le ciel et la planet, en vertical en fonction de l'index de la
     * colonne passé en paramètre, de la couleur à réduire et de du nombre restant de bubblee à trouver
     * pour détecter une réduction
     * @param sky le ciel
     * @param indexTouchSky l'index touché dna sle ciel
     * @param nextIndex l'index du bubblee adjacent
     * @param nbBubbleeNeededToReduce le nombre de bubblee restant à trouver pour réduire
     * @return vrai si on peut réduire et faux sinon
     */
    public boolean isReducableWithSkyVertical(Zone sky, int indexTouchSky, int nextIndex, int nbBubbleeNeededToReduce) {
        int nbRow = NUMBER_OF_BUBBLEE_PER_PLANET/NUMBER_OF_BUBBLEE_PER_LINE;
        int col = indexTouchSky % NUMBER_OF_BUBBLEE_PER_LINE;
        String colorToreduce;
        int i = 0;
        if (1 < nbBubbleeNeededToReduce) {
            colorToreduce = sky.getBubblees()[nextIndex].getColor();
        } else {
            colorToreduce = sky.getBubblees()[indexTouchSky].getColor();
        }
        while (i < nbRow){
            Bubblee bubblee = this.bubblees[i * NUMBER_OF_BUBBLEE_PER_LINE + col];
            if (bubblee.getColor().equals(colorToreduce)){
                nbBubbleeNeededToReduce--;
                if (0 == nbBubbleeNeededToReduce)
                    return true;
            }else{
                if (!bubblee.getColor().equals(Color.EMPTY.toString()))
                    return false;
            }
            i++;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Planet : " +  super.toString();
    }

    public static int getNumberOfBubbleePerPlanet() {
        return NUMBER_OF_BUBBLEE_PER_PLANET;
    }

    public static int getNumberOfSameColorForReduce() {
        return NUMBER_OF_SAME_COLOR_FOR_REDUCE;
    }

    public ScoreZone getScoreZone() {
        return scoreZone;
    }

    public int getCountToUnlockPower() {
        return countToUnlockPower;
    }
}
