package com.jbooktrader.strategy.common;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;


/**
 *
 */
public abstract class SecondNature extends Pendulum {
    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
    }

    @Override
    public void onBookSnapshot() {
        double volumeAcceleration = volumeAccelerationInd.getValue();
        double finalVelocity = balanceVelocityInd.getValue() + tau * balanceAccelerationInd.getValue();
        if (volumeAcceleration <= -volumeEntry) {
            if (finalVelocity >= balanceEntry) {
                setPosition(1);
            }
        } else if (volumeAcceleration - finalVelocity >= exit) {
            setPosition(0);
        }
    }
}
