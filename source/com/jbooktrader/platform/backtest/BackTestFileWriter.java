package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
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
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String MARKET_DATA_DIR = JBookTrader.getAppPath() + FILE_SEP + "marketData";
    private final DecimalFormat decimalFormat;
    private SimpleDateFormat dateFormat;
    private PrintWriter writer;

    public BackTestFileWriter(Strategy strategy) {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        JFileChooser fileChooser = new JFileChooser(MARKET_DATA_DIR);
        fileChooser.setDialogTitle("Save historical market depth " + strategy.getName());

        if (fileChooser.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
            dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
            dateFormat.setTimeZone(strategy.getTradingSchedule().getTimeZone());
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
            } catch (IOException ioe) {
                throw new JBookTraderException("Could not write to file " + fileName);
            }
        }
    }

    public BackTestFileWriter(String fileName, TimeZone timeZone, boolean isAutoSave) {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        File marketDataDir = new File(MARKET_DATA_DIR);
        if (!marketDataDir.exists()) {
            marketDataDir.mkdir();
        }

        String fullFileName = fileName;
        if (isAutoSave) {
            fullFileName = MARKET_DATA_DIR + FILE_SEP + fileName + ".txt";
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not write to file " + fileName);
        }
        dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        dateFormat.setTimeZone(timeZone);
    }


    public void write(MarketSnapshot marketSnapshot, boolean flush) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(new Date(marketSnapshot.getTime()))).append(",");
        sb.append(marketSnapshot.getLowBalance()).append(",");
        sb.append(marketSnapshot.getHighBalance()).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getBestBid())).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getBestAsk())).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getVolume())).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getTick())).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getTrin())).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getVix()));

        writer.println(sb);
        if (flush) {
            writer.flush();
        }
    }

    public void close() {
        writer.close();
    }

    private void writeHeader() {
        StringBuilder header = getHeader();
        writer.println(header);
    }

    public void write(MarketBook marketBook) {
        if (writer != null) {

            writeHeader();

            // make a defensive copy to prevent concurrent modification
            List<MarketSnapshot> marketSnapshots = new ArrayList<MarketSnapshot>();
            marketSnapshots.addAll(marketBook.getAll());

            for (MarketSnapshot marketSnapshot : marketSnapshots) {
                write(marketSnapshot, false);
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
        header.append("# Each line represents a 1-second snapshot of the market and contains ").append(BackTestFileReader.COLUMNS).append(" columns:").append(LINE_SEP);
        header.append("# 1. date in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time in the HHmmss format").append(LINE_SEP);
        header.append("# 3. period's lowest book balance").append(LINE_SEP);
        header.append("# 4. period's highest book balance").append(LINE_SEP);
        header.append("# 5. best bid price at the period's end").append(LINE_SEP);
        header.append("# 6. best ask price at the period's end").append(LINE_SEP);
        header.append("# 7. period's volume of traded contracts").append(LINE_SEP);
        header.append("# 8. NYSE TICK at the period's end").append(LINE_SEP);
        header.append("# 9. NYSE TRIN at the period's end").append(LINE_SEP);
        header.append("# 10. CBOE VIX at the period's end").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }
}
