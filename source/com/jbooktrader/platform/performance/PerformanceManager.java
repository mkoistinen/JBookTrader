package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 * Performance manager evaluates trading strategy performance based on statistics which include
 * various factors, such P&L, maximum drawdown, profit factor, etc.
 */
public class PerformanceManager {
    private final int multiplier;
    private final Commission commission;
    private final Strategy strategy;
    private final NetProfitHistory netProfitHistory;

    private int trades, profitableTrades, previousPosition;
    private long exposureStart, totalExposure;
    private double tradeCommission, totalCommission;
    private double positionValue;
    private double totalBought, totalSold, tradeProfit, grossProfit, grossLoss, netProfit, netProfitAsOfPreviousTrade;
    private double peakNetProfit, maxDrawdown;
    private double sumTradeProfit, sumTradeProfitSquared;
    private boolean isCompletedTrade;

    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
        netProfitHistory = new NetProfitHistory();
    }

    public int getTrades() {
        return trades;
    }

    public boolean getIsCompletedTrade() {
        return isCompletedTrade;
    }

    public double getPercentProfitableTrades() {
        return (100d * profitableTrades / trades);
    }

    public double getAverageProfitPerTrade() {
        return netProfit / trades;
    }

    public double getProfitFactor() {
        return grossProfit / grossLoss;
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

    public NetProfitHistory getProfitAndLossHistory() {
        return netProfitHistory;
    }

    public double getKellyCriterion() {
        int unprofitableTrades = trades - profitableTrades;
        double kellyCriterion = 0;
        if (profitableTrades > 0 && unprofitableTrades > 0) {
            double aveProfit = grossProfit / profitableTrades;
            double aveLoss = grossLoss / unprofitableTrades;
            double winLossRatio = aveProfit / aveLoss;
            double probabilityOfWin = profitableTrades / (double) trades;
            double probabilityOfLoss = 1 - probabilityOfWin;
            kellyCriterion = 100 * (probabilityOfWin - (probabilityOfLoss / winLossRatio));
        }

        return kellyCriterion;
    }

    public double getPerformanceIndex() {
        double stdev = (trades - 1) * sumTradeProfitSquared - sumTradeProfit * sumTradeProfit;
        if (stdev != 0) {
            stdev = Math.sqrt(stdev) / (trades - 1);
            return getAverageProfitPerTrade() / stdev;
        } else {
            return 0;
        }
    }

    public double getExposure() {
        int size = strategy.getMarketBook().size();
        return 100 * totalExposure / (double) size;
    }

    public void update(double price, int position) {
        positionValue = position * price * multiplier;
        netProfit = totalSold - totalBought + positionValue - totalCommission;
        peakNetProfit = Math.max(netProfit, peakNetProfit);
        maxDrawdown = Math.max(maxDrawdown, peakNetProfit - netProfit);
    }

    public void update(int quantity, double avgFillPrice, int position) {

        double tradeAmount = avgFillPrice * Math.abs(quantity) * multiplier;
        if (quantity > 0) {
            totalBought += tradeAmount;
        } else {
            totalSold += tradeAmount;
        }

        tradeCommission = commission.getCommission(Math.abs(quantity), avgFillPrice);
        totalCommission += tradeCommission;

        update(avgFillPrice, position);


        isCompletedTrade = (previousPosition != 0);
        if (isCompletedTrade) {
            trades++;

            tradeProfit = netProfit - netProfitAsOfPreviousTrade;
            sumTradeProfit += tradeProfit;
            sumTradeProfitSquared += (tradeProfit * tradeProfit);
            netProfitAsOfPreviousTrade = netProfit;

            if (tradeProfit >= 0) {
                profitableTrades++;
                grossProfit += tradeProfit;
            } else {
                grossLoss += (-tradeProfit);
            }
        }

        if ((Dispatcher.getMode() != Optimization)) {
            long time = strategy.getTime();
            netProfitHistory.add(new TimedValue(time, netProfit));
        }

        int index = strategy.getMarketBook().size();
        if (previousPosition == 0) {
            exposureStart = index;
        }

        if (position == 0) {
            totalExposure += (index - exposureStart);
        }

        previousPosition = position;
    }
}
