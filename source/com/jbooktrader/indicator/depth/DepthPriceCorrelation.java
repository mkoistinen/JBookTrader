package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;


/**
 */
public class DepthPriceCorrelation extends Indicator {
    private double sumX, sumY, sumXX, sumYY, sumXY;
    private long n;

    public DepthPriceCorrelation() {
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        double price = marketBook.getSnapshot().getPrice();

        sumX += balance;
        sumXX += (balance * balance);
        sumY += price;
        sumYY += (price * price);
        sumXY += (price * balance);
        n++;

        double nom = n * sumXY - sumX * sumY;
        double denom = Math.sqrt(n * sumXX - sumX * sumX) * Math.sqrt(n * sumYY - sumY * sumY);

        if (n > 3600) {
            if (denom != 0) {
                value = 100 * (nom / denom);
            }
        }
    }

    @Override
    public void reset() {
        sumX = sumY = sumXX = sumYY = sumXY = n = 0;
    }


}

