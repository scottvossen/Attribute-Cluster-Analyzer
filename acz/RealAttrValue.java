package acz;

public class RealAttrValue extends AttributeValue 
{
    private double value;
    public double getValue() { return value; }

    public RealAttrValue(String displayValue, double value)
    {
        this.displayValue = displayValue;
        this.value = value;
        this.ignoreValue = false;
    }

    public static double convertDisplayStrToDouble(String displayStr, int lineNum) 
            throws InvalidRealValueException {

        String workingStr = displayStr.trim();
        workingStr = workingStr.replaceAll("[$%]", "");

        try {
            if (workingStr.contains("e") || workingStr.contains("E")) {
                    String[] splitStr;
                    if (workingStr.contains("e")) { splitStr = workingStr.split("e"); }
                    else { splitStr = workingStr.split("E"); }

                    if (splitStr.length != 2) { throw new InvalidRealValueException("Invalid Scientific Notation", lineNum); }
                    return Double.parseDouble(splitStr[0]) * Math.pow(10,Double.parseDouble(splitStr[1]));
            } 
            else { return Double.parseDouble(workingStr); }
        }
        catch (Exception e)
        {
            String m = e.getMessage();
            if (e instanceof InvalidRealValueException) { throw (InvalidRealValueException)e; }
            else { throw new InvalidRealValueException(e.getMessage(), lineNum); }
        }
    }

    public static class InvalidRealValueException extends Exception {
        InvalidRealValueException(String s, int lineNum) { 
            super(s + " (Line#" + lineNum + ")"); 
        }
    }
}
