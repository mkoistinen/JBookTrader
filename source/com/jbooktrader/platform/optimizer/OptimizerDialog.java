package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.JBookTraderException;
import static com.jbooktrader.platform.optimizer.ResultComparator.SortKey.*;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Dialog to specify options for back testing using a historical data file.
 */
public class OptimizerDialog extends JDialog {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final Dimension MIN_SIZE = new Dimension(800, 600);// minimum frame size
    private static final String optimizerDataFileNamePref = "optimizer.dataFileName";
    private static final String optimizerMinTradePref = "optimizer.minTrades";
    private static final String optimizerSortBy = "optimizer.sortBy";

    private JPanel progressPanel;
    private JButton cancelButton, optimizeButton, closeButton, selectFileButton;
    private JTextField fileNameText, minTradesText;
    private JComboBox selectionCriteriaCombo;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    private ParamTableModel paramTableModel;
    private ResultsTableModel resultsTableModel;
    private Strategy strategy;
    private final PreferencesHolder preferences;
    private final String strategyName;


    private StrategyOptimizerRunner sor;

    public OptimizerDialog(JFrame parent, String strategyName) throws JBookTraderException {
        super(parent);
        preferences = PreferencesHolder.getInstance();
        this.strategyName = strategyName;
        init();
        initParams();
        pack();
        assignListeners();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setProgress(long count, long iterations, String timeRemaining) {
        progressLabel.setText(timeRemaining);
        int percent = (int) (100 * (count / (double) iterations));

        String text = "Completed back tests: " + String.valueOf(count) + " of " + String.valueOf(iterations) + " (" + percent + "%)";
        progressBar.setValue(percent);
        progressBar.setString(text);
    }

    public void showMaxIterationsLimit(long iterations, long maxIterations) {
        String message = "The range of parameters for this optimization run requires " + iterations + " iterations." + LINE_SEP;
        message += "The maximum number of iterations is " + maxIterations + "." + LINE_SEP;
        message += "Reduce the number of parameters, reduce the range of parameters, or increase the 'Step'" + "." + LINE_SEP;
        MessageDialog.showError(this, message);
    }


    public void enableProgress() {
        progressLabel.setText("");
        progressBar.setValue(0);
        progressBar.setString("Initiating optimization run...");
        progressPanel.setVisible(true);
        optimizeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        getRootPane().setDefaultButton(cancelButton);
    }

    public void showProgress(String progressText) {
        progressBar.setValue(0);
        progressBar.setString(progressText);
    }


    public void signalCompleted() {
        progressPanel.setVisible(false);
        optimizeButton.setEnabled(true);
        cancelButton.setEnabled(false);
        getRootPane().setDefaultButton(optimizeButton);
    }


    private void setOptions() throws JBookTraderException {

        String historicalFileName = fileNameText.getText();

        File file = new File(historicalFileName);
        if (!file.exists()) {
            fileNameText.requestFocus();
            String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
            throw new JBookTraderException(msg);
        }

        int minTrades = Integer.parseInt(minTradesText.getText());
        if (minTrades < 2) {
            minTradesText.requestFocus();
            throw new JBookTraderException("\"" + "Min trades" + "\"" + " must be greater or equal to 2.");
        }

    }

    private void assignListeners() {

        optimizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    preferences.setProperty(optimizerDataFileNamePref, fileNameText.getText());
                    preferences.setProperty(optimizerMinTradePref, minTradesText.getText());
                    preferences.setProperty(optimizerSortBy, (String) selectionCriteriaCombo.getSelectedItem());
                    setOptions();
                    StrategyParams params = paramTableModel.getParams();
                    strategy.setParams(params);
                    sor = new StrategyOptimizerRunner(OptimizerDialog.this, strategy);
                    new Thread(sor).start();
                } catch (Exception ex) {
                    MessageDialog.showError(OptimizerDialog.this, ex.getMessage());
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sor != null) {
                    sor.cancel();
                }
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sor != null) {
                    sor.cancel();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (sor != null) {
                    sor.cancel();
                }
                dispose();
            }
        });

        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(JBookTrader.getAppPath());
                fileChooser.setDialogTitle("Select Historical Data File");

                String filename = getFileName();
                if (filename.length() != 0) {
                    fileChooser.setSelectedFile(new File(filename));
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileNameText.setText(file.getAbsolutePath());
                }
            }
        });
    }


    private void init() {

        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Strategy Optimizer");

        getContentPane().setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new SpringLayout());
        JPanel centerPanel = new JPanel(new SpringLayout());
        JPanel southPanel = new JPanel(new BorderLayout());

        // strategy panel and its components
        JPanel strategyPanel = new JPanel(new SpringLayout());

        JLabel fileNameLabel = new JLabel("Historical data file:", JLabel.TRAILING);
        fileNameText = new JTextField();
        selectFileButton = new JButton("Browse...");
        fileNameLabel.setLabelFor(fileNameText);

        strategyPanel.add(fileNameLabel);
        strategyPanel.add(fileNameText);
        strategyPanel.add(selectFileButton);

        SpringUtilities.makeOneLineGrid(strategyPanel, 3);

        // strategy parametrs panel and its components
        JPanel strategyParamPanel = new JPanel(new SpringLayout());

        JScrollPane paramScrollPane = new JScrollPane();

        paramTableModel = new ParamTableModel();
        JTable paramTable = new JTable(paramTableModel);
        paramTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        paramScrollPane.getViewport().add(paramTable);
        paramScrollPane.setPreferredSize(new Dimension(100, 100));

        strategyParamPanel.add(paramScrollPane);
        SpringUtilities.makeOneLineGrid(strategyParamPanel, 1);

        // optimization options panel and its components
        JPanel optimizationOptionsPanel = new JPanel(new SpringLayout());

        JLabel optimizationMethodLabel = new JLabel("Optimization method: ");
        JComboBox optimizationMethodCombo = new JComboBox(new String[] {"Brute force"});
        optimizationMethodCombo.setMaximumSize(new Dimension(150, 20));
        optimizationMethodLabel.setLabelFor(optimizationMethodCombo);
        optimizationOptionsPanel.add(optimizationMethodLabel);
        optimizationOptionsPanel.add(optimizationMethodCombo);

        JLabel selectionCriteriaLabel = new JLabel("Selection criteria: ");
        String[] sortFactors = new String[] {"Highest profit factor", "Highest P&L", "Lowest max drawdown"};
        selectionCriteriaCombo = new JComboBox(sortFactors);
        selectionCriteriaCombo.setMaximumSize(new Dimension(150, 20));
        selectionCriteriaLabel.setLabelFor(selectionCriteriaCombo);
        optimizationOptionsPanel.add(selectionCriteriaLabel);
        optimizationOptionsPanel.add(selectionCriteriaCombo);

        String lastSortBy = preferences.getProperty(optimizerSortBy);
        if (lastSortBy.length() > 0) {
            selectionCriteriaCombo.setSelectedItem(lastSortBy);
        }

        JLabel minTradesLabel = new JLabel("Minimum trades for strategy inclusion: ");
        minTradesText = new JTextField("50");
        minTradesLabel.setLabelFor(minTradesText);
        optimizationOptionsPanel.add(minTradesLabel);
        optimizationOptionsPanel.add(minTradesText);

        SpringUtilities.makeOneLineGrid(optimizationOptionsPanel, 6);

        northPanel.add(new TitledSeparator(new JLabel("Strategy definition & parameters")));
        northPanel.add(strategyPanel);
        northPanel.add(strategyParamPanel);
        northPanel.add(new TitledSeparator(new JLabel("Optimization options")));
        northPanel.add(optimizationOptionsPanel);
        northPanel.add(new TitledSeparator(new JLabel("Optimization results")));
        SpringUtilities.makeCompactGrid(northPanel, 6, 1, 5, 5, 5, 8);

        JScrollPane resultsScrollPane = new JScrollPane();

        centerPanel.add(resultsScrollPane);
        SpringUtilities.makeCompactGrid(centerPanel, 1, 1, 12, 5, 8, 8);

        resultsTableModel = new ResultsTableModel();
        JTable resultsTable = new JTable(resultsTableModel);
        resultsScrollPane.getViewport().add(resultsTable);

        progressLabel = new JLabel();
        progressLabel.setForeground(Color.BLACK);
        progressBar = new JProgressBar();
        progressBar.setEnabled(false);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 20));

        optimizeButton = new JButton("Optimize");
        optimizeButton.setMnemonic('O');
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.setEnabled(false);
        closeButton = new JButton("Close");
        closeButton.setMnemonic('S');

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(optimizeButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(closeButton);

        progressPanel = new JPanel();
        progressPanel.add(progressBar);
        progressPanel.add(new JLabel(" Estimated remaining time: "));
        progressPanel.add(progressLabel);
        progressPanel.setVisible(false);

        southPanel.add(progressPanel, BorderLayout.NORTH);
        southPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(optimizeButton);
        getContentPane().setPreferredSize(MIN_SIZE);
        getContentPane().setMinimumSize(getContentPane().getPreferredSize());
        fileNameText.setText(preferences.getProperty(optimizerDataFileNamePref));
        minTradesText.setText(preferences.getProperty(optimizerMinTradePref));
    }

    private void initParams() {
        try {
            String className = "com.jbooktrader.strategy." + strategyName;
            Class<?> clazz = Class.forName(className);
            Constructor<?> ct = clazz.getConstructor(StrategyParams.class);
            strategy = (Strategy) ct.newInstance(new StrategyParams());
            strategy.setParams(strategy.initParams());
            paramTableModel.setParams(strategy.getParams());
            resultsTableModel.updateSchema(strategy);
        } catch (Exception e) {
            MessageDialog.showError(this, e.getMessage());
        }
    }

    public void setResults(List<Result> results) {
        resultsTableModel.setResults(results);
    }


    public String getFileName() {
        return fileNameText.getText();
    }

    public int getMinTrades() {
        return Integer.parseInt(minTradesText.getText());
    }


    public ResultComparator.SortKey getSortCriteria() {
        ResultComparator.SortKey sortCriteria = PROFIT_FACTOR;
        int selectedIndex = selectionCriteriaCombo.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                sortCriteria = PROFIT_FACTOR;
                break;
            case 1:
                sortCriteria = TOTAL_PROFIT;
                break;
            case 2:
                sortCriteria = DRAWDOWN;
                break;
        }

        return sortCriteria;
    }
}
