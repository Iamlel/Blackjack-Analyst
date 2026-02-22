package me.lel.counting;

import me.lel.core.Card;

public class NoCountSystem implements CountSystem {
    @Override
    public int value(Card card) {
        return 0;
    }
}
