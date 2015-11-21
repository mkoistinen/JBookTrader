package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for back testing and optimization of trading strategies.
 *
 * @author Eugene Kononov
 */
public class BackTestFileReader {
    public static final int COLUMNS = 5;
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final BufferedReader reader;
    private final MarketSnapshotFilter filter;
    private final long fileSize;
    private long previousTime, time;
    private SimpleDateFormat sdf;
    private String previousDateTimeWithoutSeconds;
    private final static Map<String, List<MarketSnapshot>> cache = new HashMap<>();
    private String cacheKey;

    public BackTestFileReader(String fileName, MarketSnapshotFilter filter) throws JBookTraderException {
        this.filter = filter;
        previousDateTimeWithoutSeconds = "";

        cacheKey = fileName;
        if (filter != null) {
            cacheKey += "," + filter.toString();
        }

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            fileSize = new File(fileName).length();
        } catch (FileNotFoundException fnf) {
            throw new JBookTraderException("Could not find file: " + fileName);
        }
    }

    private void setTimeZone(String line) throws JBookTraderException {
        String timeZone = line.substring(line.indexOf('=') + 1);
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JBookTraderException(msg);
        }
        sdf = new SimpleDateFormat("MMddyyHHmmss");
        // Enforce strict interpretation of date and time formats
        sdf.setLenient(false);
        sdf.setTimeZone(tz);
    }

    public List<MarketSnapshot> load(ProgressListener progressListener) throws JBookTraderException {

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }


        String line = "";
        int lineSeparatorSize = System.getProperty("line.separator").length();
        long sizeRead = 0, lineNumber = 0;

        List<MarketSnapshot> snapshots = new ArrayList<>();

        try {
            while ((line = reader.readLine()) != null) {
                if (lineNumber % 50000 == 0) {
                    progressListener.setProgress(sizeRead, fileSize, "Loading historical data file");
                    if (progressListener.isCancelled()) {
                        break;
                    }
                }
                lineNumber++;
                sizeRead += line.length() + lineSeparatorSize;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);
                boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);
                if (isMarketDepthLine) {
                    MarketSnapshot marketSnapshot = toMarketDepth(line);
                    if (filter == null || filter.contains(time)) {
                        snapshots.add(marketSnapshot);
                    }
                    previousTime = time;
                } else if (isProperty) {
                    if (line.startsWith("timeZone")) {
                        setTimeZone(line);
                    }
                }
            }

            if (sdf == null) {
                String msg = "Property " + "\"timeZone\"" + " is not defined in the data file." + LINE_SEP;
                throw new JBookTraderException(msg);
            }

        } catch (IOException ioe) {
            throw new JBookTraderException("Could not read data file");
        } catch (Exception e) {
            String errorMsg = "Problem parsing line #" + lineNumber + ": " + line + LINE_SEP;
            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            errorMsg += description;
            throw new RuntimeException(errorMsg);
        }

        if (!progressListener.isCancelled()) {
            cache.put(cacheKey, snapshots);
        }
        return snapshots;

    }

    private MarketSnapshot toMarketDepth(String line) throws JBookTraderException, ParseException {
        List<String> tokens = fastSplit(line);

        if (tokens.size() != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            throw new JBookTraderException(msg);
        }

        String dateTime = tokens.get(0) + tokens.get(1);
        String dateTimeWithoutSeconds = dateTime.substring(0, 10);

        if (dateTimeWithoutSeconds.equals(previousDateTimeWithoutSeconds)) {
            // only seconds need to be set
            int milliSeconds = 1000 * Integer.parseInt(dateTime.substring(10));
            long previousMilliSeconds = previousTime % 60000;
            time = previousTime + (milliSeconds - previousMilliSeconds);
        } else {
            time = sdf.parse(dateTime).getTime();
            previousDateTimeWithoutSeconds = dateTimeWithoutSeconds;
        }

        if (time <= previousTime) {
            String msg = "Timestamp of this line is before or the same as the timestamp of the previous line.";
            throw new JBookTraderException(msg);
        }

        double balance = Double.parseDouble(tokens.get(2));
        double price = Double.parseDouble(tokens.get(3));
        int volume = Integer.parseInt(tokens.get(4));
        return new MarketSnapshot(time, balance, price, volume);
    }

    private List<String> fastSplit(String s) {
        ArrayList<String> tokens = new ArrayList<>();
        int index, lastIndex = 0;
        while ((index = s.indexOf(',', lastIndex)) != -1) {
            tokens.add(s.substring(lastIndex, index));
            lastIndex = index + 1;
        }
        tokens.add(s.substring(lastIndex));
        return tokens;
    }

}
