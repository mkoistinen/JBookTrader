package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Velocity of balance in the market limit order book
 *
 * @author Eugene Kononov
 */
public class BalanceVelocity extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fast, slow;

    public BalanceVelocity(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        fast += (balance - fast) * fastMultiplier;
        slow += (balance - slow) * slowMultiplier;

        value = fast - slow;
    }

    @Override
    public void reset() {
        fast = slow = 0;
    }
}
