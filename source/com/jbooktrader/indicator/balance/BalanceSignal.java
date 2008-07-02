package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * MCD Signal of market depth balance.
 */
public class BalanceSignal extends Indicator {
    private final double fastMultiplier, slowMultiplier, smoothingMultiplier;
    private double fastBalance, slowBalance, signal;

    public BalanceSignal(MarketBook marketBook, int fastPeriod, int slowPeriod, int smoothingPeriod) {
        super(marketBook);
        fastMultiplier = 2. / (fastPeriod + 1.);
        slowMultiplier = 2. / (slowPeriod + 1.);
        smoothingMultiplier = 2. / (smoothingPeriod + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getMidBalance();
        fastBalance += (balance - fastBalance) * fastMultiplier;
        slowBalance += (balance - slowBalance) * slowMultiplier;
        double macd = fastBalance - slowBalance;
        signal += (macd - signal) * smoothingMultiplier;
        value = macd - signal;


        return value;
    }
}