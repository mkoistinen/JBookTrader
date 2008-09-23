package com.jbooktrader.platform.marketindex;

import com.ib.client.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

public enum MarketIndex {

    Tick("TICK-NYSE", "NYSE");

    private final String ticker;
    private final Contract contract;
    private static final Map<String, MarketIndex> indexes = new HashMap<String, MarketIndex>();


    MarketIndex(String ticker, String exchange) {
        this.ticker = ticker;
        contract = ContractFactory.makeIndexContract(ticker, exchange);
    }

    public Contract getContract() {
        return contract;
    }

    public static MarketIndex getMarketIndex(String ticker) {
        return indexes.get(ticker);
    }

    static {
        for (MarketIndex marketIndex : values()) {
            indexes.put(marketIndex.ticker, marketIndex);
        }
    }
}

