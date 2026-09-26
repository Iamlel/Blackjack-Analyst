package me.lel.core.action;

/**
 * Strategy table entries whose meaning depends on the rules.
 */
public enum SpecialAction implements SimpleAction {
    /**
     * Split, but only if doubling after a split is allowed.
     */
    SPLIT_DAS,
    /**
     * Surrender before the dealer checks for blackjack, which also saves half the bet against a dealer blackjack.
     */
    EARLY_SURRENDER
}
