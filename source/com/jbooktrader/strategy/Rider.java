package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.derivative.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Rider extends StrategyES {

    // Technical indicators
    private final Indicator balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Rider(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        Indicator balanceInd = new DepthBalance();
        balanceVelocityInd = new Velocity(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(balanceInd);
        addIndicator(balanceVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 1, 100, 50, 30);
        addParam(SLOW_PERIOD, 200, 900, 100, 405);
        addParam(ENTRY, 15, 45, 1, 25);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        if (balanceVelocity >= entry) {
            setPosition(1);
        } else if (balanceVelocity <= -entry) {
            setPosition(-1);
        }
    }
}
