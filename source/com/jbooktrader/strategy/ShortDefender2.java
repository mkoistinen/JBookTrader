package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class ShortDefender2 extends ShortDefender {

    public ShortDefender2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 3000, 4000, 100, 3578);
        addParam(SCALE_FACTOR, 40, 60, 1, 49);
        addParam(ENTRY, 24, 50, 1, 37);
        addParam(EXIT, 6, 12, 1, 9);
    }
}
