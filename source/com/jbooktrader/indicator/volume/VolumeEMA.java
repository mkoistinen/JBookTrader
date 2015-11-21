package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;

/**
 * Exponential moving average of volume.
 *
 * @author Eugene Kononov
 */
public class VolumeEMA extends Indicator {
    private final double multiplier;

    public VolumeEMA(int length) {
        super(length);
        multiplier = 2.0 / (length + 1.0);
    }

    @Override
    public void calculate() {
        int volume = marketBook.getSnapshot().getVolume();
        value += (volume - value) * multiplier;
    }

    @Override
    public void reset() {
        value = marketBook.getSnapshot().getVolume();
    }

}
