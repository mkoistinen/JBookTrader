package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of market depth balance.
 */
public class DepthBalanceEMA extends Indicator {
    private final double multiplier;

    public DepthBalanceEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public void calculate() {
        int balance = marketBook.getLastMarketSnapshot().getBalance();
        value += (balance - value) * multiplier;
    }
}
