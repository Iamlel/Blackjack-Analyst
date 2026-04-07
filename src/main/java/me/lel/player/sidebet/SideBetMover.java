package me.lel.player.sidebet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SideBetMover {
    private final static Pattern PATTERN = Pattern.compile("([A-z ]+),(-?\\d+)([+-])");
    private final Map<String, SideBet> bets;

    public SideBetMover() {
        this.bets = new HashMap<>();
    }

    public SideBetMover(Map<String, SideBet> bets) {
        this.bets = bets;
    }

    public void add(String bet, int count, boolean above) {
        bets.put(bet, new SideBet(count, above));
    }

    public void remove(String bet) {
        bets.remove(bet);
    }

    public boolean valid(String bet, double count) {
        if (bets.containsKey(bet)) {
            return bets.get(bet).valid(count);
        }
        return false;
    }

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
