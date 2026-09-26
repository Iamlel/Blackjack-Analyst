package me.lel.game;

import me.lel.core.Rules;
import me.lel.player.Player;
import umontreal.ssj.mcqmctools.MonteCarloModelDoubleArray;

/**
 * A game with players and rules that SSJ can simulate. One call to {@link #simulate} plays exactly one round.
 * <p>
 * {@link #getPerformance()} has {@code 2 * getPlayers().length} entries: the profit of each player in the round,
 * followed by the initial amount each player wagered in it. Dead players play nothing, so both are 0 for them.
 */
public interface SimpleGame extends MonteCarloModelDoubleArray {
    /**
     * Returns the players in the same order as the performance vector.
     */
    Player[] getPlayers();

    Rules getRules();

    /**
     * Returns the game to its starting state, such as an unshuffled shoe, so that a reset random stream replays the
     * same rounds. Player bankrolls are left alone.
     */
    void reset();

    /**
     * Returns whether any player can still cover the table minimum.
     */
    boolean hasLivingPlayers();
}
