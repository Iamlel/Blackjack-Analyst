package me.lel.counting.impl;

import me.lel.core.Card;
import me.lel.counting.CountSystem;

public class HiLoCountSystem implements CountSystem {
    public int value(Card card) {
        if (card.getValue() == 1 || card.getValue() == 10) {
            return -1;
        } else if (card.getValue() <= 6) {
            return 1;
        }
        return 0;
    }
}