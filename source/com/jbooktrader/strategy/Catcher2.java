package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Catcher2 extends Catcher {
    public Catcher2(StrategyParams optimizationParams) throws JBookTraderException {
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
        addParam(FAST_PERIOD, 8, 12, 1, 10);
        addParam(SLOW_PERIOD, 300, 1800, 5, 1143);
        addParam(TREND_PERIOD, 100, 200, 5, 160);
        addParam(ENTRY, 11, 15, 1, 13);
    }

}
