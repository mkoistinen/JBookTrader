package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final List<MarketDepth> marketDepths = new ArrayList<MarketDepth>();
    private final Properties properties = new Properties();
    // Each line contains at least 6 columns: date, time, 1 bid size and price, 1 ask size and price
    // If market is deeper than 1 bid and ask, the line will contain more columns
    private final static int MIN_COLUMNS = 6;
    private long previousTime;
    private SimpleDateFormat sdf;

    public List<MarketDepth> getMarketDepths() {
        return marketDepths;
    }

    public BackTestFileReader(String fileName) throws JBookTraderException {
        Report report = Dispatcher.getReporter();
        report.report("Reading back data file");

        String line = null;
        int lineNumber = 0;

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

        try {
            String timeZone = getPropAsString("timeZone");
            TimeZone tz = TimeZone.getTimeZone(timeZone);
            if (!tz.getID().equals(timeZone)) {
                String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + LINE_SEP;
                msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
                throw new JBookTraderException(msg);
            }

            sdf = new SimpleDateFormat("MMddyy,HH:mm:ss");
            // Enforce strict interpretation of date and time formats
            sdf.setLenient(false);
            sdf.setTimeZone(tz);

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Dispatcher.getReporter().report("Loading historical data file");

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);

                if (isMarketDepthLine) {
                    MarketDepth marketDepth = toMarketDepth(line);
                    long time = marketDepth.getTime();
                    marketDepths.add(marketDepth);
                    previousTime = time;
                }
            }

            reader.close();
            boolean showNumberOfRecords = PropertiesHolder.getInstance().getProperty("backtest.showNumberOfRecords").equals("true");
            if (showNumberOfRecords) {
                String msg = marketDepths.size() + " records have been read successfully.";
                MessageDialog.showMessage(null, msg);
            }
            report.report("Loaded " + marketDepths.size() + " records from historical data file");
        } catch (Exception e) {
            String msg = "";
            if (lineNumber > 0) {
                msg = "Problem parsing line #" + lineNumber + LINE_SEP;
                msg += line + LINE_SEP;
            }
            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            msg += description;
            throw new JBookTraderException(msg);
        }
    }

    private MarketDepth toMarketDepth(String line) throws ParseException, JBookTraderException {
        StringTokenizer st = new StringTokenizer(line, ",;");

        int tokenCount = st.countTokens();
        if (tokenCount < MIN_COLUMNS) {
            String msg = "The record should contain at least " + MIN_COLUMNS + " columns, but only " + tokenCount + " columns have been counted.";
            throw new JBookTraderException(msg);
        }

        StringTokenizer typeTokenizer = new StringTokenizer(line, ";");
        tokenCount = typeTokenizer.countTokens();
        if (tokenCount != 3) {
            String msg = "The record should contain 3" + " columns, but only " + tokenCount + " columns have been counted.";
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
                String msg = "Timestamp of this record is before or the same as the timestamp of the previous record.";
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
