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
        addParam(PERIOD, 1850, 7500, 100, 3578);
        addParam(SCALE_FACTOR, 13, 90, 1, 49);
        addParam(ENTRY, 24, 46, 1, 37);
        addParam(EXIT, -1, 17, 1, 9);
    }
}
