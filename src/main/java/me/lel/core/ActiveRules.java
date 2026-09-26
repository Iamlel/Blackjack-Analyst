package me.lel.core;

/**
 * What a {@link me.lel.player.mover.Mover} is allowed to do on the decision in front of it.
 * <p>
 * The first three flags copy the table {@link Rules}. The last three are worked out for the current hand, so they
 * also depend on the player's bankroll, the splits already made this round, and whether the hand still has only its
 * first two cards.
 *
 * @param h17          the dealer hits soft 17
 * @param das          doubling after a split is allowed
 * @param sas          surrendering after a split is allowed
 * @param canSurrender the hand may surrender now
 * @param canSplit     the hand may split now, which also means it is a pair
 * @param canDouble    the hand may double now
 */
public record ActiveRules(boolean h17, boolean das, boolean sas, boolean canSurrender, boolean canSplit, boolean canDouble) {
}
