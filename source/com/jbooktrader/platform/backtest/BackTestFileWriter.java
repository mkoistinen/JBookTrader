package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Writes historical market data to a file which is used for
 * backtesting and optimization of trading strategies.
 */
public final class BackTestFileWriter {
    private final static String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private SimpleDateFormat dateFormat;
    private PrintWriter writer;
    private final DecimalFormat decimalFormat;

    public BackTestFileWriter(Strategy strategy) throws IOException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        String dir = JBookTrader.getAppPath() + FILE_SEP + "marketData";
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setDialogTitle("Save historical market depth " + strategy.getName());

        if (fileChooser.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
            dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
            dateFormat.setTimeZone(strategy.getTradingSchedule().getTimeZone());
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
        }
    }

    public BackTestFileWriter(String fileName, TimeZone timeZone) throws IOException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        fileName = JBookTrader.getAppPath() + FILE_SEP + "marketData" + FILE_SEP + fileName + ".txt";
        writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
        dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        dateFormat.setTimeZone(timeZone);
    }


    public void write(MarketDepth marketDepth, boolean flush) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(marketDepth.getTime())));
        sb.append(",");

        marketDepth.getCumulativeBidSize();
        sb.append(marketDepth.getCumulativeBidSize());
        sb.append(",");
        sb.append(marketDepth.getCumulativeAskSize());
        sb.append(",");
        sb.append(decimalFormat.format(marketDepth.getBid()));
        sb.append(",");
        sb.append(decimalFormat.format(marketDepth.getAsk()));

        writer.println(sb);
        if (flush) {
            writer.flush();
        }
    }


    public void write(MarketBook marketBook) {
        if (writer != null) {

            StringBuilder header = getHeader();
            writer.println(header);

            // make a defensive copy to prevent concurrent modification
            List<MarketDepth> marketDepths = new ArrayList<MarketDepth>();
            marketDepths.addAll(marketBook.getAll());

            for (MarketDepth marketDepth : marketDepths) {
                write(marketDepth, false);
            }
            writer.flush();
            writer.close();
            MessageDialog.showMessage(null, "Historical market depth data has been saved.");
        }
    }

    private StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("# This historical data file is created by " + JBookTrader.APP_NAME).append(LINE_SEP);
        header.append("# Each line represents the order book at a particular time and contains 6 columns:").append(LINE_SEP);
        header.append("# date, time, cumulativeBidSize, cumulativeAskSize, bid, ask").append(LINE_SEP);
        header.append("# 1. date is in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time is in the HHmmss format").append(LINE_SEP);
        header.append("# 3. cumulativeBidSize is the sum of bid sizes for all levels of market depth").append(LINE_SEP);
        header.append("# 4. cumulativeAskSize is the sum of ask sizes for all levels of market depth").append(LINE_SEP);
        header.append("# 5. bid is the best (highest) bid price").append(LINE_SEP);
        header.append("# 6. ask is the best (lowest) ask price").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }


}
