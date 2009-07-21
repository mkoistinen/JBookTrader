package com.jbooktrader.platform.marketdepth;

import java.util.*;

public class MarketDepthValidator {
    private final LinkedList<MarketDepthItem> bids, asks;
    private List<String> errorMessages;
    private long lastValidTime;

    public MarketDepthValidator(LinkedList<MarketDepthItem> bids, LinkedList<MarketDepthItem> asks) {
        this.bids = bids;
        this.asks = asks;
        lastValidTime = System.currentTimeMillis();
        errorMessages = new ArrayList<String>();
    }

    public boolean isValid() {
        return errorMessages.isEmpty();
    }

    public long getInvalidStateDurationInSeconds() {
        return (System.currentTimeMillis() - lastValidTime) / 1000L;
    }

    public List<String> getErrors() {
        return errorMessages;
    }

    /**
     * Market depth is considered valid when all four of the following conditions are true:
     * <p/>
     * 1. The number of bid levels equals the number of ask levels and is non-zero
     * 2. The bid price of level N is smaller than the bid price of level N-1 for all levels
     * 3. The ask price of level N is greater than the ask price of level N-1 for all levels
     * 4. The best bid price (at level 0) is smaller than the best ask price (at level 0)
     */
    public void validate() {
        errorMessages.clear();

        // Number of bid levels must be the same as number of ask levels
        int bidLevels = bids.size();
        int askLevels = asks.size();
        if (bidLevels != askLevels) {
            String errorMsg = "Number of bid levels (" + bidLevels + ") is not equal to number of ask levels (" + askLevels + ")";
            errorMessages.add(errorMsg);
        }

        if (bidLevels == 0) {
            String errorMsg = "No bids";
            errorMessages.add(errorMsg);
        }

        if (askLevels == 0) {
            String errorMsg = "No asks";
            errorMessages.add(errorMsg);
        }

        // The bid price of level N must be smaller than the bid price of level N-1
        if (!bids.isEmpty()) {
            double previousLevelBidPrice = bids.getFirst().getPrice();
            for (int itemIndex = 1; itemIndex < bidLevels; itemIndex++) {
                double price = bids.get(itemIndex).getPrice();
                if (price >= previousLevelBidPrice) {
                    String errorMsg = "Bid price " + price + " at level " + itemIndex + " is greater or equal to bid price " + previousLevelBidPrice + " at level " + (itemIndex - 1);
                    errorMessages.add(errorMsg);
                }
                previousLevelBidPrice = price;
            }
        }

        // The ask price of level N must be greater than the ask price of level N-1
        if (!asks.isEmpty()) {
            double previousLevelAskPrice = asks.getFirst().getPrice();
            for (int itemIndex = 1; itemIndex < askLevels; itemIndex++) {
                double price = asks.get(itemIndex).getPrice();
                if (price <= previousLevelAskPrice) {
                    String errorMsg = "Ask price " + price + " at level " + itemIndex + " is smaller or equal to ask price " + previousLevelAskPrice + " at level " + (itemIndex - 1);
                    errorMessages.add(errorMsg);

                }
                previousLevelAskPrice = price;
            }
        }

        // Best bid price must be smaller than the best ask price
        if (!bids.isEmpty() && !asks.isEmpty()) {
            double bestBid = bids.getFirst().getPrice();
            double bestAsk = asks.getFirst().getPrice();
            if (bestBid >= bestAsk) {
                String errorMsg = "Best bid price " + bestBid + " is greater or equal to best ask price " + bestAsk;
                errorMessages.add(errorMsg);
            }
        }

        if (errorMessages.isEmpty()) {
            lastValidTime = System.currentTimeMillis();
        }
    }
}
