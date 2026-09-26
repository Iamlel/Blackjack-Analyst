package me.lel.counting.impl;

import me.lel.core.Card;
import me.lel.counting.CountSystem;

/**
 * Counts every card as 0, for players who don't count cards. The true count stays at 0.
 */
public class NoCountSystem implements CountSystem {
    @Override
    public int value(Card card) {
        return 0;
    }
}
