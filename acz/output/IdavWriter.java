/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acz.output;

import acz.Attribute;
import acz.Attribute.AttrUsage;
import acz.Instance;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Scotty Viscocity
 */
public class IdavWriter {
    private Attribute[] attributes;
    private Instance[] instances;
    
    public IdavWriter (Attribute[] attributes, Instance[] instances) {
        this.attributes = attributes;
        this.instances = instances;
    }
    
    public void createSimplifiedIDAVFile(String outputLoc) throws IOException {
        // remove/ignore instances that are marked as such
        List<Attribute> simpleAttrs = new ArrayList<Attribute>();
        Instance[] simpleInsts = instances.clone();
        
        for (Attribute attr : attributes) {
            if (attr.getAttrUsage() != AttrUsage.U) { simpleAttrs.add(attr); }
            else {
                for(Instance inst : simpleInsts) {
                    inst.properties.remove(attr);
                }
            }
        }
        
        createIDAVFile(outputLoc, 
                simpleAttrs.toArray(new Attribute[simpleAttrs.size()]), 
                simpleInsts);
    }
    
    public void createIDAVFile(String outputLoc) throws IOException {
        createIDAVFile(outputLoc, attributes, instances);
    }
    
    private void createIDAVFile(String outputLoc, Attribute[] attributes, Instance[] instances) throws IOException {
        // create IDAV file at outputLoc
        String row1 = null;
        String row2 = null;
        String row3 = null;

        for (Attribute attr : attributes) {
            if (row1 == null) { 
                row1 = attr.getAttrName();
                row2 = attr.getAttrType().toString();
                row3 = attr.getAttrUsage().toString();
            } else {
                row1 += "\t" + attr.getAttrName();
                row2 += "\t" + attr.getAttrType();
                row3 += "\t" + attr.getAttrUsage();
            }
        }

        FileWriter outputFile = new FileWriter(outputLoc);
        PrintWriter out = new PrintWriter(outputFile);

        out.println(row1);
        out.println(row2);
        out.println(row3);

        for (Instance inst : instances) {
            String instStr = null;
            for (Attribute attr : attributes) {
                // could put the actual value, but that's more work so we'll just put displayValue
                if (instStr == null) { instStr = inst.properties.get(attr).getDisplayValue(); }
                else { instStr += "\t" + inst.properties.get(attr).getDisplayValue(); }
            }
            out.println(instStr);
        }

        out.close();
    }
}
