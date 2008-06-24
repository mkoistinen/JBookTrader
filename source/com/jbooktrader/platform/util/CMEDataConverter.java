package com.jbooktrader.platform.util;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Converts historical market depth data from CME format to JBT format, and writes the data to a file.
 * The created data file can be used for backtesting and optimization of trading strategies.
 */
public class CMEDataConverter {
    private static final long RECORDING_START = 9 * 60 * 60 + 10 * 60; // 9:10:00 EDT
    private static final long RECORDING_END = 16 * 60 * 60 + 15 * 60; // 16:15:00 EDT
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepthItem> bids, asks;
    private final BackTestFileWriter backTestFileWriter;
    private final BufferedReader reader;
    private final SimpleDateFormat cmeDateFormat;
    private final String contract;
    private final Calendar instant;
    private long time, lineNumber;
    private int openBalance, highBalance, lowBalance, closeBalance;
    private double highPrice, lowPrice;


    public static void main(String[] args) throws JBookTraderException {

        if (args.length != 4) {
            throw new JBookTraderException("Usage: <cmeFileName> <jbtFileName> <contract> <samplingFrequency>");
        }

        CMEDataConverter dataConverter = new CMEDataConverter(args[0], args[1], args[2]);
        long samplingFrequency = Long.valueOf(args[3]);

        dataConverter.convert(samplingFrequency);
    }


    private CMEDataConverter(String cmeFileName, String jbtFileName, String contract) throws JBookTraderException {

        this.contract = contract;

        cmeDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        cmeDateFormat.setLenient(false);
        cmeDateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

        instant = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));

        bids = new LinkedList<MarketDepthItem>();
        asks = new LinkedList<MarketDepthItem>();

        for (int level = 0; level < 5; level++) {
            bids.add(null);
            asks.add(null);
        }

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cmeFileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + cmeFileName);
        }

        try {
            backTestFileWriter = new BackTestFileWriter(jbtFileName, TimeZone.getTimeZone("America/New_York"), false);
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not create file " + jbtFileName);
        }

        System.out.println("Converting " + cmeFileName + " to " + jbtFileName);
    }

    private int getCumulativeSize(LinkedList<MarketDepthItem> items) {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            if (item != null) {
                cumulativeSize += item.getSize();
            }
        }
        return cumulativeSize;
    }

    private void update() {
        int cumulativeBid = getCumulativeSize(bids);
        int cumulativeAsk = getCumulativeSize(asks);
        double totalDepth = cumulativeBid + cumulativeAsk;
        closeBalance = (int) (100. * (cumulativeBid - cumulativeAsk) / totalDepth);
        highBalance = Math.max(closeBalance, highBalance);
        lowBalance = Math.min(closeBalance, lowBalance);
        highPrice = Math.max(highPrice, asks.getFirst().getPrice());
        lowPrice = Math.min(lowPrice, bids.getFirst().getPrice());
    }

    private boolean isRecordable(Calendar instant) {
        int secondsOfDay = instant.get(Calendar.HOUR_OF_DAY) * 60 * 60 + instant.get(Calendar.MINUTE) * 60 + instant.get(Calendar.SECOND);
        return secondsOfDay >= RECORDING_START && secondsOfDay <= RECORDING_END;
    }


    private void convert(long samplingFrequency) {
        String line = null;

        try {
            long previousTime = 0;
            backTestFileWriter.writeHeader();

            System.out.println("Conversion started...");
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber % 500000 == 0) {
                    System.out.println(lineNumber + " lines converted");
                }

                try {
                    parse(line);
                    update();
                    instant.setTimeInMillis(time);
                    if ((time - previousTime) >= samplingFrequency) {
                        if (isRecordable(instant)) {
                            MarketDepth marketDepth = new MarketDepth(time, openBalance, highBalance, lowBalance, closeBalance, highPrice, lowPrice);
                            backTestFileWriter.write(marketDepth, true);
                        }

                        openBalance = highBalance = lowBalance = closeBalance;
                        highPrice = asks.getFirst().getPrice();
                        lowPrice = bids.getFirst().getPrice();
                        previousTime = time;
                    }
                } catch (Exception e) {
                    String errorMsg = "Problem parsing line #" + lineNumber + LINE_SEP;
                    System.out.println(errorMsg);
                    e.printStackTrace();
                }
            }
            System.out.println("Done: " + lineNumber + " lines converted successfully.");
        } catch (Exception e) {
            String errorMsg = "Problem parsing line #" + lineNumber + LINE_SEP;
            errorMsg += line + LINE_SEP;
            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            errorMsg += description;
            System.out.println(errorMsg);
        } finally {
            try {
                reader.close();
                backTestFileWriter.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    private void parse(String line) throws ParseException {

        boolean isSpecifiedContract = (line.substring(49, 54).trim().equals(contract));
        boolean isLimitOrderMessage = line.substring(33, 35).equals("MA");

        if (isLimitOrderMessage && isSpecifiedContract) {
            String centiseconds = line.substring(14, 16);
            int millis = Integer.valueOf(centiseconds) * 10;
            String date = line.substring(17, 29) + line.substring(12, 14) + millis;
            time = cmeDateFormat.parse(date).getTime();

            int position = 82;
            for (int level = 0; level < 5; level++) {
                if (line.charAt(76 + level) == '1') {

                    int bidSize = Integer.parseInt(line.substring(position, position + 12));

                    position += 16;
                    double bidPrice = Integer.valueOf(line.substring(position + 1, position + 19)) / 100d;

                    position += 19;
                    double askPrice = Integer.valueOf(line.substring(position + 1, position + 19)) / 100d;

                    position += 23;
                    int askSize = Integer.parseInt(line.substring(position, position + 12));

                    bids.set(level, new MarketDepthItem(bidSize, bidPrice));
                    asks.set(level, new MarketDepthItem(askSize, askPrice));
                    //System.out.println("Level: " + level + " Bid: " + bidPrice + " BidSize: " + bidSize + " Ask price:" + askPrice + " Ask Size:" + askSize);
                    position += 14;
                }
            }
        }
    }

}

