package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class BalanceRSI extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousBalance;

    public BalanceRSI(MarketBook marketBook, int periodLength) {
        super(marketBook);
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public double calculate() {
        double balance = marketBook.getLastMarketDepth().getBalance();
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

        return value;
    }
}
