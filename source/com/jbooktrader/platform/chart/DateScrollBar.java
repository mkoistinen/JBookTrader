package com.jbooktrader.platform.chart;

import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.data.*;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Scroll bar for a combined chart where the horizontal axis represents dates
 */
public class DateScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener {

    private static final int STEPS = 10000;
    private final DateAxis dateAxis;
    private final Range range;
    private double rangeMin, dateRange, ratio;
    private final CombinedDomainXYPlot combinedDomainPlot;

    public DateScrollBar(CombinedDomainXYPlot combinedDomainPlot) {
        super(HORIZONTAL);
        this.combinedDomainPlot = combinedDomainPlot;

        dateAxis = (DateAxis) combinedDomainPlot.getDomainAxis();
        range = combinedDomainPlot.getDataRange(dateAxis);

        dateAxis.addChangeListener(this);
        addAdjustmentListener(this);
    }

    private void rangeUpdate() {
        List<?> subPlots = combinedDomainPlot.getSubplots();
        double lowerBound = dateAxis.getLowerBound();
        double upperBound = dateAxis.getUpperBound();

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
                    int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, lowerBound, upperBound);
                    int firstItem = itemBounds[0];
                    int lastItem = itemBounds[1];

                    for (int item = firstItem; item < lastItem; item++) {
                        double high, low;
                        if (isOHLC) {
                            high = ohlcDataset.getHighValue(datasetNumber, item);
                            low = ohlcDataset.getLowValue(datasetNumber, item);
                        } else {
                            high = low = dataset.getYValue(series, item);
                        }

                        max = Math.max(high, max);
                        min = Math.min(low, min);
                    }
                }
            }

            if (max > min) {
                plot.getRangeAxis().setRange(min, max);
            }
        }
    }


    public void axisChanged(AxisChangeEvent event) {
        Timeline timeLine = dateAxis.getTimeline();
        rangeMin = timeLine.toTimelineValue((long) range.getLowerBound());
        double rangeMax = timeLine.toTimelineValue((long) range.getUpperBound());

        long dateMin = timeLine.toTimelineValue((long) dateAxis.getLowerBound());
        long dateMax = timeLine.toTimelineValue((long) dateAxis.getUpperBound());

        dateRange = dateMax - dateMin;
        ratio = STEPS / (rangeMax - rangeMin);

        int newExtent = (int) (dateRange * ratio);
        int newValue = (int) ((dateMin - rangeMin) * ratio);

        setValues(newValue, newExtent, 0, STEPS);
    }


    public void adjustmentValueChanged(AdjustmentEvent e) {
        long start = (long) (getValue() / ratio + rangeMin);
        long end = (long) (start + dateRange);

        if (end > start) {
            Timeline timeLine = dateAxis.getTimeline();
            start = timeLine.toMillisecond(start);
            end = timeLine.toMillisecond(end);
        }

        dateAxis.setRange(start, end);
        rangeUpdate();
    }


}
