package me.lel.simulation.ssj;

import me.lel.game.SimpleGame;
import me.lel.player.Player;
import umontreal.ssj.stat.FunctionOfMultipleMeansTally;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.stat.list.ListOfTalliesWithCovariance;

/**
 * Collects a {@link SimpleGame}'s performance vector, so it can be passed straight to
 * {@code MonteCarloExperiment.simulateRuns}. One observation is one round.
 * <p>
 * Tally {@code i} is player {@code i}'s {@link BlackjackTally} (profit per round) and tally {@code players + i} is the
 * initial amount player {@code i} wagered per round. Keeping both in one list keeps their covariance.
 * <p>
 * Player {@code i}'s edge is mean profit / mean wager, which SSJ computes with a {@link FunctionOfMultipleMeansTally}
 * built on this list. It reads the tallies here instead of keeping its own copy, so it stays private: calling its
 * {@code add} or {@code init} would add to or reset this list.
 * <p>
 * Once every player is dead the remaining rounds are not played by anyone, so they are not recorded.
 */
public class BlackjackTallyList extends ListOfTalliesWithCovariance<Tally> {
    private final SimpleGame game;
    private final int players;
    private final FunctionOfMultipleMeansTally[] edges;
    private boolean everyoneDead;

    private BlackjackTallyList(SimpleGame game) {
        this.game = game;
        this.players = game.getPlayers().length;
        this.edges = new FunctionOfMultipleMeansTally[game.getPlayers().length];
        super();
    }

    /**
     * Creates the list for {@code game}. Each player's current bankroll becomes their starting bankroll.
     *
     * @param game the game to collect results from
     * @return the new list
     * @throws IllegalArgumentException if the game doesn't report a profit and a wager for every player
     */
    public static BlackjackTallyList create(SimpleGame game) {
        BlackjackTallyList playerStatContainer = new BlackjackTallyList(game);
        if (game.getPerformanceDim() != 2 * playerStatContainer.players) {
            throw new IllegalArgumentException("The game must report a profit and a wager for every player.");
        }

        int players = playerStatContainer.players;
        for (Player player : game.getPlayers()) {
            playerStatContainer.add(new BlackjackTally(game.getRules().getMinimumBet(), player.getBankroll()));
        }
        for (int i = 0; i < players; i++) {
            playerStatContainer.add(new Tally());
        }

        playerStatContainer.init();

        for (int i = 0; i < players; i++) {
            playerStatContainer.edges[i] = new FunctionOfMultipleMeansTally(
                    new IndexedRatioFunction(i, players + i, 2 * players), playerStatContainer);
        }
        return playerStatContainer;
    }

    @Override
    public void init() {
        super.init();
        this.everyoneDead = !game.hasLivingPlayers();
    }

    @Override
    public void add(double[] x) {
        if (everyoneDead) {
            return;
        }

        super.add(x);
        this.everyoneDead = !game.hasLivingPlayers();
    }

    /**
     * Returns the number of players the list collects results for.
     *
     * @return the number of players
     */
    public int getPlayerCount() {
        return players;
    }

    /**
     * Returns player {@code i}'s profit tally.
     *
     * @param i the player's seat index
     * @return the profit tally
     */
    public BlackjackTally getPlayerTally(int i) {
        return (BlackjackTally) get(i);
    }

    /**
     * Returns the tally of player {@code i}'s initial wager per round.
     *
     * @param i the player's seat index
     * @return the wager tally
     */
    public Tally getWagerTally(int i) {
        return get(players + i);
    }

    /**
     * Returns player {@code i}'s average initial wager per round, in dollars. Rounds sat out count as 0.
     *
     * @param i the player's seat index
     * @return the average bet in dollars
     */
    public double getAverageBet(int i) {
        return getWagerTally(i).average();
    }

    /**
     * Returns player {@code i}'s edge: mean profit divided by mean initial wager. A positive edge means the player has
     * the advantage over the house.
     *
     * @param i the player's seat index
     * @return the edge as a percentage
     */
    public double getEdge(int i) {
        return edges[i].average() * 100;
    }

    /**
     * Returns the half-width of a 95% confidence interval on {@link #getEdge(int)}.
     *
     * @param i the player's seat index
     * @return the margin of error in percentage points
     */
    public double getEdgeMarginOfError(int i) {
        return getEdgeMarginOfError(i, 0.95);
    }

    /**
     * Same as {@link #getEdgeMarginOfError(int)} at another confidence level. The interval comes from the delta method,
     * which accounts for the covariance between profit and wager.
     *
     * @param i     the player's seat index
     * @param level the confidence level, such as 0.99
     * @return the margin of error in percentage points
     */
    public double getEdgeMarginOfError(int i, double level) {
        double[] centerAndRadius = new double[2];
        edges[i].confidenceIntervalDelta(level, centerAndRadius);
        return centerAndRadius[1] * 100;
    }
}
