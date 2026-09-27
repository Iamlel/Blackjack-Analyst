package me.lel.counting.impl;

import me.lel.core.Card;
import me.lel.counting.CountSystem;

/**
 * The Hi-Lo count, the most widely used counting system. Cards 2 through 6 count +1, 7 through 9 count 0, and tens and
 * aces count -1.
 */
public final class HiLoCountSystem implements CountSystem {
    public int value(Card card) {
        if (card.getValue() == 1 || card.getValue() == 10) {
            return -1;
        } else if (card.getValue() <= 6) {
            return 1;
        }
        return 0;
    }
}