package me.lel.core.hand;

import me.lel.core.Card;

import java.util.List;

public class PlayerHand extends Hand {
    private final boolean beenSplit;
    private int bet;

    public PlayerHand(int bet, List<Card> cards) {
        this.beenSplit = false;
        this.bet = bet;
        super(cards);
    }

    public PlayerHand(int bet, List<Card> cards, boolean split) {
        this.beenSplit = split;
        this.bet = bet;
        super(cards);
    }

    public int bet() {
        return this.bet;
    }

    public void doubleBet() {
        this.bet *= 2;
    }

    public boolean canSplit() {
        return (super.isInitial() && super.getCards().getFirst() == super.getCards().getLast());
    }

    public boolean hasBeenSplit() {
        return this.beenSplit;
    }
}
