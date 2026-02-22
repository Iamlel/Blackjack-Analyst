package me.lel.core;

// ORDER MATTERS
public enum Card {
    ACE(4),
    TWO(4),
    THREE(4),
    FOUR(4),
    FIVE(4),
    SIX(4),
    SEVEN(4),
    EIGHT(4),
    NINE(4),
    TEN(16);

    private final int amount;

    Card(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public int getValue() {
        return this.ordinal() + 1;
    }
}
