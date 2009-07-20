package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;

import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private boolean isResetting;
    private double lowBalance, highBalance, lastBalance;
    private double midPointPrice;
    private HashSet<String> errorMessages;
    private Report eventReport;

    public MarketDepth() {
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
        errorMessages = new HashSet<String>();
        isResetting = true;
        eventReport = Dispatcher.getReporter();
    }

    public void reset() {
        isResetting = true;
        bids.clear();
        asks.clear();
    }

    synchronized public void checkForValidity() {
        boolean isValid = true;

        // Number of bid levels must be the same as number of ask levels
        int bidLevels = bids.size();
        int askLevels = asks.size();
        if (bidLevels != askLevels) {
            isValid = false;
            String errorMsg = "Number of bid levels (" + bidLevels + ") is not equal to number of ask levels (" + askLevels + ")";
            if (!errorMessages.contains(errorMsg)) {
                errorMessages.add(errorMsg);
                eventReport.report(errorMsg);
            }
        }

        if (bidLevels == 0) {
            isValid = false;
            String errorMsg = "No bids";
            if (!errorMessages.contains(errorMsg)) {
                errorMessages.add(errorMsg);
                eventReport.report(errorMsg);
            }
        }

        if (askLevels == 0) {
            isValid = false;
            String errorMsg = "No asks";
            if (!errorMessages.contains(errorMsg)) {
                errorMessages.add(errorMsg);
                eventReport.report(errorMsg);
            }
        }

        // The bid price of level N must be smaller than the bid price of level N-1
        if (!bids.isEmpty()) {
            double previousLevelBidPrice = bids.getFirst().getPrice();
            for (int itemIndex = 1; itemIndex < bidLevels; itemIndex++) {
                double price = bids.get(itemIndex).getPrice();
                if (price >= previousLevelBidPrice) {
                    isValid = false;
                    String errorMsg = "Bid price " + price + " at level " + itemIndex + " is greater or equal to bid price " + previousLevelBidPrice + " at level " + (itemIndex - 1);
                    if (!errorMessages.contains(errorMsg)) {
                        errorMessages.add(errorMsg);
                        eventReport.report(errorMsg);
                    }
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
                    isValid = false;
                    String errorMsg = "Ask price " + price + " at level " + itemIndex + " is smaller or equal to ask price " + previousLevelAskPrice + " at level " + (itemIndex - 1);
                    if (!errorMessages.contains(errorMsg)) {
                        errorMessages.add(errorMsg);
                        eventReport.report(errorMsg);
                    }
                }
                previousLevelAskPrice = price;
            }
        }

        // Best bid price must be smaller than the best ask price
        if (!bids.isEmpty() && !asks.isEmpty()) {
            double bestBid = bids.getFirst().getPrice();
            double bestAsk = asks.getFirst().getPrice();
            if (bestBid >= bestAsk) {
                isValid = false;
                String errorMsg = "Best bid price " + bestBid + " is greater or equal to best ask price " + bestAsk;
                if (!errorMessages.contains(errorMsg)) {
                    errorMessages.add(errorMsg);
                    eventReport.report(errorMsg);
                }
            }
        }

        if (isValid && !errorMessages.isEmpty()) {
            errorMessages.clear();
            String errorMsg = "Errors cleared. Market depth is valid.";
            eventReport.report(errorMsg);
        }
    }

    public boolean isValid() {
        return !errorMessages.isEmpty();
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    public String getMarketDepthAsString() {
        return isValid() ? (getCumulativeSize(bids) + "-" + getCumulativeSize(asks)) : "invalid";
    }

    synchronized public void update(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
        List<MarketDepthItem> items = (side == MarketDepthSide.Bid) ? bids : asks;
        int levels = items.size();

        switch (operation) {
            case Insert:
                if (position <= levels) {
                    items.add(position, new MarketDepthItem(size, price));
                }
                break;
            case Update:
                if (position < levels) {
                    MarketDepthItem item = items.get(position);
                    item.setSize(size);
                    item.setPrice(price);
                }
                break;
            case Delete:
                if (position < levels) {
                    items.remove(position);
                }
                break;
        }


        if (operation == MarketDepthOperation.Update) {
            int cumulativeBid = getCumulativeSize(bids);
            int cumulativeAsk = getCumulativeSize(asks);
            double totalDepth = cumulativeBid + cumulativeAsk;

            lastBalance = 100d * (cumulativeBid - cumulativeAsk) / totalDepth;
            lowBalance = Math.min(lastBalance, lowBalance);
            highBalance = Math.max(lastBalance, highBalance);
            midPointPrice = (bids.getFirst().getPrice() + asks.getFirst().getPrice()) / 2;
            isResetting = false;
        }
    }


    public MarketSnapshot getMarketSnapshot(long time) {
        if (isResetting) {
            return null;
        }

        checkForValidity();

        int balance = (int) Math.round((lowBalance + highBalance) / 2d);
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, balance, midPointPrice);
        // reset values for the next market snapshot
        highBalance = lowBalance = lastBalance;


        return marketSnapshot;
    }


}
