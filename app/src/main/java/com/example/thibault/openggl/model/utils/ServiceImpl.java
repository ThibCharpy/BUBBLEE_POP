package com.example.thibault.openggl.model.utils;

import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Pick;
import com.example.thibault.openggl.model.Planet;
import com.example.thibault.openggl.model.ScoreZone;
import com.example.thibault.openggl.model.Sky;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.exceptions.EmptyPickException;
import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.exceptions.SkyFullException;
import com.example.thibault.openggl.model.exceptions.SkyNotFullException;

/**
 * Created by thibault on 27/04/17.
 */

public class ServiceImpl implements GameManagement,SettingsManagement {

    //TODO: la documentation de chaque fonction est dans l'interface GameManagement contenant la signature de chaque fonction

    private Pick pick;
    private Zone planetTop;
    private Zone planetBot;
    private Zone sky;

    public ServiceImpl() {
        this.pick = new Pick();
        this.planetBot = new Planet();
        this.planetTop = new Planet();
        this.sky = new Sky();
    }

    @Override
    public void initializeSpace(boolean isPowerActivated){
        Zone.setIsPowerActivated(isPowerActivated);
        this.pick.Initialize();
        this.planetTop.Initialize(this.pick);
        this.planetBot.Initialize(this.pick);
        this.pick.throwBlackBubblees();
        this.sky.Initialize(this.pick);
    }

    @Override
    public String toString() {
        return "SPACE : \n" + this.planetTop.toString() + "\n" + this.sky.toString() + "\n" + this.planetBot.toString() ;
    }

    @Override
    public Pair<Integer,Integer> applyGravity(int turn, int indexTouchedSky) throws SkyNotFullException, EmptyPickException {
        boolean skyFull = this.sky.isFull();
        boolean emptyPick = this.pick.isEmpty();
        if (skyFull && !emptyPick) {
            Pair<Integer,Integer> res = new Pair<>();
            Planet currentPlanet = getCurrentPlanet(turn);
            int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
            int row = indexTouchedSky / nbBubbleePerLine;
            int col = indexTouchedSky % nbBubbleePerLine;
            int i = indexTouchedSky;
            int nbBubbleeToApply = 0;
            boolean verticalApply;
            int nextIndex;
            while(i < this.sky.getBubblees().length && 0 <= i){
                nbBubbleeToApply++;
                if (0 == turn){
                    i += nbBubbleePerLine;
                }else{
                    i -= nbBubbleePerLine;
                }
            }
            if (0 == turn){
                if (0 < row){
                    if (0 < col){
                        nextIndex = indexTouchedSky - 1;
                    }else{
                        nextIndex = indexTouchedSky + 1;
                    }
                    verticalApply = false;
                }else{
                    nextIndex = indexTouchedSky + nbBubbleePerLine;
                    verticalApply = true;
                }
            }else{
                if (0 < row){
                    nextIndex = indexTouchedSky - nbBubbleePerLine;
                    verticalApply = true;
                }else{
                    if (col < nbBubbleePerLine - 1){
                        nextIndex = indexTouchedSky + 1;
                    }else{
                        nextIndex = indexTouchedSky - 1;
                    }
                    verticalApply = false;
                }
            }
            int row1;
            int row2;


            if (currentPlanet.planetColumnFreeForNbBubblee(indexTouchedSky,nbBubbleeToApply)) {
                if (verticalApply){
                    row2 = currentPlanet.applyGravity(nextIndex,this.sky.getBubblees()[nextIndex]);
                    this.sky.getBubblees()[nextIndex] = new Bubblee();
                }else{
                    if (currentPlanet.planetColumnFreeForNbBubblee(nextIndex,nbBubbleeToApply)){
                        row2 = currentPlanet.applyGravity(nextIndex,this.sky.getBubblees()[nextIndex]);
                        this.sky.getBubblees()[nextIndex] = new Bubblee();
                    }else{
                        return null;
                    }
                }
                row1 = currentPlanet.applyGravity(indexTouchedSky,this.sky.getBubblees()[indexTouchedSky]);
                this.sky.getBubblees()[indexTouchedSky] = new Bubblee();
            }else{
                return null;
            }
            res.setFirst(row1);
            res.setSecond(row2);
            return res;
        }else{
            if (!skyFull){
                throw new SkyNotFullException();
            } else if (emptyPick){
                throw new EmptyPickException();
            }
        }
        return null;
    }

    @Override
    public Zone fillSky() throws NoMoreBubbleeForColorException, SkyFullException {
        this.sky.fill(this.pick);
        return this.sky;
    }

    @Override
    public void swipeBubbleSky(int indexBubblee1, int indexBubblee2) {
        this.sky.swipe(indexBubblee1,indexBubblee2);
    }

    @Override
    public Zone reducePlanet(int turn) {
        boolean reduced = false;
        Planet currentPlanet = getCurrentPlanet(turn);

        Pair<Integer,String> res = currentPlanet.reducable();
        while (res != null){
            reduced = true;
            int index_reduce = res.getFirst();
            String coloToReduce = res.getSecond();
            if (0 < index_reduce) { // C'est que c'est une reduction de ligne
                currentPlanet.applyReduceOnRow(index_reduce, coloToReduce);
                currentPlanet.applyGravityOnRow(index_reduce);
            } else { // c'est que c'est une reduction en colonne
                index_reduce *= -1;
                currentPlanet.applyReduceOnColumn(index_reduce, coloToReduce);
                currentPlanet.applyGravityOnColumn(index_reduce);
            }
            if (!reduced)
                reduced = true;
            currentPlanet.decrementCountToUnlockPower();
            res = currentPlanet.reducable();
        }
        if (reduced)
            return currentPlanet;
        else
            return null;
    }

    @Override
    public Zone reducePlanetWithSky2(int indexTouchSky, int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        int nbRow = currentPlanet.getBubblees().length/nbBubbleePerLine;
        int row = indexTouchSky / nbBubbleePerLine;
        int col = indexTouchSky % nbBubbleePerLine;
        int i = indexTouchSky;
        boolean verticalReduce;
        int nextIndex;
        if (0 == turn){
            if (0 < row){
                if (0 < col)
                    nextIndex = indexTouchSky - 1;
                else
                    nextIndex = indexTouchSky + 1;

                verticalReduce = false;
            }else{
                nextIndex = indexTouchSky + nbBubbleePerLine;
                verticalReduce = true;
            }
        }else{
            if (0 < row){
                nextIndex = indexTouchSky - nbBubbleePerLine;
                verticalReduce = true;
            }else{
                if (col < nbBubbleePerLine - 1)
                    nextIndex = indexTouchSky + 1;
                else
                    nextIndex = indexTouchSky - 1;

                verticalReduce = false;
            }
        }
        String colorToReduce;
        int colNext = nextIndex % nbBubbleePerLine;
        if (verticalReduce){
            int nbBubbleeToReduce = Planet.getNumberOfSameColorForReduce();
            Bubblee touchedBubblee = this.sky.getBubblees()[nextIndex];
            Bubblee nextBubblee = this.sky.getBubblees()[nextIndex];
            while(i < this.sky.getBubblees().length && 0 <= i){
                if (this.sky.getBubblees()[indexTouchSky].getColor().equals(this.sky.getBubblees()[i].getColor()))
                    nbBubbleeToReduce--;
                if (0 == turn){
                    i += nbBubbleePerLine;
                }else{
                    i -= nbBubbleePerLine;
                }
            }
            if (currentPlanet.isReducableWithSkyVertical(this.sky, indexTouchSky,nextIndex,nbBubbleeToReduce)){
                colorToReduce = this.sky.getBubblees()[nextIndex].getColor();
                i = 0;
                boolean finishedToReduce = false;
                while (i < nbRow && !finishedToReduce){
                    Bubblee currentBubblee = currentPlanet.getBubblees()[i*nbBubbleePerLine+col];
                    if (currentBubblee.getColor().equals(colorToReduce)){
                        currentPlanet.getScoreZone().increment(currentBubblee);
                        currentPlanet.getBubblees()[i*nbBubbleePerLine+col] = new Bubblee();
                    }else{
                        if (!currentBubblee.getColor().equals(Color.EMPTY.toString()))
                            finishedToReduce = true;
                    }
                    i++;
                }
                if (1 < nbBubbleeToReduce){ //cas ou les bubblee du ciel sont pas de la meme couleur
                    Bubblee bubbleeNotReduced = this.sky.getBubblees()[indexTouchSky];
                    currentPlanet.getBubblees()[col] = bubbleeNotReduced;
                    currentPlanet.applyGravityOnBubblee(0,col);
                }else{
                    currentPlanet.getScoreZone().increment(touchedBubblee);
                }
                currentPlanet.getScoreZone().increment(nextBubblee);
                this.sky.getBubblees()[indexTouchSky] = new Bubblee();
                this.sky.getBubblees()[nextIndex] = new Bubblee();
            }else{
                return null;
            }
        }else{
            // reutilisation de isReducableWithSkyVertical car double test de reduction vertical
            Bubblee touchedBubblee = this.sky.getBubblees()[nextIndex];
            Bubblee nextBubblee = this.sky.getBubblees()[nextIndex];
            if (currentPlanet.isReducableWithSkyVertical(this.sky, indexTouchSky,indexTouchSky,2)){
                if (currentPlanet.isReducableWithSkyVertical(this.sky, nextIndex,nextIndex,2)){
                    i = 0;
                    colorToReduce = nextBubblee.getColor();
                    boolean finishedToReduce = false;
                    while (i < nbRow && !finishedToReduce){
                        Bubblee currentBubblee = currentPlanet.getBubblees()[i*nbBubbleePerLine+colNext];
                        if (currentBubblee.getColor().equals(colorToReduce)){
                            currentPlanet.getScoreZone().increment(currentBubblee);
                            currentPlanet.getBubblees()[i*nbBubbleePerLine+colNext] = new Bubblee();
                        }else{
                            if (!currentBubblee.getColor().equals(Color.EMPTY.toString()))
                                finishedToReduce = true;
                        }
                        i++;
                    }
                    currentPlanet.getScoreZone().increment(nextBubblee);
                    this.sky.getBubblees()[nextIndex] = new Bubblee();
                }else{
                    if (!currentPlanet.columnFull(colNext)){
                        this.sky.getBubblees()[nextIndex] = new Bubblee();
                        currentPlanet.getBubblees()[colNext] = nextBubblee;
                        currentPlanet.applyGravityOnBubblee(0,colNext);
                    }else{
                        return null;
                    }
                }
                i = 0;
                colorToReduce = touchedBubblee.getColor();
                currentPlanet.getScoreZone().increment(touchedBubblee);
                boolean finishedToReduce = false;
                while (i < nbRow && !finishedToReduce){
                    Bubblee currentBubblee = currentPlanet.getBubblees()[i*nbBubbleePerLine+col];
                    if (currentBubblee.getColor().equals(colorToReduce)){
                        currentPlanet.getScoreZone().increment(currentBubblee);
                        currentPlanet.getBubblees()[i*nbBubbleePerLine+col] = new Bubblee();
                    }else{
                        if (!currentBubblee.getColor().equals(Color.EMPTY.toString()))
                            finishedToReduce = true;
                    }
                    i++;
                }
                currentPlanet.getScoreZone().increment(touchedBubblee);
                this.sky.getBubblees()[indexTouchSky] = new Bubblee();
            }else{
                if (currentPlanet.isReducableWithSkyVertical(this.sky, nextIndex,nextIndex,2)){
                    colorToReduce = nextBubblee.getColor();
                    boolean finishedToReduce = false;
                    i = 0;
                    while (i < nbRow && !finishedToReduce){
                        Bubblee currentBubblee = currentPlanet.getBubblees()[i*nbBubbleePerLine+colNext];
                        if (currentBubblee.getColor().equals(colorToReduce)){
                            currentPlanet.getScoreZone().increment(currentBubblee);
                            currentPlanet.getBubblees()[i*nbBubbleePerLine+colNext] = new Bubblee();
                        }else{
                            if (!currentBubblee.getColor().equals(Color.EMPTY.toString()))
                                finishedToReduce = true;
                        }
                        i++;
                    }
                    if (currentPlanet.getBubblees()[col].getColor().equals(Color.EMPTY.toString())){
                        this.sky.getBubblees()[indexTouchSky] = new Bubblee();
                        currentPlanet.getBubblees()[col] = touchedBubblee;
                        currentPlanet.applyGravityOnBubblee(0,col);
                    }else{
                        return null;
                    }
                    currentPlanet.getScoreZone().increment(nextBubblee);
                    this.sky.getBubblees()[nextIndex] = new Bubblee();
                }else{
                    return null;
                }
            }
        }
        currentPlanet.decrementCountToUnlockPower();
        return currentPlanet;
    }



    @Override
    public Bubblee[] getLastBubbleesWin(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        return currentPlanet.getScoreZone().getLastBubbleesWin();
    }

    @Override
    public int getPlanetScore(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        return currentPlanet.getScoreZone().getScore();
    }

    @Override
    public boolean testPowerUnlock(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        int res = currentPlanet.getCountToUnlockPower();
        return res <= 0;
    }

    @Override
    public String getPowerMessage(String color) {
        return Bubblee.getMessagePower(color);
    }

    @Override
    public void applyRedPower(int firstIndexTouched, int lastIndexTouched, int turn) {
        Planet opponentPlanet;
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        if (0 == turn) {
            int nbRow = Planet.getNumberOfBubbleePerPlanet()/nbBubbleePerLine;
            opponentPlanet = (Planet) this.planetTop;
            int row1 = firstIndexTouched/nbBubbleePerLine;
            int row1Inverse = (nbRow - row1 - 1);
            int row2 = lastIndexTouched/nbBubbleePerLine;
            int row2Inverse = (nbRow - row2 - 1);
            int col1 = firstIndexTouched%nbBubbleePerLine;
            int col2 = lastIndexTouched%nbBubbleePerLine;
            int index1 = row1Inverse * nbBubbleePerLine + col1;
            int index2 = row2Inverse * nbBubbleePerLine + col2;
            opponentPlanet.swipe(index1, index2);
        }else{
            opponentPlanet = (Planet) this.planetBot;
            opponentPlanet.swipe(firstIndexTouched, lastIndexTouched);
        }
    }

    @Override
    public void applyGreenPower(int firstIndexTouched, int lastIndexTouched, int turn) {
        Planet opponentPlanet;
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        if (0 == turn) {
            opponentPlanet = (Planet) this.planetBot;
            opponentPlanet.swipe(firstIndexTouched, lastIndexTouched);
        }else{
            int nbRow = Planet.getNumberOfBubbleePerPlanet()/nbBubbleePerLine;
            opponentPlanet = (Planet) this.planetTop;
            int row1 = firstIndexTouched/nbBubbleePerLine;
            int row1Inverse = (nbRow - row1 - 1);
            int row2 = lastIndexTouched/nbBubbleePerLine;
            int row2Inverse = (nbRow - row2 - 1);
            int col1 = firstIndexTouched%nbBubbleePerLine;
            int col2 = lastIndexTouched%nbBubbleePerLine;
            int index1 = row1Inverse * nbBubbleePerLine + col1;
            int index2 = row2Inverse * nbBubbleePerLine + col2;
            opponentPlanet.swipe(index1, index2);
        }
    }

    @Override
    public boolean applyBluePower(int indexTouched, int turn) {
        Planet opponentPlanet = getCurrentPlanetInverse(turn);
        if (!this.sky.getBubblees()[indexTouched].getColor().equals(Color.EMPTY.toString())){
            this.sky.sendBubblee(indexTouched,opponentPlanet);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean applyPurplePower(int indexTouched, int turn) {
        Planet currentPlanet;
        int index;
        if (0 == turn) { //bot
            index = indexTouched;
            currentPlanet = (Planet) this.planetBot;
        } else { //top
            int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
            int nbRow = Planet.getNumberOfBubbleePerPlanet()/nbBubbleePerLine;
            int row = indexTouched/nbBubbleePerLine;
            int rowInverse = (nbRow - row - 1);
            int col = indexTouched%nbBubbleePerLine;
            index = rowInverse * nbBubbleePerLine + col;
            currentPlanet = (Planet) this.planetTop;
        }
        if (!currentPlanet.getBubblees()[index].getColor().equals(Color.EMPTY.toString())) {
            Planet opponentPlanet = getCurrentPlanetInverse(turn);
            currentPlanet.sendBubblee(index, opponentPlanet);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Bubblee applyYellowPower(int indexTouched, int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        int index;
        if (0 == turn){
            index = indexTouched;
        }else{
            int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
            int nbRow = Planet.getNumberOfBubbleePerPlanet()/nbBubbleePerLine;
            int row = indexTouched/nbBubbleePerLine;
            int rowInverse = (nbRow - row - 1);
            int col = indexTouched%nbBubbleePerLine;
            index = rowInverse * nbBubbleePerLine + col;
        }
        if (!currentPlanet.getBubblees()[index].getColor().equals(Color.EMPTY.toString())) {
            Bubblee bubbleeWin = currentPlanet.getBubblees()[index];
            currentPlanet.getBubblees()[index] = new Bubblee();
            currentPlanet.getScoreZone().increment(bubbleeWin);
            return bubbleeWin;
        }else{
            return null;
        }
    }

    @Override
    public boolean isPlanetGotSwipable(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        return currentPlanet.gotSwipable();
    }

    @Override
    public boolean isPlanetFull(int turn) {
        Planet opponentPlanet = getCurrentPlanet(turn);
        if (opponentPlanet.isFull()){
            opponentPlanet.getScoreZone().clear();
            return  true;
        }
        return false;
    }

    @Override
    public boolean isPlanetColumnFull(int indexBubbleeTouched, int turn) {
        Planet opponentPlanet = getCurrentPlanet(turn);
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        int col = indexBubbleeTouched%nbBubbleePerLine;
        return opponentPlanet.columnFull(col);
    }

    @Override
    public boolean isBubbleCovered(int indexBubbleeTouched, int turn) {
        Planet currentPlanet;
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        int nbRow = Planet.getNumberOfBubbleePerPlanet()/nbBubbleePerLine;
        int row = indexBubbleeTouched/nbBubbleePerLine;
        int col = indexBubbleeTouched%nbBubbleePerLine;
        int index;
        if (0 == turn){
            index = (row-1) * nbBubbleePerLine + col;
            currentPlanet = (Planet) this.planetBot;
            Bubblee bubblee = currentPlanet.getBubblees()[index];
            if (!bubblee.getColor().equals(Color.EMPTY.toString())){
                return true;
            }
        }else{
            int rowInverse = (nbRow - row - 1);
            index = (rowInverse-1) * nbBubbleePerLine + col;
            currentPlanet = (Planet) this.planetTop;
            Bubblee bubblee = currentPlanet.getBubblees()[index];
            if (!bubblee.getColor().equals(Color.EMPTY.toString())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void resetPlanetCountToReduce(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        currentPlanet.resetCountToUnlockPower();
    }

    @Override
    public boolean isPlanetEmpty(int turn) {
        Planet currentPlanet = getCurrentPlanet(turn);
        return currentPlanet.isEmpty();
    }

    private Planet getCurrentPlanet(int turn){
        if (0 == turn) { //bot
            return  (Planet) this.planetBot;
        } else { //top
            return  (Planet) this.planetTop;
        }
    }

    private Planet getCurrentPlanetInverse(int turn){
        if (0 == turn) { //bot
            return  (Planet) this.planetTop;
        } else { //top
            return  (Planet) this.planetBot;
        }
    }

    public int getNumberOfBubbleePerLine(){
        return Zone.getNumberOfBubbleePerLine();
    }

    public int getNumberOfBubbleeInSky(){
        return Sky.getNumberOfBubbleeInSky();
    }

    public int getNumberOfBubbleeInPlanet(){
        return Planet.getNumberOfBubbleePerPlanet();
    }

    public int getNumberOfBubbleeInScoreZone() {
        return ScoreZone.getNumberOfBubbleeToShow();
    }

    public Zone[] getSpace(){
        return new Zone[]{this.planetTop,this.sky,this.planetBot};
    }

    @Override
    public boolean isPowerActivated() {
        return Zone.isPowerActivated();
    }
}
