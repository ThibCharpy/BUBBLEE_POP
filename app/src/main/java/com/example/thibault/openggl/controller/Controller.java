package com.example.thibault.openggl.controller;

import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.exceptions.ColumnFullException;
import com.example.thibault.openggl.model.exceptions.EmptyPickException;
import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.exceptions.SkyFullException;
import com.example.thibault.openggl.model.exceptions.SkyNotFullException;
import com.example.thibault.openggl.model.utils.Pair;
import com.example.thibault.openggl.model.utils.ServiceImpl;

import java.util.Random;

/**
 * Created by thibault on 27/04/17.
 */

public class Controller {

    /**
     * Le controlleur a pour but de transmettre les messages entre la vue et le model et d'eviter les
     * communication directe entre la vue et le model de tel sorte que l'on puisse changer la vue et
     * réutiliser le model avec cette nouvelle.
     *
     * Dans ce controlleur on gère si la partie est fini et le tour courant.
     */

    private ServiceImpl myFront;
    private int turn;
    private boolean isPowerActivated;
    private boolean end;
    private int winner;

    public Controller(){
        this.myFront = new ServiceImpl();
        Random r = new Random();
        this.isPowerActivated = this.myFront.isPowerActivated();
        this.end = false;
        this.winner = -1;
        //random int entre 1 et 0
        // 1 == planet top
        // 0 == planet bot
        this.turn = r.nextInt(2);
    }

    //Chaque fonction est un appel au model auquel on ajoute le tour courant

    public Zone[] InitializeGame(){
        this.myFront.initializeSpace(this.isPowerActivated);
        System.out.println(this.myFront.toString());
        return this.myFront.getSpace();
    }

    public int[] getGameParameters(){
        return new int[]{this.myFront.getNumberOfBubbleePerLine(),
                this.myFront.getNumberOfBubbleeInSky(),
                this.myFront.getNumberOfBubbleeInPlanet(),
                this.myFront.getNumberOfBubbleeInScoreZone()};
    }

    public void switchTurn(){
        if (1 == turn)
            this.turn = 0;
        else
            this.turn = 1;
    }

    public Pair<Integer,Integer> applyGravity(int indexBubbleeTouched) throws SkyNotFullException, EmptyPickException {
        return this.myFront.applyGravity(this.turn,indexBubbleeTouched);
    }

    public int getTurn() {
        return turn;
    }

    public boolean isPowerActivated() {
        return isPowerActivated;
    }

    public void setPowerActivated(boolean powerActivated) {
        isPowerActivated = powerActivated;
    }

    public Zone fillSky() throws NoMoreBubbleeForColorException, SkyFullException {
        return this.myFront.fillSky();
    }

    public void swipeBubbleeSky(int indexBubblee1, int indexBubblee2) {
        this.myFront.swipeBubbleSky(indexBubblee1,indexBubblee2);
    }

    public Zone reducePlanet() {
        return this.myFront.reducePlanet(this.turn);
    }

    public Zone reduceOpponentPlanet() {
        if (0 == this.turn)
            return this.myFront.reducePlanet(1);
        else
            return this.myFront.reducePlanet(0);
    }

    public Zone reducePlanetWithSky2(int indexTouchSky){
        return this.myFront.reducePlanetWithSky2(indexTouchSky,this.turn);
    }

    public Bubblee[]getLastBubbleesWin(int turn) {
        return this.myFront.getLastBubbleesWin(turn);
    }
    public Bubblee[]getLastBubbleesWinByOpponent(int turn) {
        if (0 == this.turn)
            return this.myFront.getLastBubbleesWin(1);
        else
            return this.myFront.getLastBubbleesWin(0);
    }

    public boolean testUnlockPower() {
        return this.myFront.testPowerUnlock(this.turn);
    }

    public String getBubbleePowerMessage(String color) {
        return this.myFront.getPowerMessage(color);
    }

    public void applyRedPower(int firstIndexTouched, int lastTouched) {
        this.myFront.applyRedPower(firstIndexTouched,lastTouched,this.turn);
    }

    public void applyGreenPower(int firstIndexTouched, int lastTouched) {
        this.myFront.applyGreenPower(firstIndexTouched,lastTouched,this.turn);
    }

    public boolean applyPurplePower(int indexTouched) {
        return this.myFront.applyPurplePower(indexTouched,this.turn);
    }

    public boolean applyBluePower(int indexTouchedSky) {
        return this.myFront.applyBluePower(indexTouchedSky, this.turn);
    }

    public Bubblee applyYellowPower(int indexTouchedSky) {
        return this.myFront.applyYellowPower(indexTouchedSky, this.turn);
    }

    public boolean isPlanetGotBubbleeSwipable() {
        return this.myFront.isPlanetGotSwipable(0) || this.myFront.isPlanetGotSwipable(1);
    }

    public boolean isPlanetFull() {
        return this.myFront.isPlanetFull(this.turn);
    }

    public boolean OpponentPlanetFull() {
        if (0 == this.turn)
            return this.myFront.isPlanetFull(1);
        else
            return this.myFront.isPlanetFull(0);
    }

    public boolean isPlanetColumnFull(int indexBubbleeTouched) {
        return this.myFront.isPlanetColumnFull(indexBubbleeTouched,this.turn);
    }

    public boolean OpponentColumnFull(int indexBubbleeTouched) {
        if (0 == this.turn)
            return this.myFront.isPlanetColumnFull(indexBubbleeTouched,1);
        else
            return this.myFront.isPlanetColumnFull(indexBubbleeTouched,0);
    }

    public boolean isBubbleCovered(int indexBubbleeTouched) {
        return this.myFront.isBubbleCovered(indexBubbleeTouched,this.turn);
    }

    public boolean isPlanetEmpty() {
        return this.myFront.isPlanetEmpty(this.turn);
    }

    public void gameIsFinished(){
        this.end = true;
        int res0 = this.myFront.getPlanetScore(0);
        int res1 = this.myFront.getPlanetScore(1);
        if (res0 < res1)
            this.winner = 1;
        else
            this.winner = 0;
    }

    public boolean isEnd() {
        return end;
    }

    public int getWinner() {
        return winner;
    }

    public void resetPlanetCountToReduce() {
        this.myFront.resetPlanetCountToReduce(this.turn);
    }
}
