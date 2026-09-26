package me.lel.simulation.ssj;

import umontreal.ssj.util.MultivariateFunction;
import umontreal.ssj.util.RatioFunction;

/**
 * A {@link RatioFunction} applied to two entries of a longer vector: {@code x[numerator] / x[denominator]}.
 * <p>
 * This lets a {@link umontreal.ssj.stat.FunctionOfMultipleMeansTally} compute a ratio of two means straight from a
 * list with more than two tallies. The gradient is 0 for every other entry, so only those two tallies and their
 * covariance count in the delta method confidence interval.
 */
public class IndexedRatioFunction implements MultivariateFunction {
    private final RatioFunction ratio = new RatioFunction();
    private final int numerator;
    private final int denominator;
    private final int dimension;

    public IndexedRatioFunction(int numerator, int denominator, int dimension) {
        if (numerator < 0 || numerator >= dimension || denominator < 0 || denominator >= dimension) {
            throw new IndexOutOfBoundsException("The numerator and denominator must be inside the dimension.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
        this.dimension = dimension;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public double evaluate(double... x) {
        return ratio.evaluate(x[numerator], x[denominator]);
    }

    @Override
    public double evaluateGradient(int i, double... x) {
        if (i == numerator) {
            return ratio.evaluateGradient(0, x[numerator], x[denominator]);
        }
        if (i == denominator) {
            return ratio.evaluateGradient(1, x[numerator], x[denominator]);
        }
        return 0;
    }
}
