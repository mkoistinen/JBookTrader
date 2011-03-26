package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class LongDefender1 extends LongDefender {

    public LongDefender1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 3500, 5000, 100, 4725);
        addParam(SCALE_FACTOR, 45, 55, 5, 45);
        addParam(ENTRY, 47, 54, 1, 49);
        addParam(EXIT, -6, 10, 1, 4);
    }
}
