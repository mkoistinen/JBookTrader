package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

import java.util.*;

/**
 * Displacement of any indicator
 */
public class Displacement extends Indicator {
    private final int period;
    private final LinkedList<Double> history;

    public Displacement(Indicator parentIndicator, int period) {
        super(parentIndicator);
        this.period = period;
        history = new LinkedList<Double>();
    }

    @Override
    public double calculate() {
        double parentValue = parentIndicator.getValue();
        history.add(parentValue);
        if (history.size() > period) {
            history.removeFirst();
        }

        value = history.getLast() - history.getFirst();
        return value;
    }
}

/* $Id$ */
