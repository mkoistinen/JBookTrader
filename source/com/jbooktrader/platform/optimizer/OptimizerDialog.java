package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.optimizer.ResultComparator.SortKey.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Dialog to specify options for back testing using a historical data file.
 */
public class OptimizerDialog extends JDialog {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final Dimension MIN_SIZE = new Dimension(700, 600);// minimum frame size

    private JPanel progressPanel;
    private JButton cancelButton, optimizeButton, closeButton, selectFileButton;
    private JTextField fileNameText, minTradesText;
    private JComboBox selectionCriteriaCombo, optimizationMethodCombo;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JTable resultsTable;
    private TableColumnModel paramTableColumnModel;
    private TableColumn stepColumn;

    private ParamTableModel paramTableModel;
    private ResultsTableModel resultsTableModel;
    private Strategy strategy;
    private final PreferencesHolder prefs;
    private final String strategyName;


    private OptimizerRunner optimizerRunner;

    public OptimizerDialog(JFrame parent, String strategyName) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        this.strategyName = strategyName;
        init();
        initParams();
        pack();
        assignListeners();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setProgress(long count, long iterations, String text, String label) {
        progressLabel.setText(label);
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString(text + ": " + percent + "% completed");
    }

    public void setProgress(long count, long iterations, String text) {
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString(text + percent + "%");
    }

    public void enableProgress() {
        progressLabel.setText("");
        progressBar.setValue(0);
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

        try {
            int minTrades = Integer.parseInt(minTradesText.getText());
            if (minTrades < 2) {
                minTradesText.requestFocus();
                throw new JBookTraderException("\"" + "Minimum trades" + "\"" + " must be greater or equal to 2.");
            }
        } catch (NumberFormatException nfe) {
            minTradesText.requestFocus();
            throw new JBookTraderException("\"" + "Minimum trades" + "\"" + " must be an integer.");
        }
    }

    private void setParamTableColumns() {
        int optimizationMethod = optimizationMethodCombo.getSelectedIndex();
        int columnCount = paramTableColumnModel.getColumnCount();
        if (optimizationMethod == 0) {
            if (columnCount == 3) {
                paramTableColumnModel.addColumn(stepColumn);
            }
        } else if (optimizationMethod == 1) {
            if (columnCount == 4) {
                paramTableColumnModel.removeColumn(stepColumn);
            }
        }
    }

    private void assignListeners() {

        optimizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(OptimizerFileName, fileNameText.getText());
                    prefs.set(OptimizerMinTrades, minTradesText.getText());
                    prefs.set(OptimizerSelectBy, (String) selectionCriteriaCombo.getSelectedItem());
                    prefs.set(OptimizerMethod, (String) optimizationMethodCombo.getSelectedItem());
                    setOptions();
                    StrategyParams params = paramTableModel.getParams();

                    int optimizationMethod = optimizationMethodCombo.getSelectedIndex();
                    if (optimizationMethod == 0) {
                        optimizerRunner = new BruteForceOptimizerRunner(OptimizerDialog.this, strategy, params);
                    } else if (optimizationMethod == 1) {
                        optimizerRunner = new DivideAndConquerOptimizerRunner(OptimizerDialog.this, strategy, params);
                    }
                    new Thread(optimizerRunner).start();
                } catch (Exception ex) {
                    MessageDialog.showError(OptimizerDialog.this, ex.getMessage());
                }
            }
        });

        optimizationMethodCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setParamTableColumns();
            }
        });


        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (optimizerRunner != null) {
                    closeButton.setEnabled(false);
                    optimizerRunner.cancel();
                }
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (optimizerRunner != null) {
                    cancelButton.setEnabled(false);
                    optimizerRunner.cancel();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (optimizerRunner != null) {
                    optimizerRunner.cancel();
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
        setTitle("Strategy Optimizer - " + strategyName);

        getContentPane().setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new SpringLayout());
        JPanel centerPanel = new JPanel(new SpringLayout());
        JPanel southPanel = new JPanel(new BorderLayout());

        // strategy panel and its components
        JPanel strategyPanel = new JPanel(new SpringLayout());

        JLabel fileNameLabel = new JLabel("Historical data file:", JLabel.TRAILING);
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(OptimizerFileName));
        selectFileButton = new JButton("Browse...");
        fileNameLabel.setLabelFor(fileNameText);

        strategyPanel.add(fileNameLabel);
        strategyPanel.add(fileNameText);
        strategyPanel.add(selectFileButton);

        SpringUtilities.makeCompactGrid(strategyPanel, 1, 3, 0, 0, 5, 5);

        // strategy parametrs panel and its components
        JPanel strategyParamPanel = new JPanel(new SpringLayout());

        JScrollPane paramScrollPane = new JScrollPane();

        paramTableModel = new ParamTableModel();
        JTable paramTable = new JTable(paramTableModel);
        paramTableColumnModel = paramTable.getColumnModel();
        stepColumn = paramTableColumnModel.getColumn(3);
        //tableColumnModel.removeColumn(tc);
        //tableColumnModel.addColumn(tc);

        TableCellRenderer renderer = new NumberRenderer(0);
        for (int column = 1; column < paramTableColumnModel.getColumnCount(); column++) {
            paramTableColumnModel.getColumn(column).setCellRenderer(renderer);
        }


        paramScrollPane.getViewport().add(paramTable);
        paramScrollPane.setPreferredSize(new Dimension(100, 100));

        strategyParamPanel.add(paramScrollPane);
        SpringUtilities.makeCompactGrid(strategyParamPanel, 1, 1, 0, 0, 0, 5);

        // optimization options panel and its components
        JPanel optimizationOptionsPanel = new JPanel(new SpringLayout());

        JLabel optimizationMethodLabel = new JLabel("Search method: ");
        optimizationMethodCombo = new JComboBox(new String[]{"Brute force", "Divide & Conquer"});
        String optimizerMethod = prefs.get(OptimizerMethod);
        if (optimizerMethod.length() > 0) {
            optimizationMethodCombo.setSelectedItem(optimizerMethod);
        }

        optimizationMethodLabel.setLabelFor(optimizationMethodCombo);
        optimizationOptionsPanel.add(optimizationMethodLabel);
        optimizationOptionsPanel.add(optimizationMethodCombo);

        JLabel selectionCriteriaLabel = new JLabel("Selection criteria: ");
        String[] sortFactors = new String[]{"Highest profit factor", "Highest P&L", "Lowest max DD", "Highest True Kelly"};
        selectionCriteriaCombo = new JComboBox(sortFactors);
        selectionCriteriaLabel.setLabelFor(selectionCriteriaCombo);
        optimizationOptionsPanel.add(selectionCriteriaLabel);
        optimizationOptionsPanel.add(selectionCriteriaCombo);

        String selectBy = prefs.get(OptimizerSelectBy);
        if (selectBy.length() > 0) {
            selectionCriteriaCombo.setSelectedItem(selectBy);
        }

        JLabel minTradesLabel = new JLabel("Minimum trades: ");
        minTradesText = new JTextField("50");
        minTradesText.setText(prefs.get(OptimizerMinTrades));
        minTradesLabel.setLabelFor(minTradesText);
        optimizationOptionsPanel.add(minTradesLabel);
        optimizationOptionsPanel.add(minTradesText);

        SpringUtilities.makeCompactGrid(optimizationOptionsPanel, 1, 6, 0, 0, 5, 0);

        northPanel.add(new TitledSeparator(new JLabel("Strategy parameters")));
        northPanel.add(strategyPanel);
        northPanel.add(strategyParamPanel);
        northPanel.add(new TitledSeparator(new JLabel("Optimization options")));
        northPanel.add(optimizationOptionsPanel);
        SpringUtilities.makeCompactGrid(northPanel, 5, 1, 12, 5, 12, 5);

        JScrollPane resultsScrollPane = new JScrollPane();
        centerPanel.add(new TitledSeparator(new JLabel("Optimization results")));
        centerPanel.add(resultsScrollPane);
        SpringUtilities.makeCompactGrid(centerPanel, 2, 1, 12, 5, 12, 5);

        resultsTable = new JTable();
        resultsScrollPane.getViewport().add(resultsTable);

        progressLabel = new JLabel();
        progressLabel.setForeground(Color.BLACK);
        progressBar = new JProgressBar();
        progressBar.setEnabled(false);
        progressBar.setStringPainted(true);

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

        progressPanel = new JPanel(new SpringLayout());
        progressPanel.add(progressBar);
        progressPanel.add(new JLabel(" Estimated remaining time: "));
        progressPanel.add(progressLabel);
        progressPanel.setVisible(false);
        SpringUtilities.makeCompactGrid(progressPanel, 1, 3, 12, 5, 12, 5);

        southPanel.add(progressPanel, BorderLayout.NORTH);
        southPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(optimizeButton);
        getContentPane().setPreferredSize(MIN_SIZE);
        getContentPane().setMinimumSize(getContentPane().getPreferredSize());

    }

    public void showStepColumn(boolean isVisible) {

    }

    private void initParams() {
        try {
            String className = "com.jbooktrader.strategy." + strategyName;
            strategy = ClassFinder.getInstance(className);
            paramTableModel.setParams(strategy.getParams());
            setParamTableColumns();
            resultsTableModel = new ResultsTableModel(strategy);
            resultsTable.setModel(resultsTableModel);

            // set custom column renderers
            int params = strategy.getParams().size();
            TableColumnModel resultsColumnModel = resultsTable.getColumnModel();
            for (ResultsTableModel.Column column : ResultsTableModel.Column.values()) {
                int columnIndex = column.ordinal() + params;
                TableCellRenderer renderer = column.getRenderer();
                resultsColumnModel.getColumn(columnIndex).setCellRenderer(renderer);
            }
        } catch (Exception e) {
            Dispatcher.getReporter().report(e);
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
            case 3:
                sortCriteria = TRUE_KELLY;
                break;

        }

        return sortCriteria;
    }
}
