package com.jbooktrader.platform.chart;

import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Scroll bar for a combined chart where the horizontal axis represents dates
 *
 * @author Eugene Kononov
 */
public class DateScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener {
    private static final long SCALER = 10000;
    private static final double EXTRA_MARGIN_FACTOR = 0.05; // factor above and below the min/max to produce some margin
    private final DateAxis dateAxis;
    private final CombinedDomainXYPlot combinedDomainPlot;

    public DateScrollBar(CombinedDomainXYPlot combinedDomainPlot) {

        super(HORIZONTAL);
        this.combinedDomainPlot = combinedDomainPlot;

        dateAxis = (DateAxis) combinedDomainPlot.getDomainAxis();
        int min = (int) (dateAxis.getLowerBound() / SCALER);
        int max = (int) (dateAxis.getUpperBound() / SCALER);
        setValues(max, 0, min, max);

        dateAxis.addChangeListener(this);
        addAdjustmentListener(this);
    }

    private void rangeUpdate() {
        List<?> subPlots = combinedDomainPlot.getSubplots();
        double lowerBound = dateAxis.getLowerBound();
        double upperBound = dateAxis.getUpperBound();

        if (lowerBound >= upperBound) {
            return;
        }

        for (Object subPlot : subPlots) {
            XYPlot plot = (XYPlot) subPlot;
            int datasetCount = plot.getDatasetCount();
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;


            for (int datasetNumber = 0; datasetNumber < datasetCount; datasetNumber++) {
                XYDataset dataset = plot.getDataset(datasetNumber);
                int seriesCount = dataset.getSeriesCount();

                boolean isOHLC = dataset instanceof OHLCDataset;
                OHLCDataset ohlcDataset = isOHLC ? (OHLCDataset) dataset : null;

                for (int series = 0; series < seriesCount; series++) {
                    int datasetSize = dataset.getItemCount(series);
                    if (datasetSize == 0) {
                        continue;
                    }

                    int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, lowerBound, upperBound);
                    int firstItem = itemBounds[0];
                    int lastItem = itemBounds[1];

                    // Extend the range of applicable data to be one point before and after the range,
                    // so that we get the lines (such as profit graph) that extend off the edges
                    if (!isOHLC) {
                        firstItem = Math.max(0, firstItem - 1);
                        lastItem = Math.min(datasetSize - 1, lastItem + 1);
                    }

                    for (int item = firstItem; item <= lastItem; item++) {
                        if (isOHLC) {
                            max = Math.max(ohlcDataset.getHighValue(datasetNumber, item), max);
                            min = Math.min(ohlcDataset.getLowValue(datasetNumber, item), min);
                        } else {
                            double value = dataset.getYValue(series, item);
                            max = Math.max(value, max);
                            min = Math.min(value, min);
                        }
                    }
                }
            }

            if (max > min) {
                if (plot.getDataset(0) instanceof OHLCDataset) {
                    plot.getRangeAxis().setRange(min, max);
                } else {
                    double margin = (max - min) * EXTRA_MARGIN_FACTOR;
                    plot.getRangeAxis().setRange(min - margin, max + margin);
                }
            }
        }
    }


    public void axisChanged(AxisChangeEvent event) {
        rangeUpdate();
        int value = (int) (dateAxis.getUpperBound() / SCALER);
        setValue(value);
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        double end = getValue() * SCALER;
        double start = end - dateAxis.getRange().getLength();
        if (start < end) {
            dateAxis.setRange(start, end);
        }
    }
}
