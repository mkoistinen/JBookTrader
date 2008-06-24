package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class BalanceScaled extends Indicator {
    private final double multiplier;
    private final int length;
    private double ema;

    public BalanceScaled(MarketBook marketBook, int length) {
        super(marketBook);
        this.length = length;
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getMidBalance();
        ema += (balance - ema) * multiplier;
        value = ema * Math.sqrt(length);

        return value;
    }
}