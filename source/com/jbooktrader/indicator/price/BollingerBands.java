package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

import java.util.*;

/**
 * This indicator computes the standard deviation of the last <period> prices.
 * <p/>
 * Since JBT can currently only display one value for each indicator plot, this
 * indicator plots the Standard Deviation only.
 * <p/>
 * However, to obtain the Bollinger Bands, use the methods below:
 * getMidpoint() -- this returns the midpoint of the BollingerBands
 * getUpperBand(), getLowerBand -- these return the upper and lower Bollinger Bands.
 * <p/>
 * There is an alternative constructor for times when only the Standard Deviation is required.
 *
 * @author mkoistinen
 */
public class BollingerBands extends Indicator {

    private final int period;
    private final double multiple;

    private LinkedList<Double> history = new LinkedList<Double>();
    private double price, sum, mean, sum_sqr, sigma;

    public BollingerBands(int period) {
        this(period, 1.0);
    }

    public BollingerBands(int period, double multiple) {
        this.period = period;
        this.multiple = multiple;
        reset();
    }

    @Override
    public void reset() {
        value = sum = sum_sqr = mean = sigma = 0.0;
        history.clear();
    }

    @Override
    public void calculate() {
        price = marketBook.getSnapshot().getPrice();

        history.addLast(price);
        sum += price;
        sum_sqr += price * price;

        if (history.size() > period) {
            price = history.removeFirst();
            sum -= price;
            sum_sqr -= price * price;
        }

        if (history.size() > 0) {
            mean = sum / history.size();
            value = sigma = Math.sqrt((sum_sqr - sum * mean) / history.size());
        }
    }

    /**
     * This returns the mean of the sample of prices and is the same as the midpoint of the Bollinger Bands
     *
     * @return Midpoint of the Bollinger Bands or the mean of the prices sampled.
     */
    public double getMidpoint() {
        return mean;
    }

    /**
     * @return The upper band of the Bollinger Bands specified by the period and multiple.
     */
    public double getUpperBand() {
        return mean + sigma * multiple;
    }

    /**
     * @return The lower band of the Bollinger Bands specified by the period and multiple.
     */
    public double getLowerBand() {
        return mean - sigma * multiple;
    }

    public double getWidth() {
        return sigma * multiple * 2;
    }

}
