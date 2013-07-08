package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.classfinder.*;
import com.jbooktrader.platform.util.ui.*;
import com.toedter.calendar.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * Dialog to specify options for back testing using a historical data file.
 *
 * @author Eugene Kononov
 */
public class BackTestDialog extends JBTDialog implements ProgressListener {
    private final PreferencesHolder prefs;
    private final String strategyName;
    private JButton cancelButton, backTestButton, selectFileButton;
    private JTextField fileNameText;
    private JTextFieldDateEditor fromDateEditor, toDateEditor;
    private JCheckBox useDateRangeCheckBox;
    private JPanel fromDatePanel, toDatePanel;
    private JComboBox<String> barSizeCombo;
    private JLabel toLabel;
    private JProgressBar progressBar;
    private boolean isCancelled;

    public BackTestDialog(JFrame parent, Strategy strategy) {
        super(parent);
        strategyName = strategy.getName();
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        assignListeners();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setProgress(long count, long iterations, String text) {
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString(text + ": " + percent + "% completed");
    }

    @Override
    public void setProgress(String progressText) {
        progressBar.setValue(0);
        progressBar.setString(progressText);
    }


    public void enableProgress() {
        progressBar.setValue(0);
        progressBar.setString("Starting back test...");
        progressBar.setVisible(true);
        backTestButton.setEnabled(false);
        cancelButton.setEnabled(true);
        getRootPane().setDefaultButton(cancelButton);
    }

    private void assignListeners() {

        backTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(DataFileName, fileNameText.getText());
                    prefs.set(DateRangeStart, fromDateEditor.getText());
                    prefs.set(DateRangeEnd, toDateEditor.getText());
                    prefs.set(UseDateRange, (useDateRangeCheckBox.isSelected() ? "true" : "false"));
                    prefs.set(PerformanceChartBarSize, barSizeCombo.getSelectedItem());
                    String historicalFileName = fileNameText.getText();
                    File file = new File(historicalFileName);
                    if (!file.exists()) {
                        fileNameText.requestFocus();
                        String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
                        throw new JBookTraderException(msg);
                    }

                    Strategy strategyInstance = ClassFinder.getInstance(strategyName);
                    BackTestStrategyRunner btsr = new BackTestStrategyRunner(BackTestDialog.this, strategyInstance);
                    new Thread(btsr).start();
                } catch (Exception ex) {
                    MessageDialog.showException(ex);
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
                isCancelled = true;
                dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isCancelled = true;
            }
        });

        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(Dispatcher.getInstance().getMarketDataDir());
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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);

        setTitle("Back Test - " + strategyName);
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new SpringLayout());

        JPanel filePanel = new JPanel(new SpringLayout());
        JLabel fileNameLabel = new JLabel("Historical data file: ");
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(DataFileName));
        selectFileButton = new JButton("Browse...");
        fileNameLabel.setLabelFor(fileNameText);
        filePanel.add(fileNameLabel);
        filePanel.add(fileNameText);
        filePanel.add(selectFileButton);
        SpringUtilities.makeOneLineGrid(filePanel);

        // historical data range filter panel
        JPanel dateRangePanel = new JPanel(new SpringLayout());
        String dateFormat = "MMMMM d, yyyy";
        useDateRangeCheckBox = new JCheckBox("Use date range from:", prefs.get(UseDateRange).equals("true"));
        dateRangePanel.add(useDateRangeCheckBox);

        // From date
        fromDateEditor = new JTextFieldDateEditor();
        fromDatePanel = new JDateChooser(new Date(), dateFormat, fromDateEditor);
        fromDateEditor.setText(prefs.get(DateRangeStart));
        fromDatePanel.add(fromDateEditor);
        dateRangePanel.add(fromDatePanel);

        // To date
        toLabel = new JLabel("to:");
        toDateEditor = new JTextFieldDateEditor();
        toDatePanel = new JDateChooser(new Date(), dateFormat, toDateEditor);
        toDateEditor.setText(prefs.get(DateRangeEnd));
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
        barSizeCombo = new JComboBox<>();
        for (BarSize barSize : BarSize.values()) {
            barSizeCombo.addItem(barSize.getName());
        }
        barSizeCombo.setSelectedItem(prefs.get(PerformanceChartBarSize));
        JLabel barSizeLabel = new JLabel("Bar size for chart: ");
        barSizeLabel.setLabelFor(barSizeCombo);

        barSizePanel.add(barSizeLabel);
        barSizePanel.add(barSizeCombo);
        SpringUtilities.makeOneLineGrid(barSizePanel);


        centerPanel.add(filePanel);
        centerPanel.add(dateRangePanel);
        centerPanel.add(barSizePanel);
        SpringUtilities.makeCompactGrid(centerPanel, 3, 1, 0, 8, 0, 0);


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

        add(centerPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(backTestButton);
        setMinimumSize(new Dimension(650, 250));
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
