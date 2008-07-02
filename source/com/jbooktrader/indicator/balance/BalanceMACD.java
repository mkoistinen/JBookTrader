package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * MACD of market depth balance.
 */
public class BalanceMACD extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fastBalance, slowBalance;

    public BalanceMACD(MarketBook marketBook, int fastPeriod, int slowPeriod) {
        super(marketBook);
        fastMultiplier = 2. / (fastPeriod + 1.);
        slowMultiplier = 2. / (slowPeriod + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getMidBalance();
        fastBalance += (balance - fastBalance) * fastMultiplier;
        slowBalance += (balance - slowBalance) * slowMultiplier;
        value = fastBalance - slowBalance;

        return value;
    }
}