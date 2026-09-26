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

    /**
     * Creates a player.
     *
     * @param bankroll starting bankroll, which {@link #resetBankroll()} returns to
     * @param mover    plays each hand
     * @param better   sizes each round's bets
     * @param sideBet  decides on side bets
     */
    public Player(double bankroll, Mover mover, Better better, SideBetMover sideBet) {
        this.startingBankroll = bankroll;
        this.bankroll = bankroll;
        this.mover = mover;
        this.better = better;
        this.sideBet = sideBet;
    }

    /**
     * Returns the bet for each hand the player wants this round, or an empty array to sit it out. Every hand gets the
     * {@link Better}'s units times the table minimum, capped at the table maximum.
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
     *
     * @param handValue the hand's total
     * @param dealer    the dealer's up card value, with an ace as 1
     * @param soft      whether an ace in the hand counts as 11
     * @param rules     what the hand is allowed to do right now
     * @param trueCount current true count
     * @return the move to make
     */
    public Action action(int handValue, int dealer, boolean soft, ActiveRules rules, double trueCount) {
        return mover.action(handValue, dealer, soft, rules, trueCount);
    }

    /**
     * Returns whether this player surrenders the hand early. See {@link Mover#earlySurrender}.
     *
     * @param handValue the hand's total
     * @param dealer    the dealer's up card value, with an ace as 1
     * @param soft      whether an ace in the hand counts as 11
     * @param trueCount current true count
     * @return {@code true} to surrender
     */
    public boolean earlySurrender(int handValue, int dealer, boolean soft, double trueCount) {
        return mover.earlySurrender(handValue, dealer, soft, trueCount);
    }

    /**
     * Returns whether the player takes insurance at this true count, going by the {@code "insurance"} entry of their
     * {@link SideBetMover}.
     *
     * @param count current true count
     * @return {@code true} to take insurance
     */
    public boolean insurance(double count) {
        return sideBet.valid("insurance", count);
    }

    /**
     * Adds {@code amount} to the bankroll.
     *
     * @param amount the amount won
     */
    public void give(double amount) {
        this.bankroll += amount;
    }

    /**
     * Subtracts {@code amount} from the bankroll.
     *
     * @param amount the amount lost
     */
    public void take(double amount) {
        this.bankroll -= amount;
    }

    /**
     * Sets the bankroll back to the starting bankroll.
     */
    public void resetBankroll() {
        this.bankroll = startingBankroll;
    }

    /**
     * Returns whether the bankroll covers {@code amount}.
     *
     * @param amount the amount needed
     * @return {@code true} if the bankroll is at least {@code amount}
     */
    public boolean has(double amount) {
        return (bankroll >= amount);
    }

    /**
     * Returns whether the player can no longer cover the table minimum.
     *
     * @param minimumBet the table minimum
     * @return {@code true} if the bankroll is below the minimum
     */
    public boolean isDead(int minimumBet) {
        return !has(minimumBet);
    }

    /**
     * Returns the player's current bankroll.
     *
     * @return the bankroll in dollars
     */
    public double getBankroll() {
        return bankroll;
    }

    /**
     * Returns the strategy that plays this player's hands.
     *
     * @return the mover
     */
    protected Mover getMover() {
        return mover;
    }

    /**
     * Returns the strategy that sizes this player's bets.
     *
     * @return the better
     */
    protected Better getBetter() {
        return better;
    }

    /**
     * Returns the strategy that decides this player's side bets.
     *
     * @return the side bet mover
     */
    protected SideBetMover getSideBetMover() {
        return sideBet;
    }

    /**
     * Returns a new player whose starting bankroll is this player's current bankroll. The copy shares this player's
     * {@link Mover}, {@link Better} and {@link SideBetMover}.
     *
     * @return the copy
     */
    @Override
    public Player clone() {
        return new Player(bankroll, mover, better, sideBet);
    }
}
