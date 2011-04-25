package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class ShortDefender4 extends ShortDefender {

    public ShortDefender4(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 4500, 5500, 100, 4950);
        addParam(SCALE_FACTOR, 45, 65, 1, 54);
        addParam(ENTRY, 30, 40, 1, 34);
        addParam(EXIT, -1, 10, 1, 4);
    }
}
