package me.lel.counting.impl;

import me.lel.core.Card;
import me.lel.counting.CountSystem;

public class NoCountSystem implements CountSystem {
    @Override
    public int value(Card card) {
        return 0;
    }
}
