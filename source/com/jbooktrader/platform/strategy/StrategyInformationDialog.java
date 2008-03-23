package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public final class StrategyInformationDialog extends JDialog implements ModelListener {
    private final Strategy strategy;
    private JLabel bidLabel, askLabel, cumulativeBidSizeLabel, cumulativeAskSizeLabel;
    private final DecimalFormat df5;

    public StrategyInformationDialog(JFrame parent, Strategy strategy) {
        super(parent);
        df5 = NumberFormatterFactory.getNumberFormatter(5);
        this.strategy = strategy;
        init();
        update();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public void modelChanged(ModelListener.Event event, Object value) {
        switch (event) {
            case STRATEGY_UPDATE:
                Strategy strategy = (Strategy) value;
                if (strategy.getName().equals(((Strategy) value).getName())) {
                    update();
                }

                break;
        }
    }

    private void update() {
        MarketBook book = strategy.getMarketBook();
        if (!book.isEmpty()) {
            MarketDepth marketDepth = book.getLastMarketDepth();
            if (marketDepth != null) {
                bidLabel.setText(df5.format(marketDepth.getBestBid()));
                askLabel.setText(df5.format(marketDepth.getBestAsk()));
                cumulativeBidSizeLabel.setText(df5.format(marketDepth.getCumulativeBidSize()));
                cumulativeAskSizeLabel.setText(df5.format(marketDepth.getCumulativeAskSize()));
            }
        }
    }


    private void add(JPanel panel, String fieldName, String fieldValue) {
        JLabel fieldNameLabel = new JLabel(fieldName + ":");
        JLabel fieldValueLabel = new JLabel(fieldValue);
        fieldValueLabel.setForeground(Color.BLACK);
        panel.add(fieldNameLabel);
        panel.add(fieldValueLabel);
    }

    private void add(JPanel panel, String fieldName, JLabel fieldValueLabel) {
        JLabel fieldNameLabel = new JLabel(fieldName + ":");
        fieldValueLabel.setForeground(Color.BLACK);
        panel.add(fieldNameLabel);
        panel.add(fieldValueLabel);
    }


    private void add(JPanel panel, String fieldName, int fieldValue) {
        add(panel, fieldName, String.valueOf(fieldValue));
    }


    private void init() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Information - " + strategy.getName());

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel performancePanel = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Performance", performancePanel);

        NumberFormat nf2 = NumberFormatterFactory.getNumberFormatter(2);

        PerformanceManager pm = strategy.getPerformanceManager();
        add(performancePanel, "Position", strategy.getPositionManager().getPosition());
        add(performancePanel, "Trades", pm.getTrades());
        add(performancePanel, "% Profitable", nf2.format(pm.getPercentProfitableTrades()));
        add(performancePanel, "Average trade", nf2.format(pm.getAverageProfitPerTrade()));
        add(performancePanel, "Net Profit", nf2.format(pm.getNetProfit()));
        add(performancePanel, "Max Drawdown", nf2.format(pm.getMaxDrawdown()));
        add(performancePanel, "Profit Factor", nf2.format(pm.getProfitFactor()));
        add(performancePanel, "True Kelly", nf2.format(pm.getTrueKelly()));
        SpringUtilities.makeCompactGrid(performancePanel, performancePanel.getComponentCount() / 2, 2, 12, 12, 5, 5);


        JPanel securityPanel = new JPanel(new SpringLayout());
        bidLabel = new JLabel();
        askLabel = new JLabel();
        cumulativeBidSizeLabel = new JLabel();
        cumulativeAskSizeLabel = new JLabel();
        tabbedPane1.addTab("Instrument", securityPanel);
        add(securityPanel, "Symbol", strategy.getContract().m_symbol);
        add(securityPanel, "Security Type", strategy.getContract().m_secType);
        add(securityPanel, "Exchange", strategy.getContract().m_exchange);
        add(securityPanel, "Multiplier", strategy.getContract().m_multiplier);
        add(securityPanel, "Commission", strategy.getPerformanceManager().getCommission().toString());
        add(securityPanel, "Bid", bidLabel);
        add(securityPanel, "Ask", askLabel);
        add(securityPanel, "Cum Bid Size", cumulativeBidSizeLabel);
        add(securityPanel, "Cum Ask Size", cumulativeAskSizeLabel);

        SpringUtilities.makeCompactGrid(securityPanel, securityPanel.getComponentCount() / 2, 2, 12, 12, 5, 5);

        JPanel parametersPanel = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Parameters", parametersPanel);
        StrategyParams params = strategy.getParams();

        for (StrategyParam param : params.getAll()) {
            add(parametersPanel, param.getName(), param.toString());
        }

        SpringUtilities.makeCompactGrid(parametersPanel, params.size(), 2, 12, 12, 5, 5);
        getContentPane().setPreferredSize(new Dimension(450, 400));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Dispatcher.removeListener(StrategyInformationDialog.this);
            }
        });


    }
}
