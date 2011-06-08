package com.jbooktrader.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.common.*;

/**
 *
 */
public class SecondNature1 extends SecondNature {
    public SecondNature1(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 410, 580, 5, 545);
        addParam(TAU, 30, 45, 5, 37);
        addParam(BALANCE_ENTRY, 14, 20, 1, 17);
        addParam(VOLUME_ENTRY, 8, 12, 1, 13);
        addParam(EXIT, 15, 25, 1, 20);
    }

}
