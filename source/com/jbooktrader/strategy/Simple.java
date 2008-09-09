package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Simple extends StrategyES {

    // Technical indicators
    private final Indicator balanceEmaInd, rsiInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Simple(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        // Create technical indicators
        rsiInd = new PriceRSI(getParam(PERIOD));
        balanceEmaInd = new DepthBalanceEMA(getParam(PERIOD));
        addIndicator(rsiInd);
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
        addParam(PERIOD, 5, 100, 5, 7);
        addParam(ENTRY, 30, 90, 5, 78);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balance = balanceEmaInd.getValue() - (rsiInd.getValue() - 50);
        if (balance >= entry) {
            setPosition(1);
        } else if (balance <= -entry) {
            setPosition(-1);
        }
    }
}
