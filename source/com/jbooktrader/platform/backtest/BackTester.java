package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
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

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    public void execute() throws JBookTraderException {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();
        PriceHistory priceHistory = strategy.getPriceBarHistory();


        long lineCount = 0;
        long totalLines = backTestFileReader.getTotalLineCount();
        backTestFileReader.reset();
        MarketDepth marketDepth;
        while ((marketDepth = backTestFileReader.getNextMarketDepth()) != null) {
            lineCount++;
            priceHistory.update(marketDepth.getTime(), marketDepth.getMidPoint());
            marketBook.add(marketDepth);
            long instant = marketBook.getLastMarketDepth().getTime();
            strategy.setTime(instant);
            strategy.updateIndicators();
            if (strategy.hasValidIndicators()) {
                strategy.onBookChange();
            }

            if (!tradingSchedule.contains(instant)) {
                strategy.closePosition();// force flat position
            }

            positionManager.trade();
            if (lineCount % 1000 == 0) {
                backTestDialog.setProgress(lineCount, totalLines, "Running back test");
            }
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
    }

}
