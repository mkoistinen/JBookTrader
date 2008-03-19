package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final Properties properties = new Properties();
    // Each line contains at least 6 columns: date, time, 1 bid size and price, 1 ask size and price
    // If market is deeper than 1 bid and ask, the line will contain more columns
    private final static int MIN_COLUMNS = 6;
    private long previousTime;
    private SimpleDateFormat sdf;
    private BufferedReader reader;
    private int lineNumber, totalLines;
    private String errorMsg;
    private volatile boolean cancelled;
    private final String fileName;

    public int getTotalLineCount() {
        return totalLines;
    }

    public String getError() {
        return errorMsg;
    }

    public void cancel() {
        cancelled = true;
    }


    public BackTestFileReader(String fileName) throws JBookTraderException {
        this.fileName = fileName;
        Report report = Dispatcher.getReporter();
        report.report("Reading properties in the historical market data file");

        try {
            properties.load(new FileInputStream(fileName));
        } catch (IllegalArgumentException e) {
            String msg = "Problem loading file " + fileName + ": binary format was detected. ";
            msg += e.getMessage();
            throw new JBookTraderException(msg);
        } catch (Exception e) {
            String msg = "Problem loading file " + fileName + ": " + e.getMessage();
            throw new JBookTraderException(msg);
        }


        String timeZone = getPropAsString("timeZone");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JBookTraderException(msg);
        }

        sdf = new SimpleDateFormat("MMddyy,HH:mm:ss.SSS");
        // Enforce strict interpretation of date and time formats
        sdf.setLenient(false);
        sdf.setTimeZone(tz);

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
        if (tokenCount < MIN_COLUMNS) {
            String msg = "The line should contain at least " + MIN_COLUMNS + " columns, but only " + tokenCount + " columns have been counted.";
            throw new JBookTraderException(msg);
        }

        StringTokenizer typeTokenizer = new StringTokenizer(line, ";");
        tokenCount = typeTokenizer.countTokens();
        if (tokenCount != 3) {
            String msg = "The line should contain exactly 3 semicolon-separated sections.";
            throw new JBookTraderException(msg);
        }

        StringTokenizer dateTimeTokenizer = new StringTokenizer(typeTokenizer.nextToken(), ",");
        String dateToken = dateTimeTokenizer.nextToken();
        String timeToken = dateTimeTokenizer.nextToken();
        long time = sdf.parse(dateToken + "," + timeToken).getTime();

        LinkedList<MarketDepthItem> bids = new LinkedList<MarketDepthItem>();
        LinkedList<MarketDepthItem> asks = new LinkedList<MarketDepthItem>();

        StringTokenizer bidsTokenizer = new StringTokenizer(typeTokenizer.nextToken(), ",");
        while (bidsTokenizer.hasMoreTokens()) {
            int size = Integer.parseInt(bidsTokenizer.nextToken());
            double price = Double.parseDouble(bidsTokenizer.nextToken());
            MarketDepthItem item = new MarketDepthItem(size, price);
            bids.add(item);
        }

        StringTokenizer asksTokenizer = new StringTokenizer(typeTokenizer.nextToken(), ",");
        while (asksTokenizer.hasMoreTokens()) {
            int size = Integer.parseInt(asksTokenizer.nextToken());
            double price = Double.parseDouble(asksTokenizer.nextToken());
            MarketDepthItem item = new MarketDepthItem(size, price);
            asks.add(item);
        }

        if (previousTime != 0) {
            if (time <= previousTime) {
                String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
                throw new JBookTraderException(msg);
            }
        }

        return new MarketDepth(bids, asks, time);
    }


    private String getPropAsString(String property) throws JBookTraderException {
        String propValue = (String) properties.get(property);
        if (propValue == null) {
            String msg = "Property \"" + property + "\" is not defined in the historical data file.";
            throw new JBookTraderException(msg);
        }

        return propValue;
    }
}
