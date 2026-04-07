package me.lel.simulation;

import me.lel.core.Rules;
import me.lel.player.better.impl.BetSpread;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class PlayerStatistics {
    private final int bettingUnit;
    private final SummaryStatistics stats = new SummaryStatistics();

    private final double startingBankroll;

    public PlayerStatistics(double startingBankroll, int bettingUnit) {
        if (bettingUnit < 1) {
            throw new IllegalArgumentException("Betting bet cannot be less than 1.");
        }

        this.startingBankroll = startingBankroll;
        this.bettingUnit = bettingUnit;
    }

    public void update(double value) {
        stats.addValue(value);
    }

    public double getEV() {
        return stats.getMean();
    }

    public double getUnitEV() {
        return stats.getMean() / bettingUnit;
    }

    public double getStandardDeviation() {
        return stats.getStandardDeviation() / bettingUnit;
    }

    public double getVariance() {
        return stats.getVariance() / (bettingUnit * bettingUnit);
    }

    // risk of ruin in percent
    public double getROR() {
        return Math.min(1, Math.exp(-2 * stats.getMean() * startingBankroll / stats.getVariance())) * 100;
    }

    public double getKellyFraction() {
        return bettingUnit * stats.getMean() / stats.getVariance();
    }

    public double getKelly() {
        return startingBankroll * stats.getMean() / stats.getVariance();
    }

    public long getN() {
        return stats.getN();
    }

    public double getStartingBankroll() {
        return startingBankroll;
    }

    public double getSharpeRatio() {
        return this.getEV() / this.getStandardDeviation();
    }

    public double getNZero() {
        return this.getVariance() / (this.getUnitEV() * this.getUnitEV());
    }

    // have to fully test this but I think it should work
    public double getHouseEdge(BetSpread betSpread, int minimum, Rules rules) {
        // determined from testing. Not sure why mu is -1
        NormalDistribution dist = new NormalDistribution(-1, 3);
        double avgBet = 0.0;

        for (int tc = minimum; tc < 1000; tc++) {
            double probability = dist.cumulativeProbability(tc + 1) - dist.cumulativeProbability(tc);
            avgBet += probability
                    * Math.min(rules.getMaximumBet(), betSpread.bet(tc).Units() * rules.getMinimumBet())
                    * Math.min(rules.getMaxHands(), betSpread.bet(tc).Hands());
        }

        return avgBet;
    }
}
