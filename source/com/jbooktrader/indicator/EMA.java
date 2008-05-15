package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class EMA extends Indicator {
    private final double multiplier;

    public EMA(MarketBook marketBook, int length) {
        super(marketBook);
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getMidBalance();
        if (value == 0) {
            value = balance;
        } else {
            value += (balance - value) * multiplier;
        }

        return value;
    }
}