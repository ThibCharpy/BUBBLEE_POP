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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.thibault.openggl.controller.Controller;
import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.utils.Pair;

/* MyGLRenderer implémente l'interface générique GLSurfaceView.Renderer */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Controller controller;

    //dimension initiale de l'écran
    private static float ORTHOM_TOP = 10.0f;
    private static float ORTHOM_BOTTOM = -10.0f;
    private static float ORTHOM_LEFT = -10.0f;
    private static float ORTHOM_RIGHT = 10.0f;

    // label de couleurs
    private static final String COLOR_BLACK_LABEL = "BLACK";
    private static final String COLOR_RED_LABEL = "RED";
    private static final String COLOR_GREEN_LABEL = "GREEN";
    private static final String COLOR_BLUE_LABEL = "BLUE";
    private static final String COLOR_PURPLE_LABEL = "PURPLE";
    private static final String COLOR_YELLOW_LABEL = "YELLOW";
    private static final String COLOR_EMPTY_LABEL = "EMPTY";

    //valeur entière de couleur
    private static final int COLOR_BLACK_NUMBER = 0;
    private static final int COLOR_RED_NUMBER = 1;
    private static final int COLOR_GREEN_NUMBER = 2;
    private static final int COLOR_BLUE_NUMBER = 3;
    private static final int COLOR_PURPLE_NUMBER = 4;
    private static final int COLOR_YELLOW_NUMBER = 5;
    private static final int COLOR_EMPTY_NUMBER = 6;

    //intialisation des paramètres de placement des bubbleeShape
    private static float BUBBLEESHAPE_RADIUS = 0.0f;
    private static float MARGIN_SPACING = 0.0f;
    private static float BUBBLESHAPE_SPACING_PLANET = 0.0f;
    private static float BUBBLESHAPE_SPACING_SKY = 0.0f;
    private static float BUBBLESHAPE_SPACING_SCORE_ZONE = 0.0f;
    private static float SPACING_BETWEEN_ZONES = 0.0f;
    private static int LIMIT_BUBBLEE_PER_LINE = 0;

    private int nbBubbleeShapeSky = 0;
    private int nbBubbleeShapePlanet = 0;
    private int nbBubbleeShapeScoreZone = 0;

    //tableau de BubbleeShape et de position de celle ci, du ciel
    private BubbleeShape[] skyBubbleeShapeTab;
    private float[][] skyBubbleeShapePositionTab;

    //tableau de BubbleeShape et de position de celle ci, du Haut
    private BubbleeShape[] planetTopBubbleeShapeTab;
    private float[][] planetTopBubbleeShapePositionTab;


    //tableau de BubbleeShape et de position de celle ci, du Bas
    private BubbleeShape[] planetBotBubbleeShapeTab;
    private float[][] planetBotBubbleeShapePositionTab;

    // BubbleeShape de la pioche et de sa position
    private BubbleeShape pickBubbleeShape;
    private float[] pickBubbleeShapePosition;


    //tableau de BubbleeShape et de position de celle ci, du score du Haut
    private BubbleeShape[] scoreZoneTopBubbleeShape;
    private float[][] scoreZoneTopBubbleeShapePositionTab;

    //tableau de BubbleeShape et de position de celle ci, du score du Bas
    private BubbleeShape[] scoreZoneBotBubbleeShape;
    private float[][] scoreZoneBotBubbleeShapePositionTab;


    // Les matrices habituelles Model/View/Projection

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    // tableau contenant la position initial des BubbleeShape
    private float[] initialBubbleeShapePosition = {-10.0f, 0.0f};

    private float[] backgroundColor = null;

    private boolean isPowerActivated;

    public MyGLRenderer(boolean powerActivated) {
        this.isPowerActivated = powerActivated;
    }

    /**
     * < 0 = multicolor
     * n {0..5} = precise color see BubbleeShape.java
     * 6 or > 6 = grey
     *
     * Initialise la couleur à gris = vide
     *
     * @param bubbleeShapeTab tableau de BubbleeShape
     */
    private void initializeZoneSquareColors(BubbleeShape[] bubbleeShapeTab){
        for (BubbleeShape tmp : bubbleeShapeTab) {
            tmp.set_color(6);
        }
    }

    /**
     * Initialise la planet en fonction de la zone envoyé par le model et de sa position su l'écran
     * @param zone planet envoyé par le model
     * @param zoneShape tableau de shape correspondant à la planet envoyé par le model
     * @param direction sens d'initialisation
     */
    private void updateZoneBubbleeShapeColors(Zone zone, BubbleeShape[] zoneShape, int direction){
        for (int i = 0;  i < zone.getBubblees().length; i++){
            BubbleeShape bubbleeShape;
            if (0  < direction)
                bubbleeShape = zoneShape[i];
            else {
                bubbleeShape = zoneShape[zoneShape.length - i -1];
            }
            bubbleeShape.set_color(getColorValueByLabel(zone.getBubblees()[i].getColor()));
        }
    }

    /**
     * Initialise les Zones contenant des BubbleeShape à vide
     */
    private void initializeZoneSquareColors(){
        initializeZoneSquareColors(this.skyBubbleeShapeTab);
        initializeZoneSquareColors(this.planetTopBubbleeShapeTab);
        initializeZoneSquareColors(this.planetBotBubbleeShapeTab);
    }

    /**
     * Initialise la position des BubbleeShape en fonction de la planet et des coordonnées de départ
     * @param bubbleeShapeTab tableau de BubbleShape
     * @param squarePositionTab tableau de position des BubbleeShape
     * @param startx position x de départ
     * @param starty position y de départ
     */
    private void initializeZonePositionRenderer(BubbleeShape[] bubbleeShapeTab, float[][] squarePositionTab, float startx, float starty){
        for (int i = 0; i < bubbleeShapeTab.length; i++){
            squarePositionTab[i] = new float[]{startx + initialBubbleeShapePosition[0], starty + initialBubbleeShapePosition[1]};
            bubbleeShapeTab[i] = new BubbleeShape(squarePositionTab[i]);
        }
    }

    /* Première méthode équivalente à la fonction init en OpenGLSL */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        this.controller = new Controller();
        this.controller.setPowerActivated(this.isPowerActivated);
        //change la couleur du background en fonction du tour
        setBackgroundByTurn(this.controller.getTurn());
        GLES30.glClearColor(this.backgroundColor[0],
                this.backgroundColor[1],
                this.backgroundColor[2], 1.0f);

        //récupération des paramètre de jeux stocké dans le model
        int[] parameters = controller.getGameParameters();
        LIMIT_BUBBLEE_PER_LINE = parameters[0];
        this.nbBubbleeShapeSky = parameters[1];
        this.nbBubbleeShapePlanet = parameters[2];
        this.nbBubbleeShapeScoreZone = parameters[3];

        Zone[] space = controller.InitializeGame();

        //affectation des valeurs de placement des bubbleShape
        BUBBLEESHAPE_RADIUS = BubbleeShape.getRadius();
        MARGIN_SPACING = BUBBLEESHAPE_RADIUS * 6;
        BUBBLESHAPE_SPACING_PLANET = BUBBLEESHAPE_RADIUS * 2.5f;
        BUBBLESHAPE_SPACING_SKY = BUBBLEESHAPE_RADIUS * 2.5f;
        BUBBLESHAPE_SPACING_SCORE_ZONE = BUBBLEESHAPE_RADIUS * 2;
        SPACING_BETWEEN_ZONES = BUBBLEESHAPE_RADIUS * 2;

        initialBubbleeShapePosition = new float[]{0.0f , BUBBLEESHAPE_RADIUS /2};

        pickBubbleeShapePosition = new float[]{0.0f + BUBBLEESHAPE_RADIUS + MARGIN_SPACING/4 + ORTHOM_LEFT,0.0f};
        pickBubbleeShape = new BubbleeShape(pickBubbleeShapePosition);

        //intialisation de la vue de la planet du haut
        scoreZoneTopBubbleeShape = new BubbleeShape[this.nbBubbleeShapeScoreZone];
        scoreZoneTopBubbleeShapePositionTab = new float[this.nbBubbleeShapeScoreZone][];
        initializeZonePositionRenderer(scoreZoneTopBubbleeShape,scoreZoneTopBubbleeShapePositionTab,
                ORTHOM_LEFT + BUBBLEESHAPE_RADIUS + MARGIN_SPACING/4,
                ORTHOM_TOP + BUBBLEESHAPE_RADIUS + SPACING_BETWEEN_ZONES);

        //intialisation de la vue de la planet du bas
        scoreZoneBotBubbleeShape = new BubbleeShape[this.nbBubbleeShapeScoreZone];
        scoreZoneBotBubbleeShapePositionTab = new float[this.nbBubbleeShapeScoreZone][];
        initializeZonePositionRenderer(scoreZoneBotBubbleeShape,scoreZoneBotBubbleeShapePositionTab,
                ORTHOM_LEFT + BUBBLEESHAPE_RADIUS + MARGIN_SPACING/4,
                -1.5f * SPACING_BETWEEN_ZONES - BUBBLESHAPE_SPACING_SCORE_ZONE + BUBBLEESHAPE_RADIUS);

        //intialisation de la vue du ciel
        skyBubbleeShapeTab = new BubbleeShape[nbBubbleeShapeSky];
        skyBubbleeShapePositionTab = new float[nbBubbleeShapeSky][];
        initializeZonePositionRenderer(skyBubbleeShapeTab, skyBubbleeShapePositionTab,
                ORTHOM_LEFT + BUBBLEESHAPE_RADIUS + MARGIN_SPACING,
                BUBBLEESHAPE_RADIUS - BUBBLEESHAPE_RADIUS/3.5f);

        //intialisation des positions des BubbleeShape de la planet du Haut
        planetTopBubbleeShapeTab = new BubbleeShape[nbBubbleeShapePlanet];
        planetTopBubbleeShapePositionTab = new float[nbBubbleeShapePlanet][];
        initializeZonePositionRenderer(planetTopBubbleeShapeTab, planetTopBubbleeShapePositionTab,
                ORTHOM_LEFT + BUBBLEESHAPE_RADIUS + MARGIN_SPACING,
                ORTHOM_TOP + SPACING_BETWEEN_ZONES + BUBBLEESHAPE_RADIUS);

        //intialisation des positions des BubbleeShape de la planet du bas
        planetBotBubbleeShapeTab = new BubbleeShape[nbBubbleeShapePlanet];
        planetBotBubbleeShapePositionTab = new float[nbBubbleeShapePlanet][];
        initializeZonePositionRenderer(planetBotBubbleeShapeTab, planetBotBubbleeShapePositionTab,
                ORTHOM_LEFT + BUBBLEESHAPE_RADIUS + MARGIN_SPACING,
                -2.0f * SPACING_BETWEEN_ZONES - BUBBLESHAPE_SPACING_PLANET);

        //intialisation des couleurs de chaque zone (planet du haut/bas et ciel)
        initializeZoneSquareColors();
        updateZoneBubbleeShapeColors(space[0], planetTopBubbleeShapeTab,-1);
        updateZoneBubbleeShapeColors(space[1], skyBubbleeShapeTab,1);
        updateZoneBubbleeShapeColors(space[2], planetBotBubbleeShapeTab,1);
    }

    /**
     * Fonction qui déplace chaque BubbleShape créé à sa bonne position en fonction de la zone,
     * du nombre de bubbleeShape et de leur espacement
     * @param zoneBubbleeShapeTab tableau des BubbleeShape
     * @param zoneSquarePositionTab tableau de la position de chaque BubbleeShape
     * @param nb_bubblee nombre de Bubblee
     * @param bubbleeSpacing espacement entre les BubbleeShape
     */
    private void drawZoneBubbleeShapeTab(BubbleeShape[] zoneBubbleeShapeTab, float[][] zoneSquarePositionTab, int nb_bubblee, float bubbleeSpacing){
        float[] scratch = new float[16]; // pour stocker une matrice

        Matrix.setIdentityM(mViewMatrix,0);

        for (int i=0; i<nb_bubblee; i++){
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            Matrix.setIdentityM(mModelMatrix,0);
            Matrix.translateM(mModelMatrix, 0, zoneSquarePositionTab[i][0]+(i% LIMIT_BUBBLEE_PER_LINE)* bubbleeSpacing,
                    zoneSquarePositionTab[i][1] - (i/ LIMIT_BUBBLEE_PER_LINE) * bubbleeSpacing, 0);

            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);

            zoneBubbleeShapeTab[i].draw(scratch);
        }
    }

    /**
     * Fonction qui déplace chaque BubbleShape créé à sa bonne position en fonction de la zone de score,
     * du nombre de bubbleeShape
     * @param scoreZoneBubbleeShapeTab tableau de BubbleeShape
     * @param scoreZoneBubbleeShapePositionTab tableau de position des bubbleeShape
     * @param nb_square nombre de bubblee
     */
    private void drawScoreZoneTab(BubbleeShape[] scoreZoneBubbleeShapeTab, float[][] scoreZoneBubbleeShapePositionTab, int nb_square){
        float[] scratch = new float[16];
        Matrix.setIdentityM(mViewMatrix,0);

        for (int i=0; i<nb_square; i++){
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            Matrix.setIdentityM(mModelMatrix,0);

            Matrix.translateM(mModelMatrix, 0, scoreZoneBubbleeShapePositionTab[i][0],
                    scoreZoneBubbleeShapePositionTab[i][1] - i * BUBBLESHAPE_SPACING_SCORE_ZONE, 0);

            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);
            scoreZoneBubbleeShapeTab[i].draw(scratch);
        }
    }

    /* Deuxième méthode équivalente à la fonction Display */
    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16]; // pour stocker une matrice


        GLES30.glClearColor(this.backgroundColor[0],
                this.backgroundColor[1],
                this.backgroundColor[2], 1.0f);

        // glClear rien de nouveau on vide le buffer de couleur et de profondeur */
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        /*pour positionner la caméra mais ici on n'en a pas besoin*/
        if (!this.controller.isEnd()) {
            Matrix.setIdentityM(mViewMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, pickBubbleeShapePosition[0], pickBubbleeShapePosition[1], 0);
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);
            pickBubbleeShape.draw(scratch);

            //dessine les différente zone du plateau de jeux
            drawZoneBubbleeShapeTab(skyBubbleeShapeTab, skyBubbleeShapePositionTab, nbBubbleeShapeSky, BUBBLESHAPE_SPACING_SKY);
            drawZoneBubbleeShapeTab(planetTopBubbleeShapeTab, planetTopBubbleeShapePositionTab, nbBubbleeShapePlanet, BUBBLESHAPE_SPACING_PLANET);
            drawZoneBubbleeShapeTab(planetBotBubbleeShapeTab, planetBotBubbleeShapePositionTab, nbBubbleeShapePlanet, BUBBLESHAPE_SPACING_PLANET);

            //dessine les deux zones de score du plateau
            drawScoreZoneTab(scoreZoneTopBubbleeShape, scoreZoneTopBubbleeShapePositionTab, nbBubbleeShapeScoreZone);
            drawScoreZoneTab(scoreZoneBotBubbleeShape, scoreZoneBotBubbleeShapePositionTab, nbBubbleeShapeScoreZone);
        }else{
            //Si la partie est fini cela garde le fond d'écran de la couleur de celui qui a gagné
            int winner = this.controller.getWinner();
            if (0 == winner)
                setBackgroundByTurn(1);
            if (1 == winner)
                setBackgroundByTurn(0);
            GLES30.glClearColor(this.backgroundColor[0],
                    this.backgroundColor[1],
                    this.backgroundColor[2], 1.0f);
        }
    }

    /* équivalent au Reshape en OpenGLSL */

    /**
     * Dimenssione correctement la fenetre de dessin des bubbleeShapes
     * @param unused not used
     * @param width largeur de l'écran
     * @param height hauteur de l'écran
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {


        GLES30.glViewport(0, 0, width, height);
        float ratio;
        if (height > width){
            ratio = height*1.0f / width*1.0f;
            ORTHOM_TOP *= ratio;
            ORTHOM_BOTTOM *= ratio;
        }
        System.out.println("T = " + ORTHOM_TOP);
        System.out.println("B = " + ORTHOM_BOTTOM);
        System.out.println("L = " + ORTHOM_LEFT);
        System.out.println("R = " + ORTHOM_RIGHT);
        Matrix.orthoM(mProjectionMatrix, 0, ORTHOM_LEFT, ORTHOM_RIGHT, ORTHOM_BOTTOM, ORTHOM_TOP, -1.0f, 1.0f);

    }

    /* La gestion des shaders ... */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
    }

    /**
     * met à jour le ciel quand le model l'a remplis lorsqu'un joueur a touché à la pioche
     * @param sky objet contenant les bubblee du ciel
     */
    public void fillEmptyBubbleeShapeSky(Zone sky) {
        for (int i=0; i < this.skyBubbleeShapeTab.length ; i++){
            if (6 == this.skyBubbleeShapeTab[i].getColor()){
                this.skyBubbleeShapeTab[i].set_color(getColorValueByLabel(sky.getBubblees()[i].getColor()));
            }
        }
    }

    /**
     * Met à jour la vue et ses BubbleeShapes lorsque le model à appliqué la gravité à une bille
     * @param indexTouchSky position de la bubblee touché dans le ciel
     * @param newRows pair qui contient la position des deux billes auxquelles on a appliqué la gravité
     */
    public void updateBubbleeShapeColorGravity(int indexTouchSky, Pair<Integer,Integer> newRows) {
        int nbRow = this.nbBubbleeShapePlanet / LIMIT_BUBBLEE_PER_LINE;
        int newCol = indexTouchSky%LIMIT_BUBBLEE_PER_LINE;
        int index;
        int indexTop;
        int indexNext;
        int indexNextSky;
        if (0 == this.controller.getTurn()){ //bot
            index = newRows.getFirst() * LIMIT_BUBBLEE_PER_LINE + newCol;
            if (indexTouchSky < LIMIT_BUBBLEE_PER_LINE) { //chain
                indexTop = (newRows.getFirst() + 1) * LIMIT_BUBBLEE_PER_LINE + newCol;
                this.planetBotBubbleeShapeTab[indexTop].set_color(this.skyBubbleeShapeTab[indexTouchSky + LIMIT_BUBBLEE_PER_LINE].getColor());
                this.skyBubbleeShapeTab[indexTouchSky + LIMIT_BUBBLEE_PER_LINE].set_color(COLOR_EMPTY_NUMBER);
            } else {
                if (0 == newCol){
                    indexNext = newRows.getSecond() * LIMIT_BUBBLEE_PER_LINE + (newCol + 1);
                    indexNextSky = indexTouchSky + 1;
                }else{
                    indexNext = newRows.getSecond() * LIMIT_BUBBLEE_PER_LINE + (newCol - 1);
                    indexNextSky = indexTouchSky - 1;
                }
                this.planetBotBubbleeShapeTab[indexNext].set_color(this.skyBubbleeShapeTab[indexNextSky].getColor());
                this.skyBubbleeShapeTab[indexNextSky].set_color(COLOR_EMPTY_NUMBER);
            }
            this.planetBotBubbleeShapeTab[index].set_color(this.skyBubbleeShapeTab[indexTouchSky].getColor());
        }else{ // top
            int rowInverse = (nbRow - newRows.getFirst() - 1);
            int rowInverseNext = (nbRow - newRows.getSecond() - 1);
            index = rowInverse * LIMIT_BUBBLEE_PER_LINE + newCol;
            if (LIMIT_BUBBLEE_PER_LINE <= indexTouchSky){ // chain
                indexTop = (rowInverse - 1) * LIMIT_BUBBLEE_PER_LINE + newCol;
                this.planetTopBubbleeShapeTab[indexTop].set_color(this.skyBubbleeShapeTab[indexTouchSky - LIMIT_BUBBLEE_PER_LINE].getColor());
                this.skyBubbleeShapeTab[indexTouchSky - LIMIT_BUBBLEE_PER_LINE].set_color(COLOR_EMPTY_NUMBER);
            } else {
                if ((LIMIT_BUBBLEE_PER_LINE - 1)  == newCol){
                    indexNext = rowInverseNext * LIMIT_BUBBLEE_PER_LINE + (newCol - 1);
                    indexNextSky = indexTouchSky - 1;
                }else{
                    indexNext = rowInverseNext * LIMIT_BUBBLEE_PER_LINE + (newCol + 1);
                    indexNextSky = indexTouchSky + 1;
                }
                this.planetTopBubbleeShapeTab[indexNext].set_color(this.skyBubbleeShapeTab[indexNextSky].getColor());
                this.skyBubbleeShapeTab[indexNextSky].set_color(COLOR_EMPTY_NUMBER);
            }
            this.planetTopBubbleeShapeTab[index].set_color(this.skyBubbleeShapeTab[indexTouchSky].getColor());
        }
        this.skyBubbleeShapeTab[indexTouchSky].set_color(COLOR_EMPTY_NUMBER);
    }

    /**
     * Met à jour une planet en fonction du tableau de Bubblee donné par le model et du tour courant
     * @param zone tableau de bubblee donné par le model
     * @param turn le tour courant
     */
    public void updatePlanet(Zone zone, int turn) {
        int nbRow = this.nbBubbleeShapePlanet / LIMIT_BUBBLEE_PER_LINE;
        BubbleeShape[] currentPlanetBubbleeShapeTab;
        if (0 == turn){
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }else{
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }
        int index;
        for (int i = 0; i < nbRow; i++){
            for (int j = 0; j < LIMIT_BUBBLEE_PER_LINE; j++){
                if (0 == turn){
                    index = i * LIMIT_BUBBLEE_PER_LINE + j;
                }else{
                    int rowInverse = (nbRow - i - 1);
                    index = rowInverse * LIMIT_BUBBLEE_PER_LINE + j;
                }
                currentPlanetBubbleeShapeTab[index].set_color(getColorValueByLabel(zone.getBubblees()[i * LIMIT_BUBBLEE_PER_LINE + j].getColor()));
            }
        }
    }

    /**
     * Met à jour la vue lorsqu'une réduction à eu lieu en fonction du bubblee touché dans le ciel,
     * du tour courant et de la planet concerné
     * @param indexTouchSky index du bubblee touché dans le ciel
     * @param turn le tour courant
     * @param planet planet courante en fonction du tour
     */
    public void applyReduceToView(int indexTouchSky, int turn, Zone planet) {
        int nbBubbleePerLine = Zone.getNumberOfBubbleePerLine();
        int row = indexTouchSky / nbBubbleePerLine;
        int col = indexTouchSky % nbBubbleePerLine;
        int nextIndex;
        if (0 == turn){
            if (0 < row){
                if (0 < col){
                    nextIndex = indexTouchSky - 1;
                }else{
                    nextIndex = indexTouchSky + 1;
                }
            }else{
                nextIndex = indexTouchSky + nbBubbleePerLine;
            }
        }else{
            if (0 < row){
                nextIndex = indexTouchSky - nbBubbleePerLine;
            }else{
                if (col < nbBubbleePerLine - 1){
                    nextIndex = indexTouchSky + 1;
                }else{
                    nextIndex = indexTouchSky - 1;
                }
            }
        }
        this.skyBubbleeShapeTab[indexTouchSky].set_color(COLOR_EMPTY_NUMBER);
        this.skyBubbleeShapeTab[nextIndex].set_color(COLOR_EMPTY_NUMBER);
        updatePlanet(planet,turn);
    }

    /*public void applyReduceToView2(int indexTouchSky, String colorToReduceString, int turn) {
        int i = indexTouchSky;
        int colorToReduce = getColorValueByLabel(colorToReduceString);
        int nbRow = this.nbBubbleeShapePlanet / LIMIT_BUBBLEE_PER_LINE;
        int col = indexTouchSky%LIMIT_BUBBLEE_PER_LINE;
        int colorToSave = colorToReduce;
        BubbleeShape[] currentPlanetBubbleeShapeTab = null;
        if (0 == turn){
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }else{
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }
        while(i < this.nbBubbleeShapeSky && 0 <= i){
            if (this.skyBubbleeShapeTab[i].getColor() != colorToReduce)
                colorToSave = this.skyBubbleeShapeTab[i].getColor();
            this.skyBubbleeShapeTab[i].set_color(COLOR_EMPTY_NUMBER);
            if (0 == turn)
                i+=LIMIT_BUBBLEE_PER_LINE;
            else
                i-=LIMIT_BUBBLEE_PER_LINE;
        }
        i = 0;
        int index;
        boolean finished = false;
        while (!finished){
            index = 0;
            if (0 == turn){
                index = i * LIMIT_BUBBLEE_PER_LINE + col;
            }else{
                int rowInverse = (nbRow - i - 1);
                index = rowInverse * LIMIT_BUBBLEE_PER_LINE + col;
            }
            if (currentPlanetBubbleeShapeTab[index].getColor() == colorToReduce)
                currentPlanetBubbleeShapeTab[index].set_color(COLOR_EMPTY_NUMBER);
            else
            if (currentPlanetBubbleeShapeTab[index].getColor() != COLOR_EMPTY_NUMBER) {
                finished = true;
                if (0 == turn) {
                    index = (i - 1) * LIMIT_BUBBLEE_PER_LINE + col;
                } else {
                    int rowInverse = (nbRow - i - 1);
                    index = (rowInverse + 1) * LIMIT_BUBBLEE_PER_LINE + col;
                }
                if (colorToReduce != colorToSave)
                    currentPlanetBubbleeShapeTab[index].set_color(colorToSave);
                else
                    currentPlanetBubbleeShapeTab[index].set_color(COLOR_EMPTY_NUMBER);

            }
            i++;
        }
    }*/

    /**
     * Met à jour les BubbleeShapes de la zone de score du joueur qui à joué en fonction du tour
     * @param scoreZoneBubblees tableau des Bubblee envoyé par le model
     * @param turn le tour courant
     */
    public void updateScoreZone(Bubblee[] scoreZoneBubblees, int turn) {
        BubbleeShape[] currentScoreZoneTab;
        int index;
        if (0 == turn){
            currentScoreZoneTab = this.scoreZoneBotBubbleeShape;
        }else{
            currentScoreZoneTab = this.scoreZoneTopBubbleeShape;
        }
        for (int i = 0; i<scoreZoneBubblees.length; i++){
            if (0 == turn){
                index = i;
            }else{
                index = this.nbBubbleeShapeScoreZone - i - 1;
            }
            int color = getColorValueByLabel(scoreZoneBubblees[i].getColor());
            currentScoreZoneTab[index].set_color(color);
        }
    }

    /**
     * Retourn la couleur du BubbleeShape touché dans la zone de score quand les pourvoirs sont
     * activés en fonction de l'index du bubblee touché et du tour courant
     * @param testTouchPick index du bubblee touché dans la zone de score
     * @param turn le tour courant
     * @return la valeur entière de la couleur
     */
    public int getScoreZoneBubbleeShapeColor(int testTouchPick, int turn) {
        BubbleeShape[] currentScoreZoneTab;
        if (0 == turn){
            currentScoreZoneTab = this.scoreZoneBotBubbleeShape;
        }else{
            currentScoreZoneTab = this.scoreZoneTopBubbleeShape;
        }
        return currentScoreZoneTab[testTouchPick].getColor();
    }

    /**
     * Met à jour la vue après l'execution d'un pouvoir rouge en fonction du tour et des deux billes
     * touchés lors de l'échange
     * @param firstIndexTouched index du premier bubblee touché
     * @param lastIndexTouched index du deuxième bubblee touché
     * @param turn le tour courant
     */
    public void applyRedPowerToView(int firstIndexTouched, int lastIndexTouched, int turn) {
        BubbleeShape[] currentPlanetBubbleeShapeTab;
        //ATENTION: Ici on a inversé les planetes comme on echange sur la planet adverse !
        if (0 == turn){
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }else{
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }
        int tmp_color = currentPlanetBubbleeShapeTab[firstIndexTouched].getColor();
        currentPlanetBubbleeShapeTab[firstIndexTouched].set_color(currentPlanetBubbleeShapeTab[lastIndexTouched].getColor());
        currentPlanetBubbleeShapeTab[lastIndexTouched].set_color(tmp_color);
    }

    /**
     * Met à jour la vue après l'execution d'un pouvoir vert en fonction du tour et des deux billes
     * touchés lors de l'échange
     * @param firstIndexTouched index du premier bubblee touché
     * @param lastIndexTouched index du deuxième bubblee touché
     * @param turn le tour courant
     */
    public void applyGreenPowerToView(int firstIndexTouched, int lastIndexTouched, int turn) {
        BubbleeShape[] currentPlanetBubbleeShapeTab;
        if (0 == turn){
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }else{
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }
        int tmp_color = currentPlanetBubbleeShapeTab[firstIndexTouched].getColor();
        currentPlanetBubbleeShapeTab[firstIndexTouched].set_color(currentPlanetBubbleeShapeTab[lastIndexTouched].getColor());
        currentPlanetBubbleeShapeTab[lastIndexTouched].set_color(tmp_color);
    }

    /**
     * Met à jour la vue après l'éxecution d'un pouvoir violet en fonction du bubblee touché de la
     * planet du joueur courant et du tour courant
     * @param indexBubbleShapeTouched index du bubblee touché
     * @param turn le tour courant
     */
    public void applyPurplePowerToView(int indexBubbleShapeTouched, int turn) {
        BubbleeShape[] currentPlanetBubbleeShapeTab;
        BubbleeShape[] opponentPlanetBubbleeShapeTab;
        int nbRow = this.nbBubbleeShapePlanet / LIMIT_BUBBLEE_PER_LINE;
        int col = indexBubbleShapeTouched%LIMIT_BUBBLEE_PER_LINE;
        int indexToErase;
        if (0 == turn){
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
            opponentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
            indexToErase = indexBubbleShapeTouched;
        }else{
            int row = indexBubbleShapeTouched/LIMIT_BUBBLEE_PER_LINE;
            int rowInverse = (nbRow - row -1);
            indexToErase = rowInverse * LIMIT_BUBBLEE_PER_LINE + col;
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
            opponentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }
        int colorToSend = currentPlanetBubbleeShapeTab[indexToErase].getColor();
        currentPlanetBubbleeShapeTab[indexToErase].set_color(COLOR_EMPTY_NUMBER);
        int i = 0;
        boolean falling = true;
        while (i < nbRow  && falling){
            int indexOpponent;
            //ATTENTION: ici les turn sont inversé car on tape sur le tableau adverse
            if (0 == turn){
                int rowInverse = (nbRow - i - 1);
                indexOpponent = rowInverse * LIMIT_BUBBLEE_PER_LINE + col;
            }else{
                indexOpponent = i * LIMIT_BUBBLEE_PER_LINE + col;
            }
            if (opponentPlanetBubbleeShapeTab[indexOpponent].getColor() == COLOR_EMPTY_NUMBER){
                i++;
            }else{
                if (opponentPlanetBubbleeShapeTab[indexOpponent].getColor() != COLOR_EMPTY_NUMBER){
                    if (0 == turn)
                        opponentPlanetBubbleeShapeTab[indexOpponent + LIMIT_BUBBLEE_PER_LINE].set_color(colorToSend);
                    else
                        opponentPlanetBubbleeShapeTab[indexOpponent - LIMIT_BUBBLEE_PER_LINE].set_color(colorToSend);
                    falling = false;
                }
                i++;
            }
            if (nbRow <= i && falling){
                if (0 == turn) {
                    int rowInverse = (nbRow - (i-1) - 1);
                    opponentPlanetBubbleeShapeTab[rowInverse * LIMIT_BUBBLEE_PER_LINE + col].set_color(colorToSend);
                }else {
                    opponentPlanetBubbleeShapeTab[(i-1) * LIMIT_BUBBLEE_PER_LINE + col].set_color(colorToSend);
                }
                falling = false;
            }
        }

    }

    /**
     * Met à jour la vue après l'execution du pouvoir bleu en fonction du bubblee touché dans le ciel
     * et du tour courant
     * @param indexBubbleShapeTouched index du bubblee touché dan le ciel
     * @param turn le tour courant
     */
    public void applyBluePowerToView(int indexBubbleShapeTouched, int turn) {
        BubbleeShape[] opponentPlanetBubbleeShapeTab;
        int colorToSend = this.skyBubbleeShapeTab[indexBubbleShapeTouched].getColor();
        this.skyBubbleeShapeTab[indexBubbleShapeTouched].set_color(COLOR_EMPTY_NUMBER);
        int nbRow = this.nbBubbleeShapePlanet / LIMIT_BUBBLEE_PER_LINE;
        int col = indexBubbleShapeTouched%LIMIT_BUBBLEE_PER_LINE;
        if (0 == turn){
            opponentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }else{
            opponentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }
        int i = 0;
        boolean falling = true;
        while (i < nbRow && falling){
            int indexOpponent;
            //ATTENTION: ici les turn sont inversé car on tape sur le tableau adverse
            if (0 == turn){
                int rowInverse = (nbRow - i - 1);
                indexOpponent = rowInverse * LIMIT_BUBBLEE_PER_LINE + col;
            }else{
                indexOpponent = i * LIMIT_BUBBLEE_PER_LINE + col;
            }
            if (opponentPlanetBubbleeShapeTab[indexOpponent].getColor() == COLOR_EMPTY_NUMBER){
                i++;
            }else{
                if (opponentPlanetBubbleeShapeTab[indexOpponent].getColor() != COLOR_EMPTY_NUMBER){
                    if (0 == turn)
                        opponentPlanetBubbleeShapeTab[indexOpponent + LIMIT_BUBBLEE_PER_LINE].set_color(colorToSend);
                    else
                        opponentPlanetBubbleeShapeTab[indexOpponent - LIMIT_BUBBLEE_PER_LINE].set_color(colorToSend);
                    falling = false;
                }
                i++;
            }
            if (nbRow <= i && falling){
                if (0 == turn) {
                    int rowInverse = (nbRow - (i-1) - 1);
                    opponentPlanetBubbleeShapeTab[rowInverse * LIMIT_BUBBLEE_PER_LINE + col].set_color(colorToSend);
                }else {
                    opponentPlanetBubbleeShapeTab[(i-1) * LIMIT_BUBBLEE_PER_LINE + col].set_color(colorToSend);
                }
                falling = false;
            }
        }
    }

    /**
     * Met à jour la vue après l'execution du pouvoir jaune en fonction de l'index du bublee touché
     * dans la planète courante et du tour courant. La vue mise à jour ici est la scoreZone du joueur
     * qui a utilisé le pouvoir
     * @param indexBubbleShapeTouched index du bubblee touché
     * @param turn le tour courant
     */
    public void applyYellowPowerToView(int indexBubbleShapeTouched, int turn) {
        BubbleeShape[] currentPlanetBubbleeShapeTab;
        int col = indexBubbleShapeTouched%LIMIT_BUBBLEE_PER_LINE;
        int row = indexBubbleShapeTouched/LIMIT_BUBBLEE_PER_LINE;
        int index;

        if (0 == turn){
            index = row * LIMIT_BUBBLEE_PER_LINE + col;
            currentPlanetBubbleeShapeTab = this.planetBotBubbleeShapeTab;
        }else{
            index = indexBubbleShapeTouched;
            currentPlanetBubbleeShapeTab = this.planetTopBubbleeShapeTab;
        }
        currentPlanetBubbleeShapeTab[index].set_color(COLOR_EMPTY_NUMBER);
    }

    /**
     * converti une couleur donné par une valeur entière
     * @param label label de la couleur à convertir
     * @return valeur entière de la couleur
     */
    public static int getColorValueByLabel(String label){
        switch (label){
            case COLOR_BLACK_LABEL: return COLOR_BLACK_NUMBER;
            case COLOR_RED_LABEL: return COLOR_RED_NUMBER;
            case COLOR_GREEN_LABEL: return COLOR_GREEN_NUMBER;
            case COLOR_BLUE_LABEL: return COLOR_BLUE_NUMBER;
            case COLOR_PURPLE_LABEL: return COLOR_PURPLE_NUMBER;
            case COLOR_YELLOW_LABEL: return COLOR_YELLOW_NUMBER;
            case COLOR_EMPTY_LABEL: return COLOR_EMPTY_NUMBER;
        }
        return -1;
    }

    /**
     * converti une couleur donné par la string correspondante
     * @param value valeur entière de la couleur à convertir
     * @return string correspondante à la couleur
     */
    public static String getColorStringByValue(int value){
        switch (value){
            case COLOR_BLACK_NUMBER: return COLOR_BLACK_LABEL;
            case COLOR_RED_NUMBER: return COLOR_RED_LABEL;
            case COLOR_GREEN_NUMBER: return COLOR_GREEN_LABEL;
            case COLOR_BLUE_NUMBER: return COLOR_BLUE_LABEL;
            case COLOR_PURPLE_NUMBER: return COLOR_PURPLE_LABEL;
            case COLOR_YELLOW_NUMBER: return COLOR_YELLOW_LABEL;
            case COLOR_EMPTY_NUMBER: return COLOR_EMPTY_LABEL;
        }
        return "";
    }

    /**
     * Echange la couleur de deux BubbleShape
     * @param indexBubbleeShape1 index du bubbleShape touché
     * @param indexBubbleeShape2 index du bubbleShape touché
     */
    public void swipeSkyBubbleeShapeTab(int indexBubbleeShape1, int indexBubbleeShape2) {
        int tmp_color = this.skyBubbleeShapeTab[indexBubbleeShape1].getColor();
        this.skyBubbleeShapeTab[indexBubbleeShape1].set_color(this.skyBubbleeShapeTab[indexBubbleeShape2].getColor());
        this.skyBubbleeShapeTab[indexBubbleeShape2].set_color(tmp_color);
    }

    /**
     * Change la couleur du fond d'écran en fonction du tour à jouer
     * @param turn le tour courant
     */
    public void setBackgroundByTurn(int turn){
        if (1 == turn)
            this.backgroundColor = new float[]{1.0f, 0.0f, 1.0f};
        else
            this.backgroundColor = new float[]{0.0f, 1.0f, 1.0f};
    }

    /**
     * Désactive la pioche en la coloriant en gris
     */
    public void disablePick(){
        this.pickBubbleeShape.set_color(6);
    }

    /**
     * Active la pioche en la coloriant en multicolor
     */
    public void activatePick(){
        this.pickBubbleeShape.set_color(-1);
    }


    // La suite est une série de getter et setter utilisé au cours du dévellopement

    public Controller getController() {
        return controller;
    }

    public static float getOrthomTop() {
        return ORTHOM_TOP;
    }

    public static float getOrthomBottom() {
        return ORTHOM_BOTTOM;
    }

    public static float getOrthomLeft() {
        return ORTHOM_LEFT;
    }

    public static float getOrthomRight() {
        return ORTHOM_RIGHT;
    }

    public float[][] getSkyBubbleeShapePositionTab() {
        return skyBubbleeShapePositionTab;
    }

    public float[][] getPlanetTopBubbleeShapePositionTab() { return planetTopBubbleeShapePositionTab; }

    public float[][] getPlanetBotBubbleeShapePositionTab() { return planetBotBubbleeShapePositionTab; }

    public float[][] getScoreZoneTopBubbleeShapePositionTab() {
        return scoreZoneTopBubbleeShapePositionTab;
    }

    public float[][] getScoreZoneBotBubbleeShapePositionTab() {
        return scoreZoneBotBubbleeShapePositionTab;
    }

    public float[] getPickBubbleeShapePosition() {return pickBubbleeShapePosition; }

    public static float getBubbleshapeSpacingPlanet() {
        return BUBBLESHAPE_SPACING_PLANET;
    }

    public static float getBubbleshapeSpacingSky() {
        return BUBBLESHAPE_SPACING_SKY;
    }

    public static float getBubbleshapeSpacingScoreZone() {
        return BUBBLESHAPE_SPACING_SCORE_ZONE;
    }

    public static int getLimitBubbleePerLine() {
        return LIMIT_BUBBLEE_PER_LINE;
    }

    public static float getBubbleeshapeRadius() {
        return BUBBLEESHAPE_RADIUS;
    }

    public static int getColorRedNumber() {
        return COLOR_RED_NUMBER;
    }

    public static int getColorGreenNumber() {
        return COLOR_GREEN_NUMBER;
    }

    public static int getColorBlueNumber() {
        return COLOR_BLUE_NUMBER;
    }

    public static int getColorPurpleNumber() {
        return COLOR_PURPLE_NUMBER;
    }

    public static int getColorYellowNumber() {
        return COLOR_YELLOW_NUMBER;
    }

    public static float getMarginSpacing() {
        return MARGIN_SPACING;
    }

    public static float getSpacingBetweenZones() {
        return SPACING_BETWEEN_ZONES;
    }
}
