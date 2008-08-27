package com.jbooktrader.indicator.index;

import com.jbooktrader.platform.indicator.*;

/**
 * Trin Index
 */
public class TrinIndexEMA extends Indicator {
    private final double multiplier;

    public TrinIndexEMA(int length) {
        multiplier = 2. / (length + 1.);
        value = 1;
    }

    @Override
    public double calculate() {
        double trin = marketBook.getLastMarketSnapshot().getTrin();
        value += (trin - value) * multiplier;
        return value;
    }
}