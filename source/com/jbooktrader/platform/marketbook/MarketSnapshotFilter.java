/**
 *
 */
package com.jbooktrader.platform.marketbook;

import com.toedter.calendar.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class MarketSnapshotFilter {
    private final long fromDate, toDate;

    public MarketSnapshotFilter(JTextFieldDateEditor fromDateEditor, JTextFieldDateEditor toDateEditor) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        fromDate = calendar.getTimeInMillis();
        calendar.setTime(toDateEditor.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        toDate = calendar.getTimeInMillis();
        if (fromDate > toDate) {
            throw new RuntimeException("The \"from\" date must be before or the same as the \"to\" date.");
        }
    }


    public boolean contains(long time) {
        return (time >= fromDate && time <= toDate);
    }

    @Override
    public String toString() {
        return "MarketSnapshotFilter{" + "fromDate=" + fromDate + ", toDate=" + toDate + '}';
    }
}



