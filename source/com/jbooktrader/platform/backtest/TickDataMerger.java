package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;

import java.io.*;
import java.util.*;

/**
 * Reads a JBT data file, TickData file, and merges the two.
 */
public class TickDataMerger {
    public static final int COLUMNS = 4;
    private final BufferedReader readerOrig, readerTick;
    private final PrintWriter writer;

    public static void main(String[] args) throws JBookTraderException, IOException {
        if (args.length != 3) {
            System.out.println("Usage: <fileNameOrig> <fileNameTick> <fileNameMerged>");
            System.exit(1);
        }

        TickDataMerger merger = new TickDataMerger(args[0], args[1], args[2]);
        merger.process();
        System.out.println("Completed.");
    }

    public TickDataMerger(String fileNameOrig, String fileNameTick, String fileNameMerged) throws JBookTraderException {

        try {
            readerOrig = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameOrig)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileNameOrig);
        }

        try {
            readerTick = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameTick)));
        } catch (FileNotFoundException fnfe) {
            throw new JBookTraderException("Could not find file " + fileNameTick);
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileNameMerged, true)));
        } catch (IOException ioe) {
            throw new JBookTraderException("Could not write to file " + fileNameMerged);
        }
    }

    public void process() throws IOException, JBookTraderException {
        Map<String, String> volumes = new HashMap<String, String>();


        String line;
        long lineNumber = 0;
        while ((line = readerTick.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, ",");

            int tokenCount = st.countTokens();
            if (tokenCount != 4) {
                String msg = "The line should contain exactly 4 comma-separated columns.";
                throw new JBookTraderException(msg);
            }

            String dateToken = st.nextToken();
            String timeToken = st.nextToken();
            st.nextToken(); // price
            String volume = st.nextToken();
            volumes.put(dateToken + timeToken, volume);

            lineNumber++;
            if (lineNumber % 1000000 == 0) {
                System.out.println("Loaded " + lineNumber + " lines from TickData file");
            }
        }
        readerTick.close();
        System.out.println("Loaded " + lineNumber + " lines from TickData file");


        lineNumber = 0;
        while ((line = readerOrig.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, ",");

            int tokenCount = st.countTokens();
            if (tokenCount != 4) {
                String msg = "The line should contain exactly 4 comma-separated columns.";
                throw new JBookTraderException(msg);
            }

            String dateToken = st.nextToken();
            String timeToken = st.nextToken();
            String volume = volumes.get(dateToken + timeToken);
            String volumeWritten = "0";
            if (volume != null) {
                volumeWritten = volume;
            }


            writer.println(line + "," + volumeWritten);
            writer.flush();

            lineNumber++;
            if (lineNumber % 1000000 == 0) {
                System.out.println("Written " + lineNumber + " lines to the merged file.");
            }

        }
        readerOrig.close();
        writer.close();
        System.out.println("Written " + lineNumber + " lines to the merged file.");
    }

}

