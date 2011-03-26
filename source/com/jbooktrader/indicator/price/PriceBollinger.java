package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

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
public class PriceBollinger extends Indicator {

    private final int multiple;
    private final MovingWindowStDev prices;
    private double mean, sigma;

    public PriceBollinger(int period) {
        this(period, 1);
    }

    public PriceBollinger(int period, int multiple) {
        super(period, multiple);
        this.multiple = multiple;
        prices = new MovingWindowStDev(period);
    }

    @Override
    public void reset() {
        value = 0;
        prices.clear();
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        prices.add(price);

        if (prices.isFull()) {
            mean = prices.getMean();
            value = sigma = prices.getStdev();
        }
    }

    /**
     * Returns the mean of the sample of prices and is the same as the midpoint of the Bollinger Bands
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
