package com.jbooktrader.platform.test;

import java.util.TimeZone;

import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketdepth.*;

import org.junit.Assert;
import org.junit.Test;

/**
  * Extremely simple example test that confirms the snapshots list is empty
  * when the MarketBookTest is constructed.
  */

public class MarketBookTest {

    @Test
    public void testSnapshotsEmpty() {
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        Assert.assertEquals(0, marketBook.getAll().size());
    }
    
    @Test
    public void testInvalidUntil10Prices() {
        final double[] bids = { 5.0, 4.0, 3.0, 2.0, 1.0 };
        final double[] asks = { 6.0, 7.0, 8.0, 9.0, 10.0 };        
        final MarketBook marketBook = new MarketBook("Test", TimeZone.getDefault());

        Assert.assertEquals(false, marketBook.isValid());

        for (int i = 0; i < 5; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, bids[i], 50);
            Assert.assertEquals(false, marketBook.isValid());
        }
        for (int i = 0; i < 4; i++) {
            marketBook.updateDepth(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[i], 50);
            Assert.assertEquals(false, marketBook.isValid());
        }

        marketBook.updateDepth(4, MarketDepthOperation.Insert, MarketDepthSide.Ask, asks[4], 50);
        Assert.assertEquals(true, marketBook.isValid());
    }

}
