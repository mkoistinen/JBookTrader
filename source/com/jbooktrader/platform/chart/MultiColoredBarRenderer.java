package com.jbooktrader.platform.chart;

import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Custom renderer for the HighLowChart to paint OHLC bars in different colors.
 * This functionality is not yet available in JFreeChart, thus the need for a
 * custom renderer.
 */
public class MultiColoredBarRenderer extends HighLowRenderer {
    private static final Color BULLISH_COLOR = Color.GREEN;
    private static final Color BEARISH_COLOR = Color.RED;
    private static final Color NEUTRAL_COLOR = Color.YELLOW;
    private OHLCDataset dataset;

    @Override
    public Paint getItemPaint(int series, int item) {
        double open = dataset.getOpenValue(series, item);
        double close = dataset.getCloseValue(series, item);
        if (open == close) {
            return NEUTRAL_COLOR;
        } else {
            return (close > open) ? BULLISH_COLOR : BEARISH_COLOR;
        }
    }

    @Override
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
        this.dataset = (OHLCDataset) dataset;
        return super.initialise(g2, dataArea, plot, dataset, info);
    }
}
