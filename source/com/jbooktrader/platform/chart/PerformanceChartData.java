package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

import java.util.*;


/**
 * Encapsulates performance chart data.
 */
public class PerformanceChartData {
    private final Strategy strategy;

    public PerformanceChartData(Strategy strategy) {
        this.strategy = strategy;
    }

    public TimeSeries getProfitAndLossSeries() {
        NetProfitHistory netProfitHistory = strategy.getPerformanceManager().getProfitAndLossHistory();
        TimeSeries ts = new TimeSeries("P&L", Second.class);
        ts.setRangeDescription("P&L");

        for (TimedValue profitAndLoss : netProfitHistory.getHistory()) {
            ts.addOrUpdate(new Second(new Date(profitAndLoss.getTime())), profitAndLoss.getValue());
        }

        ts.fireSeriesChanged();
        return ts;
    }

    public OHLCDataset getPriceDataset(long frequency) {
        MarketBook marketBook = strategy.getMarketBook();
        List<Bar> priceBars = new ArrayList<Bar>();

        Bar bar = null;
        for (MarketSnapshot marketSnapshot : marketBook.getAll()) {
            long time = marketSnapshot.getTime();
            double open, close;
            open = close = marketSnapshot.getMidPrice();
            double low = marketSnapshot.getBestBid();
            double high = marketSnapshot.getBestAsk();

            // Integer division gives us the number of whole periods
            long completedPeriods = time / frequency;
            long barTime = (completedPeriods + 1) * frequency;

            if (bar == null) {
                bar = new Bar(barTime, open, high, low, close);
            }

            if (barTime > bar.getTime()) {
                priceBars.add(bar);
                bar = new Bar(barTime, open, high, low, close);
            }

            bar.setClose(close);
            bar.setLow(Math.min(low, bar.getLow()));
            bar.setHigh(Math.max(high, bar.getHigh()));
        }

        int size = priceBars.size();
        OHLCDataItem[] items = new OHLCDataItem[size];

        int index = 0;
        for (Bar priceBar : priceBars) {
            Date date = new Date(priceBar.getTime());
            double high = priceBar.getHigh();
            double low = priceBar.getLow();
            double open = priceBar.getOpen();
            double close = priceBar.getClose();
            items[index] = new OHLCDataItem(date, open, high, low, close, 0);
            index++;
        }


        return new DefaultOHLCDataset("", items);

    }

    public OHLCDataset getIndicatorDataset(ChartableIndicator chartableIndicator, long frequency) {

        List<TimedValue> indicatorValues = chartableIndicator.getIndicatorHistory();
        List<Bar> indicatorBars = new ArrayList<Bar>();

        Bar indicatorBar = null;
        for (TimedValue indicatorValue : indicatorValues) {
            long time = indicatorValue.getTime();
            double value = indicatorValue.getValue();

            // Integer division gives us the number of whole periods
            long completedPeriods = time / frequency;
            long barTime = (completedPeriods + 1) * frequency;

            if (indicatorBar == null) {
                indicatorBar = new Bar(barTime, value);
            }

            if (barTime > indicatorBar.getTime()) {
                indicatorBars.add(indicatorBar);
                indicatorBar = new Bar(barTime, value);
            }

            indicatorBar.setClose(value);
            indicatorBar.setLow(Math.min(value, indicatorBar.getLow()));
            indicatorBar.setHigh(Math.max(value, indicatorBar.getHigh()));
        }

        int size = indicatorBars.size();
        OHLCDataItem[] items = new OHLCDataItem[size];

        int index = 0;
        for (Bar indicator : indicatorBars) {
            Date date = new Date(indicator.getTime());
            double high = indicator.getHigh();
            double low = indicator.getLow();
            double open = indicator.getOpen();
            double close = indicator.getClose();
            items[index] = new OHLCDataItem(date, open, high, low, close, 0);
            index++;
        }


        return new DefaultOHLCDataset("", items);
    }


}

/* $Id$ */
