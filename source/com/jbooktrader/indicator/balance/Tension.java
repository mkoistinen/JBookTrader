package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class Tension extends Indicator {
    private final double multiplier;
    private final int period;
    private double balanceEMA;


    public Tension(MarketBook marketBook, int period) {
        super(marketBook);
        this.period = period;
        multiplier = 2. / (period + 1.);
    }

    @Override
    public double calculate() {
        int balance = marketBook.getLastMarketDepth().getMidBalance();
        balanceEMA += (balance - balanceEMA) * multiplier;
        double priceNow = marketBook.getLastMarketDepth().getMidPrice();
        double priceThen = marketBook.getMarketDepth(marketBook.size() - 1 - period).getMidPrice();
        double priceChange = priceNow - priceThen;

        value = priceChange - balanceEMA;

        //if (priceChange < 0 && balanceEMA > 0) {
//            value = Math.abs(balanceEMA * priceChange);
        //      }

        //    if (priceChange > 0 && balanceEMA < 0) {
        //      value = -Math.abs(balanceEMA * priceChange);
//        }


        return value;
    }
}