/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.strategies.MovingMomentumStrategy;
import ta4jexamples.strategies.RSI2Strategy;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class builds a graphical chart showing the buy/sell signals of a
 * strategy.
 */
public class BuyAndSellSignalsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j bar series and an indicator.
     *
     * @param barSeries the ta4j bar series
     * @param indicator the indicator
     * @param name      the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(BarSeries barSeries, Indicator<Num> indicator,
            String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            try {
                chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())),
                        indicator.getValue(i).doubleValue());
            } catch (Exception e) {

            }
        }
        return chartTimeSeries;
    }

    /**
     * Runs a strategy over a bar series and adds the value markers corresponding to
     * buy/sell signals to the plot.
     *
     * @param series   the bar series
     * @param strategy the trading strategy
     * @param plot     the plot
     */
    private static void addBuySellSignals(BarSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        List<Trade> trades = seriesManager.run(strategy).getTrades();
        // Adding markers to plot
        for (Trade trade : trades) {
            try {
                // Buy signal
                double buySignalBarTime = new Minute(
                        Date.from(series.getBar(trade.getEntry().getIndex()).getEndTime().toInstant()))
                        .getFirstMillisecond();
                Marker buyMarker = new ValueMarker(buySignalBarTime);
                buyMarker.setPaint(Color.GREEN);
                buyMarker.setLabel("B");
                plot.addDomainMarker(buyMarker);
                // Sell signal
                double sellSignalBarTime = new Minute(
                        Date.from(series.getBar(trade.getExit().getIndex()).getEndTime().toInstant()))
                        .getFirstMillisecond();
                Marker sellMarker = new ValueMarker(sellSignalBarTime);
                sellMarker.setPaint(Color.RED);
                sellMarker.setLabel("S");
                plot.addDomainMarker(sellMarker);
            } catch (Exception e) {

            }
        }
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        // Getting the bar series
        BarSeries series = CsvTradesLoader.loadBitstampSeries();
        // Building the trading strategy
//        Strategy strategy = MovingMomentumStrategy.buildStrategy(series);
        Strategy strategy = RSI2Strategy.buildStrategy(series);

        /*
         * Building chart datasets
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstampx Bitcoin (BTC)"));

        /*
         * Creating the chart
         */
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Bitstamp BTC " + "trades count: " + seriesManager.run(strategy).getTradeCount(), // title
                "Date", // x-axis label
                "Pricex", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        /*
         * Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, strategy, plot);

        /*
         * Displaying the chart
         */
        displayChart(chart);
    }
}
