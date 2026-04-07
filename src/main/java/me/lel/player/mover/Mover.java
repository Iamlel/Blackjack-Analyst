package me.lel.player.mover;

import me.lel.core.ActiveRules;
import me.lel.core.action.Action;

public interface Mover {
    Action action(int hand, int dealerHand, boolean soft, ActiveRules rules, double trueCount);
    boolean earlySurrender(int hand, int dealerHand, boolean soft, double trueCount);
}
