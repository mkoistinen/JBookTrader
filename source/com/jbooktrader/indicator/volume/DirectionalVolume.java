package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;


/**
 */
public class DirectionalVolume extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;

    public DirectionalVolume(int periodLength) {
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public double calculate() {
        MarketSnapshot marketSnapshot = marketBook.getLastMarketSnapshot();
        double priceNow = marketBook.getLastMarketSnapshot().getMidPrice();
        double priceThen = marketBook.getPreviousMarketSnapshot().getMidPrice();
        double priceChange = priceNow - priceThen;
        int volume = marketSnapshot.getVolume();

        int upVolume = (priceChange > 0) ? volume : 0;
        int downVolume = (priceChange < 0) ? volume : 0;

        emaUp += (upVolume - emaUp) * multiplier;
        emaDown += (downVolume - emaDown) * multiplier;
        double sum = emaUp + emaDown;
        value = (sum == 0) ? 50 : (100 * emaUp / sum);
        value -= 50;

        return value;
    }
}

/* $Id$ */
