package me.lel.core;

/**
 * What a {@link me.lel.player.mover.Mover} is allowed to do on the decision in front of it. The first three flags are
 * table rules, and the last three apply to the current hand.
 *
 * @param h17          the dealer hits soft 17 (a 17 that counts an ace as 11)
 * @param das          doubling after a split is allowed
 * @param sas          surrendering after a split is allowed
 * @param canSurrender the hand may surrender now
 * @param canSplit     the hand may split now, which also means it is a pair
 * @param canDouble    the hand may double now
 */
public record ActiveRules(boolean h17, boolean das, boolean sas, boolean canSurrender, boolean canSplit, boolean canDouble) {
}
