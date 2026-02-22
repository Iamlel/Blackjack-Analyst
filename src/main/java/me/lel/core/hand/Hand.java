package me.lel.core.hand;

import me.lel.core.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards;
    private int runningHandTotal;
    private boolean soft;

    public Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        this.runningHandTotal = Hand.getHandValue(cards);

        if (this.cards.contains(Card.ACE)) {
            this.runningHandTotal += 10;
            this.soft = true;
        }
    }

    public int getHandValue() {
        return this.runningHandTotal;
    }

    public void addCard(Card card) {
        this.cards.add(card);
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
    }

    public boolean isSoft() {
        return soft;
    }

    public boolean isBlackjack() {
        return (this.isInitial() && this.runningHandTotal == 21);
    }

    public Card getFirst() {
        return cards.getFirst();
    }

    public boolean isInitial() {
        return (cards.size() == 2);
    }

    public static int getHandValue(List<Card> hand) {
        int handValue = 0;
        for (Card c : hand) {
            handValue += c.getValue();
        }
        return handValue;
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
