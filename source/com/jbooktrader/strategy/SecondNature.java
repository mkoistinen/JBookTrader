package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public class SecondNature extends Strategy {

    // Technical indicators
    private final Indicator balanceVelocityInd, trendVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String TREND_PERIOD = "Trend Period";
    private static final String BALANCE_ENTRY = "Balance Entry";

    // Strategy parameters values
    private final int balanceEntry;

    public SecondNature(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "14:00", "America/New_York");
        int multiplier = 50;// contract multiplier
        double bidAskSpread = 0.25; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread, BarSize.Minute1);


        balanceEntry = getParam(BALANCE_ENTRY);
        balanceVelocityInd = new BalanceVelocity(getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        trendVelocityInd = new TrendStrengthVelocity(getParam(TREND_PERIOD));
        addIndicator(balanceVelocityInd);
        addIndicator(trendVelocityInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 80, 230, 1, 105);
        addParam(SLOW_PERIOD, 5500, 7500, 100, 6252);
        addParam(TREND_PERIOD, 500, 1500, 100, 722);
        addParam(BALANCE_ENTRY, 12, 21, 1, 19);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double trendVelocity = trendVelocityInd.getValue();
        if (trendVelocity < 0) {
            if (balanceVelocity >= balanceEntry) {
                setPosition(1);
            } else if (balanceVelocity <= -balanceEntry) {
                setPosition(-1);
            }
        }
    }
}
