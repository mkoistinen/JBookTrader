package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class LongDefender4 extends LongDefender {

    public LongDefender4(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 3500, 5000, 100, 5384);
        addParam(SCALE_FACTOR, 45, 55, 5, 47);
        addParam(ENTRY, 47, 54, 1, 52);
        addParam(EXIT, -6, 10, 1, -16);
    }
}
