package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.c2.*;
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
    private JTextField hostText, portText, fromText, toText, emailSubjectText, webAccessUser;
    private JSpinner clientIDSpin, heartBeatIntervalSpin, webAccessPortSpin;
    private JPasswordField emailPasswordField, webAccessPasswordField, c2PasswordField;
    private JComboBox emailMonitoringCombo, webAccessCombo;
    private C2TableModel c2TableModel;

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
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        SpringUtilities.makeCompactGrid(connectionTab, 3, 2, 12, 12, 8, 5);
        setWidth(connectionTab, clientIDSpin, 45);

        JPanel remoteMonitoringTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Remote monitoring", remoteMonitoringTab);
        emailMonitoringCombo = new JComboBox(new String[]{"disabled", "enabled"});
        emailPasswordField = new JPasswordField();
        fromText = new JTextField();
        toText = new JTextField();
        emailSubjectText = new JTextField();
        heartBeatIntervalSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        add(remoteMonitoringTab, EmailMonitoring, emailMonitoringCombo);
        add(remoteMonitoringTab, EmailPassword, emailPasswordField);
        add(remoteMonitoringTab, From, fromText);
        add(remoteMonitoringTab, To, toText);
        add(remoteMonitoringTab, EmailSubject, emailSubjectText);
        add(remoteMonitoringTab, HeartBeatInterval, heartBeatIntervalSpin);
        SpringUtilities.makeCompactGrid(remoteMonitoringTab, 6, 2, 12, 12, 8, 5);
        setWidth(remoteMonitoringTab, heartBeatIntervalSpin, 65);

        JPanel webAcessTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Web Access", webAcessTab);
        webAccessCombo = new JComboBox(new String[]{"disabled", "enabled"});
        webAccessPortSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        add(webAcessTab, WebAccess, webAccessCombo);
        add(webAcessTab, WebAccessPort, webAccessPortSpin);
        add(webAcessTab, WebAccessUser, webAccessUser);
        add(webAcessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeCompactGrid(webAcessTab, 4, 2, 12, 12, 8, 5);

        JPanel c2Tab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Collective2", c2Tab);
        JPanel passwordPanel = new JPanel(new SpringLayout());
        c2PasswordField = new JPasswordField();
        add(passwordPanel, Collective2Password, c2PasswordField);
        SpringUtilities.makeCompactGrid(passwordPanel, 1, 2, 0, 8, 4, 0);
        JScrollPane scrollPane = new JScrollPane();
        c2Tab.add(passwordPanel);
        c2Tab.add(scrollPane);
        SpringUtilities.makeCompactGrid(c2Tab, 2, 1, 12, 12, 12, 12);
        c2TableModel = new C2TableModel();
        scrollPane.getViewport().add(new JTable(c2TableModel));

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());

                    prefs.set(EmailMonitoring, (String) emailMonitoringCombo.getSelectedItem());
                    prefs.set(EmailPassword, new String(emailPasswordField.getPassword()));
                    prefs.set(From, fromText.getText());
                    prefs.set(To, toText.getText());
                    prefs.set(EmailSubject, emailSubjectText.getText());
                    prefs.set(HeartBeatInterval, heartBeatIntervalSpin.getValue().toString());

                    prefs.set(WebAccess, (String) webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortSpin.getValue().toString());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

                    prefs.set(Collective2Password, new String(c2PasswordField.getPassword()));
                    prefs.set(Collective2Strategies, c2TableModel.getStrategies());

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
}
