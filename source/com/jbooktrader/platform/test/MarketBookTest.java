package com.jbooktrader.platform.test;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import org.junit.*;

/**
 * unit tests for com.jbooktrader.platform.marketbook.MarketBook
 */

public class MarketBookTest {

    /**
     * Tests that the market book is invalid when initially created (and
     * therefore empty).
     */
    @Test
    public void testInvalidWhenEmpty() {
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        Assert.assertTrue(marketBook.isEmpty());
        Assert.assertFalse(marketDepth.isValid());
    }


    /**
     * Tests that the market book is invalid when asks prices
     * are corrupted
     */
    @Test
    public void testInvalidWhenCorruptAsks() {
        double[] bids = {3.0, 2.0, 1.0};
        double[] asks = {10.0, 11.0, 4.0};
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        for (int i = 0; i < 3; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertFalse(marketDepth.isValid());
    }

    /**
     * Tests that the market book is invalid when bids prices
     * are corrupted
     */
    @Test
    public void testInvalidWhenCorruptBids() {
        double[] bids = {3.0, 4.0, 1.0};
        double[] asks = {10.0, 11.0, 12.0};
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        for (int i = 0; i < 3; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertFalse(marketDepth.isValid());
    }

    /**
     * Tests that the market book is invalid when there is
     * no spread
     */
    @Test
    public void testInvalidWhenCorruptSpread() {
        double[] bids = {10.0, 9.0, 8.0};
        double[] asks = {10.0, 11.0, 12.0};
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        for (int i = 0; i < 3; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertFalse(marketDepth.isValid());
    }

    /**
     * Tests that the market book is invalid when not balanced, and valid when
     * balanced.
     */
    @Test
    public void testValidOnlyWhenBalanced() {
        double[] bids = {5.0, 4.0, 3.0, 2.0, 1.0};
        double[] asks = {6.0, 7.0, 8.0, 9.0, 10.0, 11.0};
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        // Add each bid, ensuring it is invalid (because there are no ask prices)
        // at each step.
        for (int i = 0; i < 5; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            Assert.assertFalse(marketDepth.isValid());
        }

        // Add the first four ask prices, ensuring it is invalid (because it is
        // not balanced) at each step.
        for (int i = 0; i < 4; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
            Assert.assertFalse(marketDepth.isValid());
        }

        // Add the fifth ask, balancing the book
        marketDepth.update(4, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[4], 50);
        Assert.assertTrue(marketDepth.isValid());

        // Add the sixth ask, unbalancing the book
        marketDepth.update(5, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[5], 50);
        Assert.assertFalse(marketDepth.isValid());

        // Remove the sixth ask, re-balancing the book
        marketDepth.update(5, MarketDepthOperation.Delete, MarketDepthSide.Ask, asks[5], 50);
        Assert.assertTrue(marketDepth.isValid());
    }

}
