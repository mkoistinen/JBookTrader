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
    private MarketDepthValidator validator;

    public MarketDepth(String name) {
        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();
        validator = new MarketDepthValidator(name, bids, asks);
        isResetting = true;
    }

    public void reset() {
        isResetting = true;
        bids.clear();
        asks.clear();
    }


    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    public boolean isValid() {
        return validator.isValid();
    }

    public String getMarketDepthAsString() {
        return validator.isValid() ? (getCumulativeSize(bids) + "-" + getCumulativeSize(asks)) : "invalid";
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
            validator.validate();
            if (validator.isValid()) {
                int cumulativeBid = getCumulativeSize(bids);  // ekk calc only when valid?
                int cumulativeAsk = getCumulativeSize(asks);
                double totalDepth = cumulativeBid + cumulativeAsk;

                lastBalance = 100d * (cumulativeBid - cumulativeAsk) / totalDepth;
                lowBalance = Math.min(lastBalance, lowBalance);
                highBalance = Math.max(lastBalance, highBalance);
            }

            midPointPrice = (bids.getFirst().getPrice() + asks.getFirst().getPrice()) / 2;
            isResetting = false;
        }
    }


    synchronized public MarketSnapshot getMarketSnapshot(long time) {
        if (isResetting) {
            return null;
        }

        long invalidDuration = validator.getInvalidDurationInSeconds();
        if (invalidDuration > 60) {
            Dispatcher.getReporter().report("Invalid duration " + invalidDuration);   // ekk which market book?
        }

        int balance = (int) Math.round((lowBalance + highBalance) / 2d);
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, balance, midPointPrice);
        // reset values for the next market snapshot
        highBalance = lowBalance = lastBalance;

        return marketSnapshot;
    }


}
