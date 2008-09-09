package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 */
public final class RawDataFileWriter {
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String MARKET_DATA_DIR = JBookTrader.getAppPath() + FILE_SEP + "marketData";
    private final TimeZone timeZone;
    private static long counter;
    private SimpleDateFormat dateFormat;
    private PrintWriter writer;

    public RawDataFileWriter(String fileName, TimeZone timeZone) throws JBookTraderException {
        this.timeZone = timeZone;
        File marketDataDir = new File(MARKET_DATA_DIR);
        if (!marketDataDir.exists()) {
            marketDataDir.mkdir();
        }

        String fullFileName = MARKET_DATA_DIR + FILE_SEP + fileName + ".txt";

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not write to file " + fullFileName);
        }
        dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        dateFormat.setTimeZone(timeZone);
        writeHeader();
    }


    public void write(String s) {
        String msg = dateFormat.format(System.currentTimeMillis()) + "," + s;
        writer.println(msg);
        counter++;
        if (counter % 1000 == 0) {
            writer.flush();
        }
    }

    private void writeHeader() {
        StringBuilder header = getHeader();
        writer.println(header);
    }

    private StringBuilder getHeader() {
        SimpleDateFormat recordingDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        recordingDateFormat.setTimeZone(timeZone);

        StringBuilder header = new StringBuilder();
        String appInfo = JBookTrader.APP_NAME + ", version " + JBookTrader.VERSION;
        header.append("# This raw historical data file was created by ").append(appInfo).append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(dateFormat.getTimeZone().getID()).append(LINE_SEP);
        header.append("recordedDate=").append(recordingDateFormat.format(System.currentTimeMillis())).append(LINE_SEP);
        return header;
    }
}
