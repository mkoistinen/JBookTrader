package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Mid depth balance for the last 1-second bar
 */
public class Balance extends Indicator {

    public Balance(MarketBook marketBook) {
        super(marketBook);
    }

    @Override
    public double calculate() {
        value = marketBook.getLastMarketDepth().getBalance();
        return value;
    }
}
