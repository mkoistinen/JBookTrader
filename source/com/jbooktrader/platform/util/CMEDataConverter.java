package com.jbooktrader.platform.util;

import com.jbooktrader.platform.marketdepth.MarketDepthItem;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.startup.JBookTrader;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Converts historical market depth data from CME format to JBT format, and writes the data to a file.
 * The created data file can be used for backtesting and optimization of trading strategies.
 */
public class CMEDataConverter {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final LinkedList<MarketDepthItem> bids, asks;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final SimpleDateFormat jbtDateFormat, cmeDateFormat;
    private final DecimalFormat decimalFormat;
    private final String contract;
    private long time;
    private long lineNumber;

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
        jbtDateFormat = new SimpleDateFormat("MMddyy,HH:mm:ss.SSS");
        jbtDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cmeDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        cmeDateFormat.setLenient(false);
        cmeDateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

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
            writer = new PrintWriter(new BufferedWriter(new FileWriter(jbtFileName, false)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not create file " + jbtFileName);
        }
    }


    private void write() {
        StringBuilder sb = new StringBuilder();
        sb.append(jbtDateFormat.format(new Date(time)));
        sb.append(";");// separator after date and time

        for (MarketDepthItem item : bids) {
            if (item != null) {
                sb.append(item.getSize()).append(",");
                sb.append(decimalFormat.format(item.getPrice())).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");// separator between bids and asks

        for (MarketDepthItem item : asks) {
            if (item != null) {
                sb.append(item.getSize()).append(",");
                sb.append(decimalFormat.format(item.getPrice())).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        writer.println(sb);
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
                if (lineNumber % 50000 == 0) {
                    System.out.println(lineNumber + " lines read");
                }
                parse(line);
                if ((time - previousTime) >= samplingFrequency) {
                    previousTime = time;
                    write();

                }
            }
            System.out.println("Done: " + lineNumber + " lines read and converted successfully.");
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

        // This needs to be investigated deeper. Frost suggested that the lines
        // containing line.charAt(35) == '0' can be skipped
        boolean isValidLine = (line.charAt(35) != '0');

        boolean isSpecifiedContract = (line.substring(49, 54).trim().equals(contract));

        boolean isLimitOrderMessage = line.substring(33, 35).equals("MA");
        if (isLimitOrderMessage && isValidLine && isSpecifiedContract) {

            String centiseconds = line.substring(14, 16);
            int millis = Integer.valueOf(centiseconds) * 10;
            String date = line.substring(17, 29) + line.substring(12, 14) + millis;
            time = cmeDateFormat.parse(date).getTime();

            int position = 82;
            for (int level = 0; level < 5; level++) {
                if (line.charAt(76 + level) == '1') {

                    int bidSize = Integer.parseInt(line.substring(position, position + 12));

                    position += 16;
                    //int bidPriceDecimalLocator = Integer.valueOf(line.substring(position, position + 1));
                    double bidPrice = Integer.valueOf(line.substring(position + 1, position + 19)) / 100d;

                    position += 19;
                    //int askPriceDecimalLocator = Integer.valueOf(line.substring(position, position + 1));
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
        header.append("# Each line represents the order book at a particular time and contains 3 sections,:").append(LINE_SEP);
        header.append("# separated by semicolons as follows:").append(LINE_SEP);
        header.append("# {date, time}; {bids}; {asks}").append(LINE_SEP);
        header.append("# The date is in the MMddyy format, and the time in the HH:mm:ss.SSS format").append(LINE_SEP);
        header.append("# The {bids} section has a variable number of comma-separated columns").append(LINE_SEP);
        header.append("# and contains bids (each defined by bid size and bid price), starting from the highest bid price").append(LINE_SEP);
        header.append("# The {asks} section has a variable number of comma-separated columns").append(LINE_SEP);
        header.append("# and contains asks (each defined by ask size and ask price), starting from the lowest ask price").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(jbtDateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }


}


