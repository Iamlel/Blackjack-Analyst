package me.lel.player.better;

/**
 * How much a {@link Better} wants to bet in a round.
 *
 * @param Hands number of hands to play
 * @param Units bet on each hand, in betting units (multiples of the table minimum)
 */
public record Bet(int Hands, int Units) {
}
