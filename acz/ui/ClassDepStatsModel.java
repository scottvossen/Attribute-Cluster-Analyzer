/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acz.ui;

import acz.analyzer.AttrClustAnalyzer;
import acz.analyzer.ClassDepCatAttrStats;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Scotty Viscocity
 */
public class ClassDepStatsModel extends AbstractTableModel {
    private String[] columnNames = { "Name", "Value", "Frequency", "Predictability", "Predictiveness" };
    private String[][] data;
    
    public ClassDepStatsModel(String classValue, AttrClustAnalyzer analyzer) { 
        
        Iterator<Entry<String, Map<String, ClassDepCatAttrStats[]>>> classDepSummary = 
                analyzer.getClassDepCatAttrSummary();
        List<String[]> rowList = new ArrayList<String[]>();
        int totalRows = 0;
        
        while(classDepSummary.hasNext()){
            Entry<String, Map<String, ClassDepCatAttrStats[]>> classEntry = classDepSummary.next();
            if (!classEntry.getKey().equals(classValue)) { continue; }
            
            Iterator<Entry<String, ClassDepCatAttrStats[]>> classEntrySummary = 
                    classEntry.getValue().entrySet().iterator();
            
            while(classEntrySummary.hasNext()) {
                Entry<String, ClassDepCatAttrStats[]> entry = classEntrySummary.next();
                
                int count = 0;
                for (ClassDepCatAttrStats stats : entry.getValue()) {
                    String[] row = new String[columnNames.length];
                    if (count == 0) { row[0] = entry.getKey(); }
                    else { row[0] = ""; }

                    row[1] = stats.assocClassValue;
                    row[2] = String.valueOf(stats.freq);
                    row[3] = String.valueOf(stats.predictability);
                    row[4] = String.valueOf(stats.predictiveness);
                    rowList.add(row);
                    totalRows++;
                    count++;
                }

                // Display New Line
                String[] emptyRow = new String[columnNames.length];
                for(int i = 0; i < emptyRow.length; i++) {
                    emptyRow[i] = "";
                }
                rowList.add(emptyRow);
                totalRows++;
            }
        }
        
        this.data = rowList.toArray(new String[totalRows][columnNames.length]);
    }
    
    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public int getRowCount() { return data.length; }

    @Override
    public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    @Override
    public Class getColumnClass(int c) { return getValueAt(0, c).getClass(); }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    // Don't need to implement this method unless your table's data can change.
    @Override
    public void setValueAt(Object value, int row, int col) {
        // not editable
        //fireTableCellUpdated(row, col);
    }
    
}
