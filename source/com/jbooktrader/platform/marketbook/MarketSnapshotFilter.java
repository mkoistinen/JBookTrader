/**
 *
 */
package com.jbooktrader.platform.marketbook;

import com.toedter.calendar.*;

import java.util.*;

public class MarketSnapshotFilter {
    private final long fromDate, toDate;

    public MarketSnapshotFilter(JTextFieldDateEditor fromDateEditor, JTextFieldDateEditor toDateEditor) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        fromDate = calendar.getTimeInMillis();
        calendar.setTime(toDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        toDate = calendar.getTimeInMillis();
    }


    public boolean accept(MarketSnapshot marketSnapshot) {
        long snapshotTime = marketSnapshot.getTime();
        return (snapshotTime >= fromDate && snapshotTime <= toDate);
    }

}



