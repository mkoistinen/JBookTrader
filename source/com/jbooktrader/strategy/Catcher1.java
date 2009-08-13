package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Catcher1 extends Catcher {


    public Catcher1(StrategyParams optimizationParams) throws JBookTraderException {
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
        addParam(FAST_PERIOD, 12, 14, 1, 13);
        addParam(SLOW_PERIOD, 2000, 3000, 5, 2371);
        addParam(TREND_PERIOD, 100, 200, 5, 182);
        addParam(ENTRY, 15, 19, 1, 17);
    }

}
