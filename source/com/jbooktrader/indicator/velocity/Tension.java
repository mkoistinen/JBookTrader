package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

/**
 * Tension of limit order book balance in relation to price
 */
public class Tension extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fastBalance, slowBalance, fastPrice, slowPrice;

    public Tension(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        MarketSnapshot snapshot = marketBook.getSnapshot();

        // balance
        double depthBalanceValue = snapshot.getBalance();
        fastBalance += (depthBalanceValue - fastBalance) * fastMultiplier;
        slowBalance += (depthBalanceValue - slowBalance) * slowMultiplier;
        double balanceVelocity = fastBalance - slowBalance;

        // price
        double price = snapshot.getPrice();
        fastPrice += (price - fastPrice) * fastMultiplier;
        slowPrice += (price - slowPrice) * slowMultiplier;
        double priceVelocity = 2 * (fastPrice - slowPrice);

        // tension
        value = balanceVelocity - priceVelocity;
    }

    @Override
    public void reset() {
        fastBalance = slowBalance = value = 0;
        fastPrice = slowPrice = marketBook.getSnapshot().getPrice();
    }
}
