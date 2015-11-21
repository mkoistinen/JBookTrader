package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Balance acceleration
 *
 * @author Eugene Kononov
 */
public class BalanceAcceleration extends Indicator {
    private final double fastMultiplier, intermMultiplier, slowMultiplier;
    private double fast, slow, interm;

    public BalanceAcceleration(int period, int mult) {
        super(period, mult);
        fastMultiplier = 2.0 / (period + 1);
        double multiplier = mult / 10.0;
        intermMultiplier = 2.0 / (multiplier * period + 1);
        slowMultiplier = 2.0 / (2 * multiplier * period + 1);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        fast += (balance - fast) * fastMultiplier;
        interm += (balance - interm) * intermMultiplier;
        slow += (balance - slow) * slowMultiplier;
        value = fast - 2 * interm + slow;
    }

    @Override
    public void reset() {
        fast = slow = interm = 0;
    }
}
