package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class SecondNature extends StrategyES {

    // Technical indicators
    private final Indicator balanceEmaInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit;


    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);

        // Create technical indicators
        balanceEmaInd = new DepthBalanceEMA(getParam(PERIOD));
        addIndicator(balanceEmaInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 1, 250, 1, 240);
        addParam(ENTRY, 15, 45, 1, 23);
        addParam(EXIT, 5, 15, 1, 6);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceEma = balanceEmaInd.getValue();
        if (balanceEma >= entry) {
            setPosition(1);
        } else if (balanceEma <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && balanceEma <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && balanceEma >= exit) {
                setPosition(0);
            }
        }
    }
}
