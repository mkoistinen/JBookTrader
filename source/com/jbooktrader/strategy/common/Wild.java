package com.jbooktrader.strategy.common;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;


/**
 *
 */
public abstract class Wild extends Pendulum {
    public Wild(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void onBookSnapshot() {
        double volumeAcceleration = volumeAccelerationInd.getValue();
        double finalVelocity = balanceVelocityInd.getValue() + tau * Math.abs(balanceAccelerationInd.getValue());
        if (volumeAcceleration <= -volumeEntry) {
            if (finalVelocity >= balanceEntry) {
                setPosition(1);
            }
        } else if (volumeAcceleration - finalVelocity >= exit) {
            setPosition(0);
        }
    }
}
