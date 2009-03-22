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
    private final int entry;


    public Hybrid1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
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
        addParam(FAST_PERIOD, 22, 35, 1, 30);
        addParam(SLOW_PERIOD, 9300, 12000, 5, 10727);
        addParam(ENTRY, 21, 27, 1, 23);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double velocity = depthBalanceInd.getValue();
        double pv = priceVelocityInd.getValue();
        if (velocity >= entry && pv > 0) {
            setPosition(1);
        } else if (velocity <= -entry && pv < 0) {
            setPosition(-1);
        }
    }
}
