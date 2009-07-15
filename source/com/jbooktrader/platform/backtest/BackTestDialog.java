package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Dialog to specify options for back testing using a historical data file.
 */
public class BackTestDialog extends JBTDialog {
    private final PreferencesHolder prefs;
    private final String strategyName;
    private JButton cancelButton, backTestButton, selectFileButton;
    private JTextField fileNameText;
    private JProgressBar progressBar;
    private BackTestStrategyRunner btsr;

    private List<StrategyParam> strategyParams;
    private List<JTextField> strategyParamValues;

    public BackTestDialog(JFrame parent, Strategy strategy) {
        super(parent);
        strategyName = strategy.getName();
        strategyParams = strategy.getParams().getAll();
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        assignListeners();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public void setProgress(final long count, final long iterations) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int percent = (int) (100 * (count / (double) iterations));
                progressBar.setValue(percent);
                progressBar.setString("Running back test: " + percent + "%");
            }
        });
    }

    public void enableProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                progressBar.setString("Starting back test...");
                progressBar.setVisible(true);
                backTestButton.setEnabled(false);
                cancelButton.setEnabled(true);
                getRootPane().setDefaultButton(cancelButton);
            }
        });
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
                    String historicalFileName = fileNameText.getText();
                    File file = new File(historicalFileName);
                    if (!file.exists()) {
                        fileNameText.requestFocus();
                        String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
                        throw new JBookTraderException(msg);
                    }

                    StrategyParams newStrategyParams = new StrategyParams();

                    // Create new strategy params with the user-specified values
                    int size = strategyParams.size();
                    for (int paramIndex = 0; paramIndex < size; paramIndex++) {
                        StrategyParam param = strategyParams.get(paramIndex);
                        int value = Integer.parseInt(strategyParamValues.get(paramIndex).getText());
                        param.setValue(value);
                        newStrategyParams.add(param);
                    }

                    Strategy strategyInstance = ClassFinder.getInstance(strategyName, newStrategyParams);
                    btsr = new BackTestStrategyRunner(BackTestDialog.this, strategyInstance);
                    new Thread(btsr).start();
                }
                catch (Exception ex) {
                    MessageDialog.showError(BackTestDialog.this, ex);
                }
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
        JLabel fileNameLabel = new JLabel("Historical data file:", JLabel.TRAILING);
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(BackTesterFileName));
        selectFileButton = new JButton("Browse...");
        fileNameLabel.setLabelFor(fileNameText);
        northPanel.add(fileNameLabel);
        northPanel.add(fileNameText);
        northPanel.add(selectFileButton);

        strategyParamValues = new ArrayList<JTextField>();

        // Set up the parameters
        for (StrategyParam strategyParam : strategyParams) {
            JLabel paramNameLabel = new JLabel(strategyParam.getName() + ":", JLabel.TRAILING);
            JTextField paramValueTextField = new JTextField(String.valueOf(strategyParam.getValue()));
            String minMax = "(" + strategyParam.getMin() + " - " + strategyParam.getMax() + ")";
            JLabel minMaxLabel = new JLabel(minMax, JLabel.LEFT);
            paramNameLabel.setLabelFor(paramValueTextField);
            strategyParamValues.add(paramValueTextField);
            northPanel.add(paramNameLabel);
            northPanel.add(paramValueTextField);
            northPanel.add(minMaxLabel);
        }

        SpringUtilities.makeCompactGrid(northPanel, strategyParams.size() + 1, 3, 10, 10, 10, 5);

        JPanel centerPanel = new JPanel(new SpringLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        centerPanel.add(progressBar);
        SpringUtilities.makeOneLineGrid(centerPanel);

        JPanel southPanel = new JPanel();
        backTestButton = new JButton("Back Test");
        backTestButton.setMnemonic('B');
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        southPanel.add(backTestButton);
        southPanel.add(cancelButton);

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(backTestButton);
        setMinimumSize(getPreferredSize());
    }


    public String getFileName() {
        return fileNameText.getText();
    }

}
