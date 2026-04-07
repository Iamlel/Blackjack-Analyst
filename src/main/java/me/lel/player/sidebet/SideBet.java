package me.lel.player.sidebet;

import me.lel.utils.Utils;

public record SideBet(Integer count, boolean above) {
    public boolean valid(double tc) {
        return !Utils.pointComparison(tc, count, above);
    }
}
