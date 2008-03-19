package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.Position;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.SpringUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;


/**
 * Multi-plot strategy performance chart which combines price,
 * indicators, executions, and P&L.
 */
public class StrategyPerformanceChart {
    private static final int PRICE_PLOT_WEIGHT = 5;
    private static final int ANNOTATION_RADIUS = 6;
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Font.BOLD, 11);
    private static final Paint BACKGROUND_COLOR = new GradientPaint(0, 0, new Color(0, 0, 176), 0, 0, Color.BLACK);


    private JFreeChart chart;
    private CombinedDomainXYPlot combinedPlot;

    private DateAxis dateAxis;
    private final Strategy strategy;
    private final Map<Integer, TimeSeriesCollection> tsCollections;
    private FastXYPlot pricePlot, pnlPlot;


    private JComboBox chartTypeCombo, timeLineCombo, timeZoneCombo;
    private JCheckBox tradesVisibilityCheck, pnlVisibilityCheck;
    private final ArrayList<CircledTextAnnotation> annotations = new ArrayList<CircledTextAnnotation>();
    private final PreferencesHolder prefs;


    public StrategyPerformanceChart(Strategy strategy) throws JBookTraderException {
        prefs = PreferencesHolder.getInstance();
        this.strategy = strategy;
        tsCollections = new HashMap<Integer, TimeSeriesCollection>();
        chart = createChart();
    }

    private void setRenderer() {
        int chartType = chartTypeCombo.getSelectedIndex();
        switch (chartType) {
            case 0:
                //pricePlot.setRenderer(candleRenderer);
                break;
            case 1:
                //pricePlot.setRenderer(mcbRenderer);
                break;
        }
    }

    private void setTimeline() {
        int timeLineType = timeLineCombo == null ? 0 : timeLineCombo.getSelectedIndex();
        MarketBook marketBook = strategy.getMarketBook();
        MarketTimeLine mtl = new MarketTimeLine(marketBook);
        SegmentedTimeline segmentedTimeline = (timeLineType == 0) ? mtl.getAllHours() : mtl.getNormalHours();
        dateAxis.setTimeline(segmentedTimeline);
    }

    private void setTimeZone() {
        int timeZoneType = timeZoneCombo == null ? 0 : timeZoneCombo.getSelectedIndex();
        TimeZone tz = null;

        switch (timeZoneType) {
            case 0:
                tz = strategy.getTradingSchedule().getTimeZone();
                break;
            case 1:
                tz = TimeZone.getDefault();
                break;
        }
        dateAxis.setTimeZone(tz);
    }


    public JFrame getChartFrame(JFrame parent) {
        final JFrame chartFrame = new JFrame("Strategy Performance Chart - " + strategy);
        chartFrame.setIconImage(parent.getIconImage());

        Container contentPane = chartFrame.getContentPane();


        final JPanel chartOptionsPanel = new JPanel(new BorderLayout());
        JPanel chartControlsPanel = new JPanel(new SpringLayout());
        chartOptionsPanel.add(chartControlsPanel, BorderLayout.NORTH);

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder border = BorderFactory.createTitledBorder(etchedBorder);
        border.setTitle("Chart Options");
        chartOptionsPanel.setBorder(border);

        JLabel chartTypeLabel = new JLabel("Chart Type:", JLabel.TRAILING);
        chartTypeCombo = new JComboBox(new String[]{"Bid/Ask"});
        chartTypeLabel.setLabelFor(chartTypeCombo);

        JLabel timeLineLabel = new JLabel("Timeline:", JLabel.TRAILING);
        timeLineCombo = new JComboBox(new String[]{"All Hours", "Trading Hours"});
        timeLineLabel.setLabelFor(timeLineCombo);

        JLabel timeZoneLabel = new JLabel("Time Zone:", JLabel.TRAILING);
        timeZoneCombo = new JComboBox(new String[]{"Exchange", "Local"});
        timeZoneLabel.setLabelFor(timeZoneCombo);

        tradesVisibilityCheck = new JCheckBox("Show trades");
        tradesVisibilityCheck.setSelected(true);
        pnlVisibilityCheck = new JCheckBox("Show P&L");
        pnlVisibilityCheck.setSelected(true);

        chartControlsPanel.add(chartTypeLabel);
        chartControlsPanel.add(chartTypeCombo);
        chartControlsPanel.add(timeLineLabel);
        chartControlsPanel.add(timeLineCombo);
        chartControlsPanel.add(timeZoneLabel);
        chartControlsPanel.add(timeZoneCombo);
        chartControlsPanel.add(tradesVisibilityCheck);
        chartControlsPanel.add(pnlVisibilityCheck);

        SpringUtilities.makeCompactGrid(chartControlsPanel, 1, 8, 12, 5, 8, 5);//rows, cols, initX, initY, xPad, yPad

        setRenderer();

        chartTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setRenderer();
            }
        });

        timeLineCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTimeline();
            }
        });

        timeZoneCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTimeZone();
            }
        });


        pnlVisibilityCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean show = pnlVisibilityCheck.isSelected();
                if (show) {
                    combinedPlot.add(pnlPlot, 1);
                } else {
                    combinedPlot.remove(pnlPlot);
                }
            }
        });


        tradesVisibilityCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean show = tradesVisibilityCheck.isSelected();
                for (CircledTextAnnotation annotation : annotations) {
                    if (show) {
                        pricePlot.addAnnotation(annotation);
                    } else {
                        pricePlot.removeAnnotation(annotation);
                    }
                }
            }
        });


        contentPane.add(chartOptionsPanel, BorderLayout.NORTH);

        JPanel scrollBarPanel = new JPanel(new BorderLayout());
        DateScrollBar dateScrollBar = new DateScrollBar(combinedPlot);
        scrollBarPanel.add(dateScrollBar, BorderLayout.SOUTH);


        ChartMonitor chartMonitor = new ChartMonitor(chart, true);
        chartMonitor.setRangeZoomable(false);

        contentPane.add(chartMonitor, BorderLayout.CENTER);
        contentPane.add(scrollBarPanel, BorderLayout.PAGE_END);

        chartFrame.setContentPane(contentPane);
        chartFrame.pack();

        RefineryUtilities.centerFrameOnScreen(chartFrame);

        int chartWidth = prefs.getInt(ChartWidth);
        int chartHeight = prefs.getInt(ChartHeight);
        int chartX = prefs.getInt(ChartX);
        int chartY = prefs.getInt(ChartY);
        int chartState = prefs.getInt(ChartState);

        if (chartX >= 0 && chartY >= 0 && chartHeight > 0 && chartWidth > 0) {
            chartFrame.setBounds(chartX, chartY, chartWidth, chartHeight);
        }
        if (chartState >= 0) {
            chartFrame.setExtendedState(chartState);
        }


        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(ChartWidth, chartFrame.getWidth());
                prefs.set(ChartHeight, chartFrame.getHeight());
                prefs.set(ChartX, chartFrame.getX());
                prefs.set(ChartY, chartFrame.getY());
                prefs.set(ChartState, chartFrame.getExtendedState());
                chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });

        return chartFrame;
    }


    private TimeSeries createIndicatorSeries(ChartableIndicator chartableIndicator) {

        TimeSeries ts = new TimeSeries(chartableIndicator.getName(), Second.class);
        ts.setRangeDescription(chartableIndicator.getName());

        // make a defensive copy to prevent concurrent modification
        List<IndicatorValue> indicatorValues = new ArrayList<IndicatorValue>();
        indicatorValues.addAll(chartableIndicator.getIndicator().getHistory());

        for (IndicatorValue indicatorValue : indicatorValues) {
            ts.addOrUpdate(new Second(new Date(indicatorValue.getTime())), indicatorValue.getValue());
        }

        ts.fireSeriesChanged();
        return ts;
    }

    private TimeSeries createProfitAndLossSeries(ProfitAndLossHistory plHistory) {

        TimeSeries ts = new TimeSeries("P&L", Second.class);
        ts.setRangeDescription("P&L");

        // make a defensive copy to prevent concurrent modification
        List<ProfitAndLoss> profitAndLossHistory = new ArrayList<ProfitAndLoss>();
        profitAndLossHistory.addAll(plHistory.getHistory());

        for (ProfitAndLoss profitAndLoss : profitAndLossHistory) {
            ts.addOrUpdate(new Second(new Date(profitAndLoss.getTime())), profitAndLoss.getValue());
        }

        ts.fireSeriesChanged();
        return ts;
    }

    private TimeSeriesCollection createMarketDepthSeries(MarketBook marketBook) {
        TimeSeriesCollection tsc = new TimeSeriesCollection();

        // make a defensive copy to prevent concurrent modification
        List<MarketDepth> marketDepths = new ArrayList<MarketDepth>();
        marketDepths.addAll(marketBook.getAll());

        TimeSeries bid = new TimeSeries("Bid", Second.class);
        bid.setRangeDescription("Bid");
        for (MarketDepth marketDepth : marketDepths) {
            Second second = new Second(new Date(marketDepth.getTime()));
            bid.addOrUpdate(second, marketDepth.getBestBid());
        }
        bid.fireSeriesChanged();
        tsc.addSeries(bid);

        TimeSeries ask = new TimeSeries("Ask", Second.class);
        ask.setRangeDescription("Ask");
        for (MarketDepth marketDepth : marketDepths) {
            Second second = new Second(new Date(marketDepth.getTime()));
            ask.addOrUpdate(second, marketDepth.getBestAsk());
        }
        ask.fireSeriesChanged();
        tsc.addSeries(ask);

        return tsc;
    }


    private JFreeChart createChart() {
        dateAxis = new DateAxis();
        setTimeline();
        setTimeZone();

        // create price plot
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        pricePlot = new FastXYPlot(createMarketDepthSeries(strategy.getMarketBook()), dateAxis, priceAxis, null);
        pricePlot.setBackgroundPaint(BACKGROUND_COLOR);
        AbstractXYItemRenderer r = new StandardXYItemRenderer();
        r.setBaseStroke(new BasicStroke(1));
        pricePlot.setRenderer(r);

        // parent plot
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedPlot.add(pricePlot, PRICE_PLOT_WEIGHT);

        // Put all indicators into groups, so that each group is
        // displayed on its own subplot
        for (ChartableIndicator chartableIndicator : strategy.getIndicators()) {
            TimeSeries ts = createIndicatorSeries(chartableIndicator);
            int subChart = chartableIndicator.getChartIndex();
            if (subChart >= 0) {
                TimeSeriesCollection tsCollection = tsCollections.get(subChart);
                if (tsCollection == null) {
                    tsCollection = new TimeSeriesCollection();
                    tsCollections.put(subChart, tsCollection);
                }
                tsCollection.addSeries(ts);
            }
        }

        // create P&L series
        TimeSeriesCollection profitAndLossCollection = new TimeSeriesCollection();
        ProfitAndLossHistory plHistory = strategy.getPerformanceManager().getProfitAndLossHistory();
        TimeSeries profitAndLoss = createProfitAndLossSeries(plHistory);
        profitAndLossCollection.addSeries(profitAndLoss);
        tsCollections.put(-1, profitAndLossCollection);

        // Plot positions
        for (Position position : strategy.getPositionManager().getPositionsHistory()) {

            Date date = new Date(position.getTime());
            double aveFill = position.getAvgFillPrice();
            int pos = position.getPosition();

            Color bkColor = Color.YELLOW;
            if (pos > 0) {
                bkColor = Color.GREEN;
            } else if (pos < 0) {
                bkColor = Color.RED;
            }

            String annotationText = String.valueOf(Math.abs(pos));

            CircledTextAnnotation circledText = new CircledTextAnnotation(annotationText, date.getTime(), aveFill, ANNOTATION_RADIUS);
            circledText.setFont(ANNOTATION_FONT);
            circledText.setBkColor(bkColor);
            circledText.setPaint(Color.BLACK);
            circledText.setTextAnchor(TextAnchor.CENTER);

            pricePlot.addAnnotation(circledText);
            annotations.add(circledText);

        }

        // Now that the indicators are grouped, create subplots
        AbstractXYItemRenderer renderer;
        for (Map.Entry<Integer, TimeSeriesCollection> mapEntry : tsCollections.entrySet()) {
            int subChart = mapEntry.getKey();
            TimeSeriesCollection tsCollection = mapEntry.getValue();

            if (subChart == -1) {
                renderer = new XYLineAndShapeRenderer();
                renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
            } else {
                renderer = new StandardXYItemRenderer();
                renderer.setBaseStroke(new BasicStroke(1));
            }

            if (subChart == 0) {
                pricePlot.setDataset(1, tsCollection);
                pricePlot.setRenderer(1, new StandardXYItemRenderer());
            } else {
                String collectionName = (subChart == -1) ? "P&L" : "Indicators";
                NumberAxis indicatorAxis = new NumberAxis(collectionName);
                indicatorAxis.setAutoRangeIncludesZero(false);
                FastXYPlot plot = new FastXYPlot(tsCollection, dateAxis, indicatorAxis, renderer);
                plot.setBackgroundPaint(BACKGROUND_COLOR);
                int weight = 1;
                if (subChart == -1) {
                    pnlPlot = plot;
                }
                combinedPlot.add(plot, weight);
            }
        }


        combinedPlot.setDomainAxis(dateAxis);

        // Finally, create the chart
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        chart.getLegend().setPosition(RectangleEdge.TOP);
        chart.getLegend().setBackgroundPaint(Color.LIGHT_GRAY);

        return chart;
    }
}
