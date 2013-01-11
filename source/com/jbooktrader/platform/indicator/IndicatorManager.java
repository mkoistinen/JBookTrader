package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;

import java.util.*;

/**
 */
public class IndicatorManager {
    private static final long GAP_SIZE = 5 * 60 * 1000;// 5 minutes
    private static final long MIN_SAMPLE_SIZE = 1 * 60 * 60;// 1 hour worth of samples
    private final List<Indicator> indicators;

    private MarketBook marketBook;
    private long previousSnapshotTime;
    private long samples;

    public Indicator addIndicator(Indicator newIndicator) {
        String key = newIndicator.getKey();
        for (Indicator indicator : indicators) {
            if (key.equals(indicator.getKey())) {
                return indicator;
            }
        }

        indicators.add(newIndicator);
        newIndicator.setMarketBook(marketBook);

        return newIndicator;
    }


    public IndicatorManager() {
        indicators = new ArrayList<Indicator>();
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
        for (Indicator indicator : indicators) {
            indicator.setMarketBook(marketBook);
        }
    }

    public boolean hasValidIndicators() {
        return (samples >= MIN_SAMPLE_SIZE);
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators() {
        MarketSnapshot snapshot = marketBook.getSnapshot();
        if (snapshot == null) {
            return;
        }
        long lastSnapshotTime = snapshot.getTime();
        samples++;
        int size = indicators.size();

        if (lastSnapshotTime - previousSnapshotTime > GAP_SIZE) {
            samples = 0;
            //System.out.println("resetting " + size + " indicators in " + this);
            for (int index = 0; index < size; index++) {
                indicators.get(index).reset();
                //System.out.println( "indicator " + indicators.get(index) + " reset");
            }
        }
        previousSnapshotTime = lastSnapshotTime;

        for (int index = 0; index < size; index++) {
            indicators.get(index).calculate();
        }
    }
}
