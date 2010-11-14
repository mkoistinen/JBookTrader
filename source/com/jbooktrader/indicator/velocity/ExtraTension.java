package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

/**
 * Tension of limit order book balance in relation to price
 */
public class ExtraTension extends Indicator {
    private final double[] fastMultipliers, slowMultipliers;
    private double[] fastBalances, slowBalances, fastPrices, slowPrices;
    private final int size;

    public ExtraTension(int fastPeriod, int slowPeriod, int size) {
        this.size = size;
        fastMultipliers = new double[size];
        slowMultipliers = new double[size];
        for (int i = 0; i < size; i++) {
            fastMultipliers[i] = 2.0 / (fastPeriod / (i + 1) + 1.0);
            slowMultipliers[i] = 2.0 / (slowPeriod / (i + 1) + 1.0);
        }

        fastBalances = new double[size];
        slowBalances = new double[size];
        fastPrices = new double[size];
        slowPrices = new double[size];

    }

    @Override
    public void calculate() {
        MarketSnapshot snapshot = marketBook.getSnapshot();

        // balance
        double balanceValue = snapshot.getBalance();
        double price = snapshot.getPrice();
        double balanceVelocity = 0;
        double priceVelocity = 0;


        for (int i = 0; i < size; i++) {
            fastBalances[i] += (balanceValue - fastBalances[i]) * fastMultipliers[i];
            slowBalances[i] += (balanceValue - slowBalances[i]) * slowMultipliers[i];
            fastPrices[i] += (price - fastPrices[i]) * fastMultipliers[i];
            slowPrices[i] += (price - slowPrices[i]) * slowMultipliers[i];
            balanceVelocity += (fastBalances[i] - slowBalances[i]);
            priceVelocity += (fastPrices[i] - slowPrices[i]);
        }

        value = balanceVelocity - 2 * priceVelocity;
    }

    @Override
    public void reset() {
        for (int i = 0; i < size; i++) {
            fastBalances[i] = slowBalances[i] = 0;
            fastPrices[i] = slowPrices[i] = marketBook.getSnapshot().getPrice();
        }
    }
}
