package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 *
 * @author Eugene Kononov
 */
public class MarketBook {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private MarketSnapshot marketSnapshot;
    private final MarketDepth marketDepth;
    private final String name;
    private final TimeZone timeZone;
    private BackTestFileWriter backTestFileWriter;
    private boolean isExchangeOpen;

    public MarketBook(String name, TimeZone timeZone) {
        this.name = name;
        this.timeZone = timeZone;
        marketDepth = new MarketDepth();
        isExchangeOpen = true;
    }

    public MarketBook() {
        this(null, null);
    }

    public MarketDepth getMarketDepth() {
        return marketDepth;
    }

    public void saveSnapshot(MarketSnapshot marketSnapshot) {
        if (backTestFileWriter == null) {
            try {
                backTestFileWriter = new BackTestFileWriter(name, timeZone);
            } catch (JBookTraderException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        backTestFileWriter.write(marketSnapshot);
    }

    public boolean isEmpty() {
        return marketSnapshot == null;
    }

    public void setSnapshot(MarketSnapshot marketSnapshot) {
        this.marketSnapshot = marketSnapshot;
    }

    public boolean isExchangeOpen() {
        return isExchangeOpen;
    }

    public void setExchangeOpen(boolean exchangeOpen) {
        isExchangeOpen = exchangeOpen;
    }

    public boolean isGapping(MarketSnapshot newMarketSnapshot) {
        if (isEmpty()) {
            return false;
        }
        return (newMarketSnapshot.getTime() - marketSnapshot.getTime() > GAP_SIZE);
    }

    public MarketSnapshot getSnapshot() {
        return marketSnapshot;
    }

    public void takeMarketSnapshot(long time) {
        MarketSnapshot snapshot = marketDepth.takeMarketSnapshot(time);
        if (snapshot != null) {
            setSnapshot(snapshot);
            saveSnapshot(snapshot);
        }

    }
}
