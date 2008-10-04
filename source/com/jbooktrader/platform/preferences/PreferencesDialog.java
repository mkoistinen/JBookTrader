package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreferencesDialog extends JDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private final PreferencesHolder prefs;
    private JTextField hostText, advisorAccountText, fromText, toText, emailSubjectText, emailSMTPSHost, emailLogin, webAccessUser, SSLkeystoreLocation;
    private JSpinner clientIDSpin, heartBeatIntervalSpin, webAccessPortSpin, portSpin;
    private JPasswordField emailPasswordField, webAccessPasswordField, SSLkeystorePassword, SSLkeyPassword;
    private JComboBox accountTypeCombo, reportRecyclingCombo, emailMonitoringCombo, reportRendererCombo, webAccessCombo, webAccessHTTPSCombo;
    private JButton chooseCertificate;

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

        JPanel noticePanel = new JPanel();
        JLabel noticeLabel = new JLabel("Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.");
        noticeLabel.setForeground(Color.red);
        noticePanel.add(noticeLabel);

        getContentPane().add(noticePanel, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portSpin = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        accountTypeCombo = new JComboBox(new String[]{"Universal", "Advisor"});
        advisorAccountText = new JTextField();
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portSpin);
        add(connectionTab, ClientID, clientIDSpin);
        add(connectionTab, AccountType, accountTypeCombo);
        add(connectionTab, AdvisorAccount, advisorAccountText);
        SpringUtilities.makeCompactGrid(connectionTab, 5, 2, 12, 12, 8, 5);
        setWidth(connectionTab, clientIDSpin, 100);        
        setWidth(connectionTab, accountTypeCombo, 100);        
        setWidth(connectionTab, portSpin, 100);        

        JPanel reportingTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Reporting", reportingTab);
        reportRendererCombo = new JComboBox(ClassFinder.getReportRenderers());
        reportRecyclingCombo = new JComboBox(new String[]{"Append", "New"});
        add(reportingTab, ReportRenderer, reportRendererCombo);
        add(reportingTab, ReportRecycling, reportRecyclingCombo);
        SpringUtilities.makeCompactGrid(reportingTab, 2, 2, 12, 12, 8, 5);
        setWidth(reportingTab, reportRecyclingCombo, 100);

        JPanel remoteMonitoringTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Remote monitoring", remoteMonitoringTab);
        emailMonitoringCombo = new JComboBox(new String[]{"disabled", "enabled"});
        emailSMTPSHost = new JTextField();
        emailLogin = new JTextField();
        emailPasswordField = new JPasswordField();
        fromText = new JTextField();
        toText = new JTextField();
        emailSubjectText = new JTextField();
        heartBeatIntervalSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        add(remoteMonitoringTab, EmailMonitoring, emailMonitoringCombo);
        add(remoteMonitoringTab, SMTPSHost, emailSMTPSHost);
        add(remoteMonitoringTab, EmailLogin, emailLogin);
        add(remoteMonitoringTab, EmailPassword, emailPasswordField);
        add(remoteMonitoringTab, From, fromText);
        add(remoteMonitoringTab, To, toText);
        add(remoteMonitoringTab, EmailSubject, emailSubjectText);
        add(remoteMonitoringTab, HeartBeatInterval, heartBeatIntervalSpin);
        remoteMonitoringTab.add(new JLabel("Email test:"));
        JButton emailTestButton = new JButton("Send a test email");
        remoteMonitoringTab.add(emailTestButton);
        SpringUtilities.makeCompactGrid(remoteMonitoringTab, 9, 2, 12, 12, 8, 5);
        setWidth(remoteMonitoringTab, heartBeatIntervalSpin, 100);
        setWidth(remoteMonitoringTab, emailMonitoringCombo, 100);
        setWidth(remoteMonitoringTab, emailTestButton, 200);


        JPanel webAcessTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Web Access", webAcessTab);
        webAccessCombo = new JComboBox(new String[]{"disabled", "enabled"});
        webAccessPortSpin = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        webAccessHTTPSCombo = new JComboBox(new String[]{"disabled", "enabled"});
        SSLkeystoreLocation = new JTextField();
        SSLkeystoreLocation.setText(prefs.get(JBTPreferences.SSLkeystore));
        chooseCertificate = new JButton("...");
        chooseCertificate.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                FileChooser.fillInTextField(SSLkeystoreLocation, "Select your SSL keystore", SSLkeystoreLocation.getText());
            }
        } );
        JPanel sslPanel = new JPanel(new SpringLayout());
        sslPanel.add(SSLkeystoreLocation);
        sslPanel.add(chooseCertificate);
        SpringUtilities.makeCompactGrid(sslPanel, 1, 2, 0, 0, 2, 5);
        SSLkeystorePassword = new JPasswordField(prefs.get(JBTPreferences.SSLkeystorePassword));
        SSLkeyPassword = new JPasswordField(prefs.get(JBTPreferences.SSLkeyPassword));
        add(webAcessTab, WebAccess, webAccessCombo);
        add(webAcessTab, WebAccessPort, webAccessPortSpin);
        add(webAcessTab, WebAccessUser, webAccessUser);
        add(webAcessTab, WebAccessPassword, webAccessPasswordField);
        add(webAcessTab, WebAccessHTTPS, webAccessHTTPSCombo);
        genericAdd(webAcessTab, JBTPreferences.SSLkeystore, sslPanel);
        add(webAcessTab, JBTPreferences.SSLkeystorePassword, SSLkeystorePassword);
        add(webAcessTab, JBTPreferences.SSLkeyPassword, SSLkeyPassword);
        SpringUtilities.makeCompactGrid(webAcessTab, 8, 2, 12, 12, 8, 5);
        setWidth(webAcessTab, webAccessCombo, 100);
        setWidth(webAcessTab, webAccessPortSpin, 100);
        setWidth(webAcessTab, webAccessHTTPSCombo, 100);
        toggleHTTPS();
        
        webAccessHTTPSCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                toggleHTTPS();
            }
        });


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
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portSpin.getValue().toString());
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
                    prefs.set(HeartBeatInterval, heartBeatIntervalSpin.getValue().toString());

                    prefs.set(WebAccess, (String) webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortSpin.getValue().toString());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));
                    prefs.set(WebAccessHTTPS, (String) webAccessHTTPSCombo.getSelectedItem());
                    prefs.set(JBTPreferences.SSLkeystore, SSLkeystoreLocation.getText());
                    prefs.set(JBTPreferences.SSLkeystorePassword, new String(SSLkeyPassword.getPassword()));
                    prefs.set(JBTPreferences.SSLkeyPassword, new String(SSLkeyPassword.getPassword()));
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

    private void setWidth(JPanel p, Component c, int width) throws JBookTraderException {
        SpringLayout layout;
        try {
            layout = (SpringLayout) p.getLayout();
            SpringLayout.Constraints spinLayoutConstraint = layout.getConstraints(c);
            spinLayoutConstraint.setWidth(Spring.constant(width));
        } catch (ClassCastException exc) {
            throw new JBookTraderException("The first argument to makeGrid must use SpringLayout.");
        }
    }
    
    private void toggleHTTPS() {
        boolean enabled = webAccessHTTPSCombo.getSelectedItem().toString().equals("enabled");
        SSLkeystoreLocation.setEditable(enabled);
        SSLkeystorePassword.setEditable(enabled);
        SSLkeyPassword.setEditable(enabled);
        chooseCertificate.setEnabled(enabled);
        Color color = enabled ? Color.white : Color.gray;
        SSLkeyPassword.setBackground(color);
        SSLkeystorePassword.setBackground(color);
        SSLkeystoreLocation.setBackground(color);
    }    
}
