package com.jbooktrader.strategy;

import com.jbooktrader.indicator.volume.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Dicey1 extends StrategyES {

    // Technical indicators
    private final Indicator directionalVolumeInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Dicey1(StrategyParams optimizationParams) {
        super(optimizationParams);

        entry = getParam(ENTRY);
        directionalVolumeInd = new DirectionalVolume(getParam(PERIOD));
        addIndicator(directionalVolumeInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 60, 125, 1, 96);
        addParam(ENTRY, 35, 50, 1, 41);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double directionalVolume = directionalVolumeInd.getValue();
        if (directionalVolume >= entry) {
            setPosition(1);
        } else if (directionalVolume <= -entry) {
            setPosition(-1);
        }
    }
}
