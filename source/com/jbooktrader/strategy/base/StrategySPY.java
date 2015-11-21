package com.jbooktrader.strategy.base;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;

/**
 * @author Eugene Kononov
 */
public abstract class StrategySPY extends Strategy {
    protected StrategySPY(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeStockContract("SPY", "SMART");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:20", "15:25", "America/New_York");
        int multiplier = 1;// contract multiplier
        double bidAskSpread = 0.01; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaStockCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);
    }

}
