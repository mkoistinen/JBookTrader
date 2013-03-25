package com.jbooktrader.platform.test;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import org.junit.*;

/**
 * unit tests for com.jbooktrader.platform.marketbook.MarketBook
 *
 * @author Eugene Kononov
 */
public class MarketBookTest {

    /**
     * Tests that the market book is invalid when initially created (and
     * therefore empty).
     */
    @Test
    public void testInvalidWhenEmpty() {
        MarketBook marketBook = new MarketBook();
        Assert.assertTrue(marketBook.isEmpty());
    }


    /**
     * Tests that the market book is invalid when not balanced, and valid when
     * balanced.
     */
    @Test
    public void testValidOnlyWhenNotEmpty() {
        MarketBook marketBook = new MarketBook();
        MarketDepth marketDepth = marketBook.getMarketDepth();

        for (int i = 0; i < 10; i++) {
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Bid, 10 - i, i * 2);
            marketDepth.update(i, MarketDepthOperation.Insert, MarketDepthSide.Ask, 12 - i, i);
        }

        marketDepth.update(1, MarketDepthOperation.Update, MarketDepthSide.Ask, 1, 1);
        marketDepth.update(1, MarketDepthOperation.Update, MarketDepthSide.Bid, 1, 1);

        Assert.assertEquals("89-45", marketDepth.getSizes());

        MarketSnapshot marketSnapshot = marketDepth.takeMarketSnapshot(0);
        int balance = (int) marketSnapshot.getBalance();
        int price = (int) marketSnapshot.getPrice();

        Assert.assertEquals(balance, 29);
        Assert.assertEquals(price, 11);
    }

}
