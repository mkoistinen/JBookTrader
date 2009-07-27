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
        MarketSnapshotFilter filter = null;


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        final long fromDate = calendar.getTimeInMillis();
        calendar.setTime(toDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        final long toDate = calendar.getTimeInMillis();


        filter = new MarketSnapshotFilter() {

            public boolean accept(MarketSnapshot marketSnapshot) {
                return (marketSnapshot.getTime() >= fromDate && marketSnapshot.getTime() <= toDate);
            }

        };

        return filter;
    }


}
