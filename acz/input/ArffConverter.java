package acz.input;

import acz.Attribute.AttrType;
import acz.Attribute.AttrUsage;
import acz.*;
import acz.output.IdavWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class ArffConverter {
    private enum ReadState { FindAttr, ReadAttr, ReadInst };

    private String inputLoc;
    private String outputLoc;
    protected List<Attribute> attributes;
    protected List<Instance> instances;

    public ArffConverter(String inputLoc, String outputLoc)
            throws RealAttrValue.InvalidRealValueException, FileNotFoundException, IOException {
        this.inputLoc = inputLoc;
        this.outputLoc = outputLoc;
        this.attributes = new ArrayList<Attribute>();
        this.instances = new ArrayList<Instance>();
    }

    public void Convert(String convertedFileDest)
            throws RealAttrValue.InvalidRealValueException, FileNotFoundException, IOException { 

        // Open the file
        FileInputStream fstream = new FileInputStream(inputLoc);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        ReadState state = ReadState.FindAttr;
        String line;
        int lineNum = 0;

        // Read File Line By Line
        while ((line = br.readLine()) != null) {

            if (state == ReadState.ReadInst) { 
                lineNum++;
                readInstance(line, lineNum); }
            else {
                StringTokenizer st = new StringTokenizer(line);

                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    switch(state){
                        case FindAttr:
                            if (token.equalsIgnoreCase("@attribute")){ 
                                state = ReadState.ReadAttr;
                                readAttribute(line);
                            }
                            break;

                        case ReadAttr:
                            if (token.equalsIgnoreCase("@attribute")) { readAttribute(line); }
                            else if (token.equalsIgnoreCase("@data")) { state = ReadState.ReadInst; }
                            break;
                    }
                }
            }
        }

        in.close();
        createIDAVFile();
    }

    private void readAttribute (String line) { 

        List<String> realTags = Arrays.asList("real", "numeric");
        StringTokenizer st = new StringTokenizer(line);

        if (st.countTokens() >= 3) {
            String attrTag = st.nextToken();
            String token1 = st.nextToken();
            String token2 = st.nextToken();

            if (!attrTag.equalsIgnoreCase("@attribute")) { /* Skip Attribute */ }

            boolean isReal = false;
            for (String tag : realTags) {
                if (tag.equalsIgnoreCase(token2)) { isReal = true; break; }
            }

            if (isReal) { attributes.add(new Attribute(token1, AttrType.R, AttrUsage.I)); }
            else { attributes.add(new Attribute(token1, AttrType.C, AttrUsage.I)); }
        }
    }

    private void readInstance (String line, int lineNum)
            throws RealAttrValue.InvalidRealValueException {

        StringTokenizer st = new StringTokenizer(line, ",");
        if (st.countTokens() != attributes.size()) { return; /* Skip Line */}

        Instance inst = new Instance(lineNum);
        int attrIndex = 0;

        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            Attribute attr = attributes.get(attrIndex);
            AttributeValue attrVal;

            if (attr.getAttrType() == AttrType.R) {
                if (token.trim().equals("?")) { 
                    attrVal = new RealAttrValue("", 0.0);
                    attrVal.SetIgnoreValue(true);
                } else {
                    attrVal = new RealAttrValue(token, 0.0);
                    // If you want to put the actual double value in... but it isn't used atm
                    //attrVal = new RealAttrValue(token, 
                    //		RealAttrValue.convertDisplayStrToDouble(token, lineNum));
                }
            }
            else {
                if (token.trim().equals("?")) { 
                    attrVal = new CatAttrValue("", "");
                    attrVal.SetIgnoreValue(true);
                } else { attrVal = new CatAttrValue(token, token);}
            }

            inst.properties.put(attr, attrVal);
            attrIndex++;
        }

        instances.add(inst);
    }

    public void createIDAVFile() throws IOException{
        // change last attr usage to AttrUsage.O
        attributes.get(attributes.size() - 1).setAttrUsage(AttrUsage.O);
        IdavWriter writer = new IdavWriter(
                attributes.toArray(new Attribute[attributes.size()]), 
                instances.toArray(new Instance[instances.size()]));
        writer.createIDAVFile(outputLoc);
    }
}
