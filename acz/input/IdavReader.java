package acz.input;

import acz.AttributeValue;
import acz.RealAttrValue;
import acz.Attribute;
import acz.CatAttrValue;
import acz.Instance;
import java.io.*;
import java.util.*;

public class IdavReader
{
    protected String inputLoc;
    protected List<Attribute> attributes;
    protected List<Instance> instances;

    public Attribute[] getAttributes() { return attributes.toArray(new Attribute[attributes.size()]); }
    public Instance[] getInstances() { return instances.toArray(new Instance[instances.size()]); }

    public IdavReader(String inputLoc)
                throws IncompleteAttributeException, InvalidAttributeException,
                InvalidInstanceException, RealAttrValue.InvalidRealValueException,
                FileNotFoundException, IOException {

        this.inputLoc = inputLoc;
        this.attributes = new ArrayList<Attribute>();
        this.instances = new ArrayList<Instance>();
        readInput();
    }

    private void readInput() 
                throws IncompleteAttributeException, InvalidAttributeException,
                InvalidInstanceException, RealAttrValue.InvalidRealValueException,
                FileNotFoundException, IOException {

        // Open the file
        FileInputStream fstream = new FileInputStream(inputLoc);

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        String line1 = "";
        String line2 = "";
        int lineNum = 0;

        // Read File Line By Line
        while ((line = br.readLine()) != null) {

            lineNum++;
            switch(lineNum) {

                case 1:
                    line1 = line;
                    break;
                case 2:
                    line2 = line;
                    break;
                case 3:
                    initializeAttributes(line1, line2, line);
                    break;
                default:
                    addNewInstance(line, lineNum - 3);
                    break;
            }
        }

        in.close();
    }

    private void initializeAttributes(String line1, String line2, String line3) 
                throws IncompleteAttributeException, InvalidAttributeException {

        StringTokenizer attrNames = new StringTokenizer(line1, "\t");
        StringTokenizer attrTypes = new StringTokenizer(line2, "\t");
        StringTokenizer attrUsages = new StringTokenizer(line3, "\t");

        int numTokens = attrNames.countTokens();
        if (attrTypes.countTokens() != numTokens || attrUsages.countTokens() != numTokens) {
            throw new IncompleteAttributeException("Rows 1 through 3 have different number of tokens");
        }

        while(attrNames.hasMoreTokens()) {

            Attribute a = new Attribute(
                        attrNames.nextToken(),
                        getAttrTypeFromStr(attrTypes.nextToken()),
                        getAttrUsageFromStr(attrUsages.nextToken()));
            attributes.add(a);
        }
    }

    private Attribute.AttrType getAttrTypeFromStr(String str) throws InvalidAttributeException {

        List<String> attrTypeC = Arrays.asList("c", "C");
        List<String> attrTypeR = Arrays.asList("r", "R");

        if (attrTypeC.contains(str.trim())) { return Attribute.AttrType.C; }
        if (attrTypeR.contains(str.trim())) { return Attribute.AttrType.R; }
        throw new InvalidAttributeException("Invalid Attribute Type");
    }

    private Attribute.AttrUsage getAttrUsageFromStr(String str) throws InvalidAttributeException {

        List<String> attrTypeD = Arrays.asList("d", "D");
        List<String> attrTypeI = Arrays.asList("i", "I");
        List<String> attrTypeO = Arrays.asList("o", "O");
        List<String> attrTypeU = Arrays.asList("u", "U");

        if (attrTypeD.contains(str.trim())) { return Attribute.AttrUsage.D; }
        if (attrTypeI.contains(str.trim())) { return Attribute.AttrUsage.I; }
        if (attrTypeO.contains(str.trim())) { return Attribute.AttrUsage.O; }
        if (attrTypeU.contains(str.trim())) { return Attribute.AttrUsage.U; }
        throw new InvalidAttributeException("Invalid Attribute Usage");
    }

    private void addNewInstance(String instStr, int lineNum) 
            throws InvalidInstanceException, RealAttrValue.InvalidRealValueException {

        StringTokenizer tempProps = new StringTokenizer(instStr, "\t", true);
        List<String> props = new ArrayList<String>();
        Iterator<Attribute> attrIter = attributes.iterator();
        Instance inst = new Instance(lineNum);

        // Go through tempProps, find the empty references, and add them to props
        String lastProp = "\t";
        while(tempProps.hasMoreTokens()) {

            String token = tempProps.nextToken();

            // Check for empty references
            if (lastProp.equals("\t") && token.equals("\t")) { props.add(""); }
            if (!tempProps.hasMoreTokens() && token.equals("\t")) { props.add(""); }

            if (!token.equals("\t")) { props.add(token); }
            lastProp = token;
        }

        // Skip Invalid Instances
        if (props.size() != attributes.size()) { throw new InvalidInstanceException("Invalid Instance", lineNum); }

        // Add all values to this instance
        int i = 0;
        while(attrIter.hasNext()) {

            Attribute attr = attrIter.next();
            String displayValue = props.get(i);
            
            if (attr.getAttrType() == Attribute.AttrType.R) {
                AttributeValue attrVal;
                
                if (displayValue.equals("")) {
                    attrVal = new RealAttrValue("", 0.0);
                    attrVal.SetIgnoreValue(true);
                }
                else {
                    attrVal = new RealAttrValue(displayValue, 
                            convertDisplayStrToDouble(displayValue, lineNum));
                }
                
                inst.properties.put(attr, attrVal);
            } 
            else {
                AttributeValue attrVal = new CatAttrValue(displayValue, displayValue);
                if (displayValue.equals("")) { attrVal.SetIgnoreValue(true); }
                inst.properties.put(attr, attrVal);
            }

            i++;
        }

        instances.add(inst);
    }

    private double convertDisplayStrToDouble(String displayStr, int lineNum) 
            throws RealAttrValue.InvalidRealValueException{

        if (displayStr == null || displayStr.equals("")) { return 0.0; } // this will be ignored anyways
        return RealAttrValue.convertDisplayStrToDouble(displayStr, lineNum);
    }

    public static class IncompleteAttributeException extends Exception{ 
        IncompleteAttributeException(String s) { super(s); } } 

    public static class InvalidAttributeException extends Exception{ 
        InvalidAttributeException(String s) { super(s); } } 

    public static class InvalidInstanceException extends Exception {
        InvalidInstanceException(String s, int lineNum) { super(s + " (Line#" + lineNum + ")"); }
    }
}
