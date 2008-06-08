package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 *
 */
public class Balance extends Indicator {

    public Balance(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketDepth().getMidBalance();
        return value;
    }
}
