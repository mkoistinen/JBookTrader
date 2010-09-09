package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Balancer extends StrategyES {

    // Technical indicators
    private final Indicator tensionInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit;


    public Balancer(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        tensionInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
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
        addParam(FAST_PERIOD, 650, 1600, 10, 1338);
        addParam(SLOW_PERIOD, 8000, 13500, 100, 8964);
        addParam(ENTRY, 9, 15, 1, 10);
        addParam(EXIT, -3, 1, 1, -1);
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
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && tension <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && tension >= exit) {
                setPosition(0);
            }
        }
    }
}
