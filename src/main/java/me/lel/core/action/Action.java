package me.lel.core.action;

/**
 * A move a {@link me.lel.player.mover.Mover} can make. When the rules don't allow the move,
 * {@link me.lel.game.Blackjack} hits instead, except for {@link #DOUBLE_STAND}.
 */
public enum Action implements SimpleAction {
    SURRENDER,
    SPLIT,
    DOUBLE,
    /**
     * Double if allowed, otherwise stand.
     */
    DOUBLE_STAND,
    HIT,
    STAND;
}
