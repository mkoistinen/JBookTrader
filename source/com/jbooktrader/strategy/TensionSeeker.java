package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class TensionSeeker extends StrategyES {

    // Technical indicators
    private final Indicator tensionInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String SMOOTHING_PERIOD = "Smoothing Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;

    public TensionSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        tensionInd = new Tension(getTradingSchedule().getTimeZone(), getParam(PERIOD), getParam(SMOOTHING_PERIOD));
        addIndicator(tensionInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 2000, 5000, 1, 2510);
        addParam(SMOOTHING_PERIOD, 5, 300, 100, 110);
        addParam(ENTRY, 45, 75, 1, 62);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double tension = tensionInd.getValue();
        if (tension <= -entry) {
            int position = getPositionManager().getPosition();
            if (position == 0) {
                double balance = getMarketBook().getSnapshot().getBalance();
                if (balance < -15) {
                    setPosition(-1);
                } else if (balance > 15) {
                    setPosition(1);
                }
            }
        } else if (tension >= entry) {
            setPosition(0);
        }
    }
}
