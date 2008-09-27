package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;

public class PriceMACD extends Indicator {

    private final PriceEMA fastEMA, slowEMA;

    public PriceMACD(int fastLength, int slowLength) {
        this.fastEMA = new PriceEMA(fastLength);
        this.slowEMA = new PriceEMA(slowLength);
    }

    @Override
    public double calculate() {

        return fastEMA.calculate() - slowEMA.calculate();
    }


    @Override
    public void setMarketBook(MarketBook marketBook) {
        super.setMarketBook(marketBook);
        fastEMA.setMarketBook(marketBook);
        slowEMA.setMarketBook(marketBook);
    }

}
