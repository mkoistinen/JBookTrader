package com.jbooktrader.platform.schedule;

import com.jbooktrader.platform.model.*;

import java.util.*;


/**
 * TradingSchedule defines the time period during which a strategy can trade.
 * Trading can start after the "startTime". Open positions will be closed
 * at the "endTime". The "startTime" and "endTime" times must be specified
 * in the military time format.
 * <p/>
 * Example: A strategy defines the following trading schedule:
 * tradingSchedule = new TradingSchedule("9:35", "15:45", "America/New_York");
 * Then the following trading timeline is formed:
 * -- start trading at 9:35 EST
 * -- close open positions at 15:45 EST
 * <p/>
 * A particular period of time within the time window between The "startTime" and "endTime"
 * can be set as exclusionary.
 * Example: A strategy defines the following trading interval, along with the exclusion period:
 * tradingSchedule = new TradingSchedule("9:35", "15:45", "America/New_York");
 * tradingSchedule.setExclusion("12:00", "13:00");
 * Then the following trading timeline is formed:
 * -- start trading at 9:35 EST
 * -- close open positions at 12:00 EST
 * -- resume trading at 13:00 EST
 * -- close open positions at 15:45 EST
 */
public class TradingSchedule {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final int start, end;
    private final TimeZone tz;
    private final Calendar instant;
    private final String text;
    private int exclusionStart, exclusionEnd;
    private boolean hasExclusion;

    public TradingSchedule(String startTime, String endTime, String timeZone) throws JBookTraderException {
        tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " is invalid." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JBookTraderException(msg);
        }
        instant = Calendar.getInstance(tz);

        start = getMinutes(startTime);
        end = getMinutes(endTime);
        if (start >= end) {
            String msg = "End time must be after the start time in trading schedule.";
            throw new JBookTraderException(msg);
        }

        text = startTime + " to " + endTime + " (" + timeZone + ")";
    }

    public void setExclusion(String startExclusionTime, String endExclusionTime) throws JBookTraderException {
        exclusionStart = getMinutes(startExclusionTime);
        exclusionEnd = getMinutes(endExclusionTime);
        if (exclusionStart >= exclusionEnd) {
            String msg = "Exclusion end time must be after the exclusion start time in trading schedule.";
            throw new JBookTraderException(msg);
        }

        boolean isValidExclusion = (exclusionStart > start) && (exclusionEnd < end);
        if (!isValidExclusion) {
            String msg = "Exclusion period must be within trading period in trading schedule.";
            throw new JBookTraderException(msg);
        }

        hasExclusion = true;
    }

    public TimeZone getTimeZone() {
        return tz;
    }

    public boolean contains(long time) {
        instant.setTimeInMillis(time);
        int minutes = instant.get(Calendar.HOUR_OF_DAY) * 60 + instant.get(Calendar.MINUTE);

        boolean containsTime;
        if (hasExclusion) {
            containsTime = (minutes >= start && minutes < exclusionStart);
            containsTime = containsTime || (minutes >= exclusionEnd && minutes < end);
        } else {
            containsTime = minutes >= start && minutes < end;
        }

        return containsTime;
    }

    public boolean approximatelyContains(long time) {
        instant.setTimeInMillis(time);
        int minutes = instant.get(Calendar.HOUR_OF_DAY) * 60 + instant.get(Calendar.MINUTE);
        return minutes >= (start - 5) && minutes < (end + 5);
    }


    private int getMinutes(String time) throws JBookTraderException {
        StringTokenizer st = new StringTokenizer(time, ":");
        int tokens = st.countTokens();
        if (tokens != 2) {
            String msg = "Time " + time + " does not conform to the HH:MM format in trading schedule.";
            throw new JBookTraderException(msg);
        }

        int hours, minutes;

        String hourToken = st.nextToken();
        try {
            hours = Integer.parseInt(hourToken);
        } catch (NumberFormatException nfe) {
            String msg = hourToken + " in " + time + " can not be parsed as hours in trading schedule.";
            throw new JBookTraderException(msg);
        }

        String minuteToken = st.nextToken();
        try {
            minutes = Integer.parseInt(minuteToken);
        } catch (NumberFormatException nfe) {
            String msg = minuteToken + " in " + time + " can not be parsed as minutes in trading schedule.";
            throw new JBookTraderException(msg);
        }

        if (hours < 0 || hours > 23) {
            String msg = "Specified hours: " + hours + ". Number of hours must be in the [0..23] range in trading schedule.";
            throw new JBookTraderException(msg);
        }

        if (minutes < 0 || minutes > 59) {
            String msg = "Specified minutes: " + minutes + ". Number of minutes must be in the [0..59] range in trading schedule.";
            throw new JBookTraderException(msg);
        }

        return hours * 60 + minutes;
    }

    public String toString() {
        return text;
    }

}

/* $Id$ */
