package me.lel.simulation.ssj;

import umontreal.ssj.stat.Tally;

// TODO : Make a different Tally that has order included so we can have drawdown in that and put that in the Simulation lib that you make

/**
 * An SSJ {@link Tally} of one player's profit per round, in dollars, with blackjack statistics on top.
 * <p>
 * It also tracks the maximum drawdown, which depends on the order of the observations, so add rounds in the order
 * they were played.
 */
public class BlackjackTally extends Tally {
    private final int bettingUnit;
    private final double startingBankroll;

    // bankroll change so far, its highest point, and the largest drop from a highest point (all in $)
    private double profit;
    private double peakProfit;
    private double maxDrawdown;

    /**
     * Creates a tally for a player betting in units of {@code bettingUnit} dollars and starting with
     * {@code startingBankroll}.
     *
     * @param bettingUnit      dollar size of one betting unit
     * @param startingBankroll bankroll used for risk of ruin
     */
    public BlackjackTally(int bettingUnit, double startingBankroll) {
        this.bettingUnit = bettingUnit;
        this.startingBankroll = startingBankroll;
        super();
    }

    /**
     * Same as {@link #BlackjackTally(int, double)}, with a name for SSJ reports.
     *
     * @param name             name shown in SSJ reports
     * @param bettingUnit      dollar size of one betting unit
     * @param startingBankroll bankroll used for risk of ruin
     */
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

    /**
     * Returns the EV (expected value): the mean profit per round, in dollars.
     *
     * @return the EV in dollars
     */
    public double getEV() {
        return this.average();
    }

    /**
     * Returns the EV in betting units.
     *
     * @return the EV in betting units
     */
    public double getUnitEV() {
        return this.average() / bettingUnit;
    }

    /**
     * Returns the standard deviation of profit per round, in betting units. SSJ's {@link #standardDeviation()} gives it
     * in dollars.
     *
     * @return the standard deviation in betting units
     */
    public double getStandardDeviation() {
        return this.standardDeviation() / bettingUnit;
    }

    /**
     * Returns the variance of profit per round, in squared betting units. SSJ's {@link #variance()} gives it in squared
     * dollars.
     *
     * @return the variance in squared betting units
     */
    public double getVariance() {
        return this.variance() / (bettingUnit * bettingUnit);
    }

    /**
     * Returns the half-width of a 95% Student-t confidence interval on {@link #getEV()}, in dollars.
     *
     * @return the margin of error in dollars
     */
    public double getMarginOfError() {
        return this.getMarginOfError(0.95);
    }

    /**
     * Same as {@link #getMarginOfError()} at another confidence level.
     *
     * @param level the confidence level, such as 0.99
     * @return the margin of error in dollars
     */
    public double getMarginOfError(double level) {
        double[] centerAndRadius = new double[2];
        this.confidenceIntervalStudent(level, centerAndRadius);
        return centerAndRadius[1];
    }

    /**
     * Returns {@link #getROR(double)} for the starting bankroll.
     *
     * @return the risk of ruin as a percentage
     */
    public double getROR() {
        return this.getROR(startingBankroll);
    }

    /**
     * Returns the risk of ruin: the chance of losing the whole bankroll when starting with {@code startingBankroll}
     * dollars and playing forever at this EV and variance. It uses the approximation
     * {@code exp(-2 * EV * bankroll / variance)}.
     *
     * @param startingBankroll the bankroll to start from, in dollars
     * @return the risk of ruin as a percentage
     */
    public double getROR(double startingBankroll) {
        return Math.min(1, Math.exp(-2 * this.average() * startingBankroll / this.variance())) * 100;
    }

    /**
     * Returns the number of rounds recorded.
     *
     * @return the number of rounds
     */
    public int getN() {
        return this.numberObs();
    }

    /**
     * Returns EV divided by standard deviation, per round. Higher means more profit for the same swings.
     *
     * @return the Sharpe ratio
     */
    public double getSharpeRatio() {
        return this.getUnitEV() / this.getStandardDeviation();
    }

    /**
     * Returns N0: the number of rounds it takes for total EV to equal one standard deviation of total results. A lower
     * N0 means the edge shows through the swings sooner.
     *
     * @return N0 in rounds
     */
    public double getNZero() {
        return this.getVariance() / (this.getUnitEV() * this.getUnitEV());
    }

    /**
     * Returns SCORE (standardized comparison of risk and expectation): the expected profit in dollars per 100 rounds
     * for a $10,000 bankroll betting full Kelly, the bet size that grows a bankroll fastest. It lets you compare games
     * and strategies with different bet sizes.
     *
     * @return SCORE in dollars per 100 rounds
     */
    public double getSCORE() {
        return 1_000_000 * getSharpeRatio() * getSharpeRatio();
    }

    /**
     * Returns the largest drop, in dollars, from a bankroll high to a later low.
     *
     * @return the maximum drawdown in dollars
     */
    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    /**
     * Returns {@link #getMaxDrawdown()} in betting units.
     *
     * @return the maximum drawdown in betting units
     */
    public double getUnitMaxDrawdown() {
        return maxDrawdown / bettingUnit;
    }

    /**
     * Returns the dollar size of one betting unit.
     *
     * @return the betting unit in dollars
     */
    public int getBettingUnit() {
        return bettingUnit;
    }

    /**
     * Returns the bankroll used for risk of ruin.
     *
     * @return the starting bankroll in dollars
     */
    public double getStartingBankroll() {
        return startingBankroll;
    }
}
