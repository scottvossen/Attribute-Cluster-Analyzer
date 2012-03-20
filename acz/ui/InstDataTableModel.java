package acz.ui;

import acz.Attribute;
import acz.Attribute.AttrType;
import acz.CatAttrValue;
import acz.Instance;
import acz.RealAttrValue;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class InstDataTableModel extends AbstractTableModel {
    private AtomicReference<Attribute[]> attributes;
    private AtomicReference<Instance[]> instances;

    public Instance[] getInstances() { return instances.get(); }
    
    public InstDataTableModel(AtomicReference<Attribute[]> attributes, AtomicReference<Instance[]> instances) { 
        this.attributes = attributes;
        this.instances = instances;
    }
    
    @Override
    public int getColumnCount() { return attributes.get().length; }

    @Override
    public int getRowCount() { return instances.get().length; }

    @Override
    public String getColumnName(int col) { return attributes.get()[col].getAttrName(); }

    @Override
    public Object getValueAt(int row, int col) {
        return instances.get()[row].properties.get(attributes.get()[col]).getDisplayValue();
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    @Override
    public boolean isCellEditable(int row, int col) { return true; }

    // Don't need to implement this method unless your table's data can change.
    @Override
    public void setValueAt(Object value, int row, int col) {
        try {            
            boolean isCatAttr = attributes.get()[col].getAttrType() == AttrType.C;
            
            if (isCatAttr) {
                CatAttrValue newCav = new CatAttrValue(value.toString(), value.toString());
                if (value.toString().isEmpty()) { newCav.SetIgnoreValue(true); }                
                instances.get()[row].properties.put(attributes.get()[col], newCav);
            } else {
                RealAttrValue newRav;
                
                if (value.toString().isEmpty()) {
                    newRav = new RealAttrValue("", 0.0);
                    newRav.SetIgnoreValue(true);
                }
                else {
                    newRav = new RealAttrValue(value.toString(), 
                        RealAttrValue.convertDisplayStrToDouble(value.toString(), -1));
                }
                
                instances.get()[row].properties.put(attributes.get()[col], newRav);
            }
            fireTableCellUpdated(row, col);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Real Attribute Error", 0);
        }
    }
}
