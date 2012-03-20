package acz.ui;

import acz.Attribute;
import acz.Attribute.AttrType;
import acz.Attribute.AttrUsage;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;

public class AttrDataTableModel extends AbstractTableModel {
    EventListenerList listenerList = new EventListenerList();
    
    private String[] columnNames = { "Name", "Type", "Usage" };
    private AtomicReference<Attribute[]> attributes;
    private AtomicReference<InstDataTableModel> data;

    public Attribute[] getAttributes() { return attributes.get(); }
    
    public AttrDataTableModel(AtomicReference<Attribute[]> attributes, AtomicReference<InstDataTableModel> data) { 
        this.attributes = attributes;
        this.data = data;
    }
    
    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public int getRowCount() { return attributes.get().length; }

    @Override
    public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        switch(col) {
            case 0:
                return attributes.get()[row].getAttrName();
            case 1:
                return attributes.get()[row].getAttrType();
            case 2:
                return attributes.get()[row].getAttrUsage();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int c) { return getValueAt(0, c).getClass(); }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col < 1) { return false; }
        else { return true; }
    }

    // Don't need to implement this method unless your table's data can change.
    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 0:
                attributes.get()[row].setAttrName(value.toString());
                break;
            case 1:
                if(isSafeToConvert((AttrType)value, row)) {
                    attributes.get()[row].setAttrType((AttrType)value);
                } else {
                    StdErrorEvent event = new StdErrorEvent(this);
                    event.msg = "Cannot convert attribute type.\n" +
                                "Ensure all data values for this attribute are numeric.";
                    fireStdErrorEvent(event);
                }
                break;
            case 2:
                attributes.get()[row].setAttrUsage((AttrUsage)value);
                break;
            default:
                break;
        }
        fireTableCellUpdated(row, col);
    }
    
    private boolean isSafeToConvert(AttrType to, int attrNum) {
        if (to == AttrType.C) { return true; }
        
        for(int row = 0; row < data.get().getRowCount(); row++) {
            try {
                Double.valueOf(String.valueOf(data.get().getValueAt(row, attrNum)));
            } catch (Exception ex) { return false; }
        }
        return true;
    }    
    
    public void addStdEventListener(StdErrorEventListener listener) {
        listenerList.add(StdErrorEventListener.class, listener);
    }
    
    public void removeStdEventListener(StdErrorEventListener listener) {
        listenerList.remove(StdErrorEventListener.class, listener);
    }
    
    private void fireStdErrorEvent(StdErrorEvent event) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == StdErrorEventListener.class) {
                ((StdErrorEventListener)listeners[i + 1]).handleStdErrorEvent(event);
            }
        }
    }
}
