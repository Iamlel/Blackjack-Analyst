package me.lel.simulation.ssj;

import umontreal.ssj.stat.Tally;

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
     * Creates a tally for a player betting in units of {@code bettingUnit} dollars (normally the table minimum) and
     * starting with {@code startingBankroll}.
     */
    public BlackjackTally(int bettingUnit, double startingBankroll) {
        this.bettingUnit = bettingUnit;
        this.startingBankroll = startingBankroll;
        super();
    }

    /**
     * Same as {@link #BlackjackTally(int, double)}, with a name for SSJ reports.
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
     * Returns the mean profit per round, in dollars.
     */
    public double getEV() {
        return this.average();
    }

    /**
     * Returns the mean profit per round, in betting units.
     */
    public double getUnitEV() {
        return this.average() / bettingUnit;
    }

    /**
     * Returns the standard deviation of profit per round, in betting units. SSJ's {@link #standardDeviation()} gives
     * it in dollars.
     */
    public double getStandardDeviation() {
        return this.standardDeviation() / bettingUnit;
    }

    /**
     * Returns the variance of profit per round, in squared betting units. SSJ's {@link #variance()} gives it in
     * squared dollars.
     */
    public double getVariance() {
        return this.variance() / (bettingUnit * bettingUnit);
    }

    /**
     * Returns the half-width of a 95% Student-t confidence interval on {@link #getEV()}, in dollars.
     */
    public double getMarginOfError() {
        return this.getMarginOfError(0.95);
    }

    /**
     * Same as {@link #getMarginOfError()} at confidence {@code level}, such as 0.99.
     */
    public double getMarginOfError(double level) {
        double[] centerAndRadius = new double[2];
        this.confidenceIntervalStudent(level, centerAndRadius);
        return centerAndRadius[1];
    }

    /**
     * Returns {@link #getROR(double)} for the starting bankroll. That bankroll is 0 for a
     * {@link me.lel.player.FakePlayer}, so pass a real one to {@link #getROR(double)} instead.
     */
    public double getROR() {
        return this.getROR(startingBankroll);
    }

    /**
     * Returns the risk of ruin as a percentage: the chance that a player starting with {@code startingBankroll}
     * dollars goes broke if they keep playing forever at this EV and variance. It uses the approximation
     * {@code exp(-2 * EV * bankroll / variance)}.
     */
    public double getROR(double startingBankroll) {
        return Math.min(1, Math.exp(-2 * this.average() * startingBankroll / this.variance())) * 100;
    }

    /**
     * Returns the number of rounds recorded.
     */
    public int getN() {
        return this.numberObs();
    }

    /**
     * Returns EV divided by standard deviation, per round.
     */
    public double getSharpeRatio() {
        return this.getUnitEV() / this.getStandardDeviation();
    }

    /**
     * Returns N0, the number of rounds it takes for total EV to equal one standard deviation of total results.
     */
    public double getNZero() {
        return this.getVariance() / (this.getUnitEV() * this.getUnitEV());
    }

    /**
     * Returns SCORE (standardized comparison of risk and expectation): the expected profit in dollars per 100 rounds
     * for a $10,000 bankroll betting full Kelly.
     */
    public double getSCORE() {
        return 1_000_000 * getSharpeRatio() * getSharpeRatio();
    }

    /**
     * Returns the largest drop, in dollars, from a bankroll high to a later low.
     */
    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    /**
     * Returns {@link #getMaxDrawdown()} in betting units.
     */
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
