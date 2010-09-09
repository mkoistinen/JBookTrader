package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

/**
 * This class is responsible for running the strategy against historical market data
 */
public class BackTester {
    private final Strategy strategy;
    private final BackTestFileReader backTestFileReader;
    private final BackTestDialog backTestDialog;
    private boolean isCanceled;

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    public void cancel() {
        isCanceled = true;
    }

    public void execute() {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();
        PerformanceChartData performanceChartData = strategy.getPerformanceManager().getPerformanceChartData();

        long marketDepthCounter = 0;
        long size = backTestFileReader.getSnapshotCount();
        MarketSnapshot marketSnapshot;
        while (!isCanceled && (marketSnapshot = backTestFileReader.next()) != null) {
            marketDepthCounter++;
            marketBook.setSnapshot(marketSnapshot);
            performanceChartData.update(marketSnapshot);
            long instant = marketSnapshot.getTime();
            strategy.processInstant(instant, tradingSchedule.contains(instant));
            performanceChartData.update(indicatorManager.getIndicators(), instant);

            if (marketDepthCounter % 50000 == 0) {
                backTestDialog.setProgress(marketDepthCounter, size);
            }
        }

        if (!isCanceled) {
            // go flat at the end of the test period to finalize the run
            strategy.closePosition();
            positionManager.trade();
            strategy.setIsActive(false);
            Dispatcher.getInstance().fireModelChanged(Event.StrategyUpdate, strategy);
        }
    }
}
