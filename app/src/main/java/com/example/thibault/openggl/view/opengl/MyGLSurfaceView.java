/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.thibault.openggl.view.opengl;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thibault.openggl.controller.Controller;
import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.exceptions.EmptyPickException;
import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.exceptions.SkyFullException;
import com.example.thibault.openggl.model.exceptions.SkyNotFullException;
import com.example.thibault.openggl.model.utils.Pair;

public class MyGLSurfaceView extends GLSurfaceView {


    private Activity activity;

    private final MyGLRenderer mRenderer;

    /* pour gérer la translation */
    private float mPreviousX;
    private float mPreviousY;

    // variales utilisés dans les conditions
    private int mPreviousIndexTouched;
    private boolean condition = false;
    private boolean power_unlock;
    private int currentPowerColor;

    // label des différents swipe effectué
    private static final String LABEL_SWIPE_LEFT = "LEFT";
    private static final String LABEL_SWIPE_RIGHT = "RIGHT";
    private static final String LABEL_SWIPE_TOP = "TOP";
    private static final String LABEL_SWIPE_BOTTOM = "BOT";
    private static final String LABEL_SWIPE_NULL = "NULL";

    public MyGLSurfaceView(Context context, Activity activity, boolean powerActivated) {
        super(context);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(3);

        // Création du renderer qui va être lié au conteneur View créé
        mRenderer = new MyGLRenderer(powerActivated);
        this.activity = activity;

        //intialisations des variables utilisés dans les ifs
        this.mPreviousX = 0.0f;
        this.mPreviousY = 0.0f;
        this.mPreviousIndexTouched = -1;
        this.power_unlock = false;

        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * Renvoi l'index du bubblee touché en fonction des coordonnées x et y touché, la zone courante
     * et l'espacement entre les bubbleeShape
     * @param positionBubbleeZone tableau de position des bubbleeShapes de la zone courante
     * @param x position en abscisse touché par le joueur
     * @param y position en ordonnées touché par le joueur
     * @param bubbleeSpacing espacement entre les bubleeShapes
     * @return index du bublle touché
     */
    public int testTouchZone(float[][] positionBubbleeZone, float x, float y, float bubbleeSpacing){

        float bubbleeRadius = MyGLRenderer.getBubbleeshapeRadius();
        int limitBubbleePerLine = MyGLRenderer.getLimitBubbleePerLine();

        for (int i = 0; i < positionBubbleeZone.length; i++){
            if ((x < positionBubbleeZone[i][0] + ((i % limitBubbleePerLine) * bubbleeSpacing) + bubbleeRadius) &&
                    (x > positionBubbleeZone[i][0] + ((i % limitBubbleePerLine) * bubbleeSpacing) - bubbleeRadius) &&
                    (y < positionBubbleeZone[i][1] - ((i / limitBubbleePerLine) * bubbleeSpacing) + bubbleeRadius) &&
                    (y > positionBubbleeZone[i][1] - ((i / limitBubbleePerLine) * bubbleeSpacing) - bubbleeRadius))
                return i;
        }
        return -1;
    }

    /**
     * Renvoi l'index du bubblee touché en fonction des coordonnées x et y touché, la scoreZone courante
     * @param positionBubbleeZone tableau de position des bubbleeShapes de la scoreZone courante
     * @param x position en abscisse touché par le joueur
     * @param y position en ordonnées touché par le joueur
     * @return index du bublle touché
     */
    public int testTouchScoreZone(float[][] positionBubbleeZone, float x, float y){

        float bubbleeRadius = MyGLRenderer.getBubbleeshapeRadius();
        float bubbleeSpacingVertical = MyGLRenderer.getBubbleshapeSpacingScoreZone();

        for (int i = 0; i < positionBubbleeZone.length; i++){
            if ((x < positionBubbleeZone[i][0] + bubbleeRadius) &&
                    (x > positionBubbleeZone[i][0] - bubbleeRadius) &&
                    (y < positionBubbleeZone[i][1] - (i * bubbleeSpacingVertical) + bubbleeRadius) &&
                    (y > positionBubbleeZone[i][1] - (i * bubbleeSpacingVertical) - bubbleeRadius))
                return i;
        }
        return -1;
    }

    /* Comment interpréter les événements sur l'écran tactile */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Les coordonnées du point touché sur l'écran
        float x = e.getX();
        float y = e.getY();

        Controller controller = this.mRenderer.getController();

        float bubbleeRadius = MyGLRenderer.getBubbleeshapeRadius();
        float[][] positionBubbleePlanetTop = mRenderer.getPlanetTopBubbleeShapePositionTab();
        float[][] positionBubbleePlanetBot = mRenderer.getPlanetBotBubbleeShapePositionTab();
        float[][] positionBubbleeSky = mRenderer.getSkyBubbleeShapePositionTab();
        float[][] positionBubbleeScoreZoneTop = mRenderer.getScoreZoneTopBubbleeShapePositionTab();
        float[][] positionBubbleeScoreZoneBot = mRenderer.getScoreZoneBotBubbleeShapePositionTab();
        float[] positionPick = mRenderer.getPickBubbleeShapePosition();

        /* Conversion des coordonnées pixel en coordonnées OpenGL
        Attention l'axe x est inversé par rapport à OpenGLSL
        On suppose que l'écran correspond à un carré d'arete 2 centré en 0
         */

        float x_opengl = (MyGLRenderer.getOrthomRight()*2)*x/getWidth() + MyGLRenderer.getOrthomLeft();
        float y_opengl = (MyGLRenderer.getOrthomBottom()*2)*y/getHeight() + MyGLRenderer.getOrthomTop();

        /* Le carré représenté a une arete de 2 (oui il va falloir changer cette valeur en dur !!)
        /* On teste si le point touché appartient au carré ou pas car on ne doit le déplacer que si ce point est dans le carré
        */

        // demande au model du tour courant
        int turn = controller.getTurn();

        boolean testTouchPick = (((x_opengl < positionPick[0]+bubbleeRadius) &&  (x_opengl > positionPick[0]-bubbleeRadius)) &&
                ((y_opengl < positionPick[1]+bubbleeRadius) &&  (y_opengl > positionPick[1]-bubbleeRadius)));

        // les résulats des test en fonction de l'endroit touché par le joueur sur l'écran
        int testTouchSky = testTouchZone(positionBubbleeSky,x_opengl,y_opengl,MyGLRenderer.getBubbleshapeSpacingSky());
        int testTouchPlanetTop = testTouchZone(positionBubbleePlanetTop,x_opengl,y_opengl,MyGLRenderer.getBubbleshapeSpacingPlanet());
        int testTouchPlanetBot = testTouchZone(positionBubbleePlanetBot,x_opengl,y_opengl,MyGLRenderer.getBubbleshapeSpacingPlanet());

        int testTouchScoreZone;
        if (0 == turn)
            testTouchScoreZone = testTouchScoreZone(positionBubbleeScoreZoneBot,x_opengl,y_opengl);
        else
            testTouchScoreZone = testTouchScoreZone(positionBubbleeScoreZoneTop,x_opengl,y_opengl);

        boolean isFinished = controller.isEnd();

        // si j'ai touché quelque part et que le jeu n'est pas fini
        if ((condition || testTouchPick || 0 <= testTouchSky || 0 <= testTouchPlanetTop || 0 <= testTouchPlanetBot || 0 <= testTouchScoreZone) && !isFinished) {
            switch (e.getAction()) {
                /* Lorsqu'on touche l'écran on mémorise juste le point */
                case MotionEvent.ACTION_DOWN:
                    /*
                       les variables avec le mot clé "Previous" sont utiisé pour les cas de swipe
                       avec les pouvoirs de couleur bleu, rouge, vert et également pour les swipe
                       effectué dans le ciel
                    */
                    if (0.0f == this.mPreviousX && 0.0f == this.mPreviousY) {
                        this.mPreviousX = x_opengl;
                        this.mPreviousY = y_opengl;
                    }
                    if (0 <= testTouchSky) {
                        if (this.mPreviousIndexTouched < 0)
                            this.mPreviousIndexTouched = testTouchSky;
                    }else{
                        if (0 <= testTouchPlanetTop || 0 <= testTouchPlanetBot){
                            if (this.power_unlock &&
                                    (this.currentPowerColor == MyGLRenderer.getColorRedNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorGreenNumber())){
                                if (this.mPreviousIndexTouched < 0){
                                    if (0 <= testTouchPlanetTop){
                                        this.mPreviousIndexTouched = testTouchPlanetTop;
                                    }else{
                                        this.mPreviousIndexTouched = testTouchPlanetBot;
                                    }
                                }
                            }
                        }
                    }
                    this.condition=true;
                    Log.d("EVENT", "TURN="+controller.getTurn());
                    break;
                case MotionEvent.ACTION_UP:
                    if (testTouchPick) {
                        //Si je touche la pioche je rempli le ciel dans le model et je met à jour la vue
                        Log.d("EVENT", "PICK");
                        if (!this.power_unlock) {
                            try {
                                Zone sky = controller.fillSky();
                                mRenderer.fillEmptyBubbleeShapeSky(sky);
                                mRenderer.disablePick();
                                mRenderer.setBackgroundByTurn(turn);
                            } catch (NoMoreBubbleeForColorException e1) {
                                makeToast("Pick is EMPTY !",Toast.LENGTH_SHORT,turn);
                                controller.gameIsFinished();
                            } catch (SkyFullException e1) {
                                makeToast("Sky FULL",Toast.LENGTH_SHORT,turn);
                            }
                        }
                    } else if (0 <= testTouchSky) {
                        /*
                        Si je touche le ciel, je regarde si un swipe a été effectué
                        Si oui je le fait dns le model et je met à jour la vue
                        Sinon j'applique la gravité aux deux bubblees et je met à jour la vue.

                        J'effectue les réductions necessaire si la colonne que je vise n'est pas pleine
                        Dans le cas ou elle est pleine je test si une réduction avec le ciel est possible
                        et dans ce cas j'effectue cette réduction dans le model et je met à jour la vue.

                        Je verifie si un pouvoir a été débloqué dans le cas ou les pouvoir sont
                        activé, si oui je passe la variable powerApplied à vrai.

                        A la fin je passe le tour courant et j'active la pioche.
                         */
                        String testSwipe = testSwipeDirection(this.mPreviousIndexTouched, testTouchSky);
                        if (!testSwipe.equals(LABEL_SWIPE_NULL)){
                            Log.d("EVENT", "SWIPE " + testSwipe);
                            controller.swipeBubbleeSky(this.mPreviousIndexTouched, testTouchSky);
                            mRenderer.swipeSkyBubbleeShapeTab(this.mPreviousIndexTouched, testTouchSky);
                        }else {
                            if (this.mPreviousIndexTouched == testTouchSky) {
                                Log.d("EVENT", "TOUCH SKY TOP AT " + (testTouchSky + 1));
                                boolean played = false;
                                boolean powerApplied = false;
                                if (!this.power_unlock) {
                                    try {
                                        boolean errorApplyGravity = false;
                                        if (!controller.isPlanetFull()){
                                            if (!controller.isPlanetColumnFull(testTouchSky)){
                                                Pair<Integer,Integer> res = controller.applyGravity(testTouchSky);
                                                if (null != res){
                                                    mRenderer.updateBubbleeShapeColorGravity(testTouchSky,res);
                                                    played = true;
                                                }else{
                                                    errorApplyGravity = true;
                                                }
                                            }else{
                                                errorApplyGravity = true;
                                            }
                                        }else{
                                            makeToast( "Planet FULL !", Toast.LENGTH_SHORT, turn);
                                            controller.gameIsFinished();
                                        }
                                        if (errorApplyGravity){
                                            Zone planet = controller.reducePlanetWithSky2(testTouchSky);
                                            if (null != planet) {
                                                mRenderer.applyReduceToView(testTouchSky, turn, planet);
                                                played = true;
                                            } else {
                                                makeToast("Column FULL !", Toast.LENGTH_SHORT,turn);
                                            }
                                        }
                                    } catch (SkyNotFullException e1) {
                                        makeToast("Sky is NOT FULL !", Toast.LENGTH_SHORT,turn);
                                    } catch (EmptyPickException e1) {
                                        makeToast("Pick is EMPTY !", Toast.LENGTH_SHORT,turn);
                                        controller.gameIsFinished();
                                    }
                                    if (played) {
                                        Zone planet = controller.reducePlanet();
                                        if (null != planet)
                                            mRenderer.updatePlanet(planet, turn);
                                        Bubblee[] scoreZoneBubblees = controller.getLastBubbleesWin(turn);
                                        if (null != scoreZoneBubblees)
                                            mRenderer.updateScoreZone(scoreZoneBubblees, turn);
                                        if (controller.isPowerActivated()) {
                                            if (controller.testUnlockPower()) {
                                                this.power_unlock = true;
                                                makeToast("Active your POWER !",Toast.LENGTH_SHORT,turn);
                                            }else{
                                                this.power_unlock = false;
                                            }
                                            if (!this.power_unlock) {
                                                this.currentPowerColor = -1;
                                                controller.switchTurn();
                                                mRenderer.activatePick();
                                            }
                                        }else{
                                            controller.switchTurn();
                                            mRenderer.activatePick();
                                        }
                                    }
                                }else{
                                    if (this.currentPowerColor == MyGLRenderer.getColorBlueNumber()){
                                        if (!controller.OpponentPlanetFull()) {
                                            if (!controller.OpponentColumnFull(testTouchSky)) {
                                                if (controller.applyBluePower(testTouchSky)) {
                                                    mRenderer.applyBluePowerToView(testTouchSky, turn);
                                                    powerApplied = true;
                                                }else{
                                                    makeToast( "BLUE POWER : NOT a Bubblee !",Toast.LENGTH_SHORT,turn);
                                                    makeToast("BLUE POWER : Select another bubblee !",Toast.LENGTH_SHORT,turn);
                                                }
                                            }else{
                                                makeToast("BLUE POWER : Column FULL !",Toast.LENGTH_SHORT,turn);
                                            }
                                        }else{
                                            makeToast("BLUE POWER : Planet FULL !",Toast.LENGTH_SHORT,turn);
                                            controller.gameIsFinished();
                                        }
                                    }else{
                                        makeToast( "You must touch PLANETs !",Toast.LENGTH_SHORT,turn);
                                    }
                                }
                                if (powerApplied){
                                    controller.resetPlanetCountToReduce();
                                    controller.switchTurn();
                                    mRenderer.activatePick();
                                    this.power_unlock = false;
                                    this.currentPowerColor = -1;
                                }
                            }else{
                                makeToast( "Can't exchange with this Bubblee !",Toast.LENGTH_SHORT,turn);
                            }
                        }

                    } else if (0 <= testTouchPlanetTop || 0 <= testTouchPlanetBot) {
                        Log.d("EVENT", "TOUCH PLANET AT " + (testTouchPlanetTop+1));
                        if (this.power_unlock){
                            /*
                            Lorsqu'un pourvoir est débloqué je verifie que le joueur touche bien
                            le bubblee qui correspond au pouvoir obtenue
                            */
                            if (this.currentPowerColor != MyGLRenderer.getColorBlueNumber()){
                                boolean canApplyPower = false;
                                boolean powerApplied = false;
                                int indexTouched = -1;
                                if (0 <= testTouchPlanetTop ){ // touché la planet du haut
                                    if (this.currentPowerColor == MyGLRenderer.getColorRedNumber()){
                                        if (0 == turn){
                                            canApplyPower = true;
                                        }else{
                                            makeToast("You must touch YOUR OPPPONENT PLANET !",Toast.LENGTH_SHORT,turn);
                                        }
                                    } else if (this.currentPowerColor == MyGLRenderer.getColorPurpleNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorGreenNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorYellowNumber()){
                                        if (0 == turn){
                                            makeToast("You must touch YOUR PLANET !",Toast.LENGTH_SHORT,turn);
                                        }else{
                                            canApplyPower = true;
                                        }
                                    }
                                    if (canApplyPower){
                                        Log.d("EVENT", "POWER CAN BE APPLIED !");
                                        indexTouched = testTouchPlanetTop;
                                    }
                                }else{ // touché la planète du bas
                                    if (this.currentPowerColor == MyGLRenderer.getColorRedNumber()){
                                        if (0 == turn){
                                            makeToast("You must touch YOUR OPPPONENT PLANET !",Toast.LENGTH_SHORT,turn);
                                        }else{
                                            canApplyPower = true;
                                        }
                                    }else if (this.currentPowerColor == MyGLRenderer.getColorPurpleNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorGreenNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorYellowNumber()){
                                        if (0 == turn) {
                                            canApplyPower = true;
                                        }else {
                                            makeToast("You must touch YOUR PLANET !", Toast.LENGTH_SHORT,turn);
                                        }
                                    }
                                    if (canApplyPower){
                                        Log.d("EVENT", "POWER CAN BE APPLIED !");
                                        indexTouched = testTouchPlanetBot;
                                    }
                                }
                                if (canApplyPower){
                                    if (this.currentPowerColor == MyGLRenderer.getColorRedNumber() ||
                                            this.currentPowerColor == MyGLRenderer.getColorGreenNumber()){
                                        String testSwipe = testSwipeDirection(this.mPreviousIndexTouched, indexTouched);
                                        if (!testSwipe.equals(LABEL_SWIPE_NULL)) {
                                            Log.d("EVENT", "SWIPE " + testSwipe);
                                            if (controller.isPlanetGotBubbleeSwipable()) {
                                                if (this.currentPowerColor == MyGLRenderer.getColorRedNumber()) {
                                                    //execution dans le model du pouvoir rouge et mise à jour de la vue
                                                    Log.d("EVENT", "RED SWIPE i1=" + this.mPreviousIndexTouched + "     i2=" + indexTouched);
                                                    controller.applyRedPower(this.mPreviousIndexTouched, indexTouched);
                                                    mRenderer.applyRedPowerToView(this.mPreviousIndexTouched, indexTouched, turn);
                                                } else {
                                                    //execution dans le model du pouvoir vert et mise à jour de la vue
                                                    Log.d("EVENT", "GREEN SWIPE i1=" + this.mPreviousIndexTouched + "     i2=" + indexTouched);
                                                    controller.applyGreenPower(this.mPreviousIndexTouched, indexTouched);
                                                    mRenderer.applyGreenPowerToView(this.mPreviousIndexTouched, indexTouched, turn);
                                                }
                                                powerApplied = true;
                                            } else {
                                                makeToast("RED/GREEN: No SWIPABLE bubblee !", Toast.LENGTH_SHORT,turn);
                                                makeToast("RED/GREEN: Select a new POWER !", Toast.LENGTH_SHORT,turn);
                                                this.currentPowerColor = -1;
                                            }
                                        }
                                    }else{
                                        if (this.currentPowerColor == MyGLRenderer.getColorPurpleNumber()){
                                            if (!controller.isPlanetEmpty()) {
                                                if (!controller.OpponentPlanetFull()) {
                                                    if (!controller.OpponentColumnFull(indexTouched)) {
                                                        if (!controller.isBubbleCovered(indexTouched)) {
                                                            if (controller.applyPurplePower(indexTouched)) {
                                                                //execution du pouvoir violet et mise à jour de la vue
                                                                mRenderer.applyPurplePowerToView(indexTouched, turn);
                                                                powerApplied = true;
                                                            } else {
                                                                makeToast("PURPLE POWER : NOT a Bubblee !", Toast.LENGTH_SHORT,turn);
                                                                makeToast("PURPLE POWER : Select another bubblee !", Toast.LENGTH_SHORT,turn);
                                                            }
                                                        } else {
                                                            makeToast("PURPLE POWER : Bubblee Covered !", Toast.LENGTH_SHORT,turn);
                                                        }
                                                    } else {
                                                        makeToast("PURPLE POWER : Column FULL !", Toast.LENGTH_SHORT,turn);
                                                    }
                                                } else {
                                                    makeToast("PURPLE POWER : Planet FULL !", Toast.LENGTH_SHORT,turn);
                                                    controller.gameIsFinished();
                                                }
                                            }else{
                                                makeToast("PURPLE POWER : Planet EMPTY !", Toast.LENGTH_SHORT,turn);
                                                makeToast("PURPLE POWER : Select POWER !", Toast.LENGTH_SHORT,turn);
                                            }
                                        }else{
                                            if (!controller.isBubbleCovered(indexTouched)){
                                                Bubblee bubbleeWin = controller.applyYellowPower(indexTouched);
                                                if (null != bubbleeWin){
                                                    //execution dans le model du pouvoir jaune et mise à jour de la vue
                                                    mRenderer.applyYellowPowerToView(indexTouched,turn);
                                                    mRenderer.updateScoreZone(new Bubblee[]{bubbleeWin},turn);
                                                    powerApplied = true;
                                                }else{
                                                    makeToast("YELLOW POWER : NOT a bubble !",Toast.LENGTH_SHORT,turn);
                                                }
                                            }else{
                                                makeToast("YELLOW POWER : Bubblee Covered !",Toast.LENGTH_SHORT,turn);
                                            }
                                        }
                                    }
                                }
                                if (powerApplied){
                                    //Si un pouvoir a été appliqué je verifie les réductions possibles
                                    controller.resetPlanetCountToReduce();
                                    if (this.currentPowerColor != MyGLRenderer.getColorYellowNumber()){
                                        Zone planet;
                                        Bubblee[] scoreZoneBubblees;
                                        int currentTurn = turn;
                                        if (this.currentPowerColor != MyGLRenderer.getColorGreenNumber()){
                                            planet = controller.reduceOpponentPlanet();
                                            scoreZoneBubblees = controller.getLastBubbleesWinByOpponent(turn);
                                            if (0 == turn)
                                                currentTurn = 1;
                                            else
                                                currentTurn = 0;
                                        }else{
                                            planet = controller.reducePlanet();
                                            scoreZoneBubblees = controller.getLastBubbleesWin(turn);
                                        }
                                        if (null != planet) {
                                            mRenderer.updatePlanet(planet, currentTurn);
                                        }
                                        if (null != scoreZoneBubblees)
                                            mRenderer.updateScoreZone(scoreZoneBubblees, currentTurn);
                                    }
                                    if (controller.testUnlockPower()) {
                                        this.power_unlock = true;
                                        makeToast( "Active your POWER !",Toast.LENGTH_SHORT,turn);
                                    }else{
                                        this.power_unlock = false;
                                    }
                                    // j'active la pioche et je passe le tour
                                    controller.switchTurn();
                                    mRenderer.activatePick();
                                    this.currentPowerColor = -1;
                                }
                            }else{
                                makeToast("You must touch SKY for your POWER !",Toast.LENGTH_SHORT,turn);
                            }
                        }
                    } else if (0 <= testTouchScoreZone){
                        if (controller.isPowerActivated()){
                            if (this.power_unlock) {
                                if (this.currentPowerColor == -1) {
                                    //Si les pouvoir sont activés et qu'une couleur de pouvoir a été selectionné
                                    // je met à jour la scorezone courante
                                    // j'affiche le message correspondant au pouvoir
                                    Log.d("EVENT", "POWER !!!! " + (testTouchPlanetBot + 1));
                                    int colorValue = mRenderer.getScoreZoneBubbleeShapeColor(testTouchScoreZone, turn);
                                    String colorString = MyGLRenderer.getColorStringByValue(colorValue);
                                    String order = controller.getBubbleePowerMessage(colorString);
                                    this.currentPowerColor = colorValue;
                                    makeToast(order, Toast.LENGTH_LONG,turn);
                                }
                            }
                        }else{
                            makeToast("Powers not activated !",Toast.LENGTH_SHORT,turn);
                        }
                    }
                    // équivalent de glutPostRedisplay pour lancer le dessin avec les modifications.
                    requestRender();
                    condition=false;
                    this.mPreviousX = 0.0f;
                    this.mPreviousY = 0.0f;
                    this.mPreviousIndexTouched = -1;

            }

        }

        return true;
    }

    /**
     * Fonction qui génère un Toast android personalisé en fonction d'un message, d'une durée
     * et du tour courant pour savoir ou l'afficher et dans quel sens
     * @param message message à afficher
     * @param duration durée du message
     * @param turn le tour courant
     */
    public void makeToast(String message, int duration, int turn){
        Toast toast = new Toast(this.activity);
        toast.setDuration(duration);
        TextView text = new TextView(this.activity);
        text.setText(message);
        text.setTextColor(android.graphics.Color.WHITE);
        text.setTextSize(25.0f);
        if (0 == turn){
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER,0,0);
        }else{
            toast.setGravity(Gravity.TOP|Gravity.CENTER,0,0);
            text.setRotation(180.0f);
        }
        toast.setView(text);
        toast.show();
    }

    /**
     * Fonction qui renvoi le label correspondant au swipe effectué
     * @param firstTouched index du bubblee touché en premier
     * @param lastTouched index du bubblee touché en dernier
     * @return retourne le label du swipe effectué
     */
    private static String testSwipeDirection(int firstTouched, int lastTouched) {
        int limitBubbleePerline = MyGLRenderer.getLimitBubbleePerLine();
        if (firstTouched == lastTouched)
            return LABEL_SWIPE_NULL;
        else if (firstTouched == lastTouched + limitBubbleePerline)
            return LABEL_SWIPE_TOP;
        else if (firstTouched == lastTouched - limitBubbleePerline)
            return LABEL_SWIPE_BOTTOM;
        else if (firstTouched == lastTouched - 1)
            return LABEL_SWIPE_RIGHT;
        else if (firstTouched == lastTouched + 1)
            return LABEL_SWIPE_LEFT;
        else return LABEL_SWIPE_NULL;
    }

}
