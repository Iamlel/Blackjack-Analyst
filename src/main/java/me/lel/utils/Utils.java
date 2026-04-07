package me.lel.utils;

public class Utils {
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
