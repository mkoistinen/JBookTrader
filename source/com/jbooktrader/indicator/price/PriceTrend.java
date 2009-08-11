package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

import java.util.*;


/**
 * Calculates the slope of the "least squares" regression line through prices in the fixed-length time window
 */
public class PriceTrend extends Indicator {
    private double sumTime, sumTimeSquared, sumPrice, sumTimePrice;
    private int time;
    private final LinkedList<Double> prices;
    private final int periodLength;


    public PriceTrend(int periodLength) {
        this.periodLength = periodLength;
        prices = new LinkedList<Double>();
    }

    @Override
    public void calculate() {

        time++;
        double price = marketBook.getSnapshot().getPrice();
        prices.add(price);

        sumTime += time;
        sumTimeSquared += time * time;
        sumPrice += price;
        sumTimePrice += time * price;

        if (prices.size() > periodLength) {
            int oldTime = time - periodLength;
            double oldPrice = prices.removeFirst();

            sumTime -= oldTime;
            sumTimeSquared -= oldTime * oldTime;
            sumPrice -= oldPrice;
            sumTimePrice -= oldTime * oldPrice;

            double numerator = periodLength * sumTimePrice - sumTime * sumPrice;
            double denominator = periodLength * sumTimeSquared - sumTime * sumTime;

            if (denominator != 0) {
                value = 100 * (numerator / denominator);
            }
        }

    }

    @Override
    public void reset() {
        prices.clear();
        sumTime = sumTimeSquared = sumPrice = sumTimePrice = value = time = 0;
    }
}

