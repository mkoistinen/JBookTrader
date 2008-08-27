package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;
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
    public final static int COLUMNS = 10;
    private static final String LINE_SEP = System.getProperty("line.separator");

    private final LinkedList<MarketSnapshot> marketSnapshots;
    private long previousTime;
    private SimpleDateFormat sdf;
    private volatile boolean cancelled;
    private BufferedReader reader;

    public BackTestFileReader(String fileName) throws JBookTraderException {
        marketSnapshots = new LinkedList<MarketSnapshot>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileName);
        }
    }

    public void cancel() {
        cancelled = true;
    }

    public LinkedList<MarketSnapshot> getAll() {
        return marketSnapshots;
    }

    private void getTimeZone(String line) throws JBookTraderException {
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

    public void load() throws JBookTraderException {
        Report report = Dispatcher.getReporter();
        report.report("Scanning historical market data file");
        String line = "";
        int lineNumber = 0;

        try {
            while ((line = reader.readLine()) != null && !cancelled) {
                lineNumber++;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);
                if (isMarketDepthLine) {
                    MarketSnapshot marketSnapshot = toMarketDepth(line);
                    previousTime = marketSnapshot.getTime();
                    marketSnapshots.add(marketSnapshot);
                }

                if (isProperty && line.startsWith("timeZone")) {
                    getTimeZone(line);
                }
            }
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not read data file");
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
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                report.report(ioe);
            }
        }

        report.report("Scanning historical market data file completed");
    }


    private MarketSnapshot toMarketDepth(String line) throws JBookTraderException, ParseException {
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
        long time = sdf.parse(dateToken + "," + timeToken).getTime();

        if (previousTime != 0) {
            if (time < previousTime) {
                String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
                throw new JBookTraderException(msg);
            }
        }

        int lowBalance = Integer.parseInt(st.nextToken());
        int highBalance = Integer.parseInt(st.nextToken());
        double bestBid = Double.parseDouble(st.nextToken());
        double bestAsk = Double.parseDouble(st.nextToken());
        int volume = Integer.parseInt(st.nextToken());
        double tick = Double.parseDouble(st.nextToken());
        double trin = Double.parseDouble(st.nextToken());
        double vix = Double.parseDouble(st.nextToken());


        return new MarketSnapshot(time, lowBalance, highBalance, bestBid, bestAsk, volume, tick, trin, vix);
    }
}

