package me.lel.core;

import me.lel.counting.CountSystem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Deck {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private Map<Card, Integer> deck;
    private final int decks;
    private int cardCount;
    private final int lastCard;

    private final CountSystem countSystem;
    private int runningCount;

    public Deck(int decks, CountSystem countSystem, double penetration) {
        this.decks = decks;
        this.countSystem = countSystem;
        this.lastCard = (int) (52 * decks * (1 - penetration));
    }

    public Card takeCard() {
        if (cardCount == 0) {
            shuffleDeck();
        }

        Card randomCard = this.randomCard();
        cardCount -= 1;
        runningCount += countSystem.value(randomCard);
        this.deck.put(randomCard, this.deck.get(randomCard) - 1);
        return randomCard;
    }

    private Card randomCard() {
        int r = this.random.nextInt(this.cardCount);
        for (Map.Entry<Card, Integer> entry : this.deck.entrySet()) {
            r -= entry.getValue();
            if (r < 0) {;
                return entry.getKey();
            }
        }
        // No cards are left
        return null;
    }

    // make sure to finish current hand before shuffling unless you absolutely have to
    public void shuffleDeck() {
        this.deck = new EnumMap<>(Card.class);
        for (Card e : Card.values()) {
            this.deck.put(e, e.getAmount() * decks);
        }
        this.cardCount = 52 * decks;
        this.runningCount = 0;
    }

    public boolean isShuffleNecessary() {
        return (cardCount <= lastCard);
    }

    public int getRunningCount() {
        return this.runningCount;
    }

    public double getTrueCount() {
        return (double) runningCount / (cardCount / 52.0);
    }
}
