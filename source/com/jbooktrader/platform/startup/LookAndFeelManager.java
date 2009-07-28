package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.util.*;
import org.jvnet.substance.*;
import org.jvnet.substance.skin.*;

import javax.swing.*;

public class LookAndFeelManager {

    public static void setSubstanceSkin(String skinName) {
        for (final SkinInfo skinInfo : SubstanceLookAndFeel.getAllSkins().values()) {
            if (skinInfo.getDisplayName().equals(skinName)) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            SubstanceLookAndFeel.setSkin(skinInfo.getClassName());
                            JFrame.setDefaultLookAndFeelDecorated(true);
                            JDialog.setDefaultLookAndFeelDecorated(true);

                        } catch (Exception e) {
                            MessageDialog.showError(e);
                        }
                    }
                });
                break;
            }
        }
    }


    public static void setFromPreferences() {
        String lookAndFeel = PreferencesHolder.getInstance().get(JBTPreferences.LookAndFeel);

        if (lookAndFeel.equals("Native")) {
            boolean onMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
            if (onMac) {
                // Menu bar at top of screen
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                // Set application name
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", JBookTrader.APP_NAME);
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {
                        MessageDialog.showError(e);
                    }
                }
            });
        } else if (lookAndFeel.equals("Substance")) {
            String skinName = PreferencesHolder.getInstance().get(JBTPreferences.Skin);
            setSubstanceSkin(skinName);
        }
    }

}
