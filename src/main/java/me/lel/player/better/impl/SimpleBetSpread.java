package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

import java.util.List;

/**
 * A bet spread stored as a list indexed by true count: entry {@code i} applies from a true count of {@code i} up to
 * {@code i + 1}. Counts below 0 use the first entry and counts past the end use the last. The player sits out at or
 * below an optional minimum count.
 */
public class SimpleBetSpread implements Better {
    protected final List<Bet> betSpread;
    protected final Integer minimum;

    /**
     * Creates a spread that never sits out.
     *
     * @param betSpread bets for true counts 0, 1, 2 and so on
     */
    public SimpleBetSpread(List<Bet> betSpread) {
        this.betSpread = betSpread;
        this.minimum = null;
    }

    /**
     * Creates a spread that sits out at or below a true count of {@code minimum}, or never if it is {@code null}.
     *
     * @param betSpread bets for true counts 0, 1, 2 and so on
     * @param minimum   true count at or below which the player sits out, or {@code null}
     */
    public SimpleBetSpread(List<Bet> betSpread, Integer minimum) {
        this.betSpread = betSpread;
        this.minimum = minimum;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the spread has no entries
     */
    @Override
    public Bet bet(double count) {
        if (betSpread.isEmpty()) {
            throw new IllegalStateException("There is no bet spread to use.");
        }

        if (minimum == null || count > minimum) {
            return betSpread.get((int) Math.min(betSpread.size()-1, Math.max(0, count)));
        }
        return null;
    }

    /**
     * Returns a sample spread of one hand from 1 to 8 units, rising with each true count from 0 to 5. It sits out at -3
     * or below.
     *
     * @return the sample spread
     */
    public static Better useSampleSpread() {
        return new SimpleBetSpread(List.of(
                new Bet(1, 1),
                new Bet(1, 2),
                new Bet(1, 3),
                new Bet(1, 4),
                new Bet(1, 6),
                new Bet(1, 8)
        ), -3);
    }
}
