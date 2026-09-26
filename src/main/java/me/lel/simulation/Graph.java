package me.lel.simulation;

import me.lel.simulation.ssj.BlackjackTallyList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import umontreal.ssj.stat.list.ArrayOfObservationListener;
import umontreal.ssj.stat.list.ListOfStatProbes;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

/**
 * Charts each player's bankroll change during a simulation, using JFreeChart. It listens to a
 * {@link BlackjackTallyList} while rounds are recorded and keeps a point every {@code dx} rounds. With a single player,
 * the chart also draws the EV line, the path the bankroll would follow at exactly the measured EV.
 */
public class Graph {
    private final BlackjackTallyList playerStatContainer;
    private final ArrayOfObservationListener listener;

    private final List<List<Double>> bankrolls = new ArrayList<>();
    private final double[] bankrollChange;
    private int dx;

    /**
     * Creates a graph that records every round added to {@code playerStatContainer} from now on. Broadcasting is turned
     * on for the container.
     *
     * @param playerStatContainer       the tallies to follow
     * @param totalMeasurementsExpected rounds you expect to record, used to space about 500 points per player
     */
    public Graph(BlackjackTallyList playerStatContainer, int totalMeasurementsExpected) {
        this.playerStatContainer = playerStatContainer;
        this.listener = this::record;

        for (int i = 0; i < playerStatContainer.getPlayerCount(); i++) {
            bankrolls.add(new ArrayList<>());
        }
        this.bankrollChange = new double[playerStatContainer.getPlayerCount()];
        // No reason why it should be 500, but it seems to work well enough
        this.dx = (int) Math.ceil((double) totalMeasurementsExpected / 500);

        this.playerStatContainer.setBroadcasting(true);
        this.playerStatContainer.addArrayOfObservationListener(listener);
    }

    // the first entries of the performance vector are the profits, the rest are the wagers
    private void record(ListOfStatProbes<?> probes, double[] performance) {
        for (int i = 0; i < bankrollChange.length; i++) {
            bankrollChange[i] += performance[i];
        }

        // round 1, then every dx rounds after it
        if ((playerStatContainer.numberObs() - 1) % dx == 0) {
            for (int i = 0; i < bankrollChange.length; i++) {
                bankrolls.get(i).add(bankrollChange[i]);
            }
        }
    }

    /**
     * Opens a window with the chart, or does nothing if no rounds were recorded.
     */
    public void display() {
        if (bankrolls.getFirst().isEmpty()) {
            return;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        if (playerStatContainer.getPlayerCount() == 1) {
            dataset.addSeries(toSeries("Player", 0));

            int n = playerStatContainer.getPlayerTally(0).getN();
            XYSeries ev = new XYSeries("EV ($)", false);
            ev.add(0, 0);
            ev.add(n, playerStatContainer.getPlayerTally(0).getEV() * n);
            dataset.addSeries(ev);

        } else {
            for (int i = 1; i <= playerStatContainer.getPlayerCount(); i++) {
                dataset.addSeries(toSeries("Player " + i, i - 1));
            }
        }

        JFreeChart chart = ChartFactory.createXYLineChart(null, "Rounds", "Bankroll Change", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        JFrame frame = new JFrame("Blackjack Analyst");
        frame.setContentPane(new ChartPanel(chart));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) (screenSize.width * 0.75);
        int height = (int) (screenSize.height * 0.75);
        frame.setSize(width, height);

        frame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private XYSeries toSeries(String name, int i) {
        XYSeries series = new XYSeries(name, false);

        List<Double> data = bankrolls.get(i);
        for (int j = 0; j < data.size(); j++) {
            series.add((long) dx * j + 1, data.get(j));
        }

        // the last round only got plotted above if it landed on a step
        int rounds = playerStatContainer.numberObs();
        if ((long) dx * (data.size() - 1) + 1 != rounds) {
            series.add(rounds, bankrollChange[i]);
        }

        return series;
    }

    /**
     * Stops recording rounds. The points already recorded can still be displayed.
     */
    public void close() {
        playerStatContainer.removeArrayOfObservationListener(listener);
    }

    /**
     * Sets how many rounds apart the points are. Call it before any rounds are recorded.
     *
     * @param dx rounds between points
     */
    public void setDx(int dx) {
        this.dx = dx;
    }
}
