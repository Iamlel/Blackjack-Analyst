package me.lel.simulation;

import me.lel.game.SimpleGame;
import me.lel.player.Player;
import me.lel.simulation.ssj.BlackjackTally;
import me.lel.simulation.ssj.BlackjackTallyList;
import umontreal.ssj.mcqmctools.MonteCarloExperiment;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;

import java.text.DecimalFormat;

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

    public Simulation(SimpleGame game) {
        this.game = game;
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

    /**
     * Plays and records {@code rounds} rounds. Each call starts the statistics over, while bankrolls and the shoe
     * carry on from where the last call left them.
     */
    public void run(int rounds) {
        MonteCarloExperiment.simulateRuns(game, rounds, stream, playerStatContainer);
    }

    /**
     * Plays {@code rounds} rounds like {@link #run}, then opens a window charting each player's bankroll with about
     * 500 points per player.
     */
    public void runWithDisplay(int rounds) {
        Graph graph = new Graph(playerStatContainer, rounds);
        runWithDisplay(rounds, graph);
    }

    /**
     * Same as {@link #runWithDisplay(int)}, with a point every {@code dx} rounds.
     */
    public void runWithDisplay(int rounds, int dx) {
        Graph graph = new Graph(playerStatContainer, rounds);
        graph.setDx(dx);
        runWithDisplay(rounds, graph);
    }

    /**
     * Same as {@link #runWithDisplay(int)}, with a graph you set up yourself. The graph has to be built on
     * {@link #getPlayerStatContainer()}, and it stops recording once its window opens.
     */
    public void runWithDisplay(int rounds, Graph graph) {
        this.run(rounds);
        graph.display();
        graph.close();
    }

    /**
     * Returns {@link #getResults(int, int)} for the first player, without hourly EV.
     */
    public String getFirstResults() {
        return getResults(0, 0);
    }

    /**
     * Returns {@link #getResults(int, int)} for the first player.
     */
    public String getFirstResults(int roundsPerHour) {
        return getResults(0, roundsPerHour);
    }

    /**
     * Returns {@link #getResults(int, int)} without hourly EV.
     */
    public String getResults(int i) {
        return getResults(i, 0);
    }

    /**
     * Returns a readable report of player {@code i}'s results, such as EV, edge, variance and risk of ruin.
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
        BlackjackTally playerStatistics = playerStatContainer.getPlayerTally(i);

        if (playerStatistics.getN() == 0) {
            throw new IllegalStateException("There are no rounds to look at yet.");
        }

        StringBuilder stringBuilder = new StringBuilder();

        String newline = System.lineSeparator();
        DecimalFormat df = new DecimalFormat("#,###.##");

        stringBuilder.append(newline);
        stringBuilder.append("Information").append(newline);
        stringBuilder.append("Rounds: ").append(String.format("%,d", playerStatistics.getN())).append(newline);
        stringBuilder.append("Bankroll: ~").append(df.format(player.getBankroll())).append(newline);
        stringBuilder.append("Starting Bankroll: ~").append(df.format(playerStatistics.getStartingBankroll())).append(newline);
        stringBuilder.append("Difference: ~").append(df.format(player.getBankroll() - playerStatistics.getStartingBankroll())).append(newline);
        if (player.isDead(game.getRules().getMinimumBet())) {
            stringBuilder.append("Status: Dead (cannot cover the table minimum)").append(newline);
        }
        stringBuilder.append(newline);
        stringBuilder.append("Profit").append(newline);
        stringBuilder.append("EV ($/round): ~$").append(df.format(playerStatistics.getEV())).append(newline);
        stringBuilder.append("95% CI ($/round): +/- $").append(new DecimalFormat("#.####").format(playerStatistics.getMarginOfError())).append(newline);

        if (roundsPerHour > 0) {
            stringBuilder.append("EV ($/hr): ~$").append(df.format(playerStatistics.getEV() * roundsPerHour)).append(newline);
        }

        stringBuilder.append("Average Bet ($/round): ~$").append(df.format(playerStatContainer.getAverageBet(i))).append(newline);
        stringBuilder.append("Player Edge: ~").append(new DecimalFormat("#.####").format(playerStatContainer.getEdge(i))).append("%").append(newline);
        stringBuilder.append("95% CI (Player Edge): +/- ").append(new DecimalFormat("#.####").format(playerStatContainer.getEdgeMarginOfError(i))).append("%").append(newline);

        stringBuilder.append(newline);
        stringBuilder.append("Information").append(newline);
        stringBuilder.append("EV (units/round): ~").append(df.format(playerStatistics.getUnitEV())).append(newline);
        stringBuilder.append("Standard Deviation (units): ~").append(df.format(playerStatistics.getStandardDeviation())).append(newline);
        stringBuilder.append("Variance (units): ~").append(df.format(playerStatistics.getVariance())).append(newline);
        stringBuilder.append(newline);
        stringBuilder.append("Additional Information").append(newline);
        stringBuilder.append("Risk of Ruin: ~").append(df.format(playerStatistics.getROR())).append("%").append(newline);
        stringBuilder.append("Max Drawdown: ~$").append(df.format(playerStatistics.getMaxDrawdown()))
                .append(" (").append(df.format(playerStatistics.getUnitMaxDrawdown())).append(" units)").append(newline);
        stringBuilder.append("N0 (rounds): ~").append(String.format("%,d", (int) Math.ceil(playerStatistics.getNZero()))).append(newline);
        stringBuilder.append("Sharpe Ratio: ~").append(new DecimalFormat("#.####").format(playerStatistics.getSharpeRatio())).append(newline);

        return stringBuilder.toString();
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
     * Moves the random stream back to its start. That alone doesn't replay earlier rounds, because the shoe keeps its
     * current order. Use {@link #reset()} to replay.
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
     * replaces it with a new one.
     */
    public BlackjackTallyList getPlayerStatContainer() {
        return playerStatContainer;
    }

    public BlackjackTally getFirstStats() {
        return playerStatContainer.getPlayerTally(0);
    }

    public BlackjackTally getStats(int i) {
        return playerStatContainer.getPlayerTally(i);
    }

    public Player getFirstPlayer() {
        return game.getPlayers()[0];
    }

    public Player getPlayer(int i) {
        return game.getPlayers()[i];
    }
}
