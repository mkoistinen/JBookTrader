package com.jbooktrader.platform.chart;


import com.jbooktrader.platform.bar.*;
import org.jfree.chart.axis.*;


public class MarketTimeLine {
    /**
     * Gaps less than MAX_GAP will be ignored, gaps greater than MAX_GAP will be removed
     */
    private static final long MAX_GAP = 12 * 60 * 60 * 1000;// 12 hours
    private static final long SEGMENT_SIZE = SegmentedTimeline.FIFTEEN_MINUTE_SEGMENT_SIZE;
    private static final long GAP_BUFFER = SEGMENT_SIZE;
    private final PriceHistory priceHistory;

    public MarketTimeLine(PriceHistory priceHistory) {
        this.priceHistory = priceHistory;
    }

    public SegmentedTimeline getNormalHours() {
        SegmentedTimeline timeline = new SegmentedTimeline(SEGMENT_SIZE, 1, 0);
        long previousTime = priceHistory.getFirstPriceBar().getTime();

        for (PriceBar bar : priceHistory.getAll()) {
            long barTime = bar.getTime();
            long difference = barTime - previousTime;
            if (difference > MAX_GAP) {
                timeline.addException(previousTime + GAP_BUFFER, barTime - GAP_BUFFER);
            }
            previousTime = barTime;
        }

        return timeline;
    }


    public SegmentedTimeline getAllHours() {
        return new SegmentedTimeline(SegmentedTimeline.DAY_SEGMENT_SIZE, 7, 0);
    }
}
