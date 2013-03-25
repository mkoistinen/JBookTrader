package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class PreferencesDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 25);
    private final PreferencesHolder prefs;
    private JTextField hostText, portText, webAccessUser, ntpTimeServer;
    private JSpinner clientIDSpin, webAccessPortSpin;
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

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        SpringUtilities.makeCompactGrid(connectionTab, 3, 2, 12, 12, 8, 8);

        JPanel webAcessTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Web Access", webAcessTab);
        webAccessCombo = new JComboBox<String>(new String[]{"disabled", "enabled"});
        webAccessPortSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        add(webAcessTab, WebAccess, webAccessCombo);
        add(webAcessTab, WebAccessPort, webAccessPortSpin);
        add(webAcessTab, WebAccessUser, webAccessUser);
        add(webAcessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeCompactGrid(webAcessTab, 4, 2, 12, 12, 8, 8);

        JPanel timeServerTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Time Server", timeServerTab);
        ntpTimeServer = new JTextField();
        add(timeServerTab, NTPTimeServer, ntpTimeServer);
        SpringUtilities.makeCompactGrid(timeServerTab, 1, 2, 12, 12, 8, 8);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());

                    prefs.set(WebAccess, (String) webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortSpin.getValue().toString());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

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


        setPreferredSize(new Dimension(600, 380));
    }
}
