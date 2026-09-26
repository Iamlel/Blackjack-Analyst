package me.lel.player;

import me.lel.core.ActiveRules;
import me.lel.core.action.Action;
import me.lel.player.better.Bet;
import me.lel.player.better.Better;
import me.lel.player.mover.Mover;
import me.lel.player.sidebet.SideBetMover;

import java.util.Arrays;

/**
 * A player at the table: a bankroll plus three strategies. The {@link Better} sizes bets from the true count, the
 * {@link Mover} plays each hand, and the {@link SideBetMover} decides on side bets such as insurance.
 */
public class Player {
    private final double startingBankroll;
    private double bankroll;

    private final Mover mover;
    private final Better better;
    private final SideBetMover sideBet;

    public Player(double bankroll, Mover mover, Better better, SideBetMover sideBet) {
        this.startingBankroll = bankroll;
        this.bankroll = bankroll;
        this.mover = mover;
        this.better = better;
        this.sideBet = sideBet;
    }

    /**
     * Returns the bet for each hand the player wants this round, or an empty array to sit it out. Every hand gets
     * the {@link Better}'s units times the table minimum, capped at the table maximum.
     *
     * @param minimumBet table minimum, which is also the betting unit
     * @param maximumBet largest bet allowed on one hand
     * @param maxHands   most hands the player may play
     * @param trueCount  current true count
     * @return one bet per hand, at most {@code maxHands} of them
     */
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

    /**
     * Returns this player's move. See {@link Mover#action}.
     */
    public Action action(int handValue, int dealer, boolean soft, ActiveRules rules, double trueCount) {
        return mover.action(handValue, dealer, soft, rules, trueCount);
    }

    /**
     * Returns whether this player surrenders the hand early. See {@link Mover#earlySurrender}.
     */
    public boolean earlySurrender(int handValue, int dealer, boolean soft, double trueCount) {
        return mover.earlySurrender(handValue, dealer, soft, trueCount);
    }

    /**
     * Returns whether the player takes insurance at this true count, going by the {@code "insurance"} entry of their
     * {@link SideBetMover}.
     */
    public boolean insurance(double count) {
        return sideBet.valid("insurance", count);
    }

    public void give(double amount) {
        this.bankroll += amount;
    }

    public void take(double amount) {
        this.bankroll -= amount;
    }

    public void resetBankroll() {
        this.bankroll = startingBankroll;
    }

    public boolean has(double amount) {
        return (bankroll >= amount);
    }

    /**
     * Returns whether the player can no longer cover the table minimum. A dead player is dealt out of every round.
     */
    public boolean isDead(int minimumBet) {
        return !has(minimumBet);
    }

    public double getBankroll() {
        return bankroll;
    }

    /**
     * Returns a new player whose starting bankroll is this player's current bankroll. The copy shares this player's
     * {@link Mover}, {@link Better} and {@link SideBetMover}.
     */
    @Override
    public Player clone() {
        return new Player(bankroll, mover, better, sideBet);
    }
}
