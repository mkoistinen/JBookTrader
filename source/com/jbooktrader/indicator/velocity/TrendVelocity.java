package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

import java.util.*;


/**
 *
 */
public class TrendVelocity extends Indicator {
    private double sumTime, sumTimeSquared, sumPrice, sumTimePrice;
    private long time;
    private final LinkedList<Double> prices, times;
    private final int periodLength;
    private final double multiplier;
    private double smoothedTrend;

    public TrendVelocity(int periodLength) {
        this.periodLength = periodLength;
        multiplier = 2.0 / (periodLength + 1.0);
        prices = new LinkedList<Double>();
        times = new LinkedList<Double>();
    }

    @Override
    public void calculate() {

        double price = marketBook.getSnapshot().getPrice();
        time++;

        prices.add(price);
        times.add((double) time);

        sumTime += time;
        sumTimeSquared += time * time;
        sumPrice += price;
        sumTimePrice += time * price;

        if (prices.size() > periodLength) {
            double oldTime = times.removeFirst();
            double oldPrice = prices.removeFirst();

            sumTime -= oldTime;
            sumTimeSquared -= oldTime * oldTime;
            sumPrice -= oldPrice;
            sumTimePrice -= oldTime * oldPrice;

            double numerator = periodLength * sumTimePrice - sumTime * sumPrice;
            double denominator = periodLength * sumTimeSquared - sumTime * sumTime;

            if (denominator != 0) {
                double trend = 100 * (numerator / denominator);
                smoothedTrend += multiplier * (trend - smoothedTrend);
                value = 100 * (trend - smoothedTrend);
            }
        }
    }

    @Override
    public void reset() {
        value = time = 0;
        smoothedTrend = 0;
        sumTime = sumTimeSquared = sumPrice = sumTimePrice = 0;
        prices.clear();
        times.clear();
    }
}

