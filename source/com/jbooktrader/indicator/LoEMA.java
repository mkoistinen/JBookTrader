package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class LoEMA extends Indicator {
    private final double multiplier;

    public LoEMA(MarketBook marketBook, int length) {
        super(marketBook);
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int low = marketBook.getLastMarketDepth().getLowBalance();
        value += (low - value) * multiplier;

        return value;
    }
}