package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class BalanceEMA extends Indicator {
    private final double multiplier;

    public BalanceEMA(MarketBook marketBook, int length) {
        super(marketBook);
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getBalance();
        value += (balance - value) * multiplier;

        return value;
    }
}