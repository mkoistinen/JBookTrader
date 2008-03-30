package com.jbooktrader.platform.preferences;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreferencesDialog extends JDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private JTextField hostText, portText, clientIDText, advisorAccountText, reportRendererText, emailAddressText, emailSubjectText;
    private JPasswordField emailPasswordField;
    private JComboBox accountTypeCombo, reportRecyclingCombo, emailMonitoringCombo;
    private final PreferencesHolder prefs;

    public PreferencesDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(textField);
        textField.setPreferredSize(FIELD_DIMENSION);
        textField.setMaximumSize(FIELD_DIMENSION);
        textField.setText(prefs.get(pref));
        panel.add(fieldNameLabel);
        panel.add(textField);
    }

    private void add(JPanel panel, JBTPreferences pref, JComboBox comboBox) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comboBox);
        comboBox.setPreferredSize(FIELD_DIMENSION);
        comboBox.setMaximumSize(FIELD_DIMENSION);
        comboBox.setSelectedItem(prefs.get(pref));
        panel.add(fieldNameLabel);
        panel.add(comboBox);
    }


    private void init() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);


        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);


        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDText = new JTextField();
        accountTypeCombo = new JComboBox(new String[]{"Universal", "Advisor"});
        advisorAccountText = new JTextField();
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDText);
        add(connectionTab, AccountType, accountTypeCombo);
        add(connectionTab, AdvisorAccount, advisorAccountText);
        SpringUtilities.makeCompactGrid(connectionTab, 5, 2, 12, 12, 8, 5);

        JPanel reportingTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Reporting", reportingTab);
        reportRendererText = new JTextField();
        reportRecyclingCombo = new JComboBox(new String[]{"Append", "New"});
        add(reportingTab, ReportRenderer, reportRendererText);
        add(reportingTab, ReportRecycling, reportRecyclingCombo);
        SpringUtilities.makeCompactGrid(reportingTab, 2, 2, 12, 12, 8, 5);

        JPanel remoteMonitoringTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Remote monitoring", remoteMonitoringTab);
        emailMonitoringCombo = new JComboBox(new String[]{"disabled", "enabled"});
        emailAddressText = new JTextField();
        emailPasswordField = new JPasswordField();
        emailSubjectText = new JTextField();
        add(remoteMonitoringTab, EmailMonitoring, emailMonitoringCombo);
        add(remoteMonitoringTab, EmailAddress, emailAddressText);
        add(remoteMonitoringTab, EmailPassword, emailPasswordField);
        add(remoteMonitoringTab, EmailSubject, emailSubjectText);
        SpringUtilities.makeCompactGrid(remoteMonitoringTab, 4, 2, 12, 12, 8, 5);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDText.getText());
                    prefs.set(AccountType, (String) accountTypeCombo.getSelectedItem());
                    prefs.set(AdvisorAccount, advisorAccountText.getText());
                    prefs.set(ReportRenderer, reportRendererText.getText());
                    prefs.set(ReportRecycling, (String) reportRecyclingCombo.getSelectedItem());
                    prefs.set(EmailMonitoring, (String) emailMonitoringCombo.getSelectedItem());
                    prefs.set(EmailAddress, emailAddressText.getText());
                    prefs.set(EmailPassword, new String(emailPasswordField.getPassword()));
                    prefs.set(EmailSubject, emailSubjectText.getText());
                    String msg = "Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.";
                    MessageDialog.showMessage(PreferencesDialog.this, msg);
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


        setPreferredSize(new Dimension(500, 250));

    }
}
