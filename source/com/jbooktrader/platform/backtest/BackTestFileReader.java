package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final static int COLUMNS = 8;
    private long previousTime;
    private SimpleDateFormat sdf;
    private BufferedReader reader;
    private int lineNumber, totalLines;
    private volatile boolean cancelled;
    private final String fileName;
    private TimeZone tz;

    public int getTotalLineCount() {
        return totalLines;
    }

    public void cancel() {
        cancelled = true;
    }


    public BackTestFileReader(String fileName) throws JBookTraderException {
        this.fileName = fileName;
        Report report = Dispatcher.getReporter();

        report.report("Scanning historical market data file");
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

            String line;
            while ((line = reader.readLine()) != null && !cancelled) {
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);
                if (isMarketDepthLine) {
                    totalLines++;
                }

                if (isProperty) {
                    if (line.startsWith("timeZone")) {
                        String timeZone = line.substring(line.indexOf('=') + 1);
                        tz = TimeZone.getTimeZone(timeZone);
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

                }
            }
            if (tz == null) {
                String msg = "Property " + "\"timeZone\"" + " is not defined in the data file." + LINE_SEP;
                throw new JBookTraderException(msg);
            }
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileName);
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not read file " + fileName);
        }

        report.report("Scanning historical market data file completed");
    }

    public void reset() throws JBookTraderException {
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileName);
        }
        previousTime = 0;
    }

    public MarketDepth getNextMarketDepth() throws JBookTraderException {

        String line = null;
        MarketDepth marketDepth = null;

        try {
            boolean isMarketDepthLine = false;
            do {
                line = reader.readLine();
                lineNumber++;
                if (line != null) {
                    boolean isComment = line.startsWith("#");
                    boolean isProperty = line.contains("=");
                    boolean isBlankLine = (line.trim().length() == 0);
                    isMarketDepthLine = !(isComment || isProperty || isBlankLine);

                    if (isMarketDepthLine) {
                        marketDepth = toMarketDepth(line);
                        previousTime = marketDepth.getTime();
                    }
                }
            } while (!isMarketDepthLine && line != null);
        } catch (Exception e) {
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
            throw new JBookTraderException(errorMsg);
        }

        return marketDepth;
    }


    private MarketDepth toMarketDepth(String line) throws ParseException, JBookTraderException {
        StringTokenizer st = new StringTokenizer(line, ",;");

        int tokenCount = st.countTokens();
        if (tokenCount != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            throw new JBookTraderException(msg);
        }

        String dateToken = st.nextToken();
        String timeToken = st.nextToken();
        long time = sdf.parse(dateToken + "," + timeToken).getTime();

        if (previousTime != 0) {
            if (time < previousTime) {
                String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
                throw new JBookTraderException(msg);
            }
        }

        int open = Integer.parseInt(st.nextToken());
        int high = Integer.parseInt(st.nextToken());
        int low = Integer.parseInt(st.nextToken());
        int close = Integer.parseInt(st.nextToken());

        double lowPrice = Double.parseDouble(st.nextToken());
        double highPrice = Double.parseDouble(st.nextToken());

        return new MarketDepth(time, open, high, low, close, highPrice, lowPrice);
    }


}
