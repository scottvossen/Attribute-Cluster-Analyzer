package acz;
import java.util.HashMap;

public class Instance {
    public int num;
    public HashMap<Attribute, AttributeValue> properties;
    public Instance(int num) { 
        this.num = num;
        this.properties = new HashMap<Attribute, AttributeValue>(); 
    }
}
