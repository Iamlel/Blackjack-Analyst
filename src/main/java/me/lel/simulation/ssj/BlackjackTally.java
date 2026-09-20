package me.lel.simulation.ssj;

import me.lel.core.Rules;
import me.lel.player.better.impl.BetSpread;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.stat.Tally;

// TODO : deleted kelly bet because the bet ramp is fixed, but once you make it change then add it back
public class BlackjackTally extends Tally {
    private final int bettingUnit;
    private final double startingBankroll;

    public BlackjackTally(int bettingUnit, double startingBankroll) {
        this.bettingUnit = bettingUnit;
        this.startingBankroll = startingBankroll;
        super();
    }

    public BlackjackTally(String name, int bettingUnit, double startingBankroll) {
        this.bettingUnit = bettingUnit;
        this.startingBankroll = startingBankroll;
        super(name);
    }

    public double getEV() {
        return this.average();
    }

    public double getUnitEV() {
        return this.average() / bettingUnit;
    }

    public double getStandardDeviation() {
        return this.standardDeviation() / bettingUnit;
    }

    public double getVariance() {
        return this.variance() / (bettingUnit * bettingUnit);
    }

    public double getMarginOfError() {
        return this.getMarginOfError(0.95);
    }

    public double getMarginOfError(double level) {
        double[] centerAndRadius = new double[2];
        this.confidenceIntervalStudent(level, centerAndRadius);
        return centerAndRadius[1];
    }

    // risk of ruin in percent
    public double getROR() {
        return Math.min(1, Math.exp(-2 * this.average() * startingBankroll / this.variance())) * 100;
    }

    // risk of ruin in percent
    public double getROR(double startingBankroll) {
        return Math.min(1, Math.exp(-2 * this.average() * startingBankroll / this.variance())) * 100;
    }

    public int getN() {
        return this.numberObs();
    }

    public double getSharpeRatio() {
        return this.getUnitEV() / this.getStandardDeviation();
    }

    public double getNZero() {
        return this.getVariance() / (this.getUnitEV() * this.getUnitEV());
    }

    public double getSCORE() {
        return 1_000_000 * getSharpeRatio() * getSharpeRatio();
    }

    // TODO : have to fully test this but I think it should work
    public double getHouseEdge(BetSpread betSpread, int minimum, Rules rules) {
        // determined from testing. Not sure why mu is -1
        double avgBet = 0.0;

        for (int tc = minimum; tc < 1000; tc++) {
            double probability = NormalDist.cdf(-1, 3, tc + 1) - NormalDist.cdf(-1, 3, tc);
            avgBet += probability
                    * Math.min(rules.getMaximumBet(), betSpread.bet(tc).Units() * rules.getMinimumBet())
                    * Math.min(rules.getMaxHands(), betSpread.bet(tc).Hands());
        }

        return avgBet;
    }

    public int getBettingUnit() {
        return bettingUnit;
    }

    public double getStartingBankroll() {
        return startingBankroll;
    }
}
