package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Velocity of volume
 *
 * @author Eugene Kononov
 */
public class VolumeVelocitySMA extends Indicator {
    private final MovingWindowMean fast, slow;

    public VolumeVelocitySMA(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fast = new MovingWindowMean(fastPeriod);
        slow = new MovingWindowMean(slowPeriod);
    }

    @Override
    public void calculate() {
        int volume = marketBook.getSnapshot().getVolume();
        fast.add(volume);
        slow.add(volume);

        if (slow.isFull()) {
            double fastVolume = fast.getMean();
            double slowVolume = slow.getMean();
            value = 100 * (fastVolume - slowVolume) / (fastVolume + slowVolume);
        }
    }

    @Override
    public void reset() {
        fast.clear();
        slow.clear();
        value = 0;
    }
}
