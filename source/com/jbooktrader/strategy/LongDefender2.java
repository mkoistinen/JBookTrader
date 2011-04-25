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
        addParam(PERIOD, 2000, 9000, 250, 7963);
        addParam(SCALE_FACTOR, 10, 130, 1, 87);
        addParam(ENTRY, 20, 100, 1, 85);
        addParam(EXIT, 0, 80, 1, -39);
    }


}
