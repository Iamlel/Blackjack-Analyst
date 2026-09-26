package me.lel.core.hand;

import me.lel.core.Card;

/**
 * The dealer's hand. The first card is the up card, dealt face up for the players to see. The second is the hole card,
 * which stays face down while the players act.
 */
public class DealerHand extends Hand {
    /**
     * Creates the dealer's hand.
     *
     * @param first  the up card
     * @param second the hole card
     */
    public DealerHand(Card first, Card second) {
        super(first, second);
    }

    /**
     * Returns the dealer's face-up card, which players base their decisions on.
     *
     * @return the up card
     */
    public Card getUpCard() {
        return super.getFirst();
    }
}
