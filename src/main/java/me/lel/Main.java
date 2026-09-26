package me.lel;

import me.lel.core.Rules;
import me.lel.counting.impl.HiLoCountSystem;
import me.lel.game.Blackjack;
import me.lel.player.Player;
import me.lel.player.better.impl.BetSpread;
import me.lel.player.mover.Mover;
import me.lel.player.mover.impl.datadrivenmover.DataDrivenMover;
import me.lel.player.sidebet.SideBetMover;
import me.lel.simulation.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * An example simulation built from the sample strategy files in {@code src/main/resources}. Edit {@code main} to try
 * other rules, players or strategies.
 */
public class Main {

    static void main(String[] args) throws IOException {
        SideBetMover sideBetMover = SideBetMover.load(getReader("sidebet.csv"));
        Mover m = DataDrivenMover.load(getReader("H17Deviations.csv"));
        Rules rules = new Rules.Builder().build();

        Player player = new Player(10_000, m, BetSpread.load(getReader("samplebet.csv")), sideBetMover);

        Blackjack bj = new Blackjack(new Player[]{player}, new HiLoCountSystem(), rules, 6);

        Simulation test = new Simulation(bj);
        test.runWithDisplay(50_000_000);
        System.out.println(test.getResults(0, 100));
    }

    /**
     * Opens a classpath resource, such as one of the sample CSV files.
     *
     * @throws NullPointerException if there is no resource called {@code name}
     */
    public static BufferedReader getReader(String name) {
        return new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(name))));
    }
}

