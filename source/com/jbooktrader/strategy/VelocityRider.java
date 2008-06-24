package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
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
public class VelocityRider extends Strategy {

    // Technical indicators
    private final Indicator balanceMACDInd;

    // Strategy parameters names
    private static final String PERIOD1 = "Period1";
    private static final String PERIOD2 = "Period2";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public VelocityRider(StrategyParams optimizationParams, MarketBook marketBook, PriceHistory priceHistory) throws JBookTraderException {
        super(optimizationParams, marketBook, priceHistory);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
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
        addParam(PERIOD1, 1, 60, 1, 142);
        addParam(PERIOD2, 10, 100, 1, 524);
        addParam(ENTRY, 5, 20, 1, 11);
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
        }
    }
}
