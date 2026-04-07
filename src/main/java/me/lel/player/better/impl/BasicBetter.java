package me.lel.player.better.impl;

import me.lel.player.better.Bet;
import me.lel.player.better.Better;

public class BasicBetter implements Better {
    @Override
    public Bet bet(double count) {
        return new Bet(1, 1);
    }
}
