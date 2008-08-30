package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.optimizer.*;

/**
 *
 */
public class Balancer extends StrategyES {

    // Technical indicators
    private final Indicator balanceEmaInd, rsiInd;

    // Strategy parameters names
    private static final String EMA_PERIOD = "EmaPeriod";
    private static final String RSI_PERIOD = "RsiPeriod";
    private static final String BALANCE_ENTRY = "BalanceEntry";
    private static final String RSI_ENTRY = "RsiEntry";

    // Strategy parameters values
    private final int balanceEntry, rsiEntry;


    public Balancer(StrategyParams optimizationParams) {
        super(optimizationParams);

        balanceEntry = getParam(BALANCE_ENTRY);
        rsiEntry = getParam(RSI_ENTRY);

        // Create technical indicators
        rsiInd = new PriceRSI(getParam(RSI_PERIOD));
        balanceEmaInd = new DepthBalanceEMA(getParam(EMA_PERIOD));
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
        addParam(EMA_PERIOD, 10, 25, 5, 15);
        addParam(RSI_PERIOD, 125, 145, 25, 134);
        addParam(BALANCE_ENTRY, 22, 30, 5, 27);
        addParam(RSI_ENTRY, 13, 28, 5, 15);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double rsi = rsiInd.getValue() - 50;
        double balanceEma = balanceEmaInd.getValue();
        if (balanceEma >= balanceEntry && rsi <= -rsiEntry) {
            setPosition(1);
        } else if (balanceEma <= -balanceEntry && rsi >= rsiEntry) {
            setPosition(-1);
        }
    }
}
