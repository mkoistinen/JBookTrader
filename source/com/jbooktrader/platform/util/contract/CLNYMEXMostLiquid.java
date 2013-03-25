package com.jbooktrader.platform.util.contract;

import java.text.*;
import java.util.*;

/**
 * @author marcus
 */
public class CLNYMEXMostLiquid {

    public static final int VOLUME_CROSSOVER_BEFORE_EXP = 2; // in business days before expiration

    /**
     * Given a month, what is the expiration day, based on the following rule
     * - 3 business days prior to the 25th
     * - If the 25th falls on a weekend, 3 business days not counting this weekend
     * <p/>
     * - not sure about holidays yet, TBD
     *
     * @param month Integer month 1-12
     * @return day of expiration
     */
    public static int getExpireDateForMonthYear(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DATE, 25); // using this 25th as a rule here

        // get to first non weekend day, prior to the 25th
        backupToBusinessDay(cal);
        // now, we should subtract 3 business days
        for (int i = 0; i < 3; i++) {
            backupToBusinessDay(cal);
            cal.add(Calendar.DATE, -1);  // subtract a day, which don't be on a weekend
        }

        return cal.get(Calendar.DATE);

    }

    public static void backupToBusinessDay(Calendar cal) {
        while (!isBusinessDay(cal)) {
            cal.add(Calendar.DATE, -1);  // subtract a day
        }
    }

    public static boolean isBusinessDay(Calendar cal) {
        return (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
    }


    public static String getMostLiquidExpiry(Calendar mydate) {
        Calendar contract = Calendar.getInstance();
        contract.setTime(mydate.getTime());

        // start by selecting current month, and see if it is ok
        int myMonth = mydate.get(Calendar.MONTH) + 1;
        int myYear = mydate.get(Calendar.YEAR);
        int dateExp = getExpireDateForMonthYear(myMonth, myYear);

        Calendar cutoffDate = Calendar.getInstance(mydate.getTimeZone());
        cutoffDate.set(Calendar.YEAR, mydate.get(Calendar.YEAR));
        cutoffDate.set(Calendar.MONTH, mydate.get(Calendar.MONTH));
        cutoffDate.set(Calendar.DATE, dateExp);
        cutoffDate.set(Calendar.SECOND, 0);
        cutoffDate.set(Calendar.HOUR, 17);
        cutoffDate.set(Calendar.MINUTE, 0);

        for (int i = 0; i < VOLUME_CROSSOVER_BEFORE_EXP; i++) {
            backupToBusinessDay(cutoffDate);
            cutoffDate.add(Calendar.DATE, -1);
        }

        // check to see if we are far enough away to use this month

        if (mydate.compareTo(cutoffDate) <= 0) {  // we can use this month
            contract.set(Calendar.DATE, dateExp);
        } else {
            // need to use next months
            contract.add(Calendar.MONTH, 1); // next month
            dateExp = getExpireDateForMonthYear(    // replace date with exp date for that month
                contract.get(Calendar.MONTH) + 1,
                contract.get(Calendar.YEAR)
            );
            contract.set(Calendar.DATE, dateExp);
        }

        // return contract as a string as required by tws
        // note, nominal feb contract would have expire date in Jan, and we think tws expects jan expire date for Feb nominal contract
        contract.add(Calendar.MONTH, 1);  // contract is called by the month after's name for NYMEX
        // update:  we need to send normal month info, per this link: http://www.interactivebrokers.com/en/trading/expirationLabel.html
        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");

        return df.format(contract.getTime());

    }

    public static String getMostLiquid() {
        return getMostLiquidExpiry(Calendar.getInstance());
    }
}
