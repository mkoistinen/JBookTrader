package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

public class DepthBalanceStrengthEMA extends Indicator {
    private final double multiplier;

    public DepthBalanceStrengthEMA(int period) {
        multiplier = 2.0 / (period + 1);
        reset();
    }

    @Override
    public void calculate() {
        double balance = Math.abs(marketBook.getSnapshot().getBalance());
        value += multiplier * (balance - value);
    }

    @Override
    public void reset() {
        value = 0.0;
    }
}
