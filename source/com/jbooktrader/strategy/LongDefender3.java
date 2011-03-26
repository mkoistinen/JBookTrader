package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class LongDefender3 extends LongDefender {

    public LongDefender3(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 5, 600, 100, 4950);
        addParam(SCALE_FACTOR, -30, 120, 5, 46);
        addParam(ENTRY, 5, 60, 1, 50);
        addParam(EXIT, -25, 30, 1, -2);
    }
}
