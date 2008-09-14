package com.jbooktrader.platform.optimizer;

import java.util.List;

public interface OptimizerProgressIndicator {

    void showProgress(String string);

    void setResults(List<OptimizationResult> optimizationResults);

    void setProgress(long counter, long totalSteps, String text, String remainingTime);

    void enableProgress();

    void signalCompleted();

    void showMessage(String string);

    void showError(String string);

}
