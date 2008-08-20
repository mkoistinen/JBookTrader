package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of market depth balance.
 */
public class BalanceHighEMA extends Indicator {
    private final double multiplier;

    public BalanceHighEMA(int length) {
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getHighBalance();
        value += (balance - value) * multiplier;
        return value;
    }
}