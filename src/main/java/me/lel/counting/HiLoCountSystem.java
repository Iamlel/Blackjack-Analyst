package me.lel.counting;

import me.lel.core.Card;
import me.lel.core.hand.Hand;

public class HiLoCountSystem implements CountSystem {
    public int value(Card card) {
        if (card.getValue() == 1 || card.getValue() == 10) {
            return -1;
        } else if (card.getValue() < 7) {
            return 1;
        }
        return 0;
    }
}
