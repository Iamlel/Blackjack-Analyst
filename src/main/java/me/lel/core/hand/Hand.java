package me.lel.core.hand;

import me.lel.core.Card;

/**
 * A blackjack hand. It keeps the first two cards and a running total.
 */
public class Hand {
    private final Card first;
    private final Card second;

    private int runningHandTotal = 0;

    private boolean initial;
    private boolean soft;

    /**
     * Creates a hand from its first two cards.
     *
     * @param first  the first card
     * @param second the second card
     */
    public Hand(Card first, Card second) {
        this.first = first;
        this.second = second;

        this.addCard(first);
        this.addCard(second);

        this.initial = true;
    }

    /**
     * Returns the hand's total.
     *
     * @return the total, counting an ace as 11 when that doesn't bust the hand
     */
    public int getHandValue() {
        return this.runningHandTotal;
    }

    /**
     * Adds a card to the hand. Afterward the hand is no longer {@linkplain #isInitial() initial}.
     *
     * @param card the card to add
     */
    public void addCard(Card card) {
        this.runningHandTotal += card.getValue();

        if (soft) {
            if (this.runningHandTotal > 21) {
                this.runningHandTotal -= 10;
                this.soft = false;
            }
        } else if (card == Card.ACE && runningHandTotal <= 11) {
            this.runningHandTotal += 10;
            this.soft = true;
        }

        if (initial) {
            this.initial = false;
        }
    }

    /**
     * Returns whether the hand is soft, meaning an ace in it is currently counted as 11. A soft hand can't bust on the
     * next card, because the ace can drop back to 1.
     *
     * @return {@code true} if the hand is soft
     */
    public boolean isSoft() {
        return this.soft;
    }

    /**
     * Returns whether the hand is a blackjack: 21 on its first two cards, which takes an ace and a ten-value card.
     *
     * @return {@code true} if the hand is a blackjack
     */
    public boolean isBlackjack() {
        return (this.initial && this.runningHandTotal == 21);
    }

    /**
     * Returns the first card dealt to the hand.
     *
     * @return the first card
     */
    public Card getFirst() {
        return this.first;
    }

    /**
     * Returns the second card dealt to the hand.
     *
     * @return the second card
     */
    public Card getSecond() {
        return this.second;
    }

    /**
     * Returns whether the hand still has only its first two cards.
     *
     * @return {@code true} if no cards have been added
     */
    public boolean isInitial() {
        return this.initial;
    }
}
