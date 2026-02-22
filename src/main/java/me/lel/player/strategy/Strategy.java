package me.lel.player.strategy;

import me.lel.core.Action;
import me.lel.core.Rules;

public interface Strategy {
    Action action(int hand, int dealerHand, boolean soft, boolean canSplit, Rules rules, double count);
    boolean insurance(double count);
}
