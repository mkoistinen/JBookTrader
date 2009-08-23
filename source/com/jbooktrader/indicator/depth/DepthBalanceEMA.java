package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of the market depth balance.
 */
public class DepthBalanceEMA extends Indicator {
    private final double multiplier;

    public DepthBalanceEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        value += (balance - value) * multiplier;
    }

    @Override
    public void reset() {
        value = 0;
    }

}
