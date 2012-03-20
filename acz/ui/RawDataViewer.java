/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acz.ui;

import java.io.*;

/**
 *
 * @author Scotty Viscocity
 */
public class RawDataViewer {
    private String inputLoc;
    
    public RawDataViewer (String inputLoc) {
        this.inputLoc = inputLoc;
    }
    
    public String read() throws FileNotFoundException, IOException{
        FileInputStream fstream = new FileInputStream(inputLoc);

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        String line;
        String rawData = "";
        int inputLineNum = 0;
        int dataLineNum = 0;
        boolean data = false;
        boolean isARFF = inputLoc.endsWith(".arff");
        
        // Read File Line By Line
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) { inputLineNum++; }
            
            if (data) {
                dataLineNum++;
                rawData += dataLineNum + "\t" + line + "\n";
            } else {
                rawData += "\t" + line + "\n";
            }
            
            // Flag when we've hit the start of the data
            if (isARFF) {
                if (line.trim().startsWith("@data")) { data = true; }
            } else {
                if (inputLineNum == 3) { data = true; }
            }
        }
        
        return rawData;
    }
}
