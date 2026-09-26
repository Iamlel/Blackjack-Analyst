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
    Player[] getPlayers();
    Rules getRules();
    void reset();

    // false once every player is dead (cannot cover the table minimum)
    boolean hasLivingPlayers();
}
