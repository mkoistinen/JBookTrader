package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

import java.util.*;


/**
 *
 */
public class Tension extends Indicator {
    private double sumX, sumY, sumXX, sumYY, sumXY;
    private boolean isInRegularSession;
    private final Calendar instant;
    private final LinkedList<Double> prices, balances;
    private final int periodLength;
    private final double multiplier;

    public Tension(TimeZone tz, int periodLength, int emaPeriod) {
        instant = Calendar.getInstance(tz);
        this.periodLength = periodLength;
        multiplier = 2. / (emaPeriod + 1.);
        prices = new LinkedList<Double>();
        balances = new LinkedList<Double>();
    }

    @Override
    public void calculate() {

        if (!isInRegularSession) {
            long time = marketBook.getSnapshot().getTime();
            instant.setTimeInMillis(time);
            int minutesOfDay = instant.get(Calendar.HOUR_OF_DAY) * 60 + instant.get(Calendar.MINUTE);
            if (minutesOfDay >= 580) {
                isInRegularSession = true;
            }
        }

        if (isInRegularSession) {

            double price = marketBook.getSnapshot().getPrice();
            double balance = marketBook.getSnapshot().getBalance();

            prices.add(price);
            balances.add(balance);

            sumX += balance;
            sumXX += (balance * balance);
            sumY += price;
            sumYY += (price * price);
            sumXY += (price * balance);

            if (prices.size() > periodLength) {
                double oldPrice = prices.removeFirst();
                double oldBalance = balances.removeFirst();
                sumX -= oldBalance;
                sumXX -= (oldBalance * oldBalance);
                sumY -= oldPrice;
                sumYY -= (oldPrice * oldPrice);
                sumXY -= (oldPrice * oldBalance);

                double numerator = periodLength * sumXY - sumX * sumY;
                double denominator = Math.sqrt(periodLength * sumXX - sumX * sumX) * Math.sqrt(periodLength * sumYY - sumY * sumY);

                if (denominator != 0) {
                    double spot = 100 * (numerator / denominator);
                    value += (spot - value) * multiplier;
                }
            }
        }
    }

    @Override
    public void reset() {
        isInRegularSession = false;
        value = 0;
        sumX = sumY = sumXX = sumYY = sumXY = 0;
        prices.clear();
        balances.clear();
    }


}

