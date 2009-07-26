/**
 *
 */
package com.jbooktrader.platform.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.marketbook.MarketSnapshotFilter;

/**
 * @author yueming
 *
 */
public abstract class MarkSnapshotUtilities {

    public static MarketSnapshotFilter getMarketDepthFilter(SimpleDateFormat sdf, String fromText, String toText) {
        MarketSnapshotFilter filter = null;

        if (fromText.length() == 0 && toText.length() == 0) {
            return filter;
        }

        long timeFrom, timeTo;
        if (fromText.length() != 0) {
            try {
                timeFrom = sdf.parse(fromText).getTime();
            } catch (ParseException e) {
                timeFrom = Long.MIN_VALUE;
            }
        } else {
            timeFrom = Long.MIN_VALUE;
        }

        if (toText.length() != 0) {
            try {
                timeTo = sdf.parse(toText).getTime();
            } catch (ParseException e) {
                timeTo = Long.MAX_VALUE;
            }
        } else {
            timeTo = Long.MAX_VALUE;
        }

        final long timeStart = timeFrom, timeEnd = timeTo;
        filter = new MarketSnapshotFilter() {
        	
            public boolean accept(MarketSnapshot marketSnapshot) {
                if (marketSnapshot.getTime() >= timeStart && marketSnapshot.getTime() <= timeEnd) {
                    return true;
                } else {
                    return false;
                }
            }
            
            public String toString() {
            	return "MarketSnapshotFilter from: " + timeStart + " to: " + timeEnd;
            }
        };

        return filter;
    }


    public static MarketSnapshotFilter getMarketDepthFilter(final long start, final int days, final TimeZone zone) {
        MarketSnapshotFilter filter = new MarketSnapshotFilter() {
            private int prev = 0;
            private int counter = 0;
            private Calendar cal = Calendar.getInstance(zone);

            public boolean accept(MarketSnapshot marketSnapshot) {
                if (marketSnapshot.getTime() >= start && counter < days) {
                    cal.setTime(new Date(marketSnapshot.getTime()));
                    if (prev != 0 && prev != cal.get(Calendar.DAY_OF_YEAR)) {
                        counter++;
                    }
                    prev = cal.get(Calendar.DAY_OF_YEAR);
                    return true;
                }
                return false;
            }

        };
        return filter;
    }

}

// $Id: MarkSnapshotUtilities.java 380 2008-10-08 10:10:08Z florent.guiliani $
