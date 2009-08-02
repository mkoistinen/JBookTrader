package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

import java.util.*;


/**
 * Measures the correlation between depth balance and price
 */
public class DepthPriceCorrelation extends Indicator {
    private double sumX, sumY, sumXX, sumYY, sumXY;
    private long n;
    private boolean isInRegularSession;
    private final Calendar instant;

    public DepthPriceCorrelation(TimeZone tz) {
        instant = Calendar.getInstance(tz);
    }

    @Override
    public void calculate() {

        if (!isInRegularSession) {
            long time = marketBook.getSnapshot().getTime();
            instant.setTimeInMillis(time);
            int minutesOfDay = instant.get(Calendar.HOUR_OF_DAY) * 60 + instant.get(Calendar.MINUTE);
            if (minutesOfDay >= 570) {
                isInRegularSession = true;
            }
        }

        if (isInRegularSession) {
            isInRegularSession = true;

            double balance = marketBook.getSnapshot().getBalance();
            double price = marketBook.getSnapshot().getPrice();

            n++;
            sumX += balance;
            sumXX += (balance * balance);
            sumY += price;
            sumYY += (price * price);
            sumXY += (price * balance);

            if (n > 600) { // wait for 10 minutes of data before calculating

                double numerator = n * sumXY - sumX * sumY;
                double denominator = Math.sqrt(n * sumXX - sumX * sumX) * Math.sqrt(n * sumYY - sumY * sumY);

                if (denominator != 0) {
                    value = 100 * (numerator / denominator);
                }
            }
        }
    }

    @Override
    public void reset() {
        isInRegularSession = false;
        value = sumX = sumY = sumXX = sumYY = sumXY = n = 0;
    }


}

