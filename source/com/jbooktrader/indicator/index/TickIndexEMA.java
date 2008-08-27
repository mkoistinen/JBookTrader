package com.jbooktrader.indicator.index;

import com.jbooktrader.platform.indicator.*;

/**
 * Tick Index
 */
public class TickIndexEMA extends Indicator {
    private final double multiplier;

    public TickIndexEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        double tick = marketBook.getLastMarketSnapshot().getTick();
        value += (tick - value) * multiplier;
        return value;
    }
}