package me.lel.core;

/**
 * A card rank. There are no suits because nothing in the simulator depends on them.
 */
public enum Card {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(10),
    QUEEN(10),
    KING(10);

    private final int value;

    Card(int value) {
        this.value = value;
    }

    /**
     * Returns the card's blackjack value. Number cards are worth their number, jacks, queens and kings are worth 10,
     * and an ace is worth 1 here. An ace can also count as 11, which {@link me.lel.core.hand.Hand} takes care of.
     *
     * @return the card's value, from 1 to 10
     */
    public int getValue() {
        return this.value;
    }
}
