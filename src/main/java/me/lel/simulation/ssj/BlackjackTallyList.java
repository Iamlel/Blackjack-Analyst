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
 * built on this list. It reads the tallies here instead of keeping its own copy, so never call its {@code add} or
 * {@code init}: that would add to or reset this list.
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

    public int getPlayerCount() {
        return players;
    }

    public BlackjackTally getPlayerTally(int i) {
        return (BlackjackTally) get(i);
    }

    public Tally getWagerTally(int i) {
        return get(players + i);
    }

    // initial amount wagered in $ per round, including rounds sat out (which wager 0)
    public double getAverageBet(int i) {
        return getWagerTally(i).average();
    }

    // mean profit / mean wager, as a fraction
    public FunctionOfMultipleMeansTally getEdgeTally(int i) {
        return edges[i];
    }

    // edge in percent: EV / (amount wagered) * 100%. positive means the player has the advantage
    public double getEdge(int i) {
        return edges[i].average() * 100;
    }

    public double getEdgeMarginOfError(int i) {
        return getEdgeMarginOfError(i, 0.95);
    }

    // in percent, from the delta method using the covariance between profit and wager
    public double getEdgeMarginOfError(int i, double level) {
        double[] centerAndRadius = new double[2];
        edges[i].confidenceIntervalDelta(level, centerAndRadius);
        return centerAndRadius[1] * 100;
    }
}
