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
public class Scalper extends Strategy {

    // Technical indicators
    private final Indicator balanceEmaInd;

    // Strategy parameters names
    private static final String EMA_PERIOD = "EmaPeriod";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public Scalper(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        balanceEmaInd = new BalanceEMA(marketBook, getParam(EMA_PERIOD));
        addIndicator("BalanceEMA", balanceEmaInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(EMA_PERIOD, 1, 50, 1, 150);
        addParam(ENTRY, 15, 45, 1, 30);
        addParam(EXIT, 0, 45, 1, 10);
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
