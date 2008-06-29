package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
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
public class WildCat extends Strategy {

    // Technical indicators
    private final Indicator balanceMACDInd;

    // Strategy parameters names
    private static final String PERIOD1 = "Period1";
    private static final String PERIOD2 = "Period2";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public WildCat(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        balanceMACDInd = new BalanceMACD(marketBook, getParam(PERIOD1), getParam(PERIOD2));
        addIndicator("balanceMACD", balanceMACDInd);


    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        /*
        addParam(PERIOD1, 100, 200, 10, 145);
        addParam(PERIOD2, 400, 600, 10, 525);
        addParam(ENTRY, 5, 30, 1, 14);
        addParam(EXIT, 1, 20, 1, 8);
        */

        // 145-525-14-8
        // 156-741-16-8

        addParam(PERIOD1, 100, 200, 5, 156);
        addParam(PERIOD2, 400, 800, 5, 741);
        addParam(ENTRY, 9, 18, 1, 16);
        addParam(EXIT, 5, 10, 1, 8);


    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceMACD = balanceMACDInd.getValue();
        if (balanceMACD >= entry) {
            setPosition(1);
        } else if (balanceMACD <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && balanceMACD <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && balanceMACD >= exit) {
                setPosition(0);
            }
        }
    }
}
