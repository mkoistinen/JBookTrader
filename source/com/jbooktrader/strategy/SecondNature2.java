package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class SecondNature2 extends SecondNature {
    public SecondNature2(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 400, 600, 1, 436);
        addParam(TAU, -5, 20, 5, 0);
        addParam(BALANCE_ENTRY, 13, 20, 1, 15);
        addParam(VOLUME_ENTRY, 3, 20, 1, 9);
        addParam(EXIT, 15, 25, 1, 23);
    }

}
