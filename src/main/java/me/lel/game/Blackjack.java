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

/**
 * Standard blackjack for any number of players, one round per {@link #simulate} call.
 * <p>
 * A round shuffles if the shoe is past its penetration, takes bets and deals, offers insurance and then early
 * surrender, plays out the players' hands and the dealer's (both skipped when the dealer has blackjack), and settles.
 * Each step is a protected method, so a variant such as {@link FreeBetBlackjack} overrides only the steps that
 * differ. All money goes through {@link #pay} and {@link #take}.
 * <p>
 * Decisions use the shoe's current true count, which includes every card dealt so far, the dealer's hole card among
 * them. Splitting or doubling needs a bankroll of at least twice the hand's bet.
 */
public class Blackjack implements SimpleGame {
    private final Rules rules;
    private final Player[] players;

    private final Deck deck;
    private DealerHand dealerHand;

    // [profit of each player, initial amount wagered by each player] for the last round
    private final double[] performance;

    private Map<Player, List<PlayerHand>> hands;
    private Map<Player, Integer> splitTimes;

    /**
     * Creates a game with the default {@link Rules}, a six-deck shoe and no card counting.
     */
    public Blackjack(Player[] players) {
        this(players, new NoCountSystem(), Rules.buildDefault(), 6);
    }

    /**
     * Creates a game with the default {@link Rules} and a six-deck shoe.
     */
    public Blackjack(Player[] players, CountSystem countSystem) {
        this(players, countSystem, Rules.buildDefault(), 6);
    }

    /**
     * Creates a game with a six-deck shoe.
     */
    public Blackjack(Player[] players, CountSystem countSystem, Rules rules) {
        this(players, countSystem, rules, 6);
    }

    /**
     * Creates a game.
     *
     * @param players     players in seat order
     * @param countSystem counting system behind the true count players see
     * @param rules       table rules
     * @param decks       number of decks in the shoe
     */
    public Blackjack(Player[] players, CountSystem countSystem, Rules rules, int decks) {
        this.players = players;
        this.rules = rules;
        this.deck = new Deck(decks, countSystem, rules.getPenetration());
        this.performance = new double[2 * players.length];
    }

    /**
     * Plays one round and records each player's profit and initial wager. Once every player is dead, it records zeros
     * without dealing.
     */
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

    /**
     * Returns whether {@code player} can no longer cover the table minimum and is dealt out.
     */
    protected boolean isDead(Player player) {
        return player.isDead(rules.getMinimumBet());
    }

    @Override
    public int getPerformanceDim() {
        return performance.length;
    }

    /**
     * Puts the shoe back in its unshuffled starting order.
     */
    @Override
    public void reset() {
        deck.reset();
    }

    /**
     * Plays one round without recording it. The class description lists the steps.
     */
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

    /**
     * Takes bets from every living player, deals their hands, then deals the dealer. A player gets no more hands once
     * their bankroll can't cover the next bet. The dealer is dealt even when nobody bets.
     * <p>
     * This also records each player's initial wager, so an override should call it.
     *
     * @return whether anyone bet; if nobody did, the round ends here
     */
    protected boolean createHands(RandomStream stream) {
        this.hands = new HashMap<>();
        this.splitTimes = new HashMap<>();
        int totalHandCounter = 0;
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            List<PlayerHand> playerHands = new ArrayList<>();
            this.hands.put(p, playerHands);
            this.splitTimes.put(p, 0);
            // cleared every round, so sitting out or being dead records 0 instead of the last bet
            performance[players.length + i] = 0;

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

    /**
     * Offers insurance when the dealer shows an ace and settles it right away. A player who takes it bets half of
     * their total initial bet.
     */
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

    /**
     * Lets each hand give up half its bet before the dealer checks for blackjack.
     */
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

    /**
     * Plays out every player's hands. Blackjacks are paid right away. Any other hand keeps asking its player for a
     * move until it stands, busts, reaches 21, or doubles, splits or surrenders. See {@link Action} for what happens
     * when a move isn't allowed.
     */
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

    /**
     * Returns whether {@code hand} may split now. {@code splitAces} is true when the hand itself came from splitting
     * aces.
     */
    protected boolean canSplit(Player player, PlayerHand hand, boolean splitAces) {
        return player.has(hand.bet() * 2) &&
                hand.canSplit() &&
                splitTimes.get(player) < rules.getSplitAmount() &&
                (!splitAces || rules.isReSplitAces());
    }

    /**
     * Returns whether {@code hand} may double now. {@code splitAces} is true when the hand itself came from splitting
     * aces.
     */
    protected boolean canDouble(Player player, PlayerHand hand, boolean splitAces) {
        return player.has(hand.bet() * 2) &&
                hand.isInitial() &&
                (rules.isDas() || !hand.hasBeenSplit()) &&
                (!splitAces || rules.isDoubleSplitAces());
    }

    /**
     * Takes half of the hand's bet and removes the hand at {@code index} from play.
     */
    protected void surrenderLogic(Player player, PlayerHand hand, int index) {
        take(player, (double) hand.bet() / 2);
        hands.get(player).remove(index);
    }

    /**
     * Replaces the hand at {@code index} with two hands that each keep one of its cards and draw a new second card.
     * The split counts toward {@link Rules#getSplitAmount()}.
     */
    protected void splitLogic(Player player, PlayerHand hand, int index, RandomStream stream) {
        hands.get(player).add(new PlayerHand(hand.bet(), hand.getFirst(), deck.takeCard(stream), true));
        hands.get(player).add(new PlayerHand(hand.bet(), hand.getSecond(), deck.takeCard(stream), true));
        hands.get(player).remove(index);
        addSplit(player);
    }

    /**
     * Deals the hand one more card and doubles its bet.
     */
    protected void doubleLogic(PlayerHand hand, RandomStream stream) {
        hand.addCard(deck.takeCard(stream));
        hand.doubleBet();
    }

    /**
     * Draws for the dealer until the total is 17 or more, and hits soft 17 when the rules say so.
     */
    protected void dealerAction(RandomStream stream) {
        if (dealerHand.getHandValue() < 17 || (dealerHand.getHandValue() == 17 && dealerHand.isSoft() && rules.isH17())) {
            dealerHand.addCard(deck.takeCard(stream));
            dealerAction(stream);
        }
    }

    /**
     * Settles every hand still in play. A bust loses, a higher total or a dealer bust wins even money, a lower total
     * loses, and a tie pushes.
     */
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

    /**
     * Returns the players in seat order, in a new array.
     */
    @Override
    public Player[] getPlayers() {
        return players.clone();
    }

    /**
     * Returns the players array itself, without the copy {@link #getPlayers()} makes.
     */
    protected Player[] getInternalPlayers() {
        return players;
    }

    protected Deck getDeck() {
        return deck;
    }

    protected DealerHand getDealerHand() {
        return dealerHand;
    }

    /**
     * Returns the list of hands {@code player} still has in play this round. Changes to the list change the round.
     */
    protected List<PlayerHand> getHands(Player player) {
        return hands.get(player);
    }

    /**
     * Counts one split by {@code player} toward {@link Rules#getSplitAmount()}.
     */
    protected void addSplit(Player player) {
        splitTimes.put(player, splitTimes.get(player) + 1);
    }

    /**
     * Pays {@code amount} to {@code player}. Every win in the game goes through this method.
     */
    protected void pay(Player player, double amount) {
        player.give(amount);
    }

    /**
     * Takes {@code amount} from {@code player}. Every loss in the game goes through this method.
     */
    protected void take(Player player, double amount) {
        player.take(amount);
    }
}
