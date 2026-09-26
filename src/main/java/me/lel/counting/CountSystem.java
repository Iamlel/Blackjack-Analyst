package me.lel.counting;

import me.lel.core.Card;

/**
 * A card counting system, defined by the tag it gives each card.
 */
public interface CountSystem {
    /**
     * Returns how much {@code card} changes the running count.
     */
    int value(Card card);
}
