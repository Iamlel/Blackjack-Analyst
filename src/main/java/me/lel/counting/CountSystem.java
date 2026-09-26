package me.lel.counting;

import me.lel.core.Card;

/**
 * A card counting system. Counting keeps a running total of a value given to each card as it is dealt. In most systems
 * low cards count up and high cards count down, so a high count means the cards left favor the player.
 */
public interface CountSystem {
    /**
     * Returns how much {@code card} changes the running count.
     *
     * @param card the card just dealt
     * @return the card's count value
     */
    int value(Card card);
}
