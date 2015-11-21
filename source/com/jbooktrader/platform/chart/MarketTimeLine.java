package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.strategy.*;
import org.jfree.chart.axis.*;
import org.jfree.data.xy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class MarketTimeLine {
    /**
     * Gaps less than MAX_GAP will be ignored, gaps greater than MAX_GAP will be removed
     */
    private static final long MAX_GAP = 12 * 60 * 60 * 1000;// 12 hours
    private static final long SEGMENT_SIZE = SegmentedTimeline.HOUR_SEGMENT_SIZE;
    private final Strategy strategy;

    public MarketTimeLine(Strategy strategy) {
        this.strategy = strategy;
    }

    public SegmentedTimeline getNormalHours() {
        SegmentedTimeline timeline = new SegmentedTimeline(SEGMENT_SIZE, 1, 0);
        List<OHLCDataItem> items = strategy.getPerformanceManager().getPerformanceChartData().getPrices();

        long previousTime = items.get(0).getDate().getTime();

        for (OHLCDataItem item : items) {
            long time = item.getDate().getTime();
            long difference = time - previousTime;
            if (difference > MAX_GAP) {
                timeline.addException(previousTime + SEGMENT_SIZE, time - SEGMENT_SIZE);
            }
            previousTime = time;
        }

        return timeline;
    }


    public SegmentedTimeline getAllHours() {
        return new SegmentedTimeline(SegmentedTimeline.DAY_SEGMENT_SIZE, 7, 0);
    }
}
