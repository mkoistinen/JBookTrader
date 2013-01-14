package com.jbooktrader.platform.backtest;

/**
 * @author Eugene Kononov
 */
public interface ProgressListener {
    void setProgress(String progressText);

    void setProgress(long count, long iterations, String progressText);

    boolean isCancelled();
}
