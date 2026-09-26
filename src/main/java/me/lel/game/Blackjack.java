package me.lel.game;

import me.lel.core.ActiveRules;
import me.lel.core.Card;
import me.lel.core.Deck;
import me.lel.core.Rules;
import me.lel.core.action.Action;
import me.lel.core.hand.DealerHand;
import me.lel.core.hand.PlayerHand;
import me.lel.counting.CountSystem;
import me.lel.counting.impl.NoCountSystem;
import me.lel.player.Player;
import umontreal.ssj.rng.RandomStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blackjack implements SimpleGame {
    private final Rules rules;
    private final Player[] players;

    private final Deck deck;
    private DealerHand dealerHand;

    // [profit of each player, initial amount wagered by each player] for the last round
    private final double[] performance;

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
        this.performance = new double[2 * players.length];
    }

    @Override
    public void simulate(RandomStream stream) {
        if (hasLivingPlayers()) {
            for (int i = 0; i < players.length; i++) {
                performance[i] = -players[i].getBankroll();
            }

            playRound(stream);

            for (int i = 0; i < players.length; i++) {
                performance[i] += players[i].getBankroll();
            }
        } else {
            Arrays.fill(performance, 0);
        }
    }

    @Override
    public double[] getPerformance() {
        return performance;
    }

    @Override
    public boolean hasLivingPlayers() {
        for (Player player : players) {
            if (!isDead(player)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDead(Player player) {
        return player.isDead(rules.getMinimumBet());
    }

    @Override
    public int getPerformanceDim() {
        return performance.length;
    }

    @Override
    public void reset() {
        deck.reset();
    }

    protected void playRound(RandomStream stream) {
        if (deck.isShuffleNecessary()) {
            deck.shuffleDeck(stream);
        }

        if (!createHands(stream)) {
            return;
        }

        if (rules.isInsuranceAllowed()) {
            checkInsurance();
        }

        if (rules.isEarlySurrender()) {
            checkEarlySurrender();
        }

        if (!dealerHand.isBlackjack()) {
            getPlayerAction(stream);
            dealerAction(stream);
        }

        compareHands();
    }

    protected boolean createHands(RandomStream stream) {
        this.hands = new HashMap<>();
        this.splitTimes = new HashMap<>();
        int totalHandCounter = 0;
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            List<PlayerHand> playerHands = new ArrayList<>();
            this.hands.put(p, playerHands);
            this.splitTimes.put(p, 0);

            if (isDead(p)) {
                continue;
            }

            int handCounter = 0;
            int betAmount = 0;
            for (int bet : p.placeBets(rules.getMinimumBet(), rules.getMaximumBet(), rules.getMaxHands(), deck.getTrueCount())) {
                if (handCounter >= rules.getMaxHands()) {
                    break;
                }

                betAmount += bet;
                if (!p.has(betAmount)) {
                    break;
                }

                playerHands.add(new PlayerHand(bet, deck.takeCard(stream), deck.takeCard(stream)));
                performance[players.length + i] = betAmount;
                handCounter++;
            }
            totalHandCounter += handCounter;
        }
        this.dealerHand = new DealerHand(deck.takeCard(stream), deck.takeCard(stream));
        return (totalHandCounter > 0);
    }

    protected void checkInsurance() {
        if (dealerHand.getUpCard() == Card.ACE) {
            for (Player player : players) {
                double bets = (double) hands.get(player).stream().mapToInt(PlayerHand::bet).sum() / 2;
                if (player.insurance(deck.getTrueCount()) && player.has(bets)) {
                    if (dealerHand.isBlackjack()) {
                        pay(player, bets * rules.getInsurancePay());
                    } else {
                        take(player, bets);
                    }
                }
            }
        }
    }

    protected void checkEarlySurrender() {
        for (Player player : players) {
            for (int i = 0; i < hands.get(player).size(); i++) {
                PlayerHand hand = hands.get(player).get(i);

                if (player.earlySurrender(hand.getHandValue(), dealerHand.getUpCard().getValue(), hand.isSoft(),  deck.getTrueCount())) {
                    take(player, (double) hand.bet() / 2);
                    hands.get(player).remove(i--);
                }
            }
        }
    }

    protected void getPlayerAction(RandomStream stream) {
        for (Player player : players) {
            for (int i = 0; i < hands.get(player).size(); i++) {
                PlayerHand hand = hands.get(player).get(i);

                if (!dealerHand.isBlackjack() && hand.isBlackjack()) {
                    pay(player, hand.bet() * rules.getBlackjackPay());
                    hands.get(player).remove(i--);
                    continue;
                }

                while (hand.getHandValue() < 21) {
                    boolean splitAces = hand.hasBeenSplit() && hand.getFirst() == Card.ACE;
                    ActiveRules activeRules = new ActiveRules(rules.isH17(), rules.isDas(), rules.isSas(),
                            canSurrender(hand), canSplit(player, hand, splitAces), canDouble(player, hand, splitAces));

                    Action playerAction = player.action(hand.getHandValue(), dealerHand.getUpCard().getValue(), hand.isSoft(), activeRules, deck.getTrueCount());
                    if (playerAction == Action.SURRENDER && activeRules.canSurrender()) {
                        surrenderLogic(player, hand, i);
                        i--;
                        break;

                    } else if (playerAction == Action.SPLIT && activeRules.canSplit()) {
                        splitLogic(player, hand, i, stream);
                        i--;
                        break;

                    } else if ((playerAction == Action.DOUBLE || playerAction == Action.DOUBLE_STAND) && activeRules.canDouble()) {
                        doubleLogic(hand, stream);
                        break;

                    } else if (playerAction == Action.STAND || playerAction == Action.DOUBLE_STAND) {
                        break;
                    }

                    if (!splitAces || rules.isHitSplitAces()) {
                        hand.addCard(deck.takeCard(stream));
                    } else {
                        break;
                    }
                }
            }
        }
    }

    protected boolean canSurrender(PlayerHand hand) {
        return hand.isInitial() && rules.isLateSurrender() && (rules.isSas() || !hand.hasBeenSplit());
    }

    protected boolean canSplit(Player player, PlayerHand hand, boolean splitAces) {
        return player.has(hand.bet() * 2) &&
                hand.canSplit() &&
                splitTimes.get(player) < rules.getSplitAmount() &&
                (!splitAces || rules.isReSplitAces());
    }

    protected boolean canDouble(Player player, PlayerHand hand, boolean splitAces) {
        return player.has(hand.bet() * 2) &&
                hand.isInitial() &&
                (rules.isDas() || !hand.hasBeenSplit()) &&
                (!splitAces || rules.isDoubleSplitAces());
    }

    protected void surrenderLogic(Player player, PlayerHand hand, int index) {
        take(player, (double) hand.bet() / 2);
        hands.get(player).remove(index);
    }

    protected void splitLogic(Player player, PlayerHand hand, int index, RandomStream stream) {
        hands.get(player).add(new PlayerHand(hand.bet(), hand.getFirst(), deck.takeCard(stream), true));
        hands.get(player).add(new PlayerHand(hand.bet(), hand.getSecond(), deck.takeCard(stream), true));
        hands.get(player).remove(index);
        addSplit(player);
    }

    protected void doubleLogic(PlayerHand hand, RandomStream stream) {
        hand.addCard(deck.takeCard(stream));
        hand.doubleBet();
    }

    protected void dealerAction(RandomStream stream) {
        if (dealerHand.getHandValue() < 17 || (dealerHand.getHandValue() == 17 && dealerHand.isSoft() && rules.isH17())) {
            dealerHand.addCard(deck.takeCard(stream));
            dealerAction(stream);
        }
    }

    protected void compareHands() {
        for (Player player : players) {
            for (PlayerHand hand : hands.getOrDefault(player, Collections.emptyList())) {
                if (hand.getHandValue() > 21) {
                    take(player, hand.bet());
                } else if (dealerHand.getHandValue() > 21 || (hand.getHandValue() > dealerHand.getHandValue())) {
                    pay(player, hand.bet());
                } else if (hand.getHandValue() < dealerHand.getHandValue()) {
                    take(player, hand.bet());
                }
            }
        }
    }

    @Override
    public Rules getRules() {
        return rules;
    }

    @Override
    public Player[] getPlayers() {
        return players.clone();
    }

    protected Player[] getInternalPlayers() {
        return players;
    }

    protected Deck getDeck() {
        return deck;
    }

    protected DealerHand getDealerHand() {
        return dealerHand;
    }

    protected List<PlayerHand> getHands(Player player) {
        return hands.get(player);
    }

    protected void addSplit(Player player) {
        splitTimes.put(player, splitTimes.get(player) + 1);
    }

    protected void pay(Player player, double amount) {
        player.give(amount);
    }

    protected void take(Player player, double amount) {
        player.take(amount);
    }
}
