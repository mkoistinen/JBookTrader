package com.jbooktrader.indicator.price;

import java.util.List;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;

public class PriceNoiseAdjustedRSI extends Indicator {
    private final int periodLength;
    
    public PriceNoiseAdjustedRSI(int periodLength) {
        this.periodLength = periodLength;
    }
    
    @Override
    public double calculate() {
        List<MarketSnapshot> marketSnapshots = marketBook.getAll();
        int mbSize = marketSnapshots.size();
        int lastBar = mbSize - 1;
        int firstBar = lastBar - periodLength + 1;

        double gains = 0, losses = 0;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = marketSnapshots.get(bar).getMidPrice() - marketSnapshots.get(bar - 1).getMidPrice();
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        double rsi = (change == 0) ? 50 : (100 * gains / change);
        value = (rsi - 50) * Math.sqrt(periodLength - 1);

        return value;
    }

}
