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
    private final int numerator;
    private final int denominator;
    private final int dimension;

    private final RatioFunction ratio = new RatioFunction();

    /**
     * Creates {@code x[numerator] / x[denominator]} over vectors with {@code dimension} entries.
     *
     * @param numerator   index of the numerator
     * @param denominator index of the denominator
     * @param dimension   length of the vectors
     * @throws IndexOutOfBoundsException if either index is outside the vector
     * @throws IllegalArgumentException  if both indices are the same
     */
    public IndexedRatioFunction(int numerator, int denominator, int dimension) {
        if (numerator < 0 || numerator >= dimension || denominator < 0 || denominator >= dimension) {
            throw new IndexOutOfBoundsException("The numerator and denominator must be inside the dimension.");
        }
        // x[i] / x[i] is constant, so the gradient below (one partial per entry) would be wrong for it
        if (numerator == denominator) {
            throw new IllegalArgumentException("The numerator and denominator must be different entries.");
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
        checkLength(x);
        return ratio.evaluate(x[numerator], x[denominator]);
    }

    @Override
    public double evaluateGradient(int i, double... x) {
        checkLength(x);
        if (i < 0 || i >= dimension) {
            throw new IndexOutOfBoundsException("Invalid value of i: " + i);
        }
        if (i == numerator) {
            return ratio.evaluateGradient(0, x[numerator], x[denominator]);
        }
        if (i == denominator) {
            return ratio.evaluateGradient(1, x[numerator], x[denominator]);
        }
        return 0;
    }

    // MultivariateFunction requires x to have exactly getDimension() entries
    private void checkLength(double[] x) {
        if (x.length != dimension) {
            throw new IllegalArgumentException("Invalid length of x: " + x.length + ", required " + dimension);
        }
    }
}
