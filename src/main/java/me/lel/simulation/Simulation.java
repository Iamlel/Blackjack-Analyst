package me.lel.simulation;

import me.lel.game.Game;
import me.lel.player.Player;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private final Game game;
    private final List<SimulatedPlayer> players = new ArrayList<>();;

    /**
     * @param game The Game to simulate.
     */
    public Simulation(Game game) {
        this(game, 1, true);
    }

    /**
     * @param game The Game to simulate.
     * @param saveData Whether data should be saved in an array to graph.
     */
    public Simulation(Game game, boolean saveData) {
        this(game, 1, saveData);
    }

    /**
     * @param game The Game to simulate.
     * @param dx The interval at which the data is saved.
     * @param saveData Whether data should be saved in an array to graph.
     */
    public Simulation(Game game, int dx, boolean saveData) {
        this.game = game;

        for (Player player : game.getPlayers()) {
            players.add(new SimulatedPlayer(player, new PlayerStatistics(player.getBankroll(), game.getRules().getMinimumBet()), saveData, dx));
        }
    }

    public void run(int simulations) {
        for (int i = 0; i < simulations; i++) {
            game.play();
            for (SimulatedPlayer player : players) {
                player.update();
            }
        }
    }

    public void viewFirst() {
        view(0, 0);
    }

    public void viewFirst(int handsPerHour) {
        view(0, handsPerHour);
    }

    public void view(int i) {
        view(i, 0);
    }

    public void view(int i, int handsPerHour) {
        if (i >= players.size()) {
            throw new IndexOutOfBoundsException("There are not enough players for that.");
        }

        Player player = players.get(i).getPlayer();
        PlayerStatistics playerStatistics = this.getPlayerStatistics(i);
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
        System.out.println("Kelly bet (units): ~" + df.format(playerStatistics.getKelly()));
        System.out.println("N0 (hands): ~" + String.format("%,d", (int) Math.ceil(playerStatistics.getNZero())));
        System.out.println("Sharpe Ratio: ~" + new DecimalFormat("#.####").format(playerStatistics.getSharpeRatio()));
    }

    public void display() {
        if (players.getFirst().getData().length == 0) {
            return;
        }

        Plot2DPanel plot = new Plot2DPanel();

        plot.setAxisLabel(0, "Hands");
        plot.setAxisLabel(1, "Bankroll Change");
        plot.addLegend("NORTH");

        if (players.size() == 1) {
            SimulatedPlayer player = players.getFirst();

            plot.addLinePlot("Player", player.getData());

            int n = Math.toIntExact(player.getPlayerStatistics().getN());
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                y[i] = player.getPlayerRegression().getSlope() * i;
            }
            plot.addLinePlot("EV ($)", y);

        } else {
            for (int i = 1; i <= players.size(); i++) {
                plot.addLinePlot("Player " + i, players.get(i - 1).getData());
            }
        }

        JFrame frame = new JFrame("Blackjack Analyst");
        frame.setContentPane(plot);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) (screenSize.width * 0.75);
        int height = (int) (screenSize.height * 0.75);
        frame.setSize(width, height);

        frame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public PlayerStatistics getPlayerStatistics() {
        return players.getFirst().getPlayerStatistics();
    }

    public PlayerStatistics getPlayerStatistics(int i) {
        return players.get(i).getPlayerStatistics();
    }

    public Player getPlayer() {
        return players.getFirst().getPlayer();
    }

    public Player getPlayer(int i) {
        return players.get(i).getPlayer();
    }
}
