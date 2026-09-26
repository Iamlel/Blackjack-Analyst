package me.lel.core.hand;

import me.lel.core.Card;

/**
 * A player's hand and the amount bet on it.
 */
public class PlayerHand extends Hand {
    private final boolean beenSplit;
    private int bet;

    /**
     * Creates a hand dealt at the start of a round, with {@code bet} on it.
     */
    public PlayerHand(int bet, Card first, Card second) {
        this.beenSplit = false;
        this.bet = bet;
        super(first, second);
    }

    /**
     * Creates a hand with {@code bet} on it. Set {@code split} if the hand came from splitting a pair.
     */
    public PlayerHand(int bet, Card first, Card second, boolean split) {
        this.beenSplit = split;
        this.bet = bet;
        super(first, second);
    }

    /**
     * Returns the amount bet on the hand, including any double.
     */
    public int bet() {
        return this.bet;
    }

    public void doubleBet() {
        this.bet *= 2;
    }

    /**
     * Returns whether the hand is a two-card pair. The cards must share a rank, so a jack and a king are not a pair
     * even though both are worth 10.
     */
    public boolean canSplit() {
        return (super.isInitial() && super.getFirst() == super.getSecond());
    }

    /**
     * Returns whether the hand came from splitting a pair.
     */
    public boolean hasBeenSplit() {
        return this.beenSplit;
    }

    /**
     * {@inheritDoc} A hand that came from a split never counts as blackjack.
     */
    @Override
    public boolean isBlackjack() {
        return (super.isBlackjack() && !this.hasBeenSplit());
    }
}
