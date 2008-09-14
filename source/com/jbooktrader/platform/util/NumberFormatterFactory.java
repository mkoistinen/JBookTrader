package com.jbooktrader.platform.util;

import java.text.*;

public class NumberFormatterFactory {

    private static DecimalFormat getNumberFormatter(int minFractionDigits, int maxFractionDigits) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        DecimalFormatSymbols decimalFormatSeparator = new DecimalFormatSymbols();
        decimalFormatSeparator.setDecimalSeparator('.');
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(minFractionDigits);
        decimalFormat.setMaximumFractionDigits(maxFractionDigits);
        decimalFormat.setDecimalFormatSymbols(decimalFormatSeparator);

        return decimalFormat;
    }

    public static DecimalFormat getNumberFormatter(int maxFractionDigits) {
        return getNumberFormatter(0, maxFractionDigits);
    }
}

/* $Id$ */
