package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;


/**
 */
public class Price extends Indicator {
    public Price() {

    }

    @Override
    public void calculate() {
        value = marketBook.getSnapshot().getPrice();
    }

    @Override
    public void reset() {
        value = marketBook.getSnapshot().getPrice();
    }


}
