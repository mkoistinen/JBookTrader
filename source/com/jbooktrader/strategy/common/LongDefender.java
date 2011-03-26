package com.jbooktrader.strategy.common;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;


/**
 *
 */
public abstract class LongDefender extends Defender {

    protected LongDefender(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension >= entry) {
            setPosition(1);
        } else if (tension <= -exit) {
            setPosition(0);
        }
    }
}
