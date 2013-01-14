package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;

/**
 * Volume acceleration
 *
 * @author Eugene Kononov
 */
public class VolumeAcceleration extends Indicator {
    private final double fastMultiplier, intermMutiplier, slowMultiplier;
    private double fast, slow, interm;

    public VolumeAcceleration(int period, int mult) {
        super(period, mult);
        fastMultiplier = 2.0 / (period + 1);
        double multiplier = mult / 10.0;
        intermMutiplier = 2.0 / (multiplier * period + 1);
        slowMultiplier = 2.0 / (2 * multiplier * period + 1);
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
        fast = slow = interm = marketBook.getSnapshot().getVolume();
    }
}
