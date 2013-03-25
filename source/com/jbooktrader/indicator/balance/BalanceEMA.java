package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of the balance in the limit order book.
 *
 * @author Eugene Kononov
 */
public class BalanceEMA extends Indicator {
    private final double multiplier;

    public BalanceEMA(int length) {
        super(length);
        multiplier = 2.0 / (length + 1.0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        value += (balance - value) * multiplier;
    }

    @Override
    public void reset() {
        value = 0;
    }

}
