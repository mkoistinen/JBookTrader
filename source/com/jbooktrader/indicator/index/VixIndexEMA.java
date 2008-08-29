package com.jbooktrader.indicator.index;

import com.jbooktrader.platform.indicator.*;

/**
 * EMA of VIX Index
 */
public class VixIndexEMA extends Indicator {
    private final double multiplier;

    public VixIndexEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        double vix = marketBook.getLastMarketSnapshot().getVix();
        value += (vix - value) * multiplier;
        return value;
    }
}