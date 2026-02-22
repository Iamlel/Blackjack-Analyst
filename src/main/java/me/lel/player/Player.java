package me.lel.player;

import me.lel.core.Action;
import me.lel.core.Rules;
import me.lel.player.strategy.Strategy;

public class Player {
    private double bankroll;
    private final Strategy strategy;

    public Player(double bankroll, Strategy strategy) {
        this.bankroll = bankroll;
        this.strategy = strategy;
    }

    public boolean insurance(double count) {
        return strategy.insurance(count);
    }

    public int[] placeBets(int minimumBet, int maximumBet, int maxHands) {
        if (this.has(minimumBet)) {
            return new int[]{minimumBet};
        } else {
            return new int[]{};
        }
    }

    public Action action(int handValue, int dealer, boolean activeAce, boolean canSplit, Rules rules, double trueCount) {
        return strategy.action(handValue, dealer, activeAce, canSplit, rules, trueCount);
    }

    public void give(double amount) {
        this.bankroll += amount;
    }

    public void take(double amount) {
        this.bankroll -= amount;
    }

    public boolean has(double amount) {
        return (bankroll >= amount);
    }

    public double getBankroll() {
        return bankroll;
    }
}
