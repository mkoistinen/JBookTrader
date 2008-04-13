package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.*;
import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public class Hybrid extends Strategy {
    // Technical indicators
    private final Indicator depthVelocityInd, rsiInd;

    // Strategy parameters names
    private static final String DEPTH_PERIOD = "Depth Period";
    private static final String RSI_PERIOD = "RSI Period";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public Hybrid(StrategyParams optimizationParams, MarketBook marketBook, PriceHistory priceHistory) throws JBookTraderException {
        super(optimizationParams, marketBook, priceHistory);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        int depthPeriod = getParam(DEPTH_PERIOD);
        int rsiPeriod = getParam(RSI_PERIOD);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);

        // Create technical indicators
        depthVelocityInd = new DepthVelocity(marketBook, depthPeriod);
        rsiInd = new RSI(priceHistory, rsiPeriod);
        addIndicator("DepthVelocity", depthVelocityInd);
        addIndicator("RSI", rsiInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(DEPTH_PERIOD, 2, 8, 1, 5);
        addParam(RSI_PERIOD, 5, 15, 1, 10);
        addParam(ENTRY, 55, 120, 1, 68);
        addParam(EXIT, 25, 50, 1, 42);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double depthVelocity = depthVelocityInd.getValue();
        double rsi = rsiInd.getValue() - 50;
        if (depthVelocity <= -entry) {
            setPosition(-1);
        } else if (depthVelocity >= entry) {
            setPosition(1);
        } else {
            boolean flat = (currentPosition > 0 && rsi <= -exit) || (currentPosition < 0 && rsi >= exit);
            if (flat) {
                setPosition(0);
            }
        }
    }
}
