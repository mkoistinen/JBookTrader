package com.jbooktrader.platform.util;

import java.text.*;
import java.util.*;

/**
 * "Remaining time" estimator for long-running computational processes.
 */
public class ComputationalTimeEstimator {
    private final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private final long startTime;
    private long totalIterations;
    private final SimpleDateFormat sdf;

    public ComputationalTimeEstimator(long startTime, long totalIterations) {
        this.startTime = startTime;
        this.totalIterations = totalIterations;
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void setTotalIterations(long totalIterations) {
        this.totalIterations = totalIterations;
    }

    public String getTimeLeft(long iterationsSoFar) {
        String timeLeft = "";

        if (iterationsSoFar > 0) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            double millisPerIteration = (double) elapsedTime / iterationsSoFar;
            long remainingMillis = (long) (millisPerIteration * (totalIterations - iterationsSoFar));


            long remainingDays = remainingMillis / MILLIS_IN_DAY;
            if (remainingDays == 0) {
                timeLeft = sdf.format(new Date(remainingMillis));
            } else {
                timeLeft = "more than " + remainingDays + " days";
            }
        }

        return timeLeft;
    }
}
