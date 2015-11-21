package com.jbooktrader.platform.optimizer;

import java.text.*;
import java.util.*;

/**
 * "Remaining time" estimator for long-running computational processes, such as strategy optimization.
 *
 * @author Eugene Kononov
 */
public class ComputationalTimeEstimator {
    private static final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final long MIN_ITERATIONS = 50000000;
    private final long startTime;
    private final SimpleDateFormat sdf;
    private long totalIterations;

    public ComputationalTimeEstimator(long startTime, long totalIterations) {
        this.startTime = startTime;
        this.totalIterations = totalIterations;
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void setTotalIterations(long totalIterations) {
        this.totalIterations = totalIterations;
    }

    public String getTimeLeft(long completedIterations) {
        String timeLeft = "";

        if (completedIterations > MIN_ITERATIONS) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            double millisPerIteration = (double) elapsedTime / completedIterations;
            long remainingMillis = (long) (millisPerIteration * (totalIterations - completedIterations));
            long remainingDays = remainingMillis / MILLIS_IN_DAY;
            if (remainingDays == 0) {
                timeLeft = sdf.format(remainingMillis);
            } else {
                timeLeft = "more than " + remainingDays + " days";
            }
        }

        return timeLeft;
    }
}
