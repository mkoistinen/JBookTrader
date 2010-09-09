package com.jbooktrader.indicator.velocity;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

/**
 * Tension of limit order book balance in relation to price
 */
public class Tension extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fastBalance, slowBalance, fastPrice, slowPrice;
    private DepthBalance depthBalance;
    private final int magnifier;

    public Tension(int fastPeriod, int slowPeriod, int magnifier) {
        this.magnifier = magnifier;
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
        reset();
    }

    @Override
    public void setMarketBook(MarketBook marketBook) {
        super.setMarketBook(marketBook);
        depthBalance = new DepthBalance();
        depthBalance.setMarketBook(marketBook);
    }

    @Override
    public void calculate() {
        // balance
        depthBalance.calculate();
        double depthBalanceValue = depthBalance.getValue();
        fastBalance += (depthBalanceValue - fastBalance) * fastMultiplier;
        slowBalance += (depthBalanceValue - slowBalance) * slowMultiplier;
        double balanceVelocity = fastBalance - slowBalance;

        // price
        double price = marketBook.getSnapshot().getPrice();
        if (fastPrice != 0 && slowPrice != 0) {
            fastPrice += (price - fastPrice) * fastMultiplier;
            slowPrice += (price - slowPrice) * slowMultiplier;
        } else {
            fastPrice = slowPrice = price;
        }
        double priceVelocity = magnifier * (fastPrice - slowPrice);
        value = balanceVelocity - priceVelocity;
    }

    @Override
    public void reset() {
        fastBalance = slowBalance = fastPrice = slowPrice = value = 0;
    }
}
