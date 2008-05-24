package com.jbooktrader.platform.util;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

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
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final SimpleDateFormat jbtDateFormat, cmeDateFormat;
    private final DecimalFormat decimalFormat;
    private final String contract;
    private long time;
    private long lineNumber;
    private final Calendar instant;
    private int lowBalance, highBalance;

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
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);

        jbtDateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        jbtDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));

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

        lowBalance = 100;
        highBalance = -100;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cmeFileName)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + cmeFileName);
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(jbtFileName, false)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not create file " + jbtFileName);
        }

        System.out.println("Converting " + cmeFileName + " to " + jbtFileName);
    }


    private void write() {
        StringBuilder sb = new StringBuilder();
        sb.append(jbtDateFormat.format(new Date(time))).append(",");
        sb.append(lowBalance).append(",");
        sb.append(highBalance).append(",");
        sb.append(decimalFormat.format(bids.getFirst().getPrice()));

        writer.println(sb);
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

    private void updateMinMaxBalance() {
        int cumulativeBid = getCumulativeSize(bids);
        int cumulativeAsk = getCumulativeSize(asks);
        double totalDepth = cumulativeBid + cumulativeAsk;
        int balance = (int) (100. * (cumulativeBid - cumulativeAsk) / totalDepth);
        lowBalance = Math.min(balance, lowBalance);
        highBalance = Math.max(balance, highBalance);
    }

    private boolean isRecordable(Calendar instant) {
        int secondsOfDay = instant.get(Calendar.HOUR_OF_DAY) * 60 * 60 + instant.get(Calendar.MINUTE) * 60 + instant.get(Calendar.SECOND);
        return secondsOfDay >= RECORDING_START && secondsOfDay <= RECORDING_END;
    }


    private void convert(long samplingFrequency) {
        String line = null;

        try {
            long previousTime = 0;
            StringBuilder header = getHeader();
            writer.println(header);

            System.out.println("Conversion started...");
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber % 250000 == 0) {
                    System.out.println(lineNumber + " lines converted");
                }

                try {
                    parse(line);
                    updateMinMaxBalance();
                    instant.setTimeInMillis(time);
                    if ((time - previousTime) >= samplingFrequency) {
                        if (isRecordable(instant)) {
                            write();
                        }
                        lowBalance = 100;
                        highBalance = -100;
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
                writer.close();
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

    private StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("# This historical data file is created by " + JBookTrader.APP_NAME).append(LINE_SEP);
        header.append("# Each line represents the order book at a particular time and contains 5 columns:").append(LINE_SEP);
        header.append("# date, time, lowBalance, highBalance, bid").append(LINE_SEP);
        header.append("# 1. date is in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time is in the HHmmss format").append(LINE_SEP);
        header.append("# 3. lowBalance is the period's lowest balance between cumulativeBidSize and cumulativeAskSize as percentage").append(LINE_SEP);
        header.append("# 4. highBalance is the period's highest balance between cumulativeBidSize and cumulativeAskSize as percentage").append(LINE_SEP);
        header.append("# 5. bid is the best (highest) bid price").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(jbtDateFormat.getTimeZone().getID()).append(LINE_SEP);
        header.append("bidAskSpread=0.25").append(LINE_SEP);
        return header;
    }


}

