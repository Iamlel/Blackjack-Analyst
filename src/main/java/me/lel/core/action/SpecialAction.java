package me.lel.core.action;

/**
 * Strategy table entries whose meaning depends on the rules.
 * {@link me.lel.player.mover.impl.datadrivenmover.DataDrivenMover} resolves them when it picks a move.
 */
public enum SpecialAction implements SimpleAction {
    /**
     * Split, but only if doubling after a split is allowed.
     */
    SPLIT_DAS,
    /**
     * Surrender before the dealer checks for blackjack.
     */
    EARLY_SURRENDER
}
