package me.lel.game;

import me.lel.core.Rules;
import me.lel.core.hand.PlayerHand;
import me.lel.counting.CountSystem;
import me.lel.player.Player;
import umontreal.ssj.rng.RandomStream;

import java.util.HashSet;
import java.util.Set;

/**
 * Free Bet Blackjack. The house puts up the extra bet on doubles of hard 9, 10 and 11 and on splits of any pair except
 * tens, and a free bet can win but is never lost. In exchange, the dealer pushes on 22 instead of busting.
 */
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
    protected boolean createHands(RandomStream stream) {
        freeHands.clear();
        freeDoubles.clear();
        return super.createHands(stream);
    }

    /**
     * Doubles like {@link Blackjack}, but a double on 9, 10 or 11 is free: if the hand loses, only the original bet
     * is lost.
     */
    @Override
    protected void doubleLogic(PlayerHand hand, RandomStream stream) {
        int h = hand.getHandValue();
        if (h == 9 || h == 10 || h == 11) {
            freeDoubles.add(hand);
        }

        hand.addCard(super.getDeck().takeCard(stream));
        hand.doubleBet();
    }

    /**
     * Splits like {@link Blackjack}, and for free unless the pair is ten-valued. The second new hand is free, and the
     * first one is too when the hand being split was already free.
     */
    @Override
    protected void splitLogic(Player player, PlayerHand hand, int index, RandomStream stream) {
        PlayerHand hand1 = new PlayerHand(hand.bet(), hand.getFirst(), super.getDeck().takeCard(stream), true);
        PlayerHand hand2 = new PlayerHand(hand.bet(), hand.getSecond(), super.getDeck().takeCard(stream), true);

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

    /**
     * Settles like {@link Blackjack}, except that a dealer 22 pushes every hand that didn't bust, a free hand never
     * loses, and a hand that lost after a free double loses only its original bet.
     */
    @Override
    protected void compareHands() {
        for (Player player : super.getInternalPlayers()) {
            for (PlayerHand hand : super.getHands(player)) {
                if (hand.getHandValue() > 21) {
                    take(player, freeDoubles.contains(hand) ? (double) hand.bet() / 2 : hand.bet(), freeHands.contains(hand));
                } else if (super.getDealerHand().getHandValue() == 22) {
                    continue;
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
