package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

/**
 * This class is responsible for running the strategy against historical market data
 */
public class BackTester {
	private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private final Strategy strategy;
    private final BackTestFileReader backTestFileReader;
    private final BackTestDialog backTestDialog;
    
    private long lastInstant;

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    public void execute() {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();
        PerformanceChartData performanceChartData = strategy.getPerformanceChartData();

        long marketDepthCounter = 0;
        long size = backTestFileReader.getSnapshotCount();

        MarketSnapshot marketSnapshot;
        while ((marketSnapshot = backTestFileReader.next()) != null) {
            marketDepthCounter++;
            marketBook.setSnapshot(marketSnapshot);
            performanceChartData.updatePrice(marketSnapshot);
            long instant = marketSnapshot.getTime();
            strategy.setTime(instant);
            indicatorManager.updateIndicators();
            performanceChartData.updateIndicators(indicatorManager.getIndicators(), instant);

            if (tradingSchedule.contains(instant)) {
            	if (instant - lastInstant > GAP_SIZE) strategy.reset();
            	lastInstant = instant;

                if (indicatorManager.hasValidIndicators()) {
                    strategy.onBookChange();
                }
            } else {
                strategy.closePosition();// force flat position
            }

            positionManager.trade();
            if (marketDepthCounter % 10000 == 0) {
                backTestDialog.setProgress(marketDepthCounter, size);
            }
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
    }
}
