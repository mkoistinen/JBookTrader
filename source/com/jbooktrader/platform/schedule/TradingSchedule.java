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
 * Then the following trading time line is formed:
 * -- start trading at 9:35 EST
 * -- close open positions at 15:45 EST
 * <p/>
 * A particular period of time within the time window between the "startTime" and "endTime"
 * can be excluded.
 * Example: A strategy defines the following trading interval, along with the exclusion period:
 * tradingSchedule = new TradingSchedule("9:35", "15:45", "America/New_York");
 * tradingSchedule.setExclusion("12:00", "13:00");
 * Then the following trading time line is formed:
 * -- start trading at 9:35 EST
 * -- close open positions at 12:00 EST
 * -- resume trading at 13:00 EST
 * -- close open positions at 15:45 EST
 *
 * @author Eugene Kononov
 */
public class TradingSchedule {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final TimeZone tz;
    private final Calendar startCalendar, endCalendar, nowCalendar;
    private Calendar exclusionStartCalendar, exclusionEndCalendar;
    private final String text;
    private long start, end, exclusionStart, exclusionEnd;
    private boolean hasExclusion;

    public TradingSchedule(String startTime, String endTime, String timeZone) throws JBookTraderException {
        tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " is invalid." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JBookTraderException(msg);
        }

        nowCalendar = Calendar.getInstance(tz);
        startCalendar = getCalendar(startTime);
        endCalendar = getCalendar(endTime);

        if (!endCalendar.after(startCalendar)) {
            String msg = "End time must be after the start time in trading schedule.";
            throw new JBookTraderException(msg);
        }

        text = startTime + " to " + endTime + " (" + timeZone + ")";
    }

    public TimeZone getTimeZone() {
        return tz;
    }

    public void setExclusion(String startExclusionTime, String endExclusionTime) throws JBookTraderException {
        exclusionStartCalendar = getCalendar(startExclusionTime);
        exclusionEndCalendar = getCalendar(endExclusionTime);

        if (!exclusionEndCalendar.after(exclusionStartCalendar)) {
            String msg = "Exclusion end time must be after the exclusion start time in trading schedule.";
            throw new JBookTraderException(msg);
        }

        boolean isValidExclusionStart = (exclusionStartCalendar.after(startCalendar) && endCalendar.after(exclusionStartCalendar));
        boolean isValidExclusionEnd = (exclusionEndCalendar.after(startCalendar) && endCalendar.after(exclusionEndCalendar));
        if (!isValidExclusionStart || !isValidExclusionEnd) {
            String msg = "Exclusion period must be within trading period in trading schedule.";
            throw new JBookTraderException(msg);
        }

        hasExclusion = true;
    }

    public boolean contains(long time) {
        if (time > end) {
            updateCalendars(time);
        }

        boolean containsTime = time >= start && time < end;

        if (hasExclusion) {
            containsTime = containsTime && (time < exclusionStart || time >= exclusionEnd);
        }

        return containsTime;
    }

    public long getRemainingTime(long time) {
        if (time > end) {
            updateCalendars(time);
        }

        return end - time;
    }


    private void updateCalendars(long time) {
        nowCalendar.setTimeInMillis(time);

        int daysForward = 0;
        while (nowCalendar.after(endCalendar)) {
            endCalendar.add(Calendar.DAY_OF_YEAR, 1);
            daysForward++;
        }

        startCalendar.add(Calendar.DAY_OF_YEAR, daysForward);
        start = startCalendar.getTimeInMillis();
        end = endCalendar.getTimeInMillis();

        if (hasExclusion) {
            exclusionStartCalendar.add(Calendar.DAY_OF_YEAR, daysForward);
            exclusionEndCalendar.add(Calendar.DAY_OF_YEAR, daysForward);
            exclusionStart = exclusionStartCalendar.getTimeInMillis();
            exclusionEnd = exclusionEndCalendar.getTimeInMillis();
        }

    }

    private Calendar getCalendar(String time) throws JBookTraderException {
        Calendar calendar = Calendar.getInstance(tz);

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

        calendar.set(Calendar.YEAR, 2008); // has to be before the first timestamp in the data file
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    @Override
    public String toString() {
        return text;
    }

}
