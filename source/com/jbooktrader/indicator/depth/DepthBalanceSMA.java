package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

import java.util.*;

public class DepthBalanceSMA extends Indicator {

    private final int period;

    private final LinkedList<Double> history = new LinkedList<Double>();
    private double sum;

    public DepthBalanceSMA(int period) {
        this.period = period;
        reset();
    }

    @Override
    public void reset() {
        value = sum = 0.0;
        history.clear();
    }


    @Override
    public void calculate() {

        double balance = marketBook.getSnapshot().getBalance();
        history.addLast(balance);
        sum += balance;

        while (history.size() > period) {
            sum -= history.removeFirst();
        }

        if (!history.isEmpty()) {
            value = sum / history.size();
        }
    }

}
