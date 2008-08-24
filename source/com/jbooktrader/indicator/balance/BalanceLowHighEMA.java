package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;


/**
 * Relative Strength Index of price
 * Specification: http://en.wikipedia.org/wiki/Relative_strength
 */
public class BalanceLowHighEMA extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousBalance;

    public BalanceLowHighEMA(int periodLength) {
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public double calculate() {
        double balance = marketBook.getLastMarketDepth().getMidBalance();
        if (previousBalance != 0) {
            double change = balance - previousBalance;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            emaUp += (up - emaUp) * multiplier;
            emaDown += (down - emaDown) * multiplier;
            double sum = emaUp + emaDown;
            value = (sum == 0) ? 50 : (100 * emaUp / sum);
        } else {
            value = 50;
        }
        previousBalance = balance;
        value -= 50;

        return value;
    }
}
