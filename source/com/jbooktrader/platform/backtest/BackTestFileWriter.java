package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.format.*;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * Writes historical market data to a file which is used for
 * back testing and optimization of trading strategies.
 *
 * @author Eugene Kononov
 */
public class BackTestFileWriter {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final DecimalFormat decimalFormat;
    private final SimpleDateFormat dateFormat;
    private PrintWriter writer;

    public BackTestFileWriter(String strategyName, TimeZone timeZone) throws JBookTraderException {
        decimalFormat = NumberFormatterFactory.getNumberFormatter(5);
        dateFormat = new SimpleDateFormat("MMddyy,HHmmss");
        dateFormat.setTimeZone(timeZone);

        String fileName = Dispatcher.getInstance().getMarketDataDir() + strategyName + ".txt";
        try {
            boolean fileExisted = new File(fileName).exists();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            if (!fileExisted) {
                StringBuilder header = getHeader();
                writer.println(header);
            }
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not write to file " + fileName);
        }
    }


    public void write(MarketSnapshot marketSnapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(marketSnapshot.getTime())).append(",");
        sb.append(marketSnapshot.getBalance()).append(",");
        sb.append(decimalFormat.format(marketSnapshot.getPrice())).append(",");
        sb.append(marketSnapshot.getVolume());

        writer.println(sb);
        writer.flush();
    }

    private StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("# This historical data file was created by ").append(JBookTrader.APP_NAME).append(LINE_SEP);
        header.append("# Each line represents a 1-second snapshot of the market and contains ").append(BackTestFileReader.COLUMNS).append(" columns:").append(LINE_SEP);
        header.append("# 1. date in the MMddyy format").append(LINE_SEP);
        header.append("# 2. time in the HHmmss format").append(LINE_SEP);
        header.append("# 3. book balance").append(LINE_SEP);
        header.append("# 4. price").append(LINE_SEP);
        header.append("# 5. volume").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }
}
