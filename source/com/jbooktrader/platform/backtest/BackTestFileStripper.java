package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;

import java.io.*;
import java.util.*;

/**
 * Reads a data file containing historical market depth records, and creates another file,
 * which contains only the records within the specified time frame.
 *
 * @author Eugene Kononov
 */
public class BackTestFileStripper {
    public static final int SECONDS_IN_HOUR = 3600;
    private static final int COLUMNS = 5;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private static long lineNumber;
    private final int startSecond, endSecond;

    public static void main(String[] args) throws JBookTraderException, IOException {
        if (args.length != 4) {
            System.out.println("Usage: <fileNameIn> <fileNameOut> <start hour> <end hour>");
            System.exit(1);
        }

        BackTestFileStripper btfs = new BackTestFileStripper(args[0], args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        btfs.process();
        System.out.println("Processed " + lineNumber + " lines. Completed.");
    }

    public BackTestFileStripper(String fileNameIn, String fileNameOut, int startHour, int endHour) throws JBookTraderException {
        startSecond = startHour * SECONDS_IN_HOUR;
        endSecond = endHour * SECONDS_IN_HOUR;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameIn)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileNameIn);
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileNameOut, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not write to file " + fileNameOut);
        }
    }

    public void process() throws IOException, JBookTraderException {
        String line;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber % 1000000 == 0) {
                System.out.println("Processed " + lineNumber + " lines");
            }
            boolean isComment = line.startsWith("#");
            boolean isProperty = line.contains("=");
            boolean isBlankLine = (line.trim().length() == 0);
            boolean isMarketDepthLine = !(isComment || isProperty || isBlankLine);
            if (!isMarketDepthLine || isInPeriod(line)) {
                writer.println(line);
            }
        }
        reader.close();
        writer.close();
    }

    private boolean isInPeriod(String line) throws JBookTraderException {

        StringTokenizer st = new StringTokenizer(line, ",");

        int tokenCount = st.countTokens();
        if (tokenCount != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            throw new JBookTraderException(msg);
        }

        st.nextToken(); // date
        String timeToken = st.nextToken();
        int hour = Integer.valueOf(timeToken.substring(0, 2));
        int min = Integer.valueOf(timeToken.substring(2, 4));
        int sec = Integer.valueOf(timeToken.substring(4, 6));

        int secondsOfDay = hour * SECONDS_IN_HOUR + min * 60 + sec;

        return (secondsOfDay >= startSecond && secondsOfDay <= endSecond);
    }
}

