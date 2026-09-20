package me.lel.simulation;

import me.lel.game.SimpleGame;
import me.lel.player.Player;
import me.lel.simulation.ssj.BlackjackTally;
import me.lel.simulation.ssj.BlackjackTallyList;
import umontreal.ssj.mcqmctools.MonteCarloExperiment;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.stat.list.ListOfTalliesWithCovariance;

import java.text.DecimalFormat;

// FIXME : clarify everywhere that when you run the sim it is rounds not hands and make sure that it is.
// TODO : kill dead players
public class Simulation {
    private final RandomStream stream = new MRG32k3a();
    private final SimpleGame game;
    private BlackjackTallyList<BlackjackTally> playerStatContainer;

    public Simulation(SimpleGame game) {
        assert game.getPlayers().length == game.getPerformanceDim();

        this.game = game;
        this.playerStatContainer = BlackjackTallyList.create(game);
    }

    public void run(int hands) {
        MonteCarloExperiment.simulateRuns(game, hands, stream, playerStatContainer);
    }

    public void runWithDisplay(int hands) {
        Graph graph = new Graph(playerStatContainer, hands);
        runWithDisplay(hands, graph);
    }

    public void runWithDisplay(int hands, int dx) {
        Graph graph = new Graph(playerStatContainer, hands);
        graph.setDx(dx);
        runWithDisplay(hands, graph);
    }

    public void runWithDisplay(int hands, Graph graph) {
        this.run(hands);
        graph.display();
        graph.close();
    }

    public void firstResults() {
        results(0, 0);
    }

    public void firstResults(int handsPerHour) {
        results(0, handsPerHour);
    }

    public void results(int i) {
        results(i, 0);
    }

    public void results(int i, int handsPerHour) {
        if (i >= playerStatContainer.size()) {
            throw new IndexOutOfBoundsException("There are not enough players for that.");
        }

        Player player = this.getPlayer(i);
        BlackjackTally playerStatistics = playerStatContainer.get(i);

        if (playerStatistics.getN() == 0) {
            throw new IllegalStateException("There are no hands to look at yet.");
        }

        DecimalFormat df = new DecimalFormat("#,###.##");

        System.out.println();
        System.out.println("Information");
        System.out.println("Rounds: " + String.format("%,d", playerStatistics.getN()));
        System.out.println("Bankroll: ~" + df.format(player.getBankroll()));
        System.out.println("Starting Bankroll: ~" + df.format(playerStatistics.getStartingBankroll()));
        System.out.println("Difference: ~" + df.format(player.getBankroll() - playerStatistics.getStartingBankroll()));
        // TODO : figure out a way to find average bet size
        // Edge = EV / Average Bet Size (all in $)
        //System.out.println("Edge: ~" + playerStatistics.getEV());
        System.out.println();
        System.out.println("Profit");
        System.out.println("EV ($/hand): ~$" + df.format(playerStatistics.getEV()));
        System.out.println("95% CI ($/hand): +/- $" + new DecimalFormat("#.####").format(playerStatistics.getMarginOfError()));

        if (handsPerHour > 0) {
            System.out.println("EV ($/hr): ~$" + df.format(playerStatistics.getEV() * handsPerHour));
        }

        System.out.println();
        System.out.println("Information");
        System.out.println("EV (units/hand): ~" + df.format(playerStatistics.getUnitEV()));
        System.out.println("Standard Deviation (units): ~" + df.format(playerStatistics.getStandardDeviation()));
        System.out.println("Variance (units): ~" + df.format(playerStatistics.getVariance()));
        System.out.println();
        System.out.println("Additional Information");
        System.out.println("Risk of Ruin: ~" + df.format(playerStatistics.getROR()) + "%");
        System.out.println("N0 (hands): ~" + String.format("%,d", (int) Math.ceil(playerStatistics.getNZero())));
        System.out.println("Sharpe Ratio: ~" + new DecimalFormat("#.####").format(playerStatistics.getSharpeRatio()));
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

    public ListOfTalliesWithCovariance<BlackjackTally> getPlayerStatContainer() {
        return playerStatContainer;
    }

    public BlackjackTally getFirstStats() {
        return playerStatContainer.getFirst();
    }

    public BlackjackTally getStats(int i) {
        return playerStatContainer.get(i);
    }

    public Player getFirstPlayer() {
        return game.getPlayers()[0];
    }

    public Player getPlayer(int i) {
        return game.getPlayers()[i];
    }
}
