package me.lel.player.sidebet;

import me.lel.utils.Utils;

/**
 * When to take a side bet, as a true count threshold. A threshold of 0 is strict, so the count has to be above or
 * below 0, never exactly 0.
 *
 * @param count threshold true count, or {@code null} to always take the bet
 * @param above {@code true} to bet at or above {@code count}, {@code false} to bet at or below it
 */
public record SideBet(Integer count, boolean above) {
    /**
     * Returns whether to take the bet at true count {@code tc}.
     *
     * @param tc current true count
     * @return {@code true} to take the bet
     */
    public boolean valid(double tc) {
        return !Utils.pointComparison(tc, count, above);
    }
}
