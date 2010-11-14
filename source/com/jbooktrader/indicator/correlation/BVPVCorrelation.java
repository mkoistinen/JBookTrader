package com.jbooktrader.indicator.correlation;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Correlation between balance velocity and price velocity in a moving window.
 */
public class BVPVCorrelation extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fastBalance, slowBalance, fastPrice, slowPrice;
    private final MovingWindowCorrelation window;

    public BVPVCorrelation(int fastPeriod, int slowPeriod) {
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
        window = new MovingWindowCorrelation(3600);
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
        double priceVelocity = fastPrice - slowPrice;

        window.add(balanceVelocity, priceVelocity);
        if (window.isFull()) {
            value = window.getCorrelation();
        }
    }

    @Override
    public void reset() {
        fastBalance = slowBalance = value = 0;
        fastPrice = slowPrice = marketBook.getSnapshot().getPrice();
        window.clear();
    }
}
