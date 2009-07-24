package com.jbooktrader.platform.util;

import java.text.*;

public class NumberFormatterFactory {

    public static DecimalFormat getNumberFormatter(int maxFractionDigits) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        DecimalFormatSymbols decimalFormatSeparator = new DecimalFormatSymbols();
        decimalFormatSeparator.setDecimalSeparator('.');
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(decimalFormatSeparator);

        return decimalFormat;
    }

    public static DecimalFormat getNumberFormatter(int maxFractionDigits, boolean grouping) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        DecimalFormatSymbols decimalFormatSeparator = new DecimalFormatSymbols();
        decimalFormatSeparator.setDecimalSeparator('.');
        decimalFormat.setGroupingUsed(grouping);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(decimalFormatSeparator);

        return decimalFormat;
    }

}
