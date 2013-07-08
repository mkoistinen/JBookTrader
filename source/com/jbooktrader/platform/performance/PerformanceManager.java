package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Performance manager evaluates trading strategy performance based on statistics
 * which include various factors, such as net profit, maximum draw down, profit factor, etc.
 *
 * @author Eugene Kononov
 */
public class PerformanceManager {
    private final int multiplier;
    private final Commission commission;
    private final Strategy strategy;
    private PerformanceChartData performanceChartData;
    private int trades, profitableTrades, previousPosition;
    private double tradeCommission, totalCommission;
    private double positionValue;
    private double totalBought, totalSold;
    private double tradeProfit, grossProfit, grossLoss, netProfit, netProfitAsOfPreviousTrade;
    private double peakNetProfit, maxDrawdown;
    private boolean isCompletedTrade;
    private double sumTradeProfit, sumTradeProfitSquared;
    private long timeInMarketStart, timeInMarket;
    private long longTrades, shortTrades;
    private double maxSingleLoss;

    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
    }

    public void createPerformanceChartData(BarSize barSize, List<Indicator> indicators) {
        performanceChartData = new PerformanceChartData(barSize, indicators, strategy.getName());
    }

    public PerformanceChartData getPerformanceChartData() {
        return performanceChartData;
    }

    public int getTrades() {
        return trades;
    }

    public double getBias() {
        if (trades == 0) {
            return 0;
        }
        return 100 * (longTrades - shortTrades) / (double) trades;
    }

    public double getAveDuration() {
        if (trades == 0) {
            return 0;
        }
        // average number of minutes per trade
        return ((double) timeInMarket / trades) / 60000;
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
            }
            return 100;
        }
        return 0;
    }

    public double getCPI() {
        double performanceIndex = getPerformanceIndex();
        double kellyCriterion = getKellyCriterion();
        double aveDuration = getAveDuration();

        if (aveDuration != 0 && performanceIndex > 0 && kellyCriterion > 0) {
            double netProfit = getNetProfit();
            double cpi = performanceIndex * (kellyCriterion / 100) * (netProfit / 1000);
            cpi /= Math.sqrt(Math.sqrt(aveDuration));
            return cpi;
        } else {
            return 0;
        }

    }

    public double getPerformanceIndex() {
        double pi = 0;
        if (trades > 0) {
            double stDev = Math.sqrt(trades * sumTradeProfitSquared - sumTradeProfit * sumTradeProfit) / trades;
            pi = (stDev == 0) ? Double.POSITIVE_INFINITY : Math.sqrt(trades) * getAverageProfitPerTrade() / stDev;
        }

        return pi;
    }

    public double getMaxSingleLoss() {
        return Math.abs(maxSingleLoss);
    }

    public void updatePositionValue(double price, int position) {
        positionValue = position * price * multiplier;
    }

    public void updateOnTrade(int quantity, double avgFillPrice, int position) {
        long snapshotTime = strategy.getMarketBook().getSnapshot().getTime();
        if (position != 0) {
            if (timeInMarketStart == 0) {
                timeInMarketStart = snapshotTime;
            }
        } else {
            timeInMarket += (snapshotTime - timeInMarketStart);
            timeInMarketStart = 0;
        }

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
            if (previousPosition > 0) {
                longTrades++;
            } else if (previousPosition < 0) {
                shortTrades++;
            }


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

            if (tradeProfit < maxSingleLoss) {
                maxSingleLoss = tradeProfit;
            }
        }


        if (Dispatcher.getInstance().getMode() == Mode.BackTest) {
            if (isCompletedTrade) {
                performanceChartData.update(new TimedValue(snapshotTime, tradeProfit));
            }

        }

        previousPosition = position;
    }
}
