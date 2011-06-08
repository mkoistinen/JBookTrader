package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;

/**
 * Volume acceleration
 */
public class VolumeAcceleration extends Indicator {
    private final double fastMultiplier, intermMutiplier, slowMultiplier;
    private double fast, slow, interm;

    public VolumeAcceleration(int period) {
        super(period);
        fastMultiplier = 2.0 / (period + 1);
        double multiplier = 2.2;
        intermMutiplier = 2.0 / (period * multiplier + 1);
        slowMultiplier = 2.0 / (period * 2 * multiplier + 1);
    }

    @Override
    public void calculate() {
        double volume = marketBook.getSnapshot().getVolume();
        fast += (volume - fast) * fastMultiplier;
        interm += (volume - interm) * intermMutiplier;
        slow += (volume - slow) * slowMultiplier;
        value = fast - 2 * interm + slow;
    }

    @Override
    public void reset() {
        fast = slow = interm = value = 0;
    }
}
