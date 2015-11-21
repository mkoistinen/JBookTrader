package com.jbooktrader.indicator.combo;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

/**
 * Tension of limit order book
 *
 * @author Eugene Kononov
 */
public class Tension extends Indicator {
    private final double multiplier;
    private double aveBalance, avePrice;
    private final double scaleFactor;

    public Tension(int period, int scaleFactor) {
        super(period, scaleFactor);
        multiplier = 2.0 / (period + 1.0);
        this.scaleFactor = scaleFactor / 10.0;
    }

    @Override
    public void calculate() {
        MarketSnapshot snapshot = marketBook.getSnapshot();

        // balance
        double balance = snapshot.getBalance();
        aveBalance += (balance - aveBalance) * multiplier;
        double balanceVelocity = balance - aveBalance;

        // price
        double price = snapshot.getPrice();
        avePrice += (price - avePrice) * multiplier;
        double priceVelocity = price - avePrice;

        // tension
        value = balanceVelocity - scaleFactor * priceVelocity;
    }

    @Override
    public void reset() {
        aveBalance = value = 0;
        avePrice = marketBook.getSnapshot().getPrice();
    }
}
