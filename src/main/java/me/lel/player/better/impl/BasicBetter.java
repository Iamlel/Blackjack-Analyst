package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

/**
 * Flat bets one hand of one unit every round, whatever the count.
 */
public class BasicBetter implements Better {
    @Override
    public Bet bet(double count) {
        return new Bet(1, 1);
    }
}
