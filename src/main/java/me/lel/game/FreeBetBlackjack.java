package me.lel.game;

import me.lel.core.Rules;
import me.lel.core.hand.PlayerHand;
import me.lel.counting.CountSystem;
import me.lel.player.Player;

import java.util.*;

public class FreeBetBlackjack extends Blackjack {
    private final Set<PlayerHand> freeHands = new HashSet<>();
    private final Set<PlayerHand> freeDoubles = new HashSet<>();

    public FreeBetBlackjack(Player[] players) {
        super(players);
    }

    public FreeBetBlackjack(Player[] players, CountSystem countSystem) {
        super(players, countSystem);
    }

    public FreeBetBlackjack(Player[] players, CountSystem countSystem, Rules rules) {
        super(players, countSystem, rules);
    }

    public FreeBetBlackjack(Player[] players, CountSystem countSystem, Rules rules, int decks) {
        super(players, countSystem, rules, decks);
    }

    @Override
    protected void doubleLogic(PlayerHand hand) {
        int h = hand.getHandValue();
        if (h == 9 || h == 10 || h == 11) {
            freeDoubles.add(hand);
        }

        hand.addCard(super.getDeck().takeCard());
        hand.doubleBet();
    }

    @Override
    protected void splitLogic(Player player, PlayerHand hand, int index) {
        PlayerHand hand1 = new PlayerHand(hand.bet(), hand.getFirst(), super.getDeck().takeCard(), true);
        PlayerHand hand2 = new PlayerHand(hand.bet(), hand.getSecond(), super.getDeck().takeCard(), true);

        if (hand.getFirst().getValue() != 10) {
            if (freeHands.contains(hand)) {
                freeHands.add(hand1);
            }

            freeHands.add(hand2);
        }

        super.getHands(player).add(hand1);
        super.getHands(player).add(hand2);
        super.getHands(player).remove(index);

        super.addSplit(player);
    }

    @Override
    protected void compareHands() {
        if (super.getDealerHand().getHandValue() == 22) {
            return;
        }

        for (Player player : super.getPlayers()) {
            for (PlayerHand hand : super.getHands(player)) {
                if (hand.getHandValue() > 21) {
                    take(player, freeDoubles.contains(hand) ? (double) hand.bet() / 2 : hand.bet(), freeHands.contains(hand));
                } else if (super.getDealerHand().getHandValue() > 21 || (hand.getHandValue() > super.getDealerHand().getHandValue())) {
                    pay(player, hand.bet());
                } else if (hand.getHandValue() < super.getDealerHand().getHandValue()) {
                    take(player, freeDoubles.contains(hand) ? (double) hand.bet() / 2 : hand.bet(), freeHands.contains(hand));
                }
            }
        }
    }

    private void take(Player player, double amount, boolean free) {
        if (!free) {
            super.take(player, amount);
        }
    }
}
