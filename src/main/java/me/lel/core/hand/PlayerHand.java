package me.lel.core.hand;

import me.lel.core.Card;

public class PlayerHand extends Hand {
    private final boolean beenSplit;
    private int bet;

    public PlayerHand(int bet, Card first, Card second) {
        this.beenSplit = false;
        this.bet = bet;
        super(first, second);
    }

    public PlayerHand(int bet, Card first, Card second, boolean split) {
        this.beenSplit = split;
        this.bet = bet;
        super(first, second);
    }

    public int bet() {
        return this.bet;
    }

    public void doubleBet() {
        this.bet *= 2;
    }

    public boolean canSplit() {
        return (super.isInitial() && super.getFirst() == super.getSecond());
    }

    public boolean hasBeenSplit() {
        return this.beenSplit;
    }

    @Override
    public boolean isBlackjack() {
        return (super.isBlackjack() && !this.hasBeenSplit());
    }
}
