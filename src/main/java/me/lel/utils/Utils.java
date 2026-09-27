package me.lel.utils;

public final class Utils {
    /**
     * Returns {@code true} when {@code x} fails a comparison against the point {@code y}.
     * <p>
     * With {@code above} set, {@code x} has to be at or above {@code y}, and otherwise at or below it. A point of 0 is
     * strict, so {@code x} has to be above or below 0, never exactly 0. A {@code null} point accepts every {@code x}.
     *
     * @param x     the value to compare
     * @param y     the point, or {@code null} for no comparison
     * @param above {@code true} to require {@code x} at or above {@code y}, {@code false} to require it at or below
     * @return {@code true} if {@code x} fails the comparison
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
