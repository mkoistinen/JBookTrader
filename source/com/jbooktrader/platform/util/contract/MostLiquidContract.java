package com.jbooktrader.platform.util.contract;

import java.text.*;
import java.util.*;

/**
 * Certain futures contracts (such as ES) expire on the 3rd Friday of the
 * contract month, but the volume shifts to the next month contract on the
 * business day preceding the 2nd Friday of the expiration month. For example,
 * a 200606 contract had more volume than a 200609 contract on Wednesday June 7,
 * 2006, but on Thursday, June 8th (the day preceding the 2nd Friday of the
 * expiration), the 200609 contract had more volume than the 200606 contract.
 * <p/>
 * This utility calculates the most liquid contract traded as of given date.
 *
 * @author Eugene Kononov
 */
public class MostLiquidContract {
    private static final Map<Integer, Integer> frontMonths = new HashMap<>();

    static {
        frontMonths.put(Calendar.DECEMBER, Calendar.MARCH);
        frontMonths.put(Calendar.JANUARY, Calendar.MARCH);
        frontMonths.put(Calendar.FEBRUARY, Calendar.MARCH);
        frontMonths.put(Calendar.MARCH, Calendar.JUNE);
        frontMonths.put(Calendar.APRIL, Calendar.JUNE);
        frontMonths.put(Calendar.MAY, Calendar.JUNE);
        frontMonths.put(Calendar.JUNE, Calendar.SEPTEMBER);
        frontMonths.put(Calendar.JULY, Calendar.SEPTEMBER);
        frontMonths.put(Calendar.AUGUST, Calendar.SEPTEMBER);
        frontMonths.put(Calendar.SEPTEMBER, Calendar.DECEMBER);
        frontMonths.put(Calendar.OCTOBER, Calendar.DECEMBER);
        frontMonths.put(Calendar.NOVEMBER, Calendar.DECEMBER);
    }

    private static boolean keepCurrent(Calendar asOfDate) {
        boolean keepCurrent = false;

        int monthNow = asOfDate.get(Calendar.MONTH);

        // March, June, September, and December are expiration months
        boolean isExpirationMonth = ((monthNow + 1) % 3 == 0);

        if (isExpirationMonth) {
            Calendar volumeShiftDate = (Calendar) asOfDate.clone();

            // Find first Friday
            volumeShiftDate.set(Calendar.DAY_OF_MONTH, 1);
            while (volumeShiftDate.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                volumeShiftDate.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Shift to third Friday
            volumeShiftDate.add(Calendar.WEEK_OF_MONTH, 2);

            // Finally, find the day before second Friday
            volumeShiftDate.add(Calendar.DAY_OF_MONTH, -8);

            if (asOfDate.before(volumeShiftDate)) {
                keepCurrent = true;
            }
        }

        return keepCurrent;
    }

    public static String getMostLiquid() {
        Calendar today = Calendar.getInstance();
        return getMostLiquid(today);
    }

    /**
     * For the contracts whose volume shifts from the front contract to the back
     * contract on the day preceding the 2nd Friday of expiration month of the
     * front contract.
     */
    public static String getMostLiquid(Calendar asOfDate) {

        int monthNow = asOfDate.get(Calendar.MONTH);
        int yearNow = asOfDate.get(Calendar.YEAR);
        int mostLiquidYear = yearNow;

        int mostLiquidMonth = frontMonths.get(monthNow);

        // special case with December
        if (monthNow == Calendar.DECEMBER) {
            mostLiquidYear = yearNow + 1;
        }

        if (keepCurrent(asOfDate)) {
            mostLiquidMonth = monthNow;
            mostLiquidYear = yearNow;
        }

        Calendar mostLiquidDate = Calendar.getInstance();
        mostLiquidDate.set(Calendar.DAY_OF_MONTH, 1);
        mostLiquidDate.set(Calendar.MONTH, mostLiquidMonth);
        mostLiquidDate.set(Calendar.YEAR, mostLiquidYear);

        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
        return df.format(mostLiquidDate.getTime());

    }
}
