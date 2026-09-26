package me.lel.core.action;

/**
 * A move a player can make on a hand.
 */
public enum Action implements SimpleAction {
    /**
     * Give up the hand and get half the bet back.
     */
    SURRENDER,
    /**
     * Split a pair into two hands, each with a bet equal to the original.
     */
    SPLIT,
    /**
     * Double the bet and take exactly one more card.
     */
    DOUBLE,
    /**
     * Double if allowed, otherwise stand.
     */
    DOUBLE_STAND,
    /**
     * Take another card.
     */
    HIT,
    /**
     * Take no more cards.
     */
    STAND;
}
