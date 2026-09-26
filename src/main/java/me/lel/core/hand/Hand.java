package me.lel.core.hand;

import me.lel.core.Card;

/**
 * A blackjack hand. It keeps the first two cards and a running total, and an ace counts as 11 whenever that doesn't
 * bust the hand.
 */
public class Hand {
    private int runningHandTotal = 0;

    private final Card first;
    private final Card second;

    private boolean initial;
    private boolean soft;

    public Hand(Card first, Card second) {
        this.first = first;
        this.second = second;

        this.addCard(first);
        this.addCard(second);

        this.initial = true;
    }

    public int getHandValue() {
        return this.runningHandTotal;
    }

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
     * Returns whether an ace in the hand is currently counted as 11.
     */
    public boolean isSoft() {
        return this.soft;
    }

    /**
     * Returns whether the hand is 21 on its first two cards.
     */
    public boolean isBlackjack() {
        return (this.initial && this.runningHandTotal == 21);
    }

    public Card getFirst() {
        return this.first;
    }

    public Card getSecond() {
        return this.second;
    }

    /**
     * Returns whether the hand still has only its first two cards.
     */
    public boolean isInitial() {
        return this.initial;
    }
}
