package com.jbooktrader.platform.util;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

/**
 * Converts historical market depth data from CME format to JBT format, and writes the data to a file.
 * The created data file can be used for backtesting and optimization of trading strategies.
 * <p/>
 * Specifications:
 * http://www.cme.com/files/SDKMDPCore.pdf
 * http://www.cme.com/files/SDKRLCMessageSpecs.pdf
 */
public class CMEDataConverter {
    private static final long RECORDING_START = 9 * 60;// 9:00
    private static final long RECORDING_END = 16 * 60 + 15;// 16:15
    private static final long UPDATING_START = RECORDING_START - 60;
    private static final String INVALID_PRICE = "999999999999999999";
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final MarketBook marketBook;

    private final BackTestFileWriter backTestFileWriter;
    private final BufferedReader reader;
    private final SimpleDateFormat cmeDateFormat;
    private final String contract;
    private final Calendar instant;
    private int minutesOfDay;
    private long time, lineNumber;

    public static void main(String[] args) throws JBookTraderException {

        if (args.length != 4) {
            throw new JBookTraderException("Usage: <cmeFileName> <jbtFileName> <contract> <samplingFrequency>");
        }

        CMEDataConverter cmeDataConverter = new CMEDataConverter(args[0], args[1], args[2]);
        long samplingFrequency = Long.valueOf(args[3]);

        cmeDataConverter.convert(samplingFrequency);
    }


    private CMEDataConverter(String cmeFileName, String jbtFileName, String contract) throws JBookTraderException {

        this.contract = contract;
        marketBook = new MarketBook();

        cmeDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        cmeDateFormat.setLenient(false);
        cmeDateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

        instant = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));

        for (int level = 0; level < 5; level++) {
            marketBook.updateDepth(level, MarketDepthOperation.Insert, MarketDepthSide.Bid, 0, 0);
            marketBook.updateDepth(level, MarketDepthOperation.Insert, MarketDepthSide.Ask, 0, 0);
        }

        String outFilename = "unzipped.cme";
        try {
            OutputStream unzippedStream;
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(cmeFileName)));
            zipInputStream.getNextEntry();
            unzippedStream = new BufferedOutputStream(new FileOutputStream(outFilename, false));

            byte[] buffer = new byte[1024 * 1024];
            int length;
            System.out.println("Unzipping " + cmeFileName + " to " + outFilename);
            while ((length = zipInputStream.read(buffer)) > 0) {
                unzippedStream.write(buffer, 0, length);
            }

            unzippedStream.close();
            zipInputStream.close();
        } catch (IOException e) {
            throw new JBookTraderException("Could unzip file " + cmeFileName);
        }

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFilename)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + outFilename);
        }


        backTestFileWriter = new BackTestFileWriter(jbtFileName, TimeZone.getTimeZone("America/New_York"), false);


        System.out.println("Converting " + outFilename + " to " + jbtFileName);
    }

    private boolean isRecordable() {
        return minutesOfDay >= RECORDING_START && minutesOfDay < RECORDING_END;
    }

    private void convert(long samplingFrequency) {
        String line = null;
        int samples = 0;
        try {
            long previousTime = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    parse(line);
                    if ((time - previousTime) >= samplingFrequency) {
                        MarketSnapshot marketSnapshot = marketBook.getNextMarketSnapshot(time);
                        if (isRecordable()) {
                            backTestFileWriter.write(marketSnapshot, true);
                            samples++;
                        }
                        previousTime = time;
                    }
                } catch (Exception e) {
                    String errorMsg = "Problem parsing line #" + lineNumber + LINE_SEP;
                    System.out.println(errorMsg);
                    e.printStackTrace();
                }
            }
            System.out.println("Done: " + samples + " samples have been converted.");
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


    private void parse(String line) throws JBookTraderException {
        String dateTime = line.substring(17, 31);

        try {
            time = cmeDateFormat.parse(dateTime).getTime();
        } catch (ParseException pe) {
            throw new JBookTraderException(" Could not parse " + dateTime);
        }
        instant.setTimeInMillis(time);
        minutesOfDay = instant.get(Calendar.HOUR_OF_DAY) * 60 + instant.get(Calendar.MINUTE);
        if (!(minutesOfDay >= UPDATING_START && minutesOfDay < RECORDING_END)) {
            return;
        }

        boolean isSpecifiedContract = (line.substring(49, 69).trim().equals(contract));
        if (!isSpecifiedContract) {
            return;
        }

        String messageType = line.substring(33, 35);
        boolean isLimitOrderMessage = messageType.equals("MA");
        boolean isTradeMessage = messageType.equals("M6");
        if (!(isLimitOrderMessage || isTradeMessage)) {
            return;
        }


        if (isLimitOrderMessage) {
            int groupStart = 0;
            for (int level = 0; level < 5; level++) {
                if (line.charAt(76 + level) == '1') {
                    String bidPriceS = line.substring(groupStart + 99, groupStart + 117);
                    String askPriceS = line.substring(groupStart + 118, groupStart + 136);
                    boolean isValid = !(bidPriceS.equals(INVALID_PRICE)) && !(askPriceS.equals(INVALID_PRICE));
                    if (isValid) {
                        double bidPrice = Integer.valueOf(bidPriceS) / 100d;
                        double askPrice = Integer.valueOf(askPriceS) / 100d;
                        int bidSize = Integer.parseInt(line.substring(groupStart + 82, groupStart + 94));
                        int askSize = Integer.parseInt(line.substring(groupStart + 140, groupStart + 152));
                        marketBook.updateDepth(level, MarketDepthOperation.Update, MarketDepthSide.Bid, bidPrice, bidSize);
                        marketBook.updateDepth(level, MarketDepthOperation.Update, MarketDepthSide.Ask, askPrice, askSize);
                    }
                    groupStart += 72;
                }
            }
        }

        if (isTradeMessage) {
            int cumulativeVolume = Integer.parseInt(line.substring(116, 128));
            marketBook.updateVolume(cumulativeVolume);
        }
    }
}
