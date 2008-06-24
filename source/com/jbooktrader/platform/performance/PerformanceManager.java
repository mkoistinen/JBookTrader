package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.strategy.*;

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
    private double averageProfitPerTrade, percentProfitableTrades, positionValue;
    private double totalBought, totalSold, tradeProfit, grossProfit, grossLoss, netProfit, netProfitAsOfPreviousTrade;
    private double profitFactor, peakNetProfit, maxDrawdown;
    private double sumTradeProfit, sumTradeProfitSquared, riskAdjustedProfitPerTrade;

    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
        profitAndLossHistory = new ProfitAndLossHistory();
    }

    public int getTrades() {
        return trades;
    }

    public double getPercentProfitableTrades() {
        return percentProfitableTrades;
    }

    public double getAverageProfitPerTrade() {
        return averageProfitPerTrade;
    }

    public double getProfitFactor() {
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

    public ProfitAndLossHistory getProfitAndLossHistory() {
        return profitAndLossHistory;
    }

    public double getTrueKelly() {
        // "True Kelly" is Kelly Criterion adjusted for the number of trades
        double trueKelly = 0;
        if (profitableTrades > 0 && unprofitableTrades > 0) {
            double aveProfit = grossProfit / profitableTrades;
            double aveLoss = grossLoss / unprofitableTrades;
            double winLossRatio = aveProfit / aveLoss;
            double probabilityOfWin = (double) profitableTrades / trades;
            double probabilityOfLoss = 1 - probabilityOfWin;
            double kellyCriterion = probabilityOfWin - (probabilityOfLoss / winLossRatio);
            double power = -6 + trades / 120.;
            double sigmoidFunction = 1. / (1. + Math.exp(-power));
            trueKelly = kellyCriterion * sigmoidFunction * 100;
        }

        return trueKelly;
    }

    public double getPerformanceIndex() {
        double stdev = (trades - 1) * sumTradeProfitSquared - sumTradeProfit * sumTradeProfit;
        stdev = Math.sqrt(stdev) / (trades - 1);
        return averageProfitPerTrade / stdev;
    }


    public void update(double price, int position) {
        positionValue = position * price * multiplier;
        netProfit = totalSold - totalBought + positionValue - totalCommission;
        peakNetProfit = Math.max(netProfit, peakNetProfit);
        double drawdown = peakNetProfit - netProfit;
        if (drawdown > maxDrawdown) {
            maxDrawdown = drawdown;
        }

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

        update(avgFillPrice, position);

        tradeProfit = netProfit - netProfitAsOfPreviousTrade;
        if (trades > 1) {
            sumTradeProfit += tradeProfit;
            sumTradeProfitSquared += (tradeProfit * tradeProfit);
        }

        netProfitAsOfPreviousTrade = netProfit;
        averageProfitPerTrade = netProfit / trades;

        // todo: dont count the trade if previous position is 0
        if (tradeProfit >= 0) {
            profitableTrades++;
            grossProfit += tradeProfit;
        } else {
            unprofitableTrades++;
            grossLoss += (-tradeProfit);
        }

        percentProfitableTrades = 100 * (profitableTrades / (double) trades);
        profitFactor = grossProfit / grossLoss;

        if ((Dispatcher.getMode() != Optimization)) {
            long time = strategy.getTime();
            profitAndLossHistory.add(new ProfitAndLoss(time, netProfit));
        }
    }
}
