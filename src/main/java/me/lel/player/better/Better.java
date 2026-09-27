package me.lel.player.better;

/**
 * Decides how much to bet each round.
 */
public interface Better {
    /**
     * Returns the bet for a round at this true count, or {@code null} to sit the round out.
     *
     * @param trueCount current true count
     * @return the bet, or {@code null} to sit out
     */
    Bet bet(double trueCount);
}
