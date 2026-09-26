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
 */
public class Simulation {
    private final RandomStream stream = new MRG32k3a();
    private final SimpleGame game;
    private BlackjackTallyList playerStatContainer;

    public Simulation(SimpleGame game) {
        this.game = game;
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

    public void run(int rounds) {
        MonteCarloExperiment.simulateRuns(game, rounds, stream, playerStatContainer);
    }

    public void runWithDisplay(int rounds) {
        Graph graph = new Graph(playerStatContainer, rounds);
        runWithDisplay(rounds, graph);
    }

    public void runWithDisplay(int rounds, int dx) {
        Graph graph = new Graph(playerStatContainer, rounds);
        graph.setDx(dx);
        runWithDisplay(rounds, graph);
    }

    public void runWithDisplay(int rounds, Graph graph) {
        this.run(rounds);
        graph.display();
        graph.close();
    }

    public String getFirstResults() {
        return getResults(0, 0);
    }

    public String getFirstResults(int roundsPerHour) {
        return getResults(0, roundsPerHour);
    }

    public String getResults(int i) {
        return getResults(i, 0);
    }

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

    public void reset() {
        Player[] players = game.getPlayers();
        for (int i = 0; i < players.length; i++) {
            players[i].resetBankroll();
        }
        game.reset();
        this.resetRandom();
        this.resetStatistics();
    }

    public void resetRandom() {
        stream.resetStartStream();
    }

    public void resetStatistics() {
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

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
