package com.jbooktrader.platform.startup;

import com.birosoft.liquid.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.util.*;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.skin.*;

import javax.swing.*;
import java.awt.*;

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

        if (lookAndFeel.equals("Seaglass")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
                        UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Lucida Grande", 0, 13));

                        
                    } catch (Exception e) {
                        MessageDialog.showError(e);
                    }
                }
            });
        } else if (lookAndFeel.equals("Liquid")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
                        LiquidLookAndFeel.setLiquidDecorations(true, "mac");
                    } catch (Exception e) {
                        MessageDialog.showError(e);
                    }
                }
            });
        } else if (lookAndFeel.equals("Nimbus")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    } catch (Exception e) {
                        MessageDialog.showError(e);
                    }
                }
            });
        } else if (lookAndFeel.equals("Native")) {
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
