package me.lel.core.hand;

import me.lel.core.Card;

/**
 * The dealer's hand. The first card dealt is the up card.
 */
public class DealerHand extends Hand {
    public DealerHand(Card first, Card second) {
        super(first, second);
    }

    public Card getUpCard() {
        return super.getFirst();
    }
}
