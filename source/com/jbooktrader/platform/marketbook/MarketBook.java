package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketBook {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketSnapshot> marketSnapshots;
    private final MarketDepth marketDepth;

    private final String name;
    private final TimeZone timeZone;
    private BackTestFileWriter backTestFileWriter;


    public MarketBook(String name, TimeZone timeZone) {
        this.name = name;
        this.timeZone = timeZone;
        marketSnapshots = new LinkedList<MarketSnapshot>();
        marketDepth = new MarketDepth();
    }

    public MarketBook() {
        this(null, null);
    }

    public MarketDepth getMarketDepth() {
        return marketDepth;
    }

    public void save(MarketSnapshot marketSnapshot) {
        if (backTestFileWriter == null) {
            try {
                backTestFileWriter = new BackTestFileWriter(name, timeZone, true);
            } catch (JBookTraderException e) {
                throw new RuntimeException(e);
            }
        }
        backTestFileWriter.write(marketSnapshot, true);
    }


    public List<MarketSnapshot> getSnapshots() {
        return marketSnapshots;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MarketSnapshot marketSnapshot : marketSnapshots) {
            sb.append(marketSnapshot).append(LINE_SEP);
        }

        return sb.toString();
    }

    public int size() {
        return marketSnapshots.size();
    }

    public void add(MarketSnapshot marketSnapshot) {
        marketSnapshots.add(marketSnapshot);
    }

    public MarketSnapshot getLastMarketSnapshot() {
        return marketSnapshots.getLast();
    }

    public MarketSnapshot getPreviousMarketSnapshot() {
        return marketSnapshots.get(marketSnapshots.size() - 2);
    }

    public MarketSnapshot getNextMarketSnapshot(long time) {
        return marketDepth.getMarketSnapshot(time);
    }

}
