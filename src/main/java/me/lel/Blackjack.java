package me.lel;

import me.lel.core.*;
import me.lel.core.hand.DealerHand;
import me.lel.counting.CountSystem;
import me.lel.counting.NoCountSystem;
import me.lel.player.Player;
import me.lel.core.hand.PlayerHand;

import java.util.*;

public class Blackjack {
    private final Rules rules;
    private final Player[] players;

    private final Deck deck;
    private DealerHand dealerHand;

    private Map<Player, List<PlayerHand>> hands;
    private Map<Player, Integer> splitTimes;

    public Blackjack(Player[] players) {
        this(players, new NoCountSystem(), Rules.buildDefault(), 6);
    }

    public Blackjack(Player[] players, CountSystem countSystem) {
        this(players, countSystem, Rules.buildDefault(), 6);
    }

    public Blackjack(Player[] players, CountSystem countSystem, Rules rules) {
        this(players, countSystem, rules, 6);
    }

    public Blackjack(Player[] players, CountSystem countSystem, Rules rules, int decks) {
        this.players = players;
        this.rules = rules;
        this.deck = new Deck(decks, countSystem, rules.getPenetration());
    }

    protected void createHands() {
        this.hands = new HashMap<>();
        this.splitTimes = new HashMap<>();
        for (Player p : players) {
            List<PlayerHand> playerHands = new ArrayList<>();

            int handCounter = 0;
            int betAmount = 0;
            for (int bet : p.placeBets(rules.getMinimumBet(), rules.getMaximumBet(), rules.getMaxHands())) {
                if (handCounter >= rules.getMaxHands()) {
                    break;
                }

                betAmount += bet;
                if (!p.has(betAmount)) {
                    break;
                }

                playerHands.add(new PlayerHand(bet, List.of(deck.takeCard(), deck.takeCard())));
                handCounter++;
            }
            this.hands.put(p, playerHands);
            this.splitTimes.put(p, 0);
        }
        this.dealerHand = new DealerHand(List.of(deck.takeCard(), deck.takeCard()));
    }

    protected void checkInsurance(boolean dealerBlackjack) {
        if (dealerHand.getUpCard() == Card.ACE) {
            for (Player player : players) {
                double bets = (double) hands.get(player).stream().mapToInt(PlayerHand::bet).sum() / 2;
                if (player.insurance(deck.getTrueCount()) && player.has(bets)) {
                    if (dealerBlackjack) {
                        player.give(bets * rules.getInsurancePay());
                    } else {
                        player.take(bets);
                    }
                }
            }
        }
    }

    protected void getPlayerActions() {
        for (Player player : players) {
            for (int i = 0; i < hands.get(player).size(); i++) {
                PlayerHand hand = hands.get(player).get(i);

                if (hand.isSoft() && hand.hasBeenSplit()) {
                    continue;
                }

                while (hand.getHandValue() < 21) {
                    boolean canSplit = hand.canSplit() && player.has(hand.bet() * 2) && splitTimes.get(player) < rules.getSplitAmount();
                    boolean canDouble = player.has(hand.bet() * 2) && hand.isInitial() && (rules.isDas() || !hand.hasBeenSplit());

                    Action playerAction = player.action(hand.getHandValue(), dealerHand.getUpCard().getValue(), hand.isSoft(), canSplit, rules, deck.getTrueCount());
                    if (playerAction == Action.SURRENDER && rules.isSurrenderAllowed()) {
                        player.take((double) hand.bet() / 2);
                        hands.get(player).remove(hand);
                        i--;
                        break;

                    } else if (playerAction == Action.SPLIT && canSplit) {
                        hands.get(player).add(new PlayerHand(hand.bet(), List.of(hand.getFirst(), deck.takeCard()), true));
                        hands.get(player).add(new PlayerHand(hand.bet(), List.of(hand.getFirst(), deck.takeCard()), true));
                        hands.get(player).remove(hand);
                        splitTimes.put(player, splitTimes.get(player) + 1);
                        i--;
                        break;

                    } else if ((playerAction == Action.DOUBLE || playerAction == Action.DOUBLE_STAND) && canDouble) {
                        hand.addCard(deck.takeCard());
                        hand.doubleBet();
                        break;

                    } else if (playerAction == Action.STAND || playerAction == Action.DOUBLE_STAND) {
                        break;
                    }

                    hand.addCard(deck.takeCard());
                }
            }
        }
    }

    protected void compareHands() {
        for (Player player : players) {
            for (PlayerHand hand : hands.getOrDefault(player, Collections.emptyList())) {
                if (hand.isBlackjack() && !hand.hasBeenSplit() && dealerHand.getHandValue() != 21) {
                    player.give(hand.bet() * rules.getBlackjackPay());
                } else if (hand.getHandValue() > 21) {
                    player.take(hand.bet());
                } else if (dealerHand.getHandValue() > 21 || (hand.getHandValue() > dealerHand.getHandValue())) {
                    player.give(hand.bet());
                } else if (hand.getHandValue() < dealerHand.getHandValue()) {
                    player.take(hand.bet());
                }
            }
        }
    }

    protected void dealerAction() {
        if (dealerHand.getHandValue() < 17 || (dealerHand.getHandValue() == 17 && dealerHand.isSoft() && rules.isH17())) {
            dealerHand.addCard(deck.takeCard());
            dealerAction();
        }
    }

    public void play() {
        if (deck.isShuffleNecessary()) {
            deck.shuffleDeck();
        }
        createHands();

        boolean dealerBlackjack = dealerHand.isBlackjack();
        if (rules.isInsuranceAllowed()) {
            checkInsurance(dealerBlackjack);
        }

        if (!dealerBlackjack) {
            getPlayerActions();
            dealerAction();
        }

        compareHands();
    }
}
