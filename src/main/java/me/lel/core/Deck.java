package me.lel.core;

import me.lel.counting.CountSystem;
import umontreal.ssj.rng.RandomStream;

import java.util.*;

/**
 * A shoe of one or more 52-card decks that keeps a running count as cards are dealt.
 * <p>
 * All randomness comes from the SSJ {@link RandomStream} passed in to deal and shuffle, so the same stream and the
 * same starting order always produce the same cards.
 */
public class Deck {
    private final Card[] deck;
    private int topIndex;
    private final int lastCard;

    private final CountSystem countSystem;
    private int runningCount;

    /**
     * Creates a shoe in its unshuffled starting order. It counts as empty, so it is shuffled before the first card is
     * dealt.
     *
     * @param decks       number of 52-card decks in the shoe
     * @param countSystem system used for the running count
     * @param penetration fraction of the shoe dealt before {@link #isShuffleNecessary()} asks for a shuffle
     */
    public Deck(int decks, CountSystem countSystem, double penetration) {
        this.countSystem = countSystem;
        this.lastCard = (int) (52 * decks * (1 - penetration));

        this.deck = new Card[52 * decks];
        this.reset();
    }

    /**
     * Deals the next card and adds it to the running count. A shoe that has run out is reshuffled first, even in the
     * middle of a round.
     *
     * @param stream random stream used if a reshuffle is needed
     * @return the card dealt
     */
    public Card takeCard(RandomStream stream) {
        if (topIndex == deck.length) {
            shuffleDeck(stream);
        }

        Card randomCard = deck[topIndex++];
        runningCount += countSystem.value(randomCard);
        return randomCard;
    }

    /**
     * Shuffles every card back into the shoe and resets the running count.
     * <p>
     * Call it between rounds when possible. Shuffling mid-round also puts the cards still on the table back into
     * the shoe, so they can be dealt again.
     *
     * @param stream random stream the shuffle draws from
     */
    public void shuffleDeck(RandomStream stream) {
        for (int i = deck.length - 1; i > 0; i--) {
            int j = stream.nextInt(0, i); // 0 <= j <= i
            Card temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
        this.topIndex = 0;
        this.runningCount = 0;
    }

    /**
     * Returns every card to its unshuffled starting order and resets the running count. Afterwards the shoe counts
     * as empty and is shuffled before the next card is dealt.
     * <p>
     * A shuffle rearranges whatever order the shoe is already in, so replaying a simulation needs this as well as a
     * reset of the random stream.
     */
    public final void reset() {
        for (int d = 0; d < deck.length / 13; d++) {
            System.arraycopy(Card.values(), 0, this.deck, d * 13, 13);
        }

        this.topIndex = this.deck.length;
        this.runningCount = 0;
    }

    /**
     * Returns whether the shoe has been dealt past its penetration and should be shuffled before the next round.
     */
    public boolean isShuffleNecessary() {
        return (this.deck.length - this.topIndex <= this.lastCard);
    }

    public int getRunningCount() {
        return this.runningCount;
    }

    /**
     * Returns the running count divided by the decks left in the shoe. Partial decks count as fractions, with no
     * rounding.
     */
    public double getTrueCount() {
        return (double) this.runningCount / ((this.deck.length - this.topIndex) / 52.0);
    }
}
