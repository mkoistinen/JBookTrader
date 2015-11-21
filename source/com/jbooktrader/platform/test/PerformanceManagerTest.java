package com.jbooktrader.platform.test;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Eugene Kononov
 */
public class PerformanceManagerTest {

    private class DummyStrategy extends Strategy {

        protected DummyStrategy(StrategyParams params) throws JBookTraderException {
            super(params);

            Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
            TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");
            int multiplier = 50;// contract multiplier
            Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
            setStrategy(contract, tradingSchedule, multiplier, commission, 0.25);

            MarketBook marketBook = new MarketBook();
            marketBook.setSnapshot(new MarketSnapshot(0, 0, 0, 0));
            setMarketBook(marketBook);
        }

        @Override
        public void onBookSnapshot() {
        }

        @Override
        protected void setParams() {
        }

        @Override
        public void setIndicators() {
        }
    }

    /**
     * Test the trade count algorithm
     *
     * @throws JBookTraderException
     */
    @Test
    public void testUpdateOnTrade() throws JBookTraderException {
        int trades = 0;
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
