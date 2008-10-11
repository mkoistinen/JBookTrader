package com.jbooktrader.indicator.derivative;

import com.jbooktrader.platform.indicator.*;

/**
 * Acceleration of any indicator. Measures the rate of change in velocity
 * of the underlying indicator.
 */
public class Acceleration extends Indicator {
    private final double fastMultiplier, slowMultiplier, velocityMultiplier;
    private double fast, slow, velocity;

    public Acceleration(Indicator parentIndicator, int fastPeriod, int slowPeriod, int velocityPeriod) {
        super(parentIndicator);
        fastMultiplier = 2. / (fastPeriod + 1.);
        slowMultiplier = 2. / (slowPeriod + 1.);
        velocityMultiplier = 2. / (velocityPeriod + 1.);
    }

    @Override
    public void calculate() {
        double parentValue = parentIndicator.getValue();
        fast += (parentValue - fast) * fastMultiplier;
        slow += (parentValue - slow) * slowMultiplier;
        double currentVelocity = fast - slow;
        velocity += (currentVelocity - velocity) * velocityMultiplier;
        value = currentVelocity - velocity;
    }
}
