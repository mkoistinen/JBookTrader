package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.PropertiesHolder;

import java.util.*;

/**
 */
public class MarketDepthFactory extends TimerTask {
    private static final int MILLIS_IN_SECOND = 1000;
    private static final Report eventReport = Dispatcher.getReporter();
    private final MarketBook marketBook;
    private final MarketDepth marketDepth;

    public MarketDepthFactory(Strategy strategy) throws JBookTraderException {
        marketBook = strategy.getMarketBook();
        marketDepth = strategy.getMarketDepth();
        PropertiesHolder props = PropertiesHolder.getInstance();
        int samplingFrequency;
        try {
            samplingFrequency = Integer.valueOf(props.getProperty("marketDepth.samplingFrequency"));
        } catch (NumberFormatException nfe) {
            throw new JBookTraderException("Property marketDepth.samplingFrequency must contain an integer.");
        }

        if (samplingFrequency < 100) {
            throw new JBookTraderException("Property marketDepth.samplingFrequency must be greater or equal to 100.");
        }

        long now = System.currentTimeMillis();
        long nextSecondTime = now + (MILLIS_IN_SECOND - now % MILLIS_IN_SECOND);
        Date start = new Date(nextSecondTime);

        Timer timer = new Timer(true);
        timer.schedule(this, start, samplingFrequency);
        eventReport.report(strategy.getName() + ": Market depth factory started");
    }

    @Override
    public void run() {
        try {
            if (marketDepth.isValid()) {
                while (System.currentTimeMillis() - marketDepth.getTime() < 5) {
                    Thread.sleep(5);
                }
                marketDepth.update();
                marketBook.addMarketDepth(marketDepth);
            }
        } catch (Throwable t) {
            eventReport.report(t);
        }
    }
}
