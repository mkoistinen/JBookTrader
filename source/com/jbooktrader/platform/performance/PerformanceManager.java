package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 * Performance manager evaluates trading strategy performance based on statistics
 * which include various factors, such as net profit, maximum drawdown, profit factor, etc.
 */
public class PerformanceManager {
    private final int multiplier;
    private final Commission commission;
    private final Strategy strategy;
    private final PerformanceChartData performanceChartData;

    private int trades, profitableTrades, previousPosition;
    private double tradeCommission, totalCommission;
    private double positionValue;
    private double totalBought, totalSold;
    private double tradeProfit, grossProfit, grossLoss, netProfit, netProfitAsOfPreviousTrade;
    private double peakNetProfit, maxDrawdown;
    private boolean isCompletedTrade;
    private double sumTradeProfit, sumTradeProfitSquared;


    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
        performanceChartData = strategy.getPerformanceChartData();
    }

    public int getTrades() {
        return trades;
    }

    public boolean getIsCompletedTrade() {
        return isCompletedTrade;
    }

    public double getPercentProfitableTrades() {
        return (trades == 0) ? 0 : (100.0d * profitableTrades / trades);
    }

    public double getAverageProfitPerTrade() {
        return (trades == 0) ? 0 : netProfit / trades;
    }

    public double getProfitFactor() {
        double profitFactor = 0;
        if (grossProfit > 0) {
            profitFactor = (grossLoss == 0) ? Double.POSITIVE_INFINITY : grossProfit / grossLoss;
        }
        return profitFactor;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getTradeProfit() {
        return tradeProfit;
    }

    public Commission getCommission() {
        return commission;
    }

    public double getTradeCommission() {
        return tradeCommission;
    }

    public double getNetProfit() {
        return totalSold - totalBought + positionValue - totalCommission;
    }

    public double getKellyCriterion() {
        int unprofitableTrades = trades - profitableTrades;
        if (profitableTrades > 0) {
            if (unprofitableTrades > 0) {
                double aveProfit = grossProfit / profitableTrades;
                double aveLoss = grossLoss / unprofitableTrades;
                double winLossRatio = aveProfit / aveLoss;
                double probabilityOfWin = profitableTrades / (double) trades;
                double kellyCriterion = probabilityOfWin - (1 - probabilityOfWin) / winLossRatio;
                kellyCriterion *= 100;
                return kellyCriterion;
            } else {
                return 100;
            }
        } else {
            return 0;
        }
    }

    public double getPerformanceIndex() {
        double pi = 0;
        if (trades > 0) {
            double stdev = Math.sqrt(trades * sumTradeProfitSquared - sumTradeProfit * sumTradeProfit) / trades;
            if (stdev != 0) {
                pi = Math.sqrt(trades) * getAverageProfitPerTrade() / stdev;
            } else {
                pi = Double.POSITIVE_INFINITY;
            }
        }

        return pi;
    }

    public void updatePositionValue(double price, int position) {
        positionValue = position * price * multiplier;
    }

    public void updateOnTrade(int quantity, double avgFillPrice, int position) {
        double tradeAmount = avgFillPrice * Math.abs(quantity) * multiplier;
        if (quantity > 0) {
            totalBought += tradeAmount;
        } else {
            totalSold += tradeAmount;
        }

        tradeCommission = commission.getCommission(Math.abs(quantity), avgFillPrice);
        totalCommission += tradeCommission;

        updatePositionValue(avgFillPrice, position);

        isCompletedTrade = (previousPosition > 0 && position < previousPosition);
        isCompletedTrade = isCompletedTrade || (previousPosition < 0 && position > previousPosition);

        if (isCompletedTrade) {
            trades++;

            netProfit = totalSold - totalBought + positionValue - totalCommission;
            peakNetProfit = Math.max(netProfit, peakNetProfit);
            maxDrawdown = Math.max(maxDrawdown, peakNetProfit - netProfit);

            tradeProfit = netProfit - netProfitAsOfPreviousTrade;
            netProfitAsOfPreviousTrade = netProfit;

            sumTradeProfit += tradeProfit;
            sumTradeProfitSquared += (tradeProfit * tradeProfit);


            if (tradeProfit >= 0) {
                profitableTrades++;
                grossProfit += tradeProfit;
            } else {
                grossLoss += (-tradeProfit);
            }
        }


        if (Dispatcher.getMode() == BackTest) {
            performanceChartData.update(new TimedValue(strategy.getTime(), netProfit));
        }

        previousPosition = position;
    }
}
