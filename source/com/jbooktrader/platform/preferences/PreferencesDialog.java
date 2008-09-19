package com.jbooktrader.platform.preferences;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreferencesDialog extends JDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private final PreferencesHolder prefs;
    private JTextField hostText, portText, advisorAccountText, fromText, toText, emailSubjectText, heartBeatIntervalText, emailSMTPSHost, emailLogin;
    private JSpinner clientIDSpin;
    private JPasswordField emailPasswordField;
    private JComboBox accountTypeCombo, reportRecyclingCombo, emailMonitoringCombo, reportRendererCombo;

    public PreferencesDialog(JFrame parent) throws JBookTraderException {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField);
    }
    
    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JBTPreferences pref, JComboBox comboBox) {
        comboBox.setSelectedItem(prefs.get(pref));
        genericAdd(panel, pref, comboBox);
    }
    
    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setPreferredSize(FIELD_DIMENSION);
        comp.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(comp);        
    }


    private void init() throws JBookTraderException {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        JPanel  noticePanel = new JPanel();        
        JLabel  noticeLabel = new JLabel("Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.");
        noticeLabel.setForeground(Color.red);
        noticePanel.add(noticeLabel);       

        getContentPane().add(noticePanel , BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
        accountTypeCombo = new JComboBox(new String[]{"Universal", "Advisor"});
        advisorAccountText = new JTextField();
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        add(connectionTab, AccountType, accountTypeCombo);
        add(connectionTab, AdvisorAccount, advisorAccountText);
        SpringUtilities.makeCompactGrid(connectionTab, 5, 2, 12, 12, 8, 5);

        JPanel reportingTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Reporting", reportingTab);
        reportRendererCombo = new JComboBox(ClassFinder.getReportRenderers());
        reportRecyclingCombo = new JComboBox(new String[]{"Append", "New"});
        add(reportingTab, ReportRenderer, reportRendererCombo);
        add(reportingTab, ReportRecycling, reportRecyclingCombo);
        SpringUtilities.makeCompactGrid(reportingTab, 2, 2, 12, 12, 8, 5);

        JPanel remoteMonitoringTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Remote monitoring", remoteMonitoringTab);
        emailMonitoringCombo = new JComboBox(new String[]{"disabled", "enabled"});
        emailSMTPSHost = new JTextField();
        emailLogin = new JTextField();
        emailPasswordField = new JPasswordField();
        fromText = new JTextField();
        toText = new JTextField();
        emailSubjectText = new JTextField();
        heartBeatIntervalText = new JTextField();
        add(remoteMonitoringTab, EmailMonitoring, emailMonitoringCombo);
        add(remoteMonitoringTab, SMTPSHost, emailSMTPSHost);
        add(remoteMonitoringTab, EmailLogin, emailLogin);
        add(remoteMonitoringTab, EmailPassword, emailPasswordField);
        add(remoteMonitoringTab, From, fromText);
        add(remoteMonitoringTab, To, toText);
        add(remoteMonitoringTab, EmailSubject, emailSubjectText);
        add(remoteMonitoringTab, HeartBeatInterval, heartBeatIntervalText);
        remoteMonitoringTab.add(new JLabel("Email test:"));
        JButton emailTestButton = new JButton("Send a test email");
        remoteMonitoringTab.add(emailTestButton);
        SpringUtilities.makeCompactGrid(remoteMonitoringTab, 9, 2, 12, 12, 8, 5);

        emailTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    SecureMailSender.test(emailSMTPSHost.getText(), emailLogin.getText(), new String(emailPasswordField.getPassword()),
                            fromText.getText(), toText.getText(), emailSubjectText.getText());
                    MessageDialog.showMessage(null, "Email notification sent.");
                } catch (Exception ex) {
                    MessageDialog.showMessage(null, "Email notification failed: " + ex);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    try {
                        int minutes = Integer.parseInt(heartBeatIntervalText.getText());
                        if (minutes < 1) {
                            throw new JBookTraderException(HeartBeatInterval.getName() + " must be a positive number.");
                        }
                    } catch (NumberFormatException nfe) {
                        throw new JBookTraderException(HeartBeatInterval.getName() + " must be a number.");
                    }

                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());
                    prefs.set(AccountType, (String) accountTypeCombo.getSelectedItem());
                    prefs.set(AdvisorAccount, advisorAccountText.getText());
                    prefs.set(ReportRenderer, (String) reportRendererCombo.getSelectedItem());
                    prefs.set(ReportRecycling, (String) reportRecyclingCombo.getSelectedItem());
                    prefs.set(EmailMonitoring, (String) emailMonitoringCombo.getSelectedItem());
                    prefs.set(SMTPSHost, emailSMTPSHost.getText());
                    prefs.set(EmailLogin, emailLogin.getText());
                    prefs.set(EmailPassword, new String(emailPasswordField.getPassword()));
                    prefs.set(From, fromText.getText());
                    prefs.set(To, toText.getText());
                    prefs.set(EmailSubject, emailSubjectText.getText());
                    prefs.set(HeartBeatInterval, heartBeatIntervalText.getText());
                    //String msg = "Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.";
                    //MessageDialog.showMessage(PreferencesDialog.this, msg);
                    dispose();
                } catch (Exception ex) {
                    MessageDialog.showError(PreferencesDialog.this, ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        setPreferredSize(new Dimension(500, 380));

    }
}

/* $Id$ */
