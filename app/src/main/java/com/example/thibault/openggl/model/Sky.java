package com.example.thibault.openggl.model;

import com.example.thibault.openggl.model.utils.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by thibault on 27/04/17.
 */

public class Sky extends Zone{

    private static final int NUMBER_OF_BUBBLEE_IN_SKY = 10;

    public Sky() {
        super(NUMBER_OF_BUBBLEE_IN_SKY);
    }

    /**
     * initialise le ciel en faisanr des tirages aléatoire sur la pioche et en vérifiant que deux
     * bubblee de même couleurs ne sont pas l'une à coté de l'autre
     * @param pick la pioche
     */
    @Override
    public void Initialize(Pick pick) {
        List<Bubblee> bubbleePicked = new ArrayList<>();
        Bubblee bubblee = null;
        // -2  car pas noir ni empty
        for (int i = 1; i < (Color.values().length - 1); i++) {
            for (int j = 0; j < (nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE); j++) {
                switch (i) {
                    case 1:
                        bubblee = pick.pickColor(Color.RED);
                        break;
                    case 2:
                        bubblee = pick.pickColor(Color.GREEN);
                        break;
                    case 3:
                        bubblee = pick.pickColor(Color.BLUE);
                        break;
                    case 4:
                        bubblee = pick.pickColor(Color.PURPLE);
                        break;
                    case 5:
                        bubblee = pick.pickColor(Color.YELLOW);
                        break;
                }
                bubbleePicked.add(bubblee);
            }
        }
        int random_x;
        int random_y;
        Bubblee bubbleePick;
        for (Bubblee aBubbleePicked : bubbleePicked) {
            bubbleePick = aBubbleePicked;
            boolean match = false;
            do {
                Random r1 = new Random();
                Random r2 = new Random();
                random_x = r1.nextInt((NUMBER_OF_BUBBLEE_PER_LINE - 1) + 1);
                random_y = r2.nextInt((nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE - 1) + 1);
                boolean wrongIndex = false;
                if (this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + random_x].getColor().equals(Color.EMPTY.toString())) {
                    if (random_x > 0) {
                        if (random_x < (NUMBER_OF_BUBBLEE_PER_LINE - 1)) {
                            if (null != this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x - 1)] &&
                                    null != this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x + 1)])
                                wrongIndex = (this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x - 1)].getColor().equals(bubbleePick.getColor()) ||
                                        this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x + 1)].getColor().equals(bubbleePick.getColor()));
                        } else {
                            if (null != this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x - 1)])
                                wrongIndex = this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x - 1)].getColor().equals(bubbleePick.getColor());
                        }
                    } else {
                        if (null != this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x + 1)])
                            wrongIndex = this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + (random_x + 1)].getColor().equals(bubbleePick.getColor());
                    }
                    if (!wrongIndex) {
                        if (random_y > 0) {
                            if (random_y < ((nbBubblee / NUMBER_OF_BUBBLEE_PER_LINE) - 1)) {
                                if (null != this.bubblees[(random_y - 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x] &&
                                        null != this.bubblees[(random_y + 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x])
                                    wrongIndex = (this.bubblees[(random_y - 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x].getColor().equals(bubbleePick.getColor()) ||
                                            this.bubblees[(random_y + 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x].getColor().equals(bubbleePick.getColor()));
                            } else {
                                if (null != this.bubblees[(random_y - 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x])
                                    wrongIndex = this.bubblees[(random_y - 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x].getColor().equals(bubbleePick.getColor());
                            }
                        } else {
                            if (null != this.bubblees[(random_y + 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x])
                                wrongIndex = this.bubblees[(random_y + 1) * NUMBER_OF_BUBBLEE_PER_LINE + random_x].getColor().equals(bubbleePick.getColor());
                        }
                    }
                } else {
                    wrongIndex = true;
                }
                if (!wrongIndex) {
                    this.bubblees[random_y * NUMBER_OF_BUBBLEE_PER_LINE + random_x] = bubbleePick;
                    match = true;
                }
            } while (!match);

        }
    }

    @Override
    public String toString() {
        return "Sky : " +  super.toString();
    }

    public static int getNumberOfBubbleeInSky() {
        return NUMBER_OF_BUBBLEE_IN_SKY;
    }
}
