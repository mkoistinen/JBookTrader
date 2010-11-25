package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;

import java.util.*;

/**
 */
public class IndicatorManager {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private static final long MIN_SAMPLE_SIZE = 60 * 60;// 1 hour worth of samples
    private final List<Indicator> indicators;

    private MarketBook marketBook;
    private boolean hasValidIndicators;
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
        return hasValidIndicators && (samples >= MIN_SAMPLE_SIZE);
    }


    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators() {
        hasValidIndicators = true;
        MarketSnapshot snapshot = marketBook.getSnapshot();
        if (snapshot == null) {
            return;
        }
        long lastSnapshotTime = snapshot.getTime();
        samples++;
        int size = indicators.size();

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
                hasValidIndicators = false;
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
