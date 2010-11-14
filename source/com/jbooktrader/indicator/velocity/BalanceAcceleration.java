package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

/**
 *
 */
public class BalanceAcceleration extends Indicator {
    private final double fastMultiplier, smoothingMultiplier;
    private double balanceEma, balanceSmoothed;

    public BalanceAcceleration(int fastPeriod, int smoothingPeriod) {
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        smoothingMultiplier = 2.0 / (smoothingPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        balanceEma += (balance - balanceEma) * fastMultiplier;
        balanceSmoothed += (balanceEma - balanceSmoothed) * smoothingMultiplier;
        value = balanceEma - balanceSmoothed;
    }

    @Override
    public void reset() {
        balanceEma = balanceSmoothed = value = 0;
    }
}
