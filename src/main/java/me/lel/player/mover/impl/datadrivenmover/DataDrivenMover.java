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

/**
 * A {@link Mover} that looks up each decision in a strategy table, usually loaded from CSV with {@link #load}. The
 * bundled {@code H17Basic.csv} and {@code H17Deviations.csv} are examples.
 * <p>
 * The header row is {@code Hand} followed by the dealer up cards, {@code 2} to {@code 10} and then {@code A}. Each
 * other row starts with a hand total, prefixed with {@code S} when soft, such as {@code 16} or {@code S18}. Pairs
 * share the row of their total, so 8,8 is on row {@code 16} and A,A on row {@code S12}. The split moves in those rows
 * only apply when the hand can split, so other hands with that total skip them.
 * <p>
 * A cell lists moves from left to right:
 * <ul>
 *   <li>{@code E} surrender early (only read when it is the first move in the cell)</li>
 *   <li>{@code U} surrender</li>
 *   <li>{@code Y} split</li>
 *   <li>{@code /} split if doubling after a split is allowed</li>
 *   <li>{@code D} double, or hit if doubling isn't allowed; {@code DS} stands instead of hitting</li>
 *   <li>{@code H} hit</li>
 *   <li>{@code S} stand</li>
 * </ul>
 * The first move whose count condition holds is used, but surrender and split moves are skipped when the hand can't
 * make them. A move followed by {@code (n+)} only applies at a true count of {@code n} or more, and {@code (n-)} at
 * {@code n} or less. Zero is strict: {@code (0+)} needs a positive count and {@code (0-)} a negative one. For example,
 * {@code YU(4+)H} splits a pair, surrenders at a true count of 4 or more, and otherwise hits. A decision the table
 * doesn't cover is a stand.
 */
public class DataDrivenMover implements Mover {
    /**
     * Matches one move in a cell: its letter, then an optional count condition such as {@code (3+)}.
     */
    protected static final Pattern PATTERN = Pattern.compile("([EUYN/DHS])(?:\\((-?\\d+)([+-])\\))?");

    private final Map<Integer, Map<String, List<MoverAction>>> table;

    /**
     * Creates a mover from a table that is already parsed. It is keyed by the dealer's up card value (an ace is 1),
     * then by the hand's row name such as {@code "16"} or {@code "S18"}, and holds each cell's moves in order.
     */
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

    /**
     * Reads a strategy table from CSV in the format described on this class. The reader is left open.
     *
     * @throws IOException if reading fails
     */
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

    /**
     * Parses one cell into its moves, in order.
     */
    protected static List<MoverAction> parseStrategy(String s) {
        List<MoverAction> list = new ArrayList<>();

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

    /**
     * Returns the move a cell letter stands for.
     *
     * @throws IllegalArgumentException if the letter isn't a known move
     */
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
