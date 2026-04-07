package me.lel.player;

import me.lel.core.ActiveRules;
import me.lel.core.action.Action;
import me.lel.player.better.Bet;
import me.lel.player.better.Better;
import me.lel.player.mover.Mover;
import me.lel.player.sidebet.SideBetMover;

import java.util.Arrays;

public class Player {
    private double bankroll;

    private final Mover mover;
    private final Better better;
    private final SideBetMover sideBet;

    public Player(double bankroll, Mover mover, Better better, SideBetMover sideBet) {
        this.bankroll = bankroll;
        this.mover = mover;
        this.better = better;
        this.sideBet = sideBet;
    }

    public int[] placeBets(int minimumBet, int maximumBet, int maxHands, double trueCount) {
        Bet bet = better.bet(trueCount);
        if (bet == null) {
            return new int[]{};
        }

        int[] hands = new int[Math.min(maxHands, bet.Hands())];
        int betValue = Math.min(maximumBet, bet.Units() * minimumBet);
        Arrays.fill(hands, betValue);
        return hands;
    }

    public Action action(int handValue, int dealer, boolean soft, ActiveRules rules, double trueCount) {
        return mover.action(handValue, dealer, soft, rules, trueCount);
    }

    public boolean earlySurrender(int handValue, int dealer, boolean soft, double trueCount) {
        return mover.earlySurrender(handValue, dealer, soft, trueCount);
    }

    public boolean insurance(double count) {
        return sideBet.valid("insurance", count);
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

    @Override
    public Player clone() {
        return new Player(bankroll, mover, better, sideBet);
    }
}
