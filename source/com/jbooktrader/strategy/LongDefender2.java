package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class LongDefender2 extends LongDefender {

    public LongDefender2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 5, 600, 100, 5622);
        addParam(SCALE_FACTOR, -30, 120, 5, 54);
        addParam(ENTRY, 5, 60, 1, 55);
        addParam(EXIT, -25, 30, 1, -12);
    }
}
