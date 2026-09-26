package me.lel.player.mover;

import me.lel.core.ActiveRules;
import me.lel.core.action.Action;

/**
 * Plays a player's hands: picks each move and decides on early surrender.
 */
public interface Mover {
    /**
     * Picks the next move for a hand.
     * <p>
     * The hand is described only by its total, so a pair is recognized through {@link ActiveRules#canSplit()}.
     *
     * @param hand       the hand's total
     * @param dealerHand the dealer's up card value, with an ace as 1
     * @param soft       whether an ace in the hand counts as 11
     * @param rules      what the hand is allowed to do right now
     * @param trueCount  current true count
     * @return the move to make
     */
    Action action(int hand, int dealerHand, boolean soft, ActiveRules rules, double trueCount);

    /**
     * Returns whether to surrender a hand before the dealer checks for blackjack, giving up half the bet.
     *
     * @param hand       the hand's total
     * @param dealerHand the dealer's up card value, with an ace as 1
     * @param soft       whether an ace in the hand counts as 11
     * @param trueCount  current true count
     * @return {@code true} to surrender
     */
    boolean earlySurrender(int hand, int dealerHand, boolean soft, double trueCount);
}
