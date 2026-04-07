package me.lel.player.mover.impl.datadrivenmover;

import me.lel.core.action.SimpleAction;
import me.lel.utils.Utils;

public record MoverAction(SimpleAction action, Integer count, boolean above) {

    public boolean invalid(double tc) {
        return Utils.pointComparison(tc, count, above);
    }
}
