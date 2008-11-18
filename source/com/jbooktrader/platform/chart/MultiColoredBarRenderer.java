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
    private OHLCDataset dataset;

    @Override
    public Paint getItemPaint(int series, int item) {
        double open = dataset.getOpenValue(series, item);
        double close = dataset.getCloseValue(series, item);
        if (open == close) {
            return Color.YELLOW;
        } else {
            return (close > open) ? Color.GREEN : Color.RED;
        }
    }

    @Override
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {
        this.dataset = (OHLCDataset) dataset;
        return super.initialise(g2, dataArea, plot, dataset, info);
    }
}
