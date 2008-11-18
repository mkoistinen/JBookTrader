package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

import java.util.*;


/**
 * Encapsulates performance chart data.
 */
public class PerformanceChartData {
    private final TimeSeries netProfit;
    private final BarSize barSize;
    private final List<OHLCDataItem> prices;
    private Bar priceBar;
    private final Map<String, Bar> indicatorBars;
    private final Map<String, List<OHLCDataItem>> indicators;

    public PerformanceChartData(BarSize barSize) {
        this.barSize = barSize;
        netProfit = new TimeSeries("Net Profit", Second.class);
        netProfit.setRangeDescription("Net Profit");
        prices = new ArrayList<OHLCDataItem>();
        indicatorBars = new HashMap<String, Bar>();
        indicators = new HashMap<String, List<OHLCDataItem>>();
    }


    public List<OHLCDataItem> getPrices() {
        return prices;
    }

    public boolean isEmpty() {
        return prices.size() == 0;
    }


    public void addIndicator(Indicator indicator) {
        indicators.put(indicator.getName(), new ArrayList<OHLCDataItem>());

    }

    public void updateNetProfit(TimedValue profitAndLoss) {
        netProfit.addOrUpdate(new Second(new Date(profitAndLoss.getTime())), profitAndLoss.getValue());
    }

    public TimeSeries getProfitAndLossSeries() {
        return netProfit;
    }

    public void updateIndicators(List<Indicator> indicatorsToUpdate, long time) {
        long frequency = barSize.getSize();
        for (Indicator indicator : indicatorsToUpdate) {

            double value = indicator.getValue();

            // Integer division gives us the number of whole periods
            long completedPeriods = time / frequency;
            long barTime = (completedPeriods + 1) * frequency;

            Bar indicatorBar = indicatorBars.get(indicator.getName());
            if (indicatorBar == null) {
                indicatorBar = new Bar(barTime, value);
                indicatorBars.put(indicator.getName(), indicatorBar);
            }

            if (barTime > indicatorBar.getTime()) {
                Date date = new Date(indicatorBar.getTime());
                OHLCDataItem item = new OHLCDataItem(date, indicatorBar.getOpen(), indicatorBar.getHigh(), indicatorBar.getLow(), indicatorBar.getClose(), 0);
                List<OHLCDataItem> ind = indicators.get(indicator.getName());
                ind.add(item);
                indicatorBar = new Bar(barTime, value);
                indicatorBars.put(indicator.getName(), indicatorBar);
            }

            indicatorBar.setClose(value);
            indicatorBar.setLow(Math.min(value, indicatorBar.getLow()));
            indicatorBar.setHigh(Math.max(value, indicatorBar.getHigh()));
        }

    }


    public void updatePrice(MarketSnapshot marketSnapshot) {
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
        List<OHLCDataItem> ind = indicators.get(indicator.getName());
        return new DefaultOHLCDataset("", ind.toArray(new OHLCDataItem[ind.size()]));
    }

}
