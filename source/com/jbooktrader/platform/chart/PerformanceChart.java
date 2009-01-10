package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.position.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


/**
 * Multi-plot strategy performance chart which combines price,
 * indicators, executions, and net profit.
 */

public class PerformanceChart {
    private static final int PRICE_PLOT_WEIGHT = 3;
    private static final Paint BACKGROUND_COLOR = new GradientPaint(0, 0, new Color(0, 0, 176), 0, 0, Color.BLACK);

    private final Strategy strategy;
    private final ArrayList<CircledTextAnnotation> annotations = new ArrayList<CircledTextAnnotation>();
    private final PreferencesHolder prefs;
    private final List<FastXYPlot> indicatorPlots;
    private final PerformanceChartData performanceChartData;

    private JFreeChart chart;
    private JFrame chartFrame;
    private CombinedDomainXYPlot combinedPlot;
    private DateAxis dateAxis;
    private FastXYPlot pricePlot, pnlPlot;
    private CandlestickRenderer candleRenderer;
    private MultiColoredBarRenderer mcbRenderer;
    private JComboBox chartTypeCombo, timeLineCombo, timeZoneCombo;
    private JCheckBox tradesVisibilityCheck, pnlVisibilityCheck;

    public PerformanceChart(JFrame parent, Strategy strategy) {
        indicatorPlots = new ArrayList<FastXYPlot>();
        performanceChartData = strategy.getPerformanceChartData();
        prefs = PreferencesHolder.getInstance();
        this.strategy = strategy;
        createChartFrame(parent);
        registerListeners();
    }

    private void setRenderer() {
        int chartType = chartTypeCombo.getSelectedIndex();
        XYItemRenderer renderer = (chartType == 0) ? candleRenderer : mcbRenderer;
        pricePlot.setRenderer(renderer);

        for (XYPlot indicatorPlot : indicatorPlots) {
            indicatorPlot.setRenderer(renderer);
        }
    }

    private void setTimeline() {
        int timeLineType = timeLineCombo.getSelectedIndex();
        MarketTimeLine mtl = new MarketTimeLine(strategy);
        SegmentedTimeline segmentedTimeline = (timeLineType == 0) ? mtl.getAllHours() : mtl.getNormalHours();
        dateAxis.setTimeline(segmentedTimeline);
    }

    private void setTimeZone() {
        int timeZoneType = timeZoneCombo.getSelectedIndex();
        TimeZone tz = (timeZoneType == 0) ? strategy.getTradingSchedule().getTimeZone() : TimeZone.getDefault();
        dateAxis.setTimeZone(tz);
    }

    private void registerListeners() {
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
                if (pnlVisibilityCheck.isSelected()) {
                    combinedPlot.add(pnlPlot);
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

        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(PerformanceChartWidth, chartFrame.getWidth());
                prefs.set(PerformanceChartHeight, chartFrame.getHeight());
                prefs.set(PerformanceChartX, chartFrame.getX());
                prefs.set(PerformanceChartY, chartFrame.getY());
                prefs.set(PerformanceChartState, chartFrame.getExtendedState());
                chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });

    }


    private void createChartFrame(JFrame parent) {
        chartFrame = new JFrame("Strategy Performance Chart - " + strategy);
        chartFrame.setIconImage(parent.getIconImage());

        JPanel chartOptionsPanel = new JPanel(new SpringLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder chartOptionsBorder = BorderFactory.createTitledBorder(etchedBorder, "Chart Options");
        chartOptionsPanel.setBorder(chartOptionsBorder);

        JLabel chartTypeLabel = new JLabel("Chart Type:", JLabel.TRAILING);
        chartTypeCombo = new JComboBox(new String[] {"Candle", "OHLC"});
        chartTypeLabel.setLabelFor(chartTypeCombo);

        JLabel timeLineLabel = new JLabel("Timeline:", JLabel.TRAILING);
        timeLineCombo = new JComboBox(new String[] {"All Hours", "Trading Hours"});
        timeLineLabel.setLabelFor(timeLineCombo);

        JLabel timeZoneLabel = new JLabel("Time Zone:", JLabel.TRAILING);
        timeZoneCombo = new JComboBox(new String[] {"Exchange", "Local"});
        timeZoneLabel.setLabelFor(timeZoneCombo);

        tradesVisibilityCheck = new JCheckBox("Show trades");
        tradesVisibilityCheck.setSelected(true);
        pnlVisibilityCheck = new JCheckBox("Show net profit");
        pnlVisibilityCheck.setSelected(true);

        chartOptionsPanel.add(chartTypeLabel);
        chartOptionsPanel.add(chartTypeCombo);
        chartOptionsPanel.add(timeLineLabel);
        chartOptionsPanel.add(timeLineCombo);
        chartOptionsPanel.add(timeZoneLabel);
        chartOptionsPanel.add(timeZoneCombo);
        chartOptionsPanel.add(tradesVisibilityCheck);
        chartOptionsPanel.add(pnlVisibilityCheck);

        SpringUtilities.makeOneLineGrid(chartOptionsPanel);
        JPanel northPanel = new JPanel(new SpringLayout());
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeTopOneLineGrid(northPanel);


        JPanel centerPanel = new JPanel(new SpringLayout());

        JPanel chartPanel = new JPanel(new BorderLayout());
        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "Performance Chart");
        chartPanel.setBorder(chartBorder);

        JPanel scrollBarPanel = new JPanel(new BorderLayout());
        createChart();
        DateScrollBar dateScrollBar = new DateScrollBar(combinedPlot);
        scrollBarPanel.add(dateScrollBar);

        ChartMonitor chartMonitor = new ChartMonitor(chart);

        chartMonitor.setRangeZoomable(false);

        chartPanel.add(chartMonitor, BorderLayout.CENTER);
        chartPanel.add(scrollBarPanel, BorderLayout.SOUTH);

        centerPanel.add(chartPanel);
        SpringUtilities.makeOneLineGrid(centerPanel);


        Container contentPane = chartFrame.getContentPane();
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        chartFrame.pack();

        RefineryUtilities.centerFrameOnScreen(chartFrame);

        int chartWidth = prefs.getInt(PerformanceChartWidth);
        int chartHeight = prefs.getInt(PerformanceChartHeight);
        int chartX = prefs.getInt(PerformanceChartX);
        int chartY = prefs.getInt(PerformanceChartY);
        int chartState = prefs.getInt(PerformanceChartState);

        if (chartX >= 0 && chartY >= 0 && chartHeight > 0 && chartWidth > 0) {
            chartFrame.setBounds(chartX, chartY, chartWidth, chartHeight);
        }
        if (chartState >= 0) {
            chartFrame.setExtendedState(chartState);
        }
    }


    private void createChart() {
        // create OHLC bar renderer
        mcbRenderer = new MultiColoredBarRenderer();
        mcbRenderer.setSeriesPaint(0, Color.WHITE);
        mcbRenderer.setBaseStroke(new BasicStroke(3));
        mcbRenderer.setSeriesPaint(0, new Color(250, 240, 150));

        // create candlestick renderer
        candleRenderer = new CandlestickRenderer(3);
        candleRenderer.setDrawVolume(false);
        candleRenderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        candleRenderer.setUpPaint(Color.GREEN);
        candleRenderer.setDownPaint(Color.RED);
        candleRenderer.setSeriesPaint(0, new Color(250, 240, 150));
        candleRenderer.setBaseStroke(new BasicStroke(1));

        dateAxis = new DateAxis();

        // parent plot
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        // price plot
        OHLCDataset priceDataset = performanceChartData.getPriceDataset();
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        pricePlot = new FastXYPlot(priceDataset, dateAxis, priceAxis, null);
        pricePlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pricePlot, PRICE_PLOT_WEIGHT);

        // indicator plots
        for (Indicator indicator : strategy.getIndicatorManager().getIndicators()) {
            NumberAxis indicatorAxis = new NumberAxis(indicator.getName());
            indicatorAxis.setLabelFont(new Font("Arial Narrow", Font.PLAIN, 11));
            OHLCDataset ds = performanceChartData.getIndicatorDataset(indicator);
            FastXYPlot indicatorPlot = new FastXYPlot(ds, dateAxis, indicatorAxis, null);
            indicatorPlot.setBackgroundPaint(BACKGROUND_COLOR);
            combinedPlot.add(indicatorPlot);
            indicatorPlots.add(indicatorPlot);
        }

        // positions plot
        for (Position position : strategy.getPositionManager().getPositionsHistory()) {
            long time = position.getTime();
            double aveFill = position.getAvgFillPrice();
            int quantity = position.getPosition();
            CircledTextAnnotation trade = new CircledTextAnnotation(quantity, time, aveFill);
            pricePlot.addAnnotation(trade);
            annotations.add(trade);
        }

        // Net profit plot
        TimeSeriesCollection profitAndLossCollection = new TimeSeriesCollection();
        TimeSeries profitAndLoss = performanceChartData.getProfitAndLossSeries();
        profitAndLossCollection.addSeries(profitAndLoss);
        NumberAxis pnlAxis = new NumberAxis("Net Profit");
        pnlAxis.setAutoRangeIncludesZero(false);
        StandardXYItemRenderer pnlRenderer = new StandardXYItemRenderer();
        pnlPlot = new FastXYPlot(profitAndLossCollection, dateAxis, pnlAxis, pnlRenderer);
        pnlPlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pnlPlot);

        combinedPlot.setDomainAxis(dateAxis);
        setRenderer();

        setTimeline();
        setTimeZone();

        // Finally, create the chart
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, false);
    }

    public JFrame getChart() {
        return chartFrame;
    }

}
