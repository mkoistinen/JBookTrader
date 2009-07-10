package com.jbooktrader.platform.dialog;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Window;
import javax.swing.JDialog;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.MessageDialog;


public class JBTDialog extends JDialog {

	public JBTDialog() { }

	public JBTDialog(Frame owner) {
		super(owner);
	}

	public JBTDialog(Dialog owner) {
		super(owner);
	}

	public JBTDialog(Window owner) {
		super(owner);
	}

	public JBTDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	public JBTDialog(Frame owner, String title) {
		super(owner, title);
	}

	public JBTDialog(Dialog owner, boolean modal) {
		super(owner, modal);
	}

	public JBTDialog(Dialog owner, String title) {
		super(owner, title);
	}

	public JBTDialog(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
	}

	public JBTDialog(Window owner, String title) {
		super(owner, title);
	}

	public JBTDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public JBTDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public JBTDialog(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
	}

	public JBTDialog(Frame owner, String title, boolean modal,
	    GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public JBTDialog(Dialog owner, String title, boolean modal,
	    GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public JBTDialog(Window owner, String title, ModalityType modalityType,
	    GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
	}
	
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		
		// This is to prevent the dialog from being drawn under the Mac menu bar.
        try {
	        if (JBookTrader.onMac()) {
	        	Point loc = this.getLocation();
	        	if (loc.getY() < JBookTrader.MAC_MENUBAR_HEIGHT)
	        		loc.translate(0, (int) (JBookTrader.MAC_MENUBAR_HEIGHT-loc.getY()));
	        	this.setLocation(loc);
	        }
        }
        catch (Exception e)
        {
        	MessageDialog.showMessage(null, e.getMessage());
        }
	}
}
