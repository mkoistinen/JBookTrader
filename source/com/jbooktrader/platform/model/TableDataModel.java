package com.jbooktrader.platform.model;

import javax.swing.table.*;
import java.util.*;

public class TableDataModel extends AbstractTableModel {
    private final List<Object> rows;
    private String[] schema;

    public TableDataModel() {
        rows = new ArrayList<Object>();
    }

    protected void addRowFast(Object[] item) {
        rows.add(item);
    }

    public void addRow(Object[] item) {
        rows.add(item);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object[] changedItem = (Object[]) rows.get(row);
        changedItem[col] = value;
        fireTableCellUpdated(row, col);
    }

    protected void setValueAtFast(Object value, int row, int col) {
        Object[] changedItem = (Object[]) rows.get(row);
        changedItem[col] = value;
    }

    protected void removeAllData() {
        rows.clear();
        fireTableDataChanged();
    }

    protected void removeRow(int row) {
        rows.remove(row);
        fireTableDataChanged();
    }

    public void setSchema(String[] schema) {
        this.schema = schema;
        fireTableStructureChanged();
    }

    public Object getValueAt(int row, int column) {
        Object[] item = (Object[]) rows.get(row);
        return item[column];
    }

    protected Object[] getRow(int row) {
        return (Object[]) rows.get(row);
    }

    @Override
    public String getColumnName(int index) {
        return schema[index];
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return (schema == null) ? 0 : schema.length;
    }
}
