package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * A bet spread, which sets how much to bet at each true count. Each entry applies from its count up to the next
 * entry's, and counts below the lowest entry use the lowest entry. The player can also sit out below a minimum count,
 * which counters call wonging out.
 */
public class BetSpread implements Better {
    private final TreeMap<Double, Bet> betSpread;
    private final Double minimum;

    /**
     * Creates a spread that never sits out.
     *
     * @param betSpread bets keyed by the true count they start at
     */
    public BetSpread(TreeMap<Double, Bet> betSpread) {
        this.betSpread = betSpread;
        this.minimum = null;
    }

    /**
     * Creates a spread that sits out below a true count of {@code minimum}, or never if it is {@code null}.
     *
     * @param betSpread bets keyed by the true count they start at
     * @param minimum   true count below which the player sits out, or {@code null}
     */
    public BetSpread(TreeMap<Double, Bet> betSpread, Double minimum) {
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

        if (minimum == null || count >= minimum) {
            if (count <= betSpread.firstKey()) {
                return betSpread.firstEntry().getValue();
            }

            return betSpread.floorEntry(count).getValue();
        }
        return null;
    }

    /**
     * Returns a sample spread of one hand from 1 to 8 units, rising with each true count from 0 to 5, that sits out
     * below a true count of -3.
     *
     * @return the sample spread
     */
    public static Better useSampleSpread() {
        TreeMap<Double, Bet> betSpread = new TreeMap<>();

        betSpread.put(0.0, new Bet(1, 1));
        betSpread.put(1.0, new Bet(1, 2));
        betSpread.put(2.0, new Bet(1, 3));
        betSpread.put(3.0, new Bet(1, 4));
        betSpread.put(4.0, new Bet(1, 6));
        betSpread.put(5.0, new Bet(1, 8));

        return new BetSpread(betSpread, -3.0);
    }

    /**
     * Reads a spread from CSV, such as the bundled {@code samplebet.csv}. The first line is a header and is skipped.
     * The first column of the second line is the minimum count, below which the player sits out. Every line after that
     * is {@code trueCount,hands,units}. The reader is left open.
     *
     * @param br the CSV to read
     * @return the spread
     * @throws IOException if reading fails
     */
    public static BetSpread load(BufferedReader br) throws IOException {
        TreeMap<Double, Bet> betSpread = new TreeMap<>();

        br.readLine();
        Double minimum = getMinimum(br.readLine());

        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            
            betSpread.put(Double.valueOf(parts[0]), new Bet(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
        return new BetSpread(betSpread, minimum);
    }

    private static Double getMinimum(String min) {
        int index = min.indexOf(",");
        if (index == -1) {
            return Double.valueOf(min);
        }
        return Double.valueOf(min.substring(0, index));
    }
}
