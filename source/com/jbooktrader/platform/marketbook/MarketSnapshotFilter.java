/**
 *
 */
package com.jbooktrader.platform.marketbook;

/**
 * @author yueming
 */
public interface MarketSnapshotFilter {
    boolean accept(MarketSnapshot marketSnapshot);
}

// $Id: MarketSnapshotFilter.java 380 2008-10-08 10:10:08Z florent.guiliani $
