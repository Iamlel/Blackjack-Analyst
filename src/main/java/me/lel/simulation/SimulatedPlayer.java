package me.lel.simulation;

import me.lel.player.Player;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.List;

public class SimulatedPlayer {
    private final Player player;
    private final PlayerStatistics playerStatistics;
    private final SimpleRegression playerRegression = new SimpleRegression(false);

    private final List<Double> bankrolls = new ArrayList<>();
    private double previousBankroll;

    private final boolean saveData;
    private final int dx;

    public SimulatedPlayer(Player player, PlayerStatistics playerStatistics, boolean saveData, int dx) {
        this.player = player;
        this.playerStatistics = playerStatistics;
        this.saveData = saveData;

        this.previousBankroll = player.getBankroll();

        this.dx = dx;
    }

    public void update() {
        double bankroll = player.getBankroll();
        double profit = bankroll - previousBankroll;
        playerStatistics.update(profit);

        double bankrollChange = bankroll - playerStatistics.getStartingBankroll();
        playerRegression.addData(playerStatistics.getN(), bankrollChange);

        if (saveData && bankroll != 0 && playerStatistics.getN() % this.dx == 0) {
            bankrolls.add(bankrollChange);
        }

        this.previousBankroll = bankroll;
    }

    public Player getPlayer() {
        return player;
    }

    public double[][] getData() {
        double[][] data = new double[this.bankrolls.size()][2];

        for (int i = 0; i < this.bankrolls.size(); i++) {
            data[i][0] = dx * i + 1;
            data[i][1] = bankrolls.get(i);
        }

        return data;
    }

    public PlayerStatistics getPlayerStatistics() {
        return playerStatistics;
    }

    public SimpleRegression getPlayerRegression() {
        return playerRegression;
    }
}
