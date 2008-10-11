package com.jbooktrader.platform.test;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import org.junit.*;

import java.util.*;

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
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        Assert.assertEquals(0, marketBook.getAll().size());
        Assert.assertEquals(false, marketBook.isValid());
    }

    /**
     * Tests that the market book is invalid when asks prices
     * are corrupted
     */
    @Test
    public void testInvalidWhenCorruptAsks() {
        final double[] bids = {3.0, 2.0, 1.0};
        final double[] asks = {10.0, 11.0, 4.0};
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        for (int i = 0; i < 3; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertEquals(false, marketBook.isValid());
    }

    /**
     * Tests that the market book is invalid when bids prices
     * are corrupted
     */
    @Test
    public void testInvalidWhenCorruptBids() {
        final double[] bids = {3.0, 4.0, 1.0};
        final double[] asks = {10.0, 11.0, 12.0};
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        for (int i = 0; i < 3; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertEquals(false, marketBook.isValid());
    }

    /**
     * Tests that the market book is invalid when there is
     * no spread
     */
    @Test
    public void testInvalidWhenCorruptSpread() {
        final double[] bids = {10.0, 9.0, 8.0};
        final double[] asks = {10.0, 11.0, 12.0};
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        for (int i = 0; i < 3; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
        }
        Assert.assertEquals(false, marketBook.isValid());
    }

    /**
     * Tests that the market book is invalid when not balanced, and valid when
     * balanced.
     */
    @Test
    public void testValidOnlyWhenBalanced() {
        final double[] bids = {5.0, 4.0, 3.0, 2.0, 1.0};
        final double[] asks = {6.0, 7.0, 8.0, 9.0, 10.0, 11.0};
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        // Add each bid, ensuring it is invalid (because there are no ask prices)
        // at each step.
        for (int i = 0; i < 5; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            Assert.assertEquals(false, marketBook.isValid());
        }

        // Add the first four ask prices, ensuring it is invalid (because it is
        // not balanced) at each step.
        for (int i = 0; i < 4; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
            Assert.assertEquals(false, marketBook.isValid());
        }

        // Add the fifth ask, balancing the book
        marketBook.updateDepth(4, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[4], 50);
        Assert.assertEquals(true, marketBook.isValid());

        // Add the sixth ask, unbalancing the book
        marketBook.updateDepth(5, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[5], 50);
        Assert.assertEquals(false, marketBook.isValid());

        // Remove the sixth ask, re-balancing the book
        marketBook.updateDepth(5, MarketDepthOperation.Delete, MarketDepthSide.Ask, asks[5], 50);
        Assert.assertEquals(true, marketBook.isValid());
    }

}
