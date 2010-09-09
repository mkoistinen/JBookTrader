package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketBook {
    private MarketSnapshot marketSnapshot;
    private final MarketDepth marketDepth;
    private final String name;
    private final TimeZone timeZone;
    private BackTestFileWriter backTestFileWriter;

    public MarketBook(String name, TimeZone timeZone) {
        this.name = name;
        this.timeZone = timeZone;
        marketDepth = new MarketDepth();
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
                throw new RuntimeException(e);
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


    public MarketSnapshot getSnapshot() {
        return marketSnapshot;
    }

    public MarketSnapshot getNextMarketSnapshot(long time) {
        return marketDepth.getMarketSnapshot(time);
    }
}
