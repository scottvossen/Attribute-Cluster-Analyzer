package acz.ui;

import acz.analyzer.AttrClustAnalyzer;
import acz.analyzer.ClassDepRealAttrStats;
import acz.analyzer.RealAttrDomainStats;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.table.AbstractTableModel;

public class RealDomainStatsModel extends AbstractTableModel {
    private String[] columnNames;
    private String[][] data;
    
    public RealDomainStatsModel(AttrClustAnalyzer analyzer) {
        if (analyzer.getNumClasses() > 30) { runMoreThanTwenty(analyzer); }
        else runNormalSetup(analyzer);
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
    
    
    private void runNormalSetup (AttrClustAnalyzer analyzer) {
        
        // Initialize Column Names, keep a colLookup for classVals
        List<String> colNameList = new ArrayList();
        HashMap<String, Integer> colLookup = new HashMap<String, Integer>();
        String[] uniqueClassVals = analyzer.getUniqueClassValues();
        
        colNameList.add("Attribute");
        for (int i = 0; i < uniqueClassVals.length; i++) {
            colLookup.put(uniqueClassVals[i], i + 1);
            colNameList.add(uniqueClassVals[i]);
        }
        colNameList.add("Domain");
        colNameList.add("Attribute Significance");
        columnNames = colNameList.toArray(new String[colNameList.size()]);
        
        // Initialize Data
        Iterator<Entry<String, RealAttrDomainStats>> domainSummary = 
                analyzer.getRealAttrDomainSummary();
        Iterator<Entry<String, ClassDepRealAttrStats[]>> classDepSummary = 
                analyzer.getClassDepRealAttrSummary();
        List<String[]> rowList = new ArrayList<String[]>();
                
        while(domainSummary.hasNext()){
            
            Entry<String, RealAttrDomainStats> domainEntry = domainSummary.next();
            Entry<String, ClassDepRealAttrStats[]> classDepEntry = classDepSummary.next();
            String[] row1 = new String[columnNames.length];
            String[] row2 = new String[columnNames.length];
            
            row1[0] = domainEntry.getKey() + " (mean)";
            row2[0] = "(sd)";
            
            for (ClassDepRealAttrStats stats : classDepEntry.getValue()) {
                row1[colLookup.get(stats.assocClassValue)] = String.valueOf(stats.avg);
                row2[colLookup.get(stats.assocClassValue)] = String.valueOf(stats.stdDev);
            }
            
            row1[row1.length - 2] = String.valueOf(domainEntry.getValue().avg);
            row2[row2.length - 2] = String.valueOf(domainEntry.getValue().stdDev);
            row1[row1.length - 1] = String.valueOf(domainEntry.getValue().attrSig);
            row2[row2.length - 1] = "";
            
            rowList.add(row1);
            rowList.add(row2);    
            
            // Display Empty Row
            String[] emptyRow = new String[columnNames.length];
            for(int i = 0; i < emptyRow.length; i++) {
                emptyRow[i] = "";
            }
            rowList.add(emptyRow);
        }
        
        if (analyzer.classIsRealAttr()) {
            List<String[]> classStats = addOutputAttribute(
                    analyzer.getClassName(), analyzer.getClassStats(), columnNames.length);
            rowList.addAll(classStats);
        }
        
        this.data = rowList.toArray(new String[rowList.size()][columnNames.length]);
    }
    
    private void runMoreThanTwenty(AttrClustAnalyzer analyzer) {
        
        // Initialize Column Names
        this.columnNames = new String[] { "Attribute", "Domain" };
        
        // Initialize Data
        Iterator<Entry<String, RealAttrDomainStats>> domainSummary = 
                analyzer.getRealAttrDomainSummary();
        Iterator<Entry<String, ClassDepRealAttrStats[]>> classDepSummary = 
                analyzer.getClassDepRealAttrSummary();
        List<String[]> rowList = new ArrayList<String[]>();
                
        while(domainSummary.hasNext()){
            
            Entry<String, RealAttrDomainStats> domainEntry = domainSummary.next();
            String[] row1 = new String[columnNames.length];
            String[] row2 = new String[columnNames.length];
            
            row1[0] = domainEntry.getKey() + " (mean)";
            row2[0] = "(sd)";
                       
            row1[1] = String.valueOf(domainEntry.getValue().avg);
            row2[1] = String.valueOf(domainEntry.getValue().stdDev);
            
            rowList.add(row1);
            rowList.add(row2);  
            
            // Display Empty Row
            String[] emptyRow = new String[columnNames.length];
            for(int i = 0; i < emptyRow.length; i++) {
                emptyRow[i] = "";
            }
            rowList.add(emptyRow);
        }
        
        if (analyzer.classIsRealAttr()) {
            List<String[]> classStats = addOutputAttribute(
                    analyzer.getClassName(), analyzer.getClassStats(), columnNames.length);
            rowList.addAll(classStats);
        }
        
        this.data = rowList.toArray(new String[rowList.size()][columnNames.length]);
    }
    
    private List<String[]> addOutputAttribute(String className, 
            Object[] classStats, int numColumns) {
        
        List<String[]> rowList = new ArrayList<String[]>();
        
        for (RealAttrDomainStats classStat : (RealAttrDomainStats[])classStats) {
            
            String[] row1 = new String[numColumns];
            String[] row2 = new String[numColumns];
            
            row1[0] = className + " (mean)";
            row2[0] = "(sd)";
            
            row1[1] = String.valueOf(classStat.avg);
            row2[1] = String.valueOf(classStat.stdDev);
            
            rowList.add(row1);
            rowList.add(row2);
            
            // Display Empty Row
            String[] emptyRow = new String[columnNames.length];
            for(int i = 0; i < emptyRow.length; i++) {
                emptyRow[i] = "";
            }
            rowList.add(emptyRow);
        }
        
        return rowList;
    }
}
