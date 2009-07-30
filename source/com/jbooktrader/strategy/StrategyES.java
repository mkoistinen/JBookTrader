package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public abstract class StrategyES extends Strategy {

    protected StrategyES(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "14:00", "America/New_York");
        int multiplier = 50;// contract multiplier
        double bidAskSpread = 0.25; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread, BarSize.Minute1);
    }

}
