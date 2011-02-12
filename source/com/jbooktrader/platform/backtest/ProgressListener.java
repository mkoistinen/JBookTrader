package com.jbooktrader.platform.backtest;

public interface ProgressListener {
    public void setProgress(String progressText);

    public void setProgress(long count, long iterations, String text, String label);

    public boolean isCancelled();
}
