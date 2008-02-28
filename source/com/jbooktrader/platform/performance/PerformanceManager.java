package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.strategy.Strategy;

/**
 * Performance manager evaluates trading strategy performance based on statistics which include
 * various factors, such P&L, maximum drawdown, profit factor, etc.
 */
public class PerformanceManager {
    private final int multiplier;
    private final double commissionRate;
    private final Strategy strategy;
    private final ProfitAndLossHistory profitAndLossHistory;

    private int trades, profitableTrades, unprofitableTrades;
    private double commission, totalCommission;
    private double totalBought, totalSold, profitAndLoss, totalProfitAndLoss;
    private double grossProfit, grossLoss, profitFactor, peakTotalProfitAndLoss, maxDrawdown, kellyCriterion;

    public PerformanceManager(Strategy strategy, int multiplier, double commissionRate) throws JBookTraderException {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commissionRate = commissionRate;
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
        return commission;
    }

    public double getTotalProfitAndLoss() {
        return totalProfitAndLoss;
    }

    public ProfitAndLossHistory getProfitAndLossHistory() {
        return profitAndLossHistory;
    }

    public double getKellyCriterion() {
        return kellyCriterion;
    }

    public void update(int quantity, double avgFillPrice, int position) {
        trades++;

        double tradeAmount = avgFillPrice * Math.abs(quantity) * multiplier;
        if (quantity > 0) {
            totalBought += tradeAmount;
        } else {
            totalSold += tradeAmount;
        }

        commission = Math.abs(quantity) * commissionRate;
        totalCommission += commission;


        double positionValue = position * avgFillPrice * multiplier;
        double previousProfitandLoss = totalProfitAndLoss;
        totalProfitAndLoss = totalSold - totalBought + positionValue - totalCommission;

        profitAndLoss = totalProfitAndLoss - previousProfitandLoss;

        if (profitAndLoss >= 0) {
            profitableTrades++;
            grossProfit += profitAndLoss;
        } else {
            unprofitableTrades++;
            grossLoss += profitAndLoss;
        }

        profitFactor = Math.abs(grossProfit / grossLoss);

        if (totalProfitAndLoss > peakTotalProfitAndLoss) {
            peakTotalProfitAndLoss = totalProfitAndLoss;
        }

        double drawdown = peakTotalProfitAndLoss - totalProfitAndLoss;
        if (drawdown > maxDrawdown) {
            maxDrawdown = drawdown;
        }

        if (profitableTrades > 0 && unprofitableTrades > 0) {
            double aveProfit = grossProfit / profitableTrades;
            double aveLoss = Math.abs(grossLoss) / unprofitableTrades;
            double winLossRatio = aveProfit / aveLoss;
            double probabilityOfWin = (double) profitableTrades / trades;
            double probabilityOfLoss = 1 - probabilityOfWin;
            kellyCriterion = probabilityOfWin - (probabilityOfLoss / winLossRatio);
            kellyCriterion *= 100;
            if (kellyCriterion < 0) {
                kellyCriterion = 0;
            }
        } else {
            kellyCriterion = 0;
        }

        long time = strategy.getTime();
        profitAndLossHistory.add(new ProfitAndLoss(time, totalProfitAndLoss));

    }

}
