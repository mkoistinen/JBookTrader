package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 */
public class Volume extends Indicator {
    private final int period;

    public Volume(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
    }

    @Override
    public double calculate() {
        int lastIndex = marketBook.size() - 1;
        int firstIndex = lastIndex - period + 1;

        int size = 0;
        for (int x = firstIndex; x <= lastIndex; x++) {
            size += marketBook.getMarketDepth(x).getCumulativeBidSize() + marketBook.getMarketDepth(x).getCumulativeAskSize();
        }

        value = size / period;
        return value;

    }
}
