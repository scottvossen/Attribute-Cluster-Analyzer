package acz;

public class Attribute {
    public enum AttrType { C, R };
    public enum AttrUsage { I, U, D, O };

    private String attrName;
    private AttrType attrType;
    private AttrUsage attrUsage;

    public String getAttrName() { return attrName; }
    public AttrType getAttrType() { return attrType; }
    public AttrUsage getAttrUsage() { return attrUsage; }

    public void setAttrName(String attrName) { this.attrName = attrName; }
    public void setAttrType(AttrType attrType) { this.attrType = attrType; }
    public void setAttrUsage(AttrUsage attrUsage) { this.attrUsage = attrUsage; }

    public Attribute(String attrName, AttrType attrType, AttrUsage attrUsage){
            this.attrName = attrName;
            this.attrType = attrType;
            this.attrUsage = attrUsage;
    }	
}
