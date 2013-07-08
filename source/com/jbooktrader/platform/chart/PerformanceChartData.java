package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

import java.util.*;


/**
 * Encapsulates performance chart data.
 *
 * @author Eugene Kononov
 */
public class PerformanceChartData {

    private final List<TimedValue> profits;
    private final BarSize barSize;
    private final List<OHLCDataItem> prices;
    private Bar priceBar;
    private final Map<String, Bar> indicatorBars;
    private final Map<String, List<OHLCDataItem>> indicators;
    private final String strategyName;

    public PerformanceChartData(BarSize barSize, List<Indicator> indicators, String strategyName) {
        this.barSize = barSize;

        profits = new ArrayList<>();

        prices = new ArrayList<>();
        indicatorBars = new HashMap<>();
        this.indicators = new HashMap<>();
        this.strategyName = strategyName;
        for (Indicator indicator : indicators) {
            this.indicators.put(indicator.getKey(), new ArrayList<OHLCDataItem>());
        }
    }

    public List<TimedValue> getProfits() {
        return profits;
    }

    public List<OHLCDataItem> getPrices() {
        return prices;
    }

    public boolean isEmpty() {
        return prices.isEmpty();
    }

    public TimeSeries getProfitAndLossSeries() {
        TimeSeries netProfit = new TimeSeries(strategyName);
        double net = 0;
        for (TimedValue profit : profits) {
            net += profit.getValue();
            netProfit.addOrUpdate(new Second(new Date(profit.getTime())), net);
        }
        return netProfit;
    }

    public void update(TimedValue profitAndLoss) {
        profits.add(profitAndLoss);
    }

    public void update(List<Indicator> indicatorsToUpdate, long time) {
        long frequency = barSize.getSize();
        for (Indicator indicator : indicatorsToUpdate) {

            double value = indicator.getValue();

            // Integer division gives us the number of whole periods
            long completedPeriods = time / frequency;
            long barTime = (completedPeriods + 1) * frequency;

            Bar indicatorBar = indicatorBars.get(indicator.getKey());
            if (indicatorBar == null) {
                indicatorBar = new Bar(barTime, value);
                indicatorBars.put(indicator.getKey(), indicatorBar);
            }

            if (barTime > indicatorBar.getTime()) {
                Date date = new Date(indicatorBar.getTime());
                OHLCDataItem item = new OHLCDataItem(date, indicatorBar.getOpen(), indicatorBar.getHigh(), indicatorBar.getLow(), indicatorBar.getClose(), 0);
                List<OHLCDataItem> ind = indicators.get(indicator.getKey());
                ind.add(item);
                indicatorBar = new Bar(barTime, value);
                indicatorBars.put(indicator.getKey(), indicatorBar);
            }

            indicatorBar.setClose(value);
            indicatorBar.setLow(Math.min(value, indicatorBar.getLow()));
            indicatorBar.setHigh(Math.max(value, indicatorBar.getHigh()));
        }
    }

    public void update(MarketSnapshot marketSnapshot) {
        long frequency = barSize.getSize();
        long time = marketSnapshot.getTime();
        double price = marketSnapshot.getPrice();

        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        long barTime = (completedPeriods + 1) * frequency;

        if (priceBar == null) {
            priceBar = new Bar(barTime, price);
        }

        if (barTime > priceBar.getTime()) {
            Date date = new Date(priceBar.getTime());
            OHLCDataItem item = new OHLCDataItem(date, priceBar.getOpen(), priceBar.getHigh(), priceBar.getLow(), priceBar.getClose(), 0);
            prices.add(item);
            priceBar = new Bar(barTime, price);
        }

        priceBar.setClose(price);
        priceBar.setLow(Math.min(price, priceBar.getLow()));
        priceBar.setHigh(Math.max(price, priceBar.getHigh()));
    }

    public OHLCDataset getPriceDataset() {
        return new DefaultOHLCDataset("", prices.toArray(new OHLCDataItem[prices.size()]));
    }

    public OHLCDataset getIndicatorDataset(Indicator indicator) {
        List<OHLCDataItem> ind = indicators.get(indicator.getKey());
        return new DefaultOHLCDataset("", ind.toArray(new OHLCDataItem[ind.size()]));
    }

}
