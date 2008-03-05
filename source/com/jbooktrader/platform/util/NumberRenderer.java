package com.jbooktrader.platform.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.*;


public class NumberRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat df;

    public NumberRenderer(int precision) {
        df = (DecimalFormat) NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(precision);
        df.setGroupingUsed(false);
        setHorizontalAlignment(RIGHT);
    }

    @Override
    public void setValue(Object value) {
        String text = "";
        if (value != null) {
            boolean validNumber;
            boolean isDouble = (value.getClass() == Double.class);
            if (isDouble) {
                Double d = (Double) value;
                validNumber = !(Double.isInfinite(d) || Double.isNaN(d));
            } else {
                validNumber = true;
            }

            if (validNumber) {
                text = df.format(value);
            }
        }

        setText(text);
    }
}
