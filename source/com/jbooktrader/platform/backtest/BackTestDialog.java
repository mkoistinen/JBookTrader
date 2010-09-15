package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import com.toedter.calendar.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Dialog to specify options for back testing using a historical data file.
 */
public class BackTestDialog extends JBTDialog {
    private static final Dimension MIN_SIZE = new Dimension(600, 350);// minimum frame size
    private final PreferencesHolder prefs;
    private final String strategyName;
    private final StrategyParams strategyParams;
    private JButton cancelButton, backTestButton, selectFileButton;
    private JTextField fileNameText;
    private JTextFieldDateEditor fromDateEditor, toDateEditor;
    private JCheckBox useDateRangeCheckBox;
    private JPanel fromDatePanel, toDatePanel;
    private JComboBox barSizeCombo;
    private JLabel toLabel;
    private JProgressBar progressBar;
    private BackTestStrategyRunner btsr;
    private BackTestParamTableModel backTestParamTableModel;

    public BackTestDialog(JFrame parent, Strategy strategy) {
        super(parent);
        strategyName = strategy.getName();
        strategyParams = strategy.getParams();
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        assignListeners();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    @Override
    public void dispose() {
        btsr = null;
        super.dispose();
    }


    public void setProgress(long count, long iterations) {
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString("Running back test: " + percent + "%");
    }

    public void enableProgress() {
        progressBar.setValue(0);
        progressBar.setString("Starting back test...");
        progressBar.setVisible(true);
        backTestButton.setEnabled(false);
        cancelButton.setEnabled(true);
        getRootPane().setDefaultButton(cancelButton);
    }

    public void showProgress(String progressText) {
        progressBar.setValue(0);
        progressBar.setString(progressText);
    }

    private void assignListeners() {

        backTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(BackTesterFileName, fileNameText.getText());
                    prefs.set(BackTesterTestingPeriodStart, fromDateEditor.getText());
                    prefs.set(BackTesterTestingPeriodEnd, toDateEditor.getText());
                    prefs.set(BackTesterUseDateRange, (useDateRangeCheckBox.isSelected() ? "true" : "false"));
                    prefs.set(PerformanceChartBarSize, (String) barSizeCombo.getSelectedItem());
                    String historicalFileName = fileNameText.getText();
                    File file = new File(historicalFileName);
                    if (!file.exists()) {
                        fileNameText.requestFocus();
                        String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
                        throw new JBookTraderException(msg);
                    }

                    StrategyParams newStrategyParams = backTestParamTableModel.getParams();

                    Strategy strategyInstance = ClassFinder.getInstance(strategyName, newStrategyParams);
                    btsr = new BackTestStrategyRunner(BackTestDialog.this, strategyInstance);
                    new Thread(btsr).start();
                }
                catch (Exception ex) {
                    MessageDialog.showError(ex);
                }
            }
        });

        useDateRangeCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean useDateRange = useDateRangeCheckBox.isSelected();
                fromDatePanel.setEnabled(useDateRange);
                toLabel.setEnabled(useDateRange);
                toDatePanel.setEnabled(useDateRange);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (btsr != null) {
                    btsr.cancel();
                }
                dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (btsr != null) {
                    btsr.cancel();
                }
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
        setTitle("Back Test - " + strategyName);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new SpringLayout());

        JPanel filePanel = new JPanel(new SpringLayout());
        JLabel fileNameLabel = new JLabel("Historical data file: ");
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(BackTesterFileName));
        selectFileButton = new JButton("Browse...");
        fileNameLabel.setLabelFor(fileNameText);
        filePanel.add(fileNameLabel);
        filePanel.add(fileNameText);
        filePanel.add(selectFileButton);
        SpringUtilities.makeOneLineGrid(filePanel);

        // historical data range filter panel
        JPanel dateRangePanel = new JPanel(new SpringLayout());
        String dateFormat = "MMMMM d, yyyy";
        useDateRangeCheckBox = new JCheckBox("Use date range from:", prefs.get(BackTesterUseDateRange).equals("true"));
        dateRangePanel.add(useDateRangeCheckBox);

        // From date
        fromDateEditor = new JTextFieldDateEditor();
        fromDatePanel = new JDateChooser(new Date(), dateFormat, fromDateEditor);
        fromDateEditor.setText(prefs.get(BackTesterTestingPeriodStart));
        fromDatePanel.add(fromDateEditor);
        dateRangePanel.add(fromDatePanel);

        // To date
        toLabel = new JLabel("to:");
        toDateEditor = new JTextFieldDateEditor();
        toDatePanel = new JDateChooser(new Date(), dateFormat, toDateEditor);
        toDateEditor.setText(prefs.get(BackTesterTestingPeriodEnd));
        toLabel.setLabelFor(toDatePanel);
        dateRangePanel.add(toLabel);
        toDatePanel.add(toDateEditor);
        dateRangePanel.add(toDatePanel);

        boolean useDateRange = useDateRangeCheckBox.isSelected();
        fromDatePanel.setEnabled(useDateRange);
        toLabel.setEnabled(useDateRange);
        toDatePanel.setEnabled(useDateRange);

        SpringUtilities.makeOneLineGrid(dateRangePanel);

        JPanel barSizePanel = new JPanel(new SpringLayout());
        barSizeCombo = new JComboBox();
        for (BarSize barSize : BarSize.values()) {
            barSizeCombo.addItem(barSize.getName());
        }
        barSizeCombo.setSelectedItem(prefs.get(PerformanceChartBarSize));
        JLabel barSizeLabel = new JLabel("Bar size for chart: ");
        barSizeLabel.setLabelFor(barSizeCombo);

        barSizePanel.add(barSizeLabel);
        barSizePanel.add(barSizeCombo);
        SpringUtilities.makeOneLineGrid(barSizePanel);


        northPanel.add(filePanel);
        northPanel.add(dateRangePanel);
        northPanel.add(barSizePanel);
        SpringUtilities.makeCompactGrid(northPanel, 3, 1, 0, 8, 0, 0);

        JPanel centerPanel = new JPanel(new SpringLayout());
        backTestParamTableModel = new BackTestParamTableModel();
        backTestParamTableModel.setParams(strategyParams);
        JTable paramTable = new JTable(backTestParamTableModel);
        paramTable.setShowVerticalLines(false);
        paramTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(paramTable);
        centerPanel.add(scrollPane);
        SpringUtilities.makeOneLineGrid(centerPanel);

        JPanel southPanel = new JPanel(new BorderLayout());

        JPanel progressPanel = new JPanel(new SpringLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        SpringUtilities.makeOneLineGrid(progressPanel);
        southPanel.add(progressPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        backTestButton = new JButton("Back Test");
        backTestButton.setMnemonic('B');
        cancelButton = new JButton("Cancel");
        buttonsPanel.add(backTestButton);
        buttonsPanel.add(cancelButton);
        southPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(backTestButton);
        setMinimumSize(MIN_SIZE);
        setPreferredSize(getMinimumSize());
    }

    public String getFileName() {
        return fileNameText.getText();
    }

    public BarSize getBarSize() {
        return BarSize.getBarSize((String) barSizeCombo.getSelectedItem());
    }

    public MarketSnapshotFilter getDateFilter() {
        return useDateRangeCheckBox.isSelected() ? new MarketSnapshotFilter(fromDateEditor, toDateEditor) : null;
    }
}
