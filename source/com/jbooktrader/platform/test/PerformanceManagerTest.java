package com.jbooktrader.platform.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

public class PerformanceManagerTest {

    private class DummyStrategy extends Strategy {

        protected DummyStrategy(StrategyParams params) throws JBookTraderException {
            super(params);

            Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
            TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");
            int multiplier = 50;// contract multiplier
            Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
            setStrategy(contract, tradingSchedule, multiplier, commission);
            
            setMarketBook(new MarketBook());
        }

        @Override
        public void onBookChange() {
        }

        @Override
        protected void setParams() {
        }
        
    }
    
    /**
     * Test the trade count algorithm
     * @throws JBookTraderException
     */
    @Test
    public void testUpdateOnTrade() throws JBookTraderException {
        int trades=0;
        DummyStrategy strategy = new DummyStrategy(new StrategyParams());
        PerformanceManager pm = strategy.getPerformanceManager();
        assertEquals(trades, pm.getTrades());

        // 0 -> 1
        pm.updateOnTrade(1, 1.0, 1);
        assertEquals(trades, pm.getTrades());
        
        // 1 -> 2
        pm.updateOnTrade(1, 1.0, 2);
        assertEquals(trades, pm.getTrades());
        
        // 2 -> 1
        trades++;
        pm.updateOnTrade(1, 1.0, 1);
        assertEquals(trades, pm.getTrades());

        // 1 -> 0
        trades++;
        pm.updateOnTrade(1, 1.0, 0);
        assertEquals(trades, pm.getTrades());
        
        // 0 -> -1
        pm.updateOnTrade(1, 1.0, -1);
        assertEquals(trades, pm.getTrades());
        
        // -1 -> -2
        pm.updateOnTrade(1, 1.0, -2);
        assertEquals(trades, pm.getTrades());
        
        // -2 -> -1
        trades++;
        pm.updateOnTrade(1, 1.0, -1);
        assertEquals(trades, pm.getTrades());

        // -1 -> 1
        trades++;
        pm.updateOnTrade(1, 1.0, 1);
        assertEquals(trades, pm.getTrades());

        // 1 -> -1
        trades++;
        pm.updateOnTrade(1, 1.0, -1);
        assertEquals(trades, pm.getTrades());
        
        // -1 -> 0
        trades++;
        pm.updateOnTrade(1, 1.0, 0);
        assertEquals(trades, pm.getTrades());
    }

}
