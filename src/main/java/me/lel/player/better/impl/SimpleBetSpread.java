package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

import java.util.List;

public class SimpleBetSpread implements Better {
    protected final List<Bet> betSpread;
    protected final Integer minimum;

    public SimpleBetSpread(List<Bet> betSpread) {
        this.betSpread = betSpread;
        this.minimum = null;
    }

    public SimpleBetSpread(List<Bet> betSpread, Integer minimum) {
        this.betSpread = betSpread;
        this.minimum = minimum;
    }

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
