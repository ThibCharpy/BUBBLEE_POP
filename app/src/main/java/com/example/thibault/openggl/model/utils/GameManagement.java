package com.example.thibault.openggl.model.utils;

import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Planet;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.exceptions.ColumnFullException;
import com.example.thibault.openggl.model.exceptions.EmptyPickException;
import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.exceptions.SkyFullException;
import com.example.thibault.openggl.model.exceptions.SkyNotFullException;

/**
 * Created by thibault on 27/04/17.
 */

public interface GameManagement {

    //Ici les signatures de chaque fonctions indispensables au jeu sont implémentées

    /**
     * Initialise chaque zone du plateau et rempli la pioche. ajoute le résulat du switch pour
     * activer les pouvoir ou non
     * @param isPowerActivated correspondant au resultat du switch dans l'activité de menu
     */
    void initializeSpace(boolean isPowerActivated);

    /**
     * Applique la gravité a une paire de bubblee en fonction du tour et de l'index de la bubble
     * touché dans le ciel
     * @param turn le tour courant
     * @param indexBubbleeTouched index du bubblee touché dans le ciel
     * @return une paire d'entier correspondant à la ligne des deux bubblee tombé
     * @throws ColumnFullException si la colonne est pleine
     * @throws SkyNotFullException si le joueur a oublié de cliuer sur la pioche
     * @throws EmptyPickException si la pioche est vide, dans ce cas enclenche le processus de fin de partie
     */
    Pair<Integer,Integer> applyGravity(int turn, int indexBubbleeTouched) throws ColumnFullException, SkyNotFullException, EmptyPickException;

    /**
     * Rempli le ciel de bubblee tiré aleatoirement dans la pioche
     * @return le contenu du ciel après remplissage
     * @throws NoMoreBubbleeForColorException si il n'y a plus de bubblee dans la pioche
     * @throws SkyFullException si le ciel est plein
     */
    Zone fillSky() throws NoMoreBubbleeForColorException, SkyFullException;

    /**
     * fonction qui échange deux bubblee dans le ciel en fonction de l'index du premier bubblee touché
     * et du bubblee adjacent
     * @param indexBubblee1 index du premier bubblee touché
     * @param indexBubblee2 index du second bubblee touché
     */
    void swipeBubbleSky(int indexBubblee1, int indexBubblee2);

    /**
     * Recherche une réduction dans la planet courante en fonction du tour courant, si il y en a une
     * elle est faite et on renvoi le contenu de la planet sinon on renvoi null
     * @param turn le tour courant
     * @return null si pas de reduction, le contenu de la zone courante sinon
     */
    Zone reducePlanet(int turn);

    /**
     * Effectue une réduction en fonction du bubblee touché dans le ciel et de la planet courante,
     * en fonction du tour. Renvoi null si ce n'est pas possible et le contenu de la zone sinon.
     * @param indexTouchSky index du bubblee touché dans le ciel
     * @param turn le tour courant
     * @return null sinon réduction impossible, le contenu de la zone courante sinon
     */
    Zone reducePlanetWithSky2(int indexTouchSky, int turn);

    /**
     * Renvoi les dernière bubblee gagné en fonction du nombre de bubblee dans la zone de score et
     * du tour courant
     * @param turn le tour courant
     * @return un tableau de bubblee de taille égale au nombre de bubble dans la zone de score
     */
    Bubblee[] getLastBubbleesWin(int turn);

    /**
     * Retourne le nombre de bubblee de la planet en fonction de i
     * @param i index representant la planet souhaité
     * @return le nombre de bubblee de la planet
     */
    int getPlanetScore(int i);

    /**
     * Si le joueur a fait deux alignement consécutif il débloque un pouvoir
     * @param turn le tour courant
     * @return vrai si un pouvoir a été débloqué et faux sinon
     */
    boolean testPowerUnlock(int turn);

    /**
     * Retourne la regle du pouvoir activé en fonction du label de la couleur
     * @param color label de la couleur du pouvoir
     * @return retourne la regle du pouvoir débloqué
     */
    String getPowerMessage(String color);

    /**
     * Execute le pouvoir rouge sur la planet adverse en fonction du tour courant, du premier bubblee
     * touché et du second bubblee touché
     * @param firstIndexTouched index du premier bubblee touché
     * @param lastIndexTouched index du second bubblee touché
     * @param turn le tour courant
     */
    void applyRedPower(int firstIndexTouched, int lastIndexTouched, int turn);

    /**
     * Execute le pouvoir vert sur la planet du joueur courant en fonction du tour courant, du
     * premier bubbleetouché et du second bubblee touché
     * @param firstIndexTouched index du premier bubblee touché
     * @param lastIndexTouched index du second bubblee touché
     * @param turn le tour courant
     */
    void applyGreenPower(int firstIndexTouched, int lastIndexTouched, int turn);

    /**
     * Execute le pouvoir bleu sur la planet adverse en fonction du tour courant, du bublee touché
     * dans le ciel
     * @param indexTouched index du bubblee touché dans le ciel
     * @param turn le tour courant
     * @return faux si le bubblee touché est vide est vrai sinon
     */
    boolean applyBluePower(int indexTouched, int turn);

    /**
     * Execute le pouvoir bleu sur la planet adverse en fonction du tour courant, du bublee touché
     * par le joueur courant
     * @param indexTouched index du bubblee touché dans la planet du joueur courant
     * @param turn le tour courant
     * @return faux si le bubblee touché est vide est vrai sinon
     */
    boolean applyPurplePower(int indexTouched, int turn);

    /**
     * Execute le pouvoir jaune sur la plane courante en fonction du tour courant et le l'index du
     * bubblee touché
     * @param indexTouched index du bubblee touché dans la planet du joueur courant
     * @param turn le tour courant
     * @return retourne le bubblee gagné et null si il est vide
     */
    Bubblee applyYellowPower(int indexTouched, int turn);

    /**
     * @param turn le tour courant
     * @return vrai si on peut faire un echange sur la planet courante faux sinon
     */
    boolean isPlanetGotSwipable(int turn);

    /**
     * @param turn le tour courant
     * @return vrai si la planet est pleine faux sinon
     */
    boolean isPlanetFull(int turn);

    /**
     * @param indexBubbleeTouched index du bubblee touché
     * @param turn le tour courant
     * @return vrai si la colonne est pleine faux sinon
     */
    boolean isPlanetColumnFull(int indexBubbleeTouched, int turn);

    /**
     * @param indexBubbleeTouched index du bublee touché
     * @param turn le tour courant
     * @return vrai si le bubblee est recouvert et faux sinon
     */
    boolean isBubbleCovered(int indexBubbleeTouched, int turn);

    /**
     * Réinitialize le nombre de réduction effectué par le joueur courant en fonction du tour courant
     * @param turn le tour courant
     */
    void resetPlanetCountToReduce(int turn);

    /**
     * @param turn le tour courant
     * @return vrai si la planet est vide et faux sinon
     */
    boolean isPlanetEmpty(int turn);
}
