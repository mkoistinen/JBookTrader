package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;

import java.util.*;

/**
 *
 */
public class IndicatorManager {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private static final long MIN_SAMPLE_SIZE = 60 * 60;// 1 hour worth of samples
    private final List<Indicator> indicators;
    private MarketBook marketBook;
    private boolean hasValidIndicators;
    private long previousSnapshotTime;
    private long samples;
    private int size;

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
        return hasValidIndicators && (samples >= MIN_SAMPLE_SIZE);
    }

    public void addIndicator(Indicator indicator) {
        indicators.add(indicator);
        size = indicators.size();
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators() {
        hasValidIndicators = true;
        long lastSnapshotTime = marketBook.getSnapshot().getTime();
        samples++;

        if (lastSnapshotTime - previousSnapshotTime > GAP_SIZE) {
            samples = 0;
            for (int index = 0; index < size; index++) {
                indicators.get(index).reset();
            }
        }
        previousSnapshotTime = lastSnapshotTime;

        for (int index = 0; index < size; index++) {
            try {
                indicators.get(index).calculate();
            } catch (IndexOutOfBoundsException iobe) {
                // This exception will occur if book size is insufficient to calculate
                // the indicator. This is normal.
                hasValidIndicators = false;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
