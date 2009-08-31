/**
 *
 */
package com.jbooktrader.platform.util;

import com.jbooktrader.platform.marketbook.*;
import com.toedter.calendar.*;

import java.util.*;

/**
 *
 */
public abstract class MarkSnapshotUtilities {

    public static MarketSnapshotFilter getMarketDepthFilter(JTextFieldDateEditor fromDateEditor, JTextFieldDateEditor toDateEditor) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        final long fromDate = calendar.getTimeInMillis();
        calendar.setTime(toDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        final long toDate = calendar.getTimeInMillis();


        return new MarketSnapshotFilter() {
            public boolean accept(MarketSnapshot marketSnapshot) {
                long snapshotTime = marketSnapshot.getTime();
                return (snapshotTime >= fromDate && snapshotTime <= toDate);
            }
        };
    }
}
