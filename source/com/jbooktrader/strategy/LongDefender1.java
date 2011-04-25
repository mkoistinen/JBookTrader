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
        addParam(PERIOD, 2500, 9000, 100, 4725);
        addParam(SCALE_FACTOR, 15, 125, 5, 45);
        addParam(ENTRY, 15, 125, 1, 49);
        addParam(EXIT, -85, 20, 1, 4);
    }
}
