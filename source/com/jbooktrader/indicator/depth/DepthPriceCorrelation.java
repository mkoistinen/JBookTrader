package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

import java.util.*;


/**
 *
 */
public class DepthPriceCorrelation extends Indicator {
    private double sumBalance, sumPrice, sumBalanceSquared, sumPriceSquared, sumBalancePrice;
    private double price, balance;
    private final LinkedList<Double> prices, balances;
    private final int periodLength;
    private final double multiplier;

    public DepthPriceCorrelation(int periodLength) {
        this.periodLength = periodLength;
        prices = new LinkedList<Double>();
        balances = new LinkedList<Double>();
        multiplier = 2.0 / (30 + 1.0);
    }

    @Override
    public void calculate() {

        double p = marketBook.getSnapshot().getPrice();
        double b = marketBook.getSnapshot().getBalance();

        if (price == 0) {
            price = p;
        }
        price += (p - price) * multiplier;
        balance += (b - balance) * multiplier;

        prices.add(price);
        balances.add(balance);

        sumBalance += balance;
        sumBalanceSquared += (balance * balance);
        sumPrice += price;
        sumPriceSquared += (price * price);
        sumBalancePrice += (balance * price);

        if (prices.size() > periodLength) {
            double oldPrice = prices.removeFirst();
            double oldBalance = balances.removeFirst();
            sumBalance -= oldBalance;
            sumBalanceSquared -= (oldBalance * oldBalance);
            sumPrice -= oldPrice;
            sumPriceSquared -= (oldPrice * oldPrice);
            sumBalancePrice -= (oldBalance * oldPrice);

            double numerator = periodLength * sumBalancePrice - sumBalance * sumPrice;
            double denominator = Math.sqrt(periodLength * sumBalanceSquared - sumBalance * sumBalance) * Math.sqrt(periodLength * sumPriceSquared - sumPrice * sumPrice);

            if (denominator != 0) {
                value = 100 * (numerator / denominator);
            }
        }
    }

    @Override
    public void reset() {
        price = balance = value = 0;
        sumBalance = sumPrice = sumBalanceSquared = sumPriceSquared = sumBalancePrice = 0;
        prices.clear();
        balances.clear();
    }

    public double getPriceSlope() {
        return prices.getLast() - prices.getFirst();
    }

}

