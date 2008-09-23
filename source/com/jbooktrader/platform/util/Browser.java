package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;

import javax.swing.*;
import java.lang.reflect.*;


public class Browser {

    private static final String ERR_MSG = "Couldn't launch web browser";

    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);
            } else {//assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String selectedBrowser = null;
                for (String browser : browsers) {
                    String[] command = new String[]{"which", browser};
                    if (Runtime.getRuntime().exec(command).waitFor() == 0) {
                        selectedBrowser = browser;
                        break;
                    }
                }

                if (selectedBrowser == null) {
                    throw new JBookTraderException("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{selectedBrowser, url});
                }
            }
        } catch (Exception e) {
            String msg = ERR_MSG + System.getProperty("line.separator") + e.getLocalizedMessage();
            JOptionPane.showMessageDialog(null, msg);
        }
    }

}
