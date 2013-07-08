package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class PreferencesDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 20);
    private final PreferencesHolder prefs;
    private JTextField hostText, portText, webAccessUser, ntpTimeServer;
    private JSpinner clientIDSpin, positionSizePerStrategy, maxOpenPositions;
    private JPasswordField webAccessPasswordField;
    private JComboBox webAccessCombo;


    public PreferencesDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JButton button) {
        button.setText(prefs.get(pref));
        genericAdd(panel, pref, button);
    }


    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField);
    }

    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner, String text) {
        spinner.setValue(prefs.getInt(pref));
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(spinner);
        spinner.setMaximumSize(FIELD_DIMENSION);
        JLabel label = new JLabel(text);
        label.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(spinner);
        panel.add(label);
    }

    private void add(JPanel panel, JBTPreferences pref, JComboBox comboBox) {
        comboBox.setSelectedItem(prefs.get(pref));
        genericAdd(panel, pref, comboBox);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(comp);
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
        getContentPane().add(contentPanel, BorderLayout.NORTH);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.NORTH);

        // connection
        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        SpringUtilities.makeTwoColumnGrid(connectionTab);

        // web access
        JPanel webAccessTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Web Access", webAccessTab);
        webAccessCombo = new JComboBox<>(new String[]{"disabled", "enabled"});
        final JTextField webAccessPortField = new JTextField();

        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        add(webAccessTab, WebAccess, webAccessCombo);
        add(webAccessTab, WebAccessPort, webAccessPortField);
        add(webAccessTab, WebAccessUser, webAccessUser);
        add(webAccessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeTwoColumnGrid(webAccessTab);

        // portfolio manager
        JPanel portfolioManagerTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Portfolio Manager", portfolioManagerTab);
        positionSizePerStrategy = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        maxOpenPositions = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        add(portfolioManagerTab, PositionSizePerStrategy, positionSizePerStrategy);
        add(portfolioManagerTab, MaxOpenPositions, maxOpenPositions);
        SpringUtilities.makeTwoColumnGrid(portfolioManagerTab);

        // auto-stop
        JPanel forcedExitTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Forced Exit", forcedExitTab);
        final JSpinner maxDisconnectionPeriod = new JSpinner(new SpinnerNumberModel(720, 10, 720, 1));
        add(forcedExitTab, MaxDisconnectionPeriod, maxDisconnectionPeriod, " seconds");
        SpringUtilities.makeCompactGrid(forcedExitTab, 1, 3, 12, 8, 12, 8);

        // account
        JPanel accountTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("FA Account", accountTab);
        final JTextField subAccount = new JTextField();
        add(accountTab, SubAccount, subAccount);
        SpringUtilities.makeTwoColumnGrid(accountTab);

        // notifications
        JPanel notificationsTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Notifications", notificationsTab);
        final JTextField smtpHost = new JTextField();
        final JTextField smtpPort = new JTextField();
        final JTextField mailUser = new JTextField();
        final JPasswordField mailPassword = new JPasswordField();
        final JTextField subject = new JTextField();
        final JTextField recipients = new JTextField();
        JButton notificationTestButton = new JButton("Test");
        add(notificationsTab, SmtpHost, smtpHost);
        add(notificationsTab, SmtpPort, smtpPort);
        add(notificationsTab, SmtpUser, mailUser);
        add(notificationsTab, SmtpPassword, mailPassword);
        add(notificationsTab, Subject, subject);
        add(notificationsTab, Recipients, recipients);
        add(notificationsTab, SendTestNotification, notificationTestButton);
        notificationTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(SmtpHost, smtpHost.getText());
                    prefs.set(SmtpPort, smtpPort.getText());
                    prefs.set(SmtpUser, mailUser.getText());
                    prefs.set(SmtpPassword, new String(mailPassword.getPassword()));
                    prefs.set(Subject, subject.getText());
                    prefs.set(Recipients, recipients.getText());
                    Notifier.getInstance().send("notification test message: " + new Date(), true);
                    MessageDialog.showMessage("Test notification sent to recipients.");
                } catch (Exception ex) {
                    MessageDialog.showException(ex);
                }
            }
        });
        SpringUtilities.makeTwoColumnGrid(notificationsTab);


        JPanel timeServerTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Time Server", timeServerTab);
        ntpTimeServer = new JTextField();
        add(timeServerTab, NTPTimeServer, ntpTimeServer);
        SpringUtilities.makeTwoColumnGrid(timeServerTab);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());

                    prefs.set(WebAccess, webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortField.getText());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

                    // portfolio manager
                    prefs.set(PositionSizePerStrategy, positionSizePerStrategy.getValue().toString());
                    prefs.set(MaxOpenPositions, maxOpenPositions.getValue().toString());

                    // forced exit
                    prefs.set(MaxDisconnectionPeriod, maxDisconnectionPeriod.getValue().toString());

                    // notifications
                    prefs.set(SmtpHost, smtpHost.getText());
                    prefs.set(SmtpPort, smtpPort.getText());
                    prefs.set(SmtpUser, mailUser.getText());
                    prefs.set(SmtpPassword, new String(mailPassword.getPassword()));
                    prefs.set(Subject, subject.getText());
                    prefs.set(Recipients, recipients.getText());

                    // FA
                    prefs.set(SubAccount, subAccount.getText());


                    prefs.set(NTPTimeServer, ntpTimeServer.getText());

                    String msg = "Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.";
                    MessageDialog.showMessage(msg);

                    dispose();
                } catch (Exception ex) {
                    MessageDialog.showException(ex);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setMinimumSize(new Dimension(660, 400));
    }
}
