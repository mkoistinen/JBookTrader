package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Velocity of volume
 */
public class VolumeVelocityEMA extends Indicator {
    private final double slowMultiplier;
    private double upVolume, downVolume;
    private final MovingWindowMean volumes, prices;

    public VolumeVelocityEMA(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        volumes = new MovingWindowMean(fastPeriod);
        prices = new MovingWindowMean(fastPeriod);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double volume = marketBook.getSnapshot().getVolume();
        double price = marketBook.getSnapshot().getPrice();
        volumes.add(volume);
        prices.add(price);

        if (volumes.isFull()) {
            double meanVolume = volumes.getMean();
            double priceChange = prices.getLast() - prices.getFirst();
            double up = (priceChange > 0) ? meanVolume : 0;
            double down = (priceChange < 0) ? meanVolume : 0;


            upVolume += (up - upVolume) * slowMultiplier;
            downVolume += (down - downVolume) * slowMultiplier;
            value = 100 * (upVolume - downVolume) / (upVolume + downVolume);
        }


    }

    @Override
    public void reset() {
        upVolume = downVolume = marketBook.getSnapshot().getVolume();
        value = 0;
    }
}
