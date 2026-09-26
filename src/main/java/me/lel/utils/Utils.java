package me.lel.utils;

public class Utils {
    /**
     * Checks a true count against a threshold and returns {@code true} when the condition <em>fails</em>.
     * <p>
     * With {@code above} set, the condition is {@code x >= y}, and otherwise it is {@code x <= y}. A threshold of 0 is
     * strict, so the condition becomes {@code x > 0} or {@code x < 0}. A {@code null} threshold always passes.
     *
     * @param x     the true count
     * @param y     the threshold, or {@code null} for no condition
     * @param above {@code true} for at or above the threshold, {@code false} for at or below it
     * @return {@code true} if the condition fails
     */
    public static boolean pointComparison(double x, Integer y, boolean above) {
        if (y == null) {
            return false;
        }

        if (y == 0) {
            if (above) {
                return (x <= 0);
            }
            return (x >= 0);
        }

        if (above) {
            return (x < y);
        }
        return (x > y);
    }
}
