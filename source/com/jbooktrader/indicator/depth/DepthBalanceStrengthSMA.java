package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

import java.util.*;

public class DepthBalanceStrengthSMA extends Indicator {
    private final int period;
    private final LinkedList<Double> balances;
    private double sum;

    public DepthBalanceStrengthSMA(int period) {
        this.period = period;
        balances = new LinkedList<Double>();
    }

    @Override
    public void calculate() {
        double balance = Math.abs(marketBook.getSnapshot().getBalance());
        sum += balance;

        // In with the new
        balances.add(balance);

        // Out with the old
        while (balances.size() > period) {
            sum -= balances.removeFirst();
        }

        if (!balances.isEmpty()) {
            value = sum / balances.size();
        }
    }

    @Override
    public void reset() {
        value = sum = 0.0;
        balances.clear();
    }
}
