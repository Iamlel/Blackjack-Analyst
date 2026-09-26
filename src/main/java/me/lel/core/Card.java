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
     * Returns the card's blackjack value, with an ace as 1 and face cards as 10. {@link me.lel.core.hand.Hand}
     * decides when an ace counts as 11.
     */
    public int getValue() {
        return this.value;
    }
}
