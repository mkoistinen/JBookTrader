package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    public final static int COLUMNS = 4;
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final String fileName;
    private long previousTime;
    private SimpleDateFormat sdf;
    private volatile boolean cancelled;
    private BufferedReader reader;
    private long snapshotCount, firstMarketLine, lineNumber;
    private MarketSnapshotFilter filter;

    public BackTestFileReader(String fileName) throws JBookTraderException {
        this.fileName = fileName;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileName);
        }
    }

    public void cancel() {
        cancelled = true;
    }

    public long getSnapshotCount() {
        return snapshotCount;
    }

    private void setTimeZone(String line) throws JBookTraderException {
        String timeZone = line.substring(line.indexOf('=') + 1);
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JBookTraderException(msg);
        }
        sdf = new SimpleDateFormat("MMddyy,HHmmss");
        // Enforce strict interpretation of date and time formats
        sdf.setLenient(false);
        sdf.setTimeZone(tz);
    }

    public void setFilter(MarketSnapshotFilter mssFilter) {
        filter = mssFilter;
    }

    public void scan() throws JBookTraderException {
        String line;
        MarketSnapshot marketSnapshot = null;

        try {
            while ((line = reader.readLine()) != null && !cancelled) {
                lineNumber++;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);
                if (isMarketDepthLine) {
                    marketSnapshot = toMarketDepth(line);
                    if (filter == null || filter.accept(marketSnapshot)) {
                        snapshotCount++;
                        if (firstMarketLine == 0) {
                            firstMarketLine = lineNumber;
                        }
                    }
                }

                if (isProperty) {
                    if (line.startsWith("timeZone")) {
                        setTimeZone(line);
                    }
                }
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            for (int lineCount = 1; lineCount < firstMarketLine; lineCount++) {
                reader.readLine();
            }
            lineNumber = firstMarketLine;

        } catch (IOException ioe) {
            throw new JBookTraderException("Could not read data file");
        }

    }

    public MarketSnapshot next() {
        String line = "";
        MarketSnapshot marketSnapshot = null;

        try {
            while (marketSnapshot == null) {
                line = reader.readLine();

                if (line == null) {
                    reader.close();
                    break;
                } else {
                    marketSnapshot = toMarketDepth(line);
                    lineNumber++;
                    if (filter == null || filter.accept(marketSnapshot)) {
                        previousTime = marketSnapshot.getTime();
                    } else {
                        marketSnapshot = null;
                    }
                }
            } // while
        }
        catch (IOException ioe) {
            throw new RuntimeException("Could not read data file");
        }
        catch (JBookTraderException e) {
            String errorMsg = "";
            if (lineNumber > 0) {
                errorMsg = "Problem parsing line #" + lineNumber + LINE_SEP;
                errorMsg += line + LINE_SEP;
            }
            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            errorMsg += description;
            throw new RuntimeException(errorMsg);
        }

        return marketSnapshot;
    }


    private MarketSnapshot toMarketDepth(String line) throws JBookTraderException {
        if (sdf == null) {
            String msg = "Property " + "\"timeZone\"" + " is not defined in the data file." + LINE_SEP;
            throw new JBookTraderException(msg);
        }

        StringTokenizer st = new StringTokenizer(line, ",");

        int tokenCount = st.countTokens();
        if (tokenCount != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            throw new JBookTraderException(msg);
        }

        String dateToken = st.nextToken();
        String timeToken = st.nextToken();
        long time;
        try {
            time = sdf.parse(dateToken + "," + timeToken).getTime();
        } catch (ParseException pe) {
            throw new JBookTraderException("Could not parse date/time in " + dateToken + "," + timeToken);
        }

        if (previousTime != 0) {
            if (time <= previousTime) {
                String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
                throw new JBookTraderException(msg);
            }
        }

        int balance = Integer.parseInt(st.nextToken());
        double price = Double.parseDouble(st.nextToken());
        return new MarketSnapshot(time, balance, price);
    }
}

