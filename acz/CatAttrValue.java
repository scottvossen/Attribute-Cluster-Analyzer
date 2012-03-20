package acz;

public class CatAttrValue extends AttributeValue 
{
    private String value;
    public String getValue() { return value; }

    public CatAttrValue(String displayValue, String value)
    {
        this.displayValue = displayValue;
        this.value = value;
        this.ignoreValue = false;
    }
}
