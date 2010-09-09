package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class TensionSeeker extends StrategyES {

    // Technical indicators
    private final Indicator tensionInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";


    // Strategy parameters values
    private final int entry;


    public TensionSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        tensionInd = new Tension(getParam(FAST_PERIOD), getParam(SLOW_PERIOD), 1);
        addIndicator(tensionInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 200, 5000, 50, 456);
        addParam(SLOW_PERIOD, 2000, 6000, 100, 4410);
        addParam(ENTRY, 1, 20, 1, 14);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension >= entry) {
            setPosition(1);
        } else if (tension <= -entry) {
            setPosition(-1);
        }
    }
}
