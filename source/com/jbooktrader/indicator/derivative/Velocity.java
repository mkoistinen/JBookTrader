package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 * Velocity of any indicator
 */
public class Velocity extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fast, slow;

    public Velocity(Indicator parentIndicator, int fastPeriod, int slowPeriod) {
        super(parentIndicator);
        fastMultiplier = 2. / (fastPeriod + 1.);
        slowMultiplier = 2. / (slowPeriod + 1.);
    }

    @Override
    public double calculate() {
        double parentValue = parentIndicator.getValue();
        fast += (parentValue - fast) * fastMultiplier;
        slow += (parentValue - slow) * slowMultiplier;
        value = fast - slow;

        return value;
    }
}
