package com.jbooktrader.platform.backtest;

public interface BackTestProgressIndicator {
	public void setProgress(long count, long iterations, String text);
	public void enableProgress();
	public void showProgress(String progressText);
	public void dispose();
}

/* $Id$ */
