package com.jbooktrader.platform.util;

/**
 * "Remaining time" estimator for long-running computational processes.
 */
public class ComputationalTimeEstimator {
    private final long startTime, totalIterations;

    public ComputationalTimeEstimator(long startTime, long totalIterations) {
        this.startTime = startTime;
        this.totalIterations = totalIterations;
    }

    public String getTimeLeft(long iterationsSoFar) {
        String timeLeft = "";

        if (iterationsSoFar > 0) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long millisPerIteration = elapsedTime / iterationsSoFar;
            long remainingMillis = millisPerIteration * (totalIterations - iterationsSoFar);

            long remaining = remainingMillis / 1000;

            remaining /= 60;
            int remainingMins = (int) (remaining % 60);
            int remainingHours = (int) remaining / 60;


            if (remainingHours == 0 && remainingMins == 0) {
                timeLeft = "Less than 1 minute";
            } else {
                timeLeft = remainingHours + " hour(s), " + remainingMins + " minute(s)";
            }
        }

        return timeLeft;
    }
}
