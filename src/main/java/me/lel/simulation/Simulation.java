package me.lel.simulation;

import me.lel.game.SimpleGame;
import me.lel.player.Player;
import me.lel.simulation.ssj.BlackjackTally;
import me.lel.simulation.ssj.BlackjackTallyList;
import umontreal.ssj.mcqmctools.MonteCarloExperiment;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;

/**
 * Runs a {@link SimpleGame} and collects per-player statistics.
 * <p>
 * Everything here is measured in <b>rounds</b>, not hands: one call to {@link SimpleGame#simulate} deals exactly one
 * round, and one round is one observation, however many hands a player plays in it (multiple spots, splits) and
 * including rounds where the player sits out (e.g. wonging out at a low count).
 * <p>
 * A player who can no longer cover the table minimum is dead: they are dealt out, and every round after that is
 * recorded as 0 profit and 0 wagered for them. Once every player is dead, no more rounds are recorded.
 * <p>
 * Rounds draw from an SSJ {@link MRG32k3a} stream, one substream per round. {@link #reset()} puts the players, the
 * game and the stream back at the start, so running the same number of rounds again gives the same results.
 */
public class Simulation {
    private final SimpleGame game;
    private final RandomStream stream = new MRG32k3a();
    private BlackjackTallyList playerStatContainer;

    /**
     * Creates a simulation of {@code game}. The statistics start from the players' current bankrolls.
     *
     * @param game the game to simulate
     */
    public Simulation(SimpleGame game) {
        this.game = game;
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

    /**
     * Plays and records {@code rounds} rounds. Each call starts the statistics over, while bankrolls and the game carry
     * on from where the last call left them.
     *
     * @param rounds number of rounds to play
     */
    public void run(int rounds) {
        MonteCarloExperiment.simulateRuns(game, rounds, stream, playerStatContainer);
    }

    /**
     * Plays {@code rounds} rounds like {@link #run}, then opens a window charting each player's bankroll with about 500
     * points per player.
     *
     * @param rounds number of rounds to play
     */
    public void runWithDisplay(int rounds) {
        Graph graph = new Graph(playerStatContainer, rounds);
        runWithDisplay(rounds, graph);
    }

    /**
     * Same as {@link #runWithDisplay(int)}, with a point every {@code dx} rounds.
     *
     * @param rounds number of rounds to play
     * @param dx     rounds between points
     */
    public void runWithDisplay(int rounds, int dx) {
        Graph graph = new Graph(playerStatContainer, rounds);
        graph.setDx(dx);
        runWithDisplay(rounds, graph);
    }

    /**
     * Same as {@link #runWithDisplay(int)}, with a graph you set up yourself. The graph has to be built on
     * {@link #getPlayerStatContainer()}, and it stops recording once its window opens.
     *
     * @param rounds number of rounds to play
     * @param graph  the graph to record into and display
     */
    public void runWithDisplay(int rounds, Graph graph) {
        this.run(rounds);
        graph.display();
        graph.close();
    }

    /**
     * Returns {@link #getResults(int, int)} for the first player, without hourly EV.
     *
     * @return the first player's report
     */
    public String getFirstResults() {
        return getResults(0, 0);
    }

    /**
     * Returns {@link #getResults(int, int)} for the first player.
     *
     * @param roundsPerHour rounds per hour, used to add EV per hour; 0 leaves it out
     * @return the first player's report
     */
    public String getFirstResults(int roundsPerHour) {
        return getResults(0, roundsPerHour);
    }

    /**
     * Returns {@link #getResults(int, int)} without hourly EV.
     *
     * @param i the player's seat index
     * @return player {@code i}'s report
     */
    public String getResults(int i) {
        return getResults(i, 0);
    }

    /**
     * Returns a readable report of player {@code i}'s results, grouped into bankroll, expected value, volatility, and
     * risk and efficiency. EV and edge come with 95% confidence intervals.
     *
     * @param i             the player's seat index
     * @param roundsPerHour rounds per hour, used to add EV per hour; 0 leaves it out
     * @return the report, one statistic per line
     * @throws IndexOutOfBoundsException if there is no player {@code i}
     * @throws IllegalStateException     if no rounds have been recorded
     */
    public String getResults(int i, int roundsPerHour) {
        if (i >= playerStatContainer.getPlayerCount()) {
            throw new IndexOutOfBoundsException("There are not enough players for that.");
        }

        Player player = this.getPlayer(i);
        BlackjackTally stats = playerStatContainer.getPlayerTally(i);

        if (stats.getN() == 0) {
            throw new IllegalStateException("There are no rounds to look at yet.");
        }

        double ev = stats.getEV();
        double evMargin = stats.getMarginOfError();
        double edge = playerStatContainer.getEdge(i);
        double edgeMargin = playerStatContainer.getEdgeMarginOfError(i);

        StringBuilder report = new StringBuilder();
        report.append("Player %d after %,d rounds (1 unit = %s)%n".formatted(i + 1, stats.getN(), dollars(stats.getBettingUnit(), 0)));

        section(report, "Bankroll");
        line(report, "Starting", dollars(stats.getStartingBankroll(), 2));
        line(report, "Ending", dollars(player.getBankroll(), 2));
        line(report, "Change", signedDollars(player.getBankroll() - stats.getStartingBankroll()));
        if (player.isDead(game.getRules().getMinimumBet())) {
            line(report, "Status", "Dead, can't cover the %s table minimum".formatted(dollars(game.getRules().getMinimumBet(), 0)));
        }

        section(report, "Expected value");
        line(report, "Per round", dollars(ev, 4) + "  95% CI " + interval(dollars(ev - evMargin, 4), dollars(ev + evMargin, 4)));
        line(report, "Per round in units", "%.4f units".formatted(stats.getUnitEV()));
        if (roundsPerHour > 0) {
            line(report, "Per hour", "%s at %,d rounds per hour".formatted(dollars(ev * roundsPerHour, 2), roundsPerHour));
        }
        line(report, "Average bet", dollars(playerStatContainer.getAverageBet(i), 2) + " per round");
        line(report, "Player edge", percent(edge) + "  95% CI " + interval(percent(edge - edgeMargin), percent(edge + edgeMargin)));

        section(report, "Volatility");
        line(report, "Standard deviation", "%.3f units per round".formatted(stats.getStandardDeviation()));
        line(report, "Variance", "%.3f squared units per round".formatted(stats.getVariance()));
        line(report, "Max drawdown", "%s (%,.1f units)".formatted(dollars(stats.getMaxDrawdown(), 2), stats.getUnitMaxDrawdown()));

        section(report, "Risk and efficiency");
        line(report, "Risk of ruin", "%.2f%% starting from %s".formatted(stats.getROR(), dollars(stats.getStartingBankroll(), 2)));
        line(report, "N0", "%,d rounds".formatted((long) Math.ceil(stats.getNZero())));
        line(report, "Sharpe ratio", "%.4f per round".formatted(stats.getSharpeRatio()));

        return report.toString();
    }

    private static void section(StringBuilder report, String title) {
        report.append("%n%s%n".formatted(title));
    }

    private static void line(StringBuilder report, String label, String value) {
        report.append("  %-20s %s%n".formatted(label + ":", value));
    }

    private static String interval(String lower, String upper) {
        return "(" + lower + ", " + upper + ")";
    }

    private static String dollars(double amount, int decimals) {
        return (amount < 0 ? "-$" : "$") + ("%,." + decimals + "f").formatted(Math.abs(amount));
    }

    private static String signedDollars(double amount) {
        return (amount > 0 ? "+" : "") + dollars(amount, 2);
    }

    private static String percent(double value) {
        return "%.4f%%".formatted(value);
    }

    /**
     * Restores every player's starting bankroll, the game's starting state and the random stream, then clears the
     * statistics.
     */
    public void reset() {
        Player[] players = game.getPlayers();
        for (int i = 0; i < players.length; i++) {
            players[i].resetBankroll();
        }
        game.reset();
        this.resetRandom();
        this.resetStatistics();
    }

    /**
     * Moves the random stream back to its start. That alone doesn't replay earlier rounds, because the game keeps its
     * current state. Use {@link #reset()} to replay.
     */
    public void resetRandom() {
        stream.resetStartStream();
    }

    /**
     * Clears the statistics. The players' current bankrolls become the starting bankrolls used for risk of ruin.
     */
    public void resetStatistics() {
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

    /**
     * Returns the tallies behind the statistics, for example to build a {@link Graph}. {@link #resetStatistics()}
     * replaces them with new ones.
     *
     * @return the current tallies
     */
    public BlackjackTallyList getPlayerStatContainer() {
        return playerStatContainer;
    }

    /**
     * Returns the first player's statistics.
     *
     * @return the first player's tally
     */
    public BlackjackTally getFirstStats() {
        return playerStatContainer.getPlayerTally(0);
    }

    /**
     * Returns player {@code i}'s statistics.
     *
     * @param i the player's seat index
     * @return player {@code i}'s tally
     */
    public BlackjackTally getStats(int i) {
        return playerStatContainer.getPlayerTally(i);
    }

    /**
     * Returns the first player.
     *
     * @return the player in seat 0
     */
    public Player getFirstPlayer() {
        return game.getPlayers()[0];
    }

    /**
     * Returns player {@code i}.
     *
     * @param i the player's seat index
     * @return the player in seat {@code i}
     */
    public Player getPlayer(int i) {
        return game.getPlayers()[i];
    }
}
