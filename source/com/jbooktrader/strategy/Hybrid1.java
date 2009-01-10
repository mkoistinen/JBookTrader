package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Hybrid1 extends StrategyES {

    // Technical indicators
    private final Indicator priceVelocityInd, depthBalanceInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";
    private static final String MULTIPLIER = "Multiplier";

    // Strategy parameters values
    private final double multiplier;
    private final int entry;


    public Hybrid1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        multiplier = getParam(MULTIPLIER) / 10d;
        priceVelocityInd = new PriceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        depthBalanceInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(priceVelocityInd);
        addIndicator(depthBalanceInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 20, 40, 50, 27);
        addParam(SLOW_PERIOD, 8000, 12000, 100, 9453);
        addParam(ENTRY, 24, 28, 1, 26);
        addParam(MULTIPLIER, 14, 23, 1, 18);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double velocity = depthBalanceInd.getValue() + multiplier * priceVelocityInd.getValue();
        if (velocity >= entry) {
            setPosition(1);
        } else if (velocity <= -entry) {
            setPosition(-1);
        }
    }
}
