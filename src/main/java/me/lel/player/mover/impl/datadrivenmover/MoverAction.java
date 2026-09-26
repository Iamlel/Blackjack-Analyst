package me.lel.player.mover.impl.datadrivenmover;

import me.lel.core.action.SimpleAction;
import me.lel.utils.Utils;

/**
 * One move from a {@link DataDrivenMover} cell, with the count condition it needs.
 *
 * @param action the move
 * @param count  the true count threshold, or {@code null} if the move always applies
 * @param above  {@code true} if the move applies at or above {@code count}, {@code false} if at or below it
 */
public record MoverAction(SimpleAction action, Integer count, boolean above) {

    /**
     * Returns whether the count condition fails at true count {@code tc}. See {@link Utils#pointComparison}.
     */
    public boolean invalid(double tc) {
        return Utils.pointComparison(tc, count, above);
    }
}
