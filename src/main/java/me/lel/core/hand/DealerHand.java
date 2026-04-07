package me.lel.core.hand;

import me.lel.core.Card;

public class DealerHand extends Hand {
    public DealerHand(Card first, Card second) {
        super(first, second);
    }

    public Card getUpCard() {
        return super.getFirst();
    }
}
