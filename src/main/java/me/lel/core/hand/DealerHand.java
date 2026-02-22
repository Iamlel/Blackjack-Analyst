package me.lel.core.hand;

import me.lel.core.Card;

import java.util.List;

public class DealerHand extends Hand {
    public DealerHand(List<Card> cards) {
        super(cards);
    }

    public Card getUpCard() {
        return super.getFirst();
    }
}
