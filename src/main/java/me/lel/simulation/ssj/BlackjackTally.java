package me.lel.simulation.ssj;

import umontreal.ssj.stat.Tally;

// Make a different Tally that has order included so we can have drawdown in that and put that in the Simulation lib that you make
public class BlackjackTally extends Tally {
    private final int bettingUnit;
    private final double startingBankroll;

    // bankroll change so far, its highest point, and the largest drop from a highest point (all in $)
    private double profit;
    private double peakProfit;
    private double maxDrawdown;

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

    @Override
    public void init() {
        super.init();
        this.profit = 0;
        this.peakProfit = 0;
        this.maxDrawdown = 0;
    }

    @Override
    public void add(double x) {
        super.add(x);
        if (!collect) {
            return;
        }

        this.profit += x;
        this.peakProfit = Math.max(peakProfit, profit);
        this.maxDrawdown = Math.max(maxDrawdown, peakProfit - profit);
    }

    // $ per round
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
        return this.getROR(startingBankroll);
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

    // largest drop in $ from a bankroll high to a later low
    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getUnitMaxDrawdown() {
        return maxDrawdown / bettingUnit;
    }

    public int getBettingUnit() {
        return bettingUnit;
    }

    public double getStartingBankroll() {
        return startingBankroll;
    }
}
