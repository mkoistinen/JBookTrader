package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.text.*;

public class StrategyInformationDialog extends JBTDialog {
    private final Strategy strategy;

    public StrategyInformationDialog(JFrame parent, Strategy strategy) {
        super(parent);
        this.strategy = strategy;
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void add(JPanel panel, String fieldName, String fieldValue) {
        JLabel fieldNameLabel = new JLabel(fieldName + ":");
        JLabel fieldValueLabel = new JLabel(fieldValue);
        fieldValueLabel.setForeground(Color.BLACK);
        panel.add(fieldNameLabel);
        panel.add(fieldValueLabel);
    }

    private void add(JPanel panel, String fieldName, int fieldValue) {
        add(panel, fieldName, String.valueOf(fieldValue));
    }

    private void add(JPanel panel, String fieldName, double fieldValue) {
        add(panel, fieldName, String.valueOf(fieldValue));
    }

    private void makeCompactGrid(JPanel panel) {
        SpringUtilities.makeCompactGrid(panel, panel.getComponentCount() / 2, 2, 12, 12, 5, 5);
    }


    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Strategy Information - " + strategy.getName());

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel performancePanel = new JPanel(new SpringLayout());
        tabbedPane.addTab("Performance", performancePanel);

        NumberFormat nf2 = NumberFormatterFactory.getNumberFormatter(2);

        PerformanceManager pm = strategy.getPerformanceManager();
        add(performancePanel, "Position", strategy.getPositionManager().getPosition());
        add(performancePanel, "Trades", pm.getTrades());
        add(performancePanel, "% Profitable", nf2.format(pm.getPercentProfitableTrades()));
        add(performancePanel, "Average trade", nf2.format(pm.getAverageProfitPerTrade()));
        add(performancePanel, "Net Profit", nf2.format(pm.getNetProfit()));
        add(performancePanel, "Max Drawdown", nf2.format(pm.getMaxDrawdown()));
        add(performancePanel, "Profit Factor", nf2.format(pm.getProfitFactor()));
        add(performancePanel, "Kelly", nf2.format(pm.getKellyCriterion()));
        add(performancePanel, "Perf. Index", nf2.format(pm.getPerformanceIndex()));
        makeCompactGrid(performancePanel);


        JPanel securityPanel = new JPanel(new SpringLayout());
        tabbedPane.addTab("Instrument", securityPanel);
        add(securityPanel, "Symbol", strategy.getContract().m_symbol);
        add(securityPanel, "Security Type", strategy.getContract().m_secType);
        add(securityPanel, "Exchange", strategy.getContract().m_exchange);
        add(securityPanel, "Multiplier", strategy.getContract().m_multiplier);
        add(securityPanel, "Commission", strategy.getPerformanceManager().getCommission().toString());
        makeCompactGrid(securityPanel);

        JPanel parametersPanel = new JPanel(new SpringLayout());
        tabbedPane.addTab("Parameters", parametersPanel);
        StrategyParams params = strategy.getParams();
        add(parametersPanel, "Schedule", strategy.getTradingSchedule().toString());
        for (StrategyParam param : params.getAll()) {
            add(parametersPanel, param.getName(), param.getValue());
        }
        makeCompactGrid(parametersPanel);


        JPanel indicatorsPanel = new JPanel(new SpringLayout());
        tabbedPane.addTab("Indicators", indicatorsPanel);
        for (Indicator indicator : strategy.getIndicatorManager().getIndicators()) {
            add(indicatorsPanel, indicator.getName(), indicator.getValue());
        }
        makeCompactGrid(indicatorsPanel);

        getContentPane().setPreferredSize(new Dimension(450, 400));
    }


}
