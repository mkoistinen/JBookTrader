package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;

import static com.jbooktrader.platform.model.Dispatcher.Mode.*;

import java.util.*;

/**
 *
 */
public class IndicatorManager {
    private final List<ChartableIndicator> indicators;
    private final boolean isOptimizationMode;
    private MarketBook marketBook;
    private boolean hasValidIndicators;

    public IndicatorManager() {
        indicators = new LinkedList<ChartableIndicator>();
        isOptimizationMode = (Dispatcher.getMode() == Optimization);
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
        for (ChartableIndicator chartableIndicator : indicators) {
            chartableIndicator.getIndicator().setMarketBook(marketBook);
        }
    }

    public boolean hasValidIndicators() {
        return hasValidIndicators;
    }

    public void addIndicator(Indicator indicator) {
        ChartableIndicator chartableIndicator = new ChartableIndicator(indicator);
        indicators.add(chartableIndicator);
    }

    public List<ChartableIndicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators() {
        hasValidIndicators = true;
        long time = marketBook.getLastMarketSnapshot().getTime();
        for (ChartableIndicator chartableIndicator : indicators) {
            Indicator indicator = chartableIndicator.getIndicator();
            try {
                indicator.calculate();
                if (!isOptimizationMode) {
                    chartableIndicator.add(time, indicator.getValue());
                }
            } catch (IndexOutOfBoundsException iob) {
                hasValidIndicators = false;
                // This exception will occur if book size is insufficient to calculate
                // the indicator. This is normal.
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
