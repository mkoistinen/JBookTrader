package com.jbooktrader.platform.c2;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

public class C2TableModel extends TableDataModel {

    public C2TableModel() {
        String[] aboutSchema = {"Strategy", "C2 System ID", "Enabled"};
        setSchema(aboutSchema);
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        Map<String, C2Value> c2Strategies = new HashMap<String, C2Value>();
        String c2Preferences = prefs.get(Collective2Strategies);
        StringTokenizer st = new StringTokenizer(c2Preferences, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            StringTokenizer strategyTokenizer = new StringTokenizer(token, "-");
            String name = strategyTokenizer.nextToken();
            String systemId = strategyTokenizer.nextToken();
            String isEnabled = strategyTokenizer.nextToken();
            C2Value c2Value = new C2Value(systemId, Boolean.parseBoolean(isEnabled));
            c2Strategies.put(name, c2Value);
        }

        List<String> strategies = ClassFinder.getClasses();
        for (String name : strategies) {
            String systemId = "";
            boolean isSelected = false;
            C2Value c2Value = c2Strategies.get(name);
            if (c2Value != null) {
                systemId = c2Value.getId();
                isSelected = c2Value.getIsEnabled();
            }

            Object[] row = {name, systemId, isSelected};
            addRow(row);
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return (column == 2) ? Boolean.class : getValueAt(0, column).getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex != 0);
    }


    public String getStrategies() {
        int rowCount = getRowCount();
        String strategies = "";
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object[] row = getRow(rowIndex);
            String name = (String) row[0];
            String systemId = (String) row[1];
            if (systemId.length() == 0) {
                systemId = "?";
            }
            String isEnabled = String.valueOf(row[2]);
            strategies += (name + "-" + systemId + "-" + isEnabled + ",");
        }

        return strategies;
    }

    public C2Value getStrategy(String strategyName) {
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object[] row = getRow(rowIndex);
            String name = (String) row[0];
            if (name.equals(strategyName)) {
                return new C2Value((String) row[1], (Boolean) row[2]);
            }
        }
        return null;
    }
}
