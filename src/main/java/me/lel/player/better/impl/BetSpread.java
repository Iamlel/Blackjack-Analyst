package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class BetSpread implements Better {
    private final TreeMap<Double, Bet> betSpread;
    private final Double minimum;

    public BetSpread(TreeMap<Double, Bet> betSpread) {
        this.betSpread = betSpread;
        this.minimum = null;
    }

    public BetSpread(TreeMap<Double, Bet> betSpread, Double minimum) {
        this.betSpread = betSpread;
        this.minimum = minimum;
    }

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
