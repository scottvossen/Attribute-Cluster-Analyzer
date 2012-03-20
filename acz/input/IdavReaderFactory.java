package acz.input;

import acz.RealAttrValue;
import java.io.*;
import java.util.StringTokenizer;

public class IdavReaderFactory {
	
    public IdavReader getIDAV_Reader(String inputLoc)
            throws IdavReader.IncompleteAttributeException, IdavReader.InvalidAttributeException,
            IdavReader.InvalidInstanceException, RealAttrValue.InvalidRealValueException,
            FileNotFoundException, IOException {

        boolean isARFF = false;

        // Open the file
        FileInputStream fstream = new FileInputStream(inputLoc);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        // Determine if input isn't already a IDAV file
        //  - Read File Line By Line
        while ((line = br.readLine()) != null) {

            StringTokenizer st = new StringTokenizer(line);

            if (st.hasMoreTokens())	{
                String token = st.nextToken();
                if (token.equalsIgnoreCase("@relation") || token.startsWith("%"))	{ isARFF = true; }
                break;
            }
        }
        in.close();

        if (isARFF) {
            // Get new file name
            String convInputLoc = inputLoc;
            int dotPos = convInputLoc.lastIndexOf(".");
            convInputLoc = convInputLoc.substring(0, dotPos + 1) + "txt";

            ArffConverter converter = new ArffConverter(inputLoc, convInputLoc);
            converter.Convert(convInputLoc);
            return new IdavReader(convInputLoc);
        }
        else { return new IdavReader(inputLoc); }
    }
}
