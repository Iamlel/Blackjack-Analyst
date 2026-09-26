package me.lel.core.hand;

import me.lel.core.Card;

/**
 * A player's hand and the amount bet on it.
 */
public class PlayerHand extends Hand {
    private final boolean beenSplit;
    private int bet;

    /**
     * Creates a hand dealt at the start of a round.
     *
     * @param bet    amount bet on the hand
     * @param first  the first card
     * @param second the second card
     */
    public PlayerHand(int bet, Card first, Card second) {
        this.beenSplit = false;
        this.bet = bet;
        super(first, second);
    }

    /**
     * Creates a hand, noting whether it came from splitting a pair.
     *
     * @param bet    amount bet on the hand
     * @param first  the first card
     * @param second the second card
     * @param split  whether the hand came from splitting a pair
     */
    public PlayerHand(int bet, Card first, Card second, boolean split) {
        this.beenSplit = split;
        this.bet = bet;
        super(first, second);
    }

    /**
     * Returns the amount bet on the hand, including any double.
     *
     * @return the bet
     */
    public int bet() {
        return this.bet;
    }

    /**
     * Doubles the amount bet on the hand.
     */
    public void doubleBet() {
        this.bet *= 2;
    }

    /**
     * Returns whether the hand is a two-card pair. The cards must share a rank, so a jack and a king are not a pair
     * even though both are worth 10.
     *
     * @return {@code true} if the hand is a pair
     */
    public boolean canSplit() {
        return (super.isInitial() && super.getFirst() == super.getSecond());
    }

    /**
     * Returns whether the hand came from splitting a pair.
     *
     * @return {@code true} if the hand came from a split
     */
    public boolean hasBeenSplit() {
        return this.beenSplit;
    }

    /**
     * {@inheritDoc} A hand that came from a split never counts as blackjack; an ace and a ten there is just 21.
     */
    @Override
    public boolean isBlackjack() {
        return (super.isBlackjack() && !this.hasBeenSplit());
    }
}
