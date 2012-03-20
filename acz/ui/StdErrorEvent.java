package acz.ui;

import java.util.EventObject;

public class StdErrorEvent extends EventObject {
    public String msg;
    
    public StdErrorEvent(Object src) {
        super(src);
    }
}
