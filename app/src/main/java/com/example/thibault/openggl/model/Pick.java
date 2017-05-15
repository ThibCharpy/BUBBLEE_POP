package com.example.thibault.openggl.model;

import com.example.thibault.openggl.model.Bubblee;
import com.example.thibault.openggl.model.Zone;
import com.example.thibault.openggl.model.exceptions.NoMoreBubbleeForColorException;
import com.example.thibault.openggl.model.utils.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by thibault on 27/04/17.
 */

public class Pick {

    private static final int NUMBER_OF_BUBBLEE = 48;
    private List<Bubblee> bubblees;

    public Pick() {
        this.bubblees = new ArrayList<>();
    }

    /**
     * Initialise la pioche en placant nbBubbleePerColor de chaque couleur dedans
     */
    public void Initialize(){
        int nbBubbleePerColor = NUMBER_OF_BUBBLEE /(Color.values().length - 1) ;
        Bubblee bubblee;
        for (int i = 0; i < Color.values().length - 1; i++){
            for (int j=0; j < nbBubbleePerColor; j++) {
                bubblee = new Bubblee();
                switch (i) {
                    case 0:
                        bubblee.setColor(Color.BLACK); break;
                    case 1:
                        bubblee.setColor(Color.RED); break;
                    case 2:
                        bubblee.setColor(Color.GREEN); break;
                    case 3:
                        bubblee.setColor(Color.BLUE); break;
                    case 4:
                        bubblee.setColor(Color.PURPLE); break;
                    case 5:
                        bubblee.setColor(Color.YELLOW); break;
                }
                this.bubblees.add(bubblee);
            }
        }
    }

    /**
     * Choisis une couleur precise de la pioche utilisé seulement pour l'initialisation
     * @param color couleur souhaité
     * @return bubblee de la couleur souhaité
     */
    public Bubblee pickColor(Color color){
        Iterator<Bubblee> iterator = this.bubblees.iterator();
        while (iterator.hasNext()){
            Bubblee b = iterator.next();
            if (b.getColor().equals(color.toString())){
                iterator.remove();
                return b;
            }
        }
        return null;
    }

    /**
     * Choisis un bubblee aleatoire dans la pioche
     * @return le bubblee choisis aléatoirement
     * @throws NoMoreBubbleeForColorException si la pioche est vide
     */
    public Bubblee onPick() throws NoMoreBubbleeForColorException {
        if (this.bubblees.isEmpty())
            throw new NoMoreBubbleeForColorException();
        else {
            Random r = new Random();
            int randomNum = r.nextInt((bubblees.size() - 1) + 1) + 1;
            return this.bubblees.remove(randomNum-1);
        }
    }

    /**
     * Enleve les billes noirs de la pioche
     */
    public void throwBlackBubblees(){
        Iterator<Bubblee> iterator = this.bubblees.iterator();
        while (iterator.hasNext()){
            Bubblee b = iterator.next();
            if (b.getColor().equals(Color.BLACK.toString())){
                iterator.remove();
            }
        }
    }

    public boolean isEmpty(){
        return this.bubblees.isEmpty();
    }

    @Override
    public String toString() {
        return "Pick{" +
                "bubblees=" + bubblees.toString() +
                '}';
    }
}
