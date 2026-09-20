package me.lel.simulation;

import me.lel.simulation.ssj.BlackjackTally;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import umontreal.ssj.stat.list.ArrayOfObservationListener;
import umontreal.ssj.stat.list.ListOfStatProbes;
import umontreal.ssj.stat.list.ListOfTalliesWithCovariance;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final ListOfTalliesWithCovariance<BlackjackTally> playerStatContainer;
    private final ArrayOfObservationListener listener;

    private final List<List<Double>> bankrolls = new ArrayList<>();
    private final double[] bankrollChange;
    private int dx;

    public Graph(ListOfTalliesWithCovariance<BlackjackTally> playerStatContainer, int totalMeasurementsExpected) {
        this.listener = this::record;
        for (int i = 0; i < playerStatContainer.size(); i++) {
            bankrolls.add(new ArrayList<>());
        }

        this.bankrollChange = new double[playerStatContainer.size()];
        // No reason why it should be 500, but it seems to work well enough
        this.dx = (int) Math.ceil((double) totalMeasurementsExpected / 500);

        this.playerStatContainer = playerStatContainer;
        this.playerStatContainer.setBroadcasting(true);
        this.playerStatContainer.addArrayOfObservationListener(listener);
    }

    private void record(ListOfStatProbes<?> probes, double[] profits) {
        for (int i = 0; i < profits.length; i++) {
            bankrollChange[i] += profits[i];
        }

        if (playerStatContainer.getFirst().numberObs() % dx == 0) {
            for (int i = 0; i < profits.length; i++) {
                bankrolls.get(i).add(bankrollChange[i]);
            }
        }
    }

    public void display() {
        if (bankrolls.getFirst().isEmpty()) {
            return;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        if (playerStatContainer.size() == 1) {
            dataset.addSeries(toSeries("Player", 0));

            int n = playerStatContainer.getFirst().getN();
            XYSeries ev = new XYSeries("EV ($)", false);
            ev.add(0, 0);
            ev.add(n, playerStatContainer.getFirst().getEV() * n);
            dataset.addSeries(ev);

        } else {
            for (int i = 1; i <= playerStatContainer.size(); i++) {
                dataset.addSeries(toSeries("Player " + i, i - 1));
            }
        }

        JFreeChart chart = ChartFactory.createXYLineChart(null, "Hands", "Bankroll Change", dataset,
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
            series.add((long) dx * (j + 1), data.get(j));
        }

        return series;
    }

    public void close() {
        playerStatContainer.removeArrayOfObservationListener(listener);
    }

    public void setDx(int dx) {
        this.dx = dx;
    }
}
