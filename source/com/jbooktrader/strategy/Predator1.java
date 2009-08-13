package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Predator1 extends Predator {

    public Predator1(StrategyParams optimizationParams) throws JBookTraderException {
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
        addParam(FAST_PERIOD, 2, 25, 1, 7);
        addParam(SLOW_PERIOD, 1000, 3000, 100, 2630);
        addParam(TREND_PERIOD, 400, 600, 100, 602);
        addParam(ENTRY, 12, 24, 1, 19);
    }

}
