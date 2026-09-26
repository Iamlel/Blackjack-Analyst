package me.lel.core;

import me.lel.counting.CountSystem;
import umontreal.ssj.rng.RandomStream;

import java.util.*;

public class Deck {
    private final Card[] deck;
    private int topIndex;
    private final int lastCard;

    private final CountSystem countSystem;
    private int runningCount;

    public Deck(int decks, CountSystem countSystem, double penetration) {
        this.countSystem = countSystem;
        this.lastCard = (int) (52 * decks * (1 - penetration));

        this.deck = new Card[52 * decks];
        this.reset();
    }

    public Card takeCard(RandomStream stream) {
        if (topIndex == deck.length) {
            shuffleDeck(stream);
        }

        Card randomCard = deck[topIndex++];
        runningCount += countSystem.value(randomCard);
        return randomCard;
    }

    // make sure to finish current hand before shuffling unless you absolutely have to
    public void shuffleDeck(RandomStream stream) {
        for (int i = deck.length - 1; i > 0; i--) {
            int j = stream.nextInt(0, i); // 0 <= j <= i
            // Swap deck[i] and deck[j]
            Card temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
        this.topIndex = 0;
        this.runningCount = 0;
    }

    // incredibly important for ssj random to work properly
    public final void reset() {
        for (int d = 0; d < deck.length / 13; d++) {
            System.arraycopy(Card.values(), 0, this.deck, d * 13, 13);
        }

        this.topIndex = this.deck.length;
        this.runningCount = 0;
    }

    public boolean isShuffleNecessary() {
        return (this.deck.length - this.topIndex <= this.lastCard);
    }

    public int getRunningCount() {
        return this.runningCount;
    }

    public double getTrueCount() {
        return (double) this.runningCount / ((this.deck.length - this.topIndex) / 52.0);
    }
}
