package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.performance.*;
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
    private FastXYPlot pricePlot, pnlPlot;
    private CandlestickRenderer candleRenderer;
    private MultiColoredBarRenderer mcbRenderer;
    private JComboBox chartTypeCombo, timeLineCombo, timeZoneCombo;
    private JCheckBox tradesVisibilityCheck, pnlVisibilityCheck;
    private final ArrayList<CircledTextAnnotation> annotations = new ArrayList<CircledTextAnnotation>();
    private final PreferencesHolder prefs;


    public StrategyPerformanceChart(Strategy strategy) {
        prefs = PreferencesHolder.getInstance();
        this.strategy = strategy;
        chart = createChart();
    }

    private void setRenderer() {
        int chartType = chartTypeCombo.getSelectedIndex();
        switch (chartType) {
            case 0:
                pricePlot.setRenderer(candleRenderer);
                break;
            case 1:
                pricePlot.setRenderer(mcbRenderer);
                break;
        }
    }

    private void setTimeline() {
        int timeLineType = timeLineCombo == null ? 0 : timeLineCombo.getSelectedIndex();
        PriceHistory priceHistory = strategy.getPriceBarHistory();
        MarketTimeLine mtl = new MarketTimeLine(priceHistory);
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


        JPanel chartOptionsPanel = new JPanel(new BorderLayout());
        JPanel chartControlsPanel = new JPanel(new SpringLayout());
        chartOptionsPanel.add(chartControlsPanel, BorderLayout.NORTH);

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder border = BorderFactory.createTitledBorder(etchedBorder);
        border.setTitle("Chart Options");
        chartOptionsPanel.setBorder(border);

        JLabel chartTypeLabel = new JLabel("Chart Type:", JLabel.TRAILING);
        chartTypeCombo = new JComboBox(new String[]{"Candle", "OHLC"});
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

        SpringUtilities.makeCompactGrid(chartControlsPanel, 1, 8, 12, 5, 8, 5);

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

    private OHLCDataset createPriceDataset() {
        PriceHistory priceHistory = strategy.getPriceBarHistory();
        int size = priceHistory.size();
        Date[] dates = new Date[size];

        double[] highs = new double[size];
        double[] lows = new double[size];
        double[] opens = new double[size];
        double[] closes = new double[size];
        double[] volumes = new double[size];

        for (int bar = 0; bar < size; bar++) {
            PriceBar priceBar = priceHistory.getPriceBar(bar);

            dates[bar] = new Date(priceBar.getTime());
            highs[bar] = priceBar.getHigh();
            lows[bar] = priceBar.getLow();
            opens[bar] = priceBar.getOpen();
            closes[bar] = priceBar.getClose();
        }

        String ticker = strategy.getContract().m_symbol;
        return new DefaultHighLowDataset(ticker, dates, highs, lows, opens, closes, volumes);
    }

    private OHLCDataset createIndicatorDataset(ChartableIndicator chartableIndicator) {

        IndicatorHistory indicatorHistory = chartableIndicator.getIndicator().getBarHistory();
        int size = indicatorHistory.size();
        Date[] dates = new Date[size];

        double[] highs = new double[size];
        double[] lows = new double[size];
        double[] opens = new double[size];
        double[] closes = new double[size];
        double[] volumes = new double[size];

        for (int bar = 0; bar < size; bar++) {
            IndicatorBar indicatorBar = indicatorHistory.getIndicatorBar(bar);

            dates[bar] = new Date(indicatorBar.getTime());
            highs[bar] = indicatorBar.getHigh();
            lows[bar] = indicatorBar.getLow();
            opens[bar] = indicatorBar.getOpen();
            closes[bar] = indicatorBar.getClose();
        }

        return new DefaultHighLowDataset("", dates, highs, lows, opens, closes, volumes);
    }

    private JFreeChart createChart() {
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

        setTimeline();
        setTimeZone();

        // parent plot
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        // price plot
        OHLCDataset priceDataset = createPriceDataset();
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        pricePlot = new FastXYPlot(priceDataset, dateAxis, priceAxis, null);
        pricePlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pricePlot, PRICE_PLOT_WEIGHT);

        // indicator plots
        for (ChartableIndicator chartableIndicator : strategy.getIndicators()) {
            NumberAxis indicatorAxis = new NumberAxis(chartableIndicator.getName());
            OHLCDataset ds = createIndicatorDataset(chartableIndicator);
            int type = chartableIndicator.getIndicator().getType();
            AbstractXYItemRenderer renderer = (type == 0) ? candleRenderer : new StandardXYItemRenderer();

            FastXYPlot indicatorPlot = new FastXYPlot(ds, dateAxis, indicatorAxis, renderer);
            indicatorPlot.setBackgroundPaint(BACKGROUND_COLOR);
            combinedPlot.add(indicatorPlot);
        }

        // positions plot
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

        // P&L plot
        TimeSeriesCollection profitAndLossCollection = new TimeSeriesCollection();
        ProfitAndLossHistory plHistory = strategy.getPerformanceManager().getProfitAndLossHistory();
        TimeSeries profitAndLoss = createProfitAndLossSeries(plHistory);
        profitAndLossCollection.addSeries(profitAndLoss);
        NumberAxis pnlAxis = new NumberAxis("P&L");
        pnlAxis.setAutoRangeIncludesZero(false);
        StandardXYItemRenderer pnlRenderer = new StandardXYItemRenderer();
        pnlPlot = new FastXYPlot(profitAndLossCollection, dateAxis, pnlAxis, pnlRenderer);
        pnlPlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pnlPlot);


        combinedPlot.setDomainAxis(dateAxis);

        // Finally, create the chart
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, false);
        return chart;
    }
}
