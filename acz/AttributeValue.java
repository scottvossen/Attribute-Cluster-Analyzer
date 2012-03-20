package acz;

public abstract class AttributeValue 
{
    protected String displayValue;
    protected boolean ignoreValue;

    public String getDisplayValue() { return displayValue; }
    public boolean ShouldIgnore() { return ignoreValue; }
    public void SetIgnoreValue(boolean shouldIgnore) { this.ignoreValue = shouldIgnore; }
}
