package me.lel.player.sidebet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decides which side bets to take, by name, from the true count. Side bets are optional bets placed next to the main
 * hand, such as insurance.
 */
public class SideBetMover {
    private final static Pattern PATTERN = Pattern.compile("([A-z ]+),(-?\\d+)([+-])");

    private final Map<String, SideBet> bets;

    /**
     * Creates a mover that takes no side bets.
     */
    public SideBetMover() {
        this.bets = new HashMap<>();
    }

    /**
     * Creates a mover from side bets keyed by name.
     *
     * @param bets when to take each side bet
     */
    public SideBetMover(Map<String, SideBet> bets) {
        this.bets = bets;
    }

    /**
     * Sets when to take {@code bet}: at or above a true count of {@code count} if {@code above} is set, otherwise at or
     * below it. Replaces any earlier setting for that bet.
     *
     * @param bet   the side bet's name
     * @param count the true count threshold
     * @param above {@code true} to bet at or above {@code count}, {@code false} to bet at or below it
     */
    public void add(String bet, int count, boolean above) {
        bets.put(bet, new SideBet(count, above));
    }

    /**
     * Stops taking {@code bet}.
     *
     * @param bet the side bet's name
     */
    public void remove(String bet) {
        bets.remove(bet);
    }

    /**
     * Returns whether to take {@code bet} at this true count. Bets it doesn't know are never taken.
     *
     * @param bet   the side bet's name
     * @param count current true count
     * @return {@code true} to take the bet
     */
    public boolean valid(String bet, double count) {
        if (bets.containsKey(bet)) {
            return bets.get(bet).valid(count);
        }
        return false;
    }

    /**
     * Reads side bets from CSV, such as the bundled {@code sidebet.csv}. The first line is a header. Each line after it
     * is a bet name and a count with a direction, such as {@code insurance,3+}. The reader is left open.
     *
     * @param br the CSV to read
     * @return the side bet mover
     * @throws IOException if reading fails
     */
    public static SideBetMover load(BufferedReader br) throws IOException {
        Map<String, SideBet> bets = new HashMap<>();

        br.readLine();

        Matcher m = PATTERN.matcher(br.readAllAsString());
        while (m.find()) {
            bets.put(m.group(1), new SideBet(Integer.parseInt(m.group(2)), (m.group(3).equals("+"))));
        }
        return new SideBetMover(bets);
    }
}
