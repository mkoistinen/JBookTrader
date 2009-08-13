package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Predator2 extends Predator {

    public Predator2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 3, 25, 1, 12);
        addParam(SLOW_PERIOD, 600, 1800, 100, 1256);
        addParam(TREND_PERIOD, 300, 600, 100, 462);
        addParam(ENTRY, 10, 20, 1, 15);
    }
}
