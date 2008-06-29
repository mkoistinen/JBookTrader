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
    private static final String MARKET_DATA_DIR = JBookTrader.getAppPath() + FILE_SEP + "marketData";
    private SimpleDateFormat dateFormat;
    private PrintWriter writer;
    private final DecimalFormat decimalFormat;

    public BackTestFileWriter(Strategy strategy) throws IOException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        JFileChooser fileChooser = new JFileChooser(MARKET_DATA_DIR);
        fileChooser.setDialogTitle("Save historical market depth " + strategy.getName());

        if (fileChooser.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
            dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
            dateFormat.setTimeZone(strategy.getTradingSchedule().getTimeZone());
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
        }
    }

    public BackTestFileWriter(String fileName, TimeZone timeZone, boolean isAutoSave) throws IOException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        File marketDataDir = new File(MARKET_DATA_DIR);
        if (!marketDataDir.exists()) {
            marketDataDir.mkdir();
        }

        String fullFileName = fileName;
        if (isAutoSave) {
            fullFileName = MARKET_DATA_DIR + FILE_SEP + fileName + ".txt";
        }
        writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        dateFormat.setTimeZone(timeZone);
    }


    public void write(MarketDepth marketDepth, boolean flush) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(marketDepth.getTime()))).append(",");
        sb.append(marketDepth.getOpenBalance()).append(",");
        sb.append(marketDepth.getHighBalance()).append(",");
        sb.append(marketDepth.getLowBalance()).append(",");
        sb.append(marketDepth.getCloseBalance()).append(",");
        sb.append(decimalFormat.format(marketDepth.getLowPrice())).append(",");
        sb.append(decimalFormat.format(marketDepth.getHighPrice()));

        writer.println(sb);
        if (flush) {
            writer.flush();
        }
    }

    public void close() {
        writer.close();
    }

    public void writeHeader() {
        StringBuilder header = getHeader();
        writer.println(header);
    }

    public void write(MarketBook marketBook) {
        if (writer != null) {

            writeHeader();

            // make a defensive copy to prevent concurrent modification
            List<MarketDepth> marketDepths = new ArrayList<MarketDepth>();
            marketDepths.addAll(marketBook.getAll());

            for (MarketDepth marketDepth : marketDepths) {
                write(marketDepth, false);
            }
            writer.flush();
            close();
            MessageDialog.showMessage(null, "Historical market depth data has been saved.");
        }
    }

    private StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        String appInfo = JBookTrader.APP_NAME + ", version " + JBookTrader.VERSION;
        header.append("# This historical data file was created by ").append(appInfo).append(LINE_SEP);
        header.append("# Each line represents the order book at a particular time and contains 8 columns:").append(LINE_SEP);
        header.append("# date, time, openBalance, highBalance, lowBalance, closeBalance, lowPrice, highPrice").append(LINE_SEP);
        header.append("# 1. date is in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time is in the HHmmss format").append(LINE_SEP);
        header.append("# 3. openBalance is the period's opening book balance").append(LINE_SEP);
        header.append("# 4. highBalance is the period's highest book balance").append(LINE_SEP);
        header.append("# 5. lowBalance is the period's lowest book balance").append(LINE_SEP);
        header.append("# 6. closeBalance is the period's close book balance").append(LINE_SEP);
        header.append("# 7. lowPrice is the period's lowest price").append(LINE_SEP);
        header.append("# 8. highPrice is the period's highest price").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }
}
