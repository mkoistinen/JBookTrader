package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of market depth balance.
 */
public class BalanceLowEMA extends Indicator {
    private final double multiplier;

    public BalanceLowEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getLowBalance();
        value += (balance - value) * multiplier;
        return value;
    }
}