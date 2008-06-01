package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class HiLoEMA extends Indicator {
    private final double multiplier;
    private double highEMA, lowEMA;

    public HiLoEMA(MarketBook marketBook, int length) {
        super(marketBook);
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int high = marketBook.getLastMarketDepth().getHighBalance();
        int low = marketBook.getLastMarketDepth().getLowBalance();

        highEMA += (high - highEMA) * multiplier;
        lowEMA += (low - lowEMA) * multiplier;
        value = (highEMA + lowEMA) / 2;

        return value;
    }
}