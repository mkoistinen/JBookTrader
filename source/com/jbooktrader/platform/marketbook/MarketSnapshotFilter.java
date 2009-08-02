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


