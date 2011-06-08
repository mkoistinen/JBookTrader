package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class ShortDefender1 extends ShortDefender {

    public ShortDefender1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 3000, 6000, 100, 4500);
        addParam(SCALE_FACTOR, 30, 80, 1, 51);
        addParam(ENTRY, 30, 50, 1, 41);
        addParam(EXIT, -2, 15, 1, 3);
    }
}
