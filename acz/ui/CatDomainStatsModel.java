package acz.ui;

import acz.analyzer.AttrClustAnalyzer;
import acz.analyzer.CatAttrDomainStats;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.table.AbstractTableModel;

public class CatDomainStatsModel extends AbstractTableModel {
    private String[] columnNames = { "Name", "Value", "Frequency", "Predictability" };
    private String[][] data;
    
    public CatDomainStatsModel(AttrClustAnalyzer analyzer) { 
        Iterator<Entry<String, CatAttrDomainStats[]>> domainSummary = analyzer.getCatAttrDomainSummary();
        List<String[]> rowList = new ArrayList<String[]>();
        
        while(domainSummary.hasNext()){
            // Display Entry
            Entry<String, CatAttrDomainStats[]> entry = domainSummary.next();
            int count = 0;
            for (CatAttrDomainStats stats : entry.getValue()) {
                String[] row = new String[columnNames.length];
                if (count == 0) { row[0] = entry.getKey(); }
                else { row[0] = ""; }
                
                row[1] = stats.catAttrVal;
                row[2] = String.valueOf(stats.freq);
                row[3] = String.valueOf(stats.predictability);
                rowList.add(row);
                count++;
            }
            
            // Display Empty Row
            String[] emptyRow = new String[columnNames.length];
            for(int i = 0; i < emptyRow.length; i++) {
                emptyRow[i] = "";
            }
            rowList.add(emptyRow);
        }
        
        if (!analyzer.classIsRealAttr()) {
            List<String[]> classStats = addOutputAttribute(
                    analyzer.getClassName(), analyzer.getClassStats(), columnNames.length);
            rowList.addAll(classStats);
        }
        
        this.data = rowList.toArray(new String[rowList.size()][columnNames.length]);
    }
    
    
    private List<String[]> addOutputAttribute(String className, 
            Object[] classStats, int numColumns) {
        
        List<String[]> rowList = new ArrayList<String[]>();
        int count = 0;
        
        for (CatAttrDomainStats classStat : (CatAttrDomainStats[])classStats) {
            
            String[] row = new String[numColumns];
            
            if (count == 0) { row[0] = className; }
            row[1] = classStat.catAttrVal;
            row[2] = String.valueOf(classStat.freq);
            row[3] = String.valueOf(classStat.predictability);
            
            rowList.add(row);
            count++;
        }
        
        // Display Empty Row
        String[] emptyRow = new String[columnNames.length];
        for(int i = 0; i < emptyRow.length; i++) {
            emptyRow[i] = "";
        }
        rowList.add(emptyRow);
            
        return rowList;
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
