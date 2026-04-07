package me.lel.player.mover.impl.datadrivenmover;

import me.lel.core.ActiveRules;
import me.lel.core.action.Action;
import me.lel.core.action.SimpleAction;
import me.lel.core.action.SpecialAction;
import me.lel.player.mover.Mover;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataDrivenMover implements Mover {
    protected static final Pattern PATTERN = Pattern.compile("([EUYN/DHS])(?:\\((-?\\d+)([+-])\\))?");

    private final Map<Integer, Map<String, List<MoverAction>>> table;

    public DataDrivenMover(Map<Integer, Map<String, List<MoverAction>>> table) {
        this.table = table;
    }

    @Override
    public Action action(int hand, int dealerHand, boolean soft, ActiveRules rules, double trueCount) {
        if (table.get(dealerHand) == null) {
            return Action.STAND;
        }

        String handValue = (soft ? "S" : "") + hand;
        if (table.get(dealerHand).get(handValue) == null) {
            return Action.STAND;
        }

        Iterator<MoverAction> actionIterator = table.get(dealerHand).get(handValue).iterator();
        while (actionIterator.hasNext()) {
            MoverAction action = actionIterator.next();

            if (action.invalid(trueCount)) {
                continue;
            }

            if (rules.canSurrender() && action.action() == Action.SURRENDER) {
                return Action.SURRENDER;

            } else if (rules.canSplit() && (action.action() == Action.SPLIT || (action.action() == SpecialAction.SPLIT_DAS && rules.das()))) {
                return Action.SPLIT;

            } else if (action.action() == Action.DOUBLE) {
                if (actionIterator.hasNext() && actionIterator.next().action() == Action.STAND) {
                    if (rules.canDouble()) {
                        return Action.DOUBLE_STAND;
                    }
                    return Action.STAND;
                }

                if (rules.canDouble()) {
                    return Action.DOUBLE;
                }
                return Action.HIT;

            } else if (action.action() == Action.HIT) {
                return Action.HIT;

            } else if (action.action() == Action.STAND) {
                return Action.STAND;
            }
        }
        return Action.STAND;
    }

    @Override
    public boolean earlySurrender(int hand, int dealerHand, boolean soft, double trueCount) {
        if (table.get(dealerHand) == null) {
            return false;
        }

        String handValue = (soft ? "S" : "") + hand;
        if (table.get(dealerHand).get(handValue) == null) {
            return false;
        }

        MoverAction action = table.get(dealerHand).get(handValue).getFirst();
        if (action.invalid(trueCount)) {
            return false;
        }

        return (action.action() == SpecialAction.EARLY_SURRENDER);
    }

    public static Mover load(BufferedReader br) throws IOException {
        Map<Integer, Map<String, List<MoverAction>>> table = new HashMap<>();

        String header = br.readLine();
        String[] headerParts = header.split(",");

        int[] columnKeys = new int[headerParts.length - 1];
        for (int i = 1; i < headerParts.length; i++) {
            columnKeys[i - 1] = Objects.equals(headerParts[i], "A") ? 1 : Integer.parseInt(headerParts[i]);
            table.put(columnKeys[i - 1], new HashMap<>());
        }

        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");

            for (int i = 1; i < parts.length; i++) {
                table.get(columnKeys[i - 1]).put(parts[0], parseStrategy(parts[i]));
            }
        }
        return new DataDrivenMover(table);
    }

    protected static List<MoverAction> parseStrategy(String s) {
        List<MoverAction> list = new ArrayList<>();

        // regex: letter followed by optional (number+/-)
        Matcher matcher = PATTERN.matcher(s);

        while (matcher.find()) {
            SimpleAction action = getSimpleAction(matcher.group(1).charAt(0));
            String numStr = matcher.group(2);
            String sign = matcher.group(3);

            Integer count = numStr != null ? Integer.parseInt(numStr) : null;
            boolean above = sign!= null && sign.equals("+");

            list.add(new MoverAction(action, count, above));
        }

        return list;
    }

    protected static SimpleAction getSimpleAction(char letter) {
        return switch (letter) {
            case 'E' -> SpecialAction.EARLY_SURRENDER;
            case 'U' -> Action.SURRENDER;
            case 'Y' -> Action.SPLIT;
            case '/' -> SpecialAction.SPLIT_DAS;
            case 'D' -> Action.DOUBLE;
            case 'H' -> Action.HIT;
            case 'S' -> Action.STAND;
            default -> throw new IllegalArgumentException("Unknown action: " + letter);
        };
    }
}
