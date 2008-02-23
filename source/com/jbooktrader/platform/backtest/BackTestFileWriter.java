package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.MessageDialog;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class BackTestFileWriter {
    private final static String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private SimpleDateFormat df;
    private PrintWriter writer;

    public BackTestFileWriter(TimeZone timeZone) throws IOException {
        String dir = JBookTrader.getAppPath() + FILE_SEP + "marketData";
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setDialogTitle("Save historical market depth");

        if (fileChooser.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
            df = new SimpleDateFormat("MMddyy,HH:mm:ss.SSS");
            df.setTimeZone(timeZone);
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
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
                StringBuilder sb = new StringBuilder();
                sb.append(df.format(new Date(marketDepth.getTime()))).append(",");
                LinkedList<MarketDepthItem> allItems = new LinkedList<MarketDepthItem>();
                allItems.addAll(marketDepth.getBids());
                allItems.addAll(marketDepth.getAsks());

                for (MarketDepthItem item : allItems) {
                    sb.append(item.getSize()).append(",").append(item.getPrice()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                writer.println(sb);
            }
            writer.flush();
            writer.close();
            MessageDialog.showMessage(null, "Historical market depth data has been saved.");
        }
    }

    private StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("# This historical data file is created by " + JBookTrader.APP_NAME).append(LINE_SEP);
        header.append("# Each record represents the order book at a particular time and contains 22 columns:").append(LINE_SEP);
        header.append("# column 1: date in the MMddyy format").append(LINE_SEP);
        header.append("# column 2: time in the HH:mm:ss.SSS format").append(LINE_SEP);
        header.append("# columns 3 to 12: five bids (each defined by bid size and bid price), starting from the highest bid").append(LINE_SEP);
        header.append("# columns 13 to 22: five asks (each defined by ask size and ask price), starting from the lowest ask").append(LINE_SEP);
        header.append(LINE_SEP);
        header.append("timeZone=").append(df.getTimeZone().getID()).append(LINE_SEP);
        return header;
    }


}
