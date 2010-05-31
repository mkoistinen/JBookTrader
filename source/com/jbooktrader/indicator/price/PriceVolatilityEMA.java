package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

public class PriceVolatilityEMA extends Indicator {
    private final double multiplier;
    private final int period;
    private PriceVolatility priceVolatility;

    public PriceVolatilityEMA(int period, int smoothingPeriod) {
        this.period = period;
        multiplier = 2.0 / (smoothingPeriod + 1.0);
    }

    @Override
    public void setMarketBook(MarketBook marketBook) {
        super.setMarketBook(marketBook);
        priceVolatility = new PriceVolatility(period);
        priceVolatility.setMarketBook(marketBook);
    }

    @Override
    public void calculate() {
        priceVolatility.calculate();
        value += (priceVolatility.getValue() - value) * multiplier;
    }

    @Override
    public void reset() {
        value = 0;
    }
}
