package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class ShortDefender3 extends ShortDefender {

    public ShortDefender3(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 1850, 7500, 100, 3695);
        addParam(SCALE_FACTOR, 13, 90, 1, 48);
        addParam(ENTRY, 24, 46, 1, 37);
        addParam(EXIT, -1, 17, 1, 9);
    }
}
