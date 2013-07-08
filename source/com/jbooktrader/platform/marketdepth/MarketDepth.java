package com.jbooktrader.platform.marketdepth;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

/**
 * Holds history of market snapshots for a trading instrument.
 *
 * @author Eugene Kononov
 */
public class MarketDepth {
    private final LinkedList<MarketDepthItem> bids, asks;
    private final LinkedList<Double> balances;
    private double averageBalance;
    private double midPointPrice;
    private final DecimalFormat df2;
    private int volume, previousVolume;
    private int cumulativeBid, cumulativeAsk;


    public MarketDepth() {
        bids = new LinkedList<>();
        asks = new LinkedList<>();
        balances = new LinkedList<>();
        df2 = NumberFormatterFactory.getNumberFormatter(2);
    }

    public synchronized String getSizes() {
        return cumulativeBid + "-" + cumulativeAsk;
    }

    public synchronized String getTop() {
        return bids.getFirst().getPrice() + "-" + asks.getFirst().getPrice();
    }


    public void reset() {
        bids.clear();
        asks.clear();
        balances.clear();
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }

        return cumulativeSize;
    }

    public synchronized void update(int position, MarketDepthOperation operation, MarketDepthSide side, double price, int size) {
        if (price < 0) {
            // IB API sometimes sends "-1" as the price when market depth is resetting.
            return;
        }

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
            if (!bids.isEmpty() && !asks.isEmpty()) {
                cumulativeBid = getCumulativeSize(bids);
                cumulativeAsk = getCumulativeSize(asks);

                double totalDepth = cumulativeBid + cumulativeAsk;
                if (totalDepth != 0) {
                    double balance = 100.0d * (cumulativeBid - cumulativeAsk) / totalDepth;
                    balances.add(balance);
                    midPointPrice = (bids.getFirst().getPrice() + asks.getFirst().getPrice()) / 2;
                }
            }
        }
    }

    public synchronized void update(int volume) {
        this.volume = volume;
    }

    public synchronized MarketSnapshot takeMarketSnapshot(long time) {
        if (balances.isEmpty()) {
            return null;
        }

        int oneSecondVolume = (previousVolume == 0) ? 0 : Math.max(0, volume - previousVolume);
        previousVolume = volume;

        double multiplier = 2.0 / (balances.size() + 1.0);
        for (double balance : balances) {
            averageBalance += (balance - averageBalance) * multiplier;
        }

        double oneSecondBalance = Double.valueOf(df2.format(averageBalance));
        MarketSnapshot marketSnapshot = new MarketSnapshot(time, oneSecondBalance, midPointPrice, oneSecondVolume);
        // retain the last balance, clear the rest
        while (balances.size() > 1) {
            balances.removeFirst();
        }

        return marketSnapshot;
    }
}
