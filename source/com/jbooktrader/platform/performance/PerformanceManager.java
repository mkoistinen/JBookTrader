package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.commission.Commission;
//import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.strategy.Strategy;

/**
 * Performance manager evaluates trading strategy performance based on statistics which include
 * various factors, such P&L, maximum drawdown, profit factor, etc.
 */
public class PerformanceManager {
    private final int multiplier;
    private final Commission commission;
    private final Strategy strategy;
    private final ProfitAndLossHistory profitAndLossHistory;

    private int trades, profitableTrades, unprofitableTrades;
    private double tradeCommission, totalCommission;
    private double totalBought, totalSold, profitAndLoss, grossProfit, grossLoss, totalProfitAndLoss;
    private double profitFactor, peakTotalProfitAndLoss, maxDrawdown, trueKelly;

    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
        profitAndLossHistory = new ProfitAndLossHistory();
    }

    public int getTrades() {
        return trades;
    }

    public double getProfitFactor() {
        return profitFactor;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getProfitAndLoss() {
        return profitAndLoss;
    }

    public double getCommission() {
        return tradeCommission;
    }

    public double getTotalProfitAndLoss() {
        return totalProfitAndLoss;
    }

    public ProfitAndLossHistory getProfitAndLossHistory() {
        return profitAndLossHistory;
    }

    public double getTrueKelly() {
        return trueKelly;
    }

    public void update(int quantity, double avgFillPrice, int position) {
        trades++;

        double tradeAmount = avgFillPrice * Math.abs(quantity) * multiplier;
        if (quantity > 0) {
            totalBought += tradeAmount;
        } else {
            totalSold += tradeAmount;
        }

        tradeCommission = commission.getCommission(Math.abs(quantity), avgFillPrice);
        totalCommission += tradeCommission;


        double positionValue = position * avgFillPrice * multiplier;
        double previousProfitandLoss = totalProfitAndLoss;
        totalProfitAndLoss = totalSold - totalBought + positionValue - totalCommission;

        profitAndLoss = totalProfitAndLoss - previousProfitandLoss;

        if (profitAndLoss >= 0) {
            profitableTrades++;
            grossProfit += profitAndLoss;
        } else {
            unprofitableTrades++;
            grossLoss += (-profitAndLoss);
        }

        profitFactor = grossProfit / grossLoss;

        if (totalProfitAndLoss > peakTotalProfitAndLoss) {
            peakTotalProfitAndLoss = totalProfitAndLoss;
        }

        double drawdown = peakTotalProfitAndLoss - totalProfitAndLoss;
        if (drawdown > maxDrawdown) {
            maxDrawdown = drawdown;
        }

        // Calculate "True Kelly", which is Kelly Criterion adjusted
        // for the number of trades and the confidence interval
        if (profitableTrades > 0 && unprofitableTrades > 0) {
            double aveProfit = grossProfit / profitableTrades;
            double aveLoss = grossLoss / unprofitableTrades;
            double winLossRatio = aveProfit / aveLoss;
            double probabilityOfWin = (double) profitableTrades / trades;
            double probabilityOfLoss = 1 - probabilityOfWin;
            double kellyCriterion = probabilityOfWin - (probabilityOfLoss / winLossRatio);
            double standardError = 1 / Math.sqrt(trades);
            // 1.64485 corresponds to the 90% confidence interval
            trueKelly = (kellyCriterion - 1.64485 * standardError) * 100;
            if (trueKelly < 0) {
                trueKelly = 0;
            }
        }

        long time = strategy.getTime();
        profitAndLossHistory.add(new ProfitAndLoss(time, totalProfitAndLoss));
    }
}
