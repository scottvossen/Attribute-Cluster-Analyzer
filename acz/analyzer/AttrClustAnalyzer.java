package acz.analyzer;

import acz.Attribute.AttrType;
import acz.Attribute.AttrUsage;
import acz.*;
import java.util.Map.Entry;
import java.util.*;

public class AttrClustAnalyzer {
    
    private final int CLASS_INDEX = 0;
    private Attribute[] attributes;
    private Instance[] instances;
    private String className;
    private String[] uniqueClassValues;
    private Object[] classStats;
    private HashMap<String, CatAttrDomainStats[]> catAttrDomainSummaryTable;
    private HashMap<String, RealAttrDomainStats> realAttrDomainSummaryTable;
    private HashMap<String, Map<String, ClassDepCatAttrStats[]>> classDepCatAttrSummaryTable;
    private HashMap<String, ClassDepRealAttrStats[]> classDepRealAttrSummaryTable;
        
    public AttrClustAnalyzer(Attribute[] attributes, Instance[] instances) {
        // Ignore Attributes Marked as 'U' (unused)
        List<Attribute> attrsToBeUsed = new ArrayList<Attribute>();
        for (Attribute attr : attributes) {
            if (attr.getAttrUsage() == AttrUsage.O) {
                attrsToBeUsed.add(CLASS_INDEX, attr);
            } else if (attr.getAttrUsage() != AttrUsage.U) {
                attrsToBeUsed.add(attr);
            }
        }
        
        this.className = attrsToBeUsed.get(CLASS_INDEX).getAttrName();
        this.attributes = attrsToBeUsed.toArray(new Attribute[attrsToBeUsed.size()]);
        this.instances = instances;
        this.uniqueClassValues = findUniqueClassValues();
        this.catAttrDomainSummaryTable = new HashMap<String, CatAttrDomainStats[]>();
        this.realAttrDomainSummaryTable = new HashMap<String, RealAttrDomainStats>();
        this.classDepCatAttrSummaryTable = new HashMap<String, Map<String, ClassDepCatAttrStats[]>>();
        this.classDepRealAttrSummaryTable = new HashMap<String, ClassDepRealAttrStats[]>();
        
        initClassStats();
        calcCatAttrDomainStats();
        calcDepCatAttrStats();
        calcRealAttrStats();
    }
        
    public int getNumClasses() { return uniqueClassValues.length; }
    
    public int getNumInstancesForClassValue(String classDisplayValue) {
        int count = 0;
        for(Instance inst : instances) {
            String instValForClass = inst.properties.get(attributes[CLASS_INDEX]).getDisplayValue();
            if (instValForClass.equals(classDisplayValue)) { count++; }
        }
        return count;
    }
    
    public String getClassName() { return className; }
    
    public boolean classIsRealAttr() { return attributes[CLASS_INDEX].getAttrType() == AttrType.R; }
    
    public String[] getUniqueClassValues() { return uniqueClassValues; }
    
    public Object[] getClassStats() { return classStats; }
        
    public Iterator<Entry<String, CatAttrDomainStats[]>> getCatAttrDomainSummary() {
        Map m = new TreeMap(catAttrDomainSummaryTable);
        return m.entrySet().iterator();
    }
    
    public Iterator<Entry<String, RealAttrDomainStats>> getRealAttrDomainSummary() {
        Map m = new TreeMap(realAttrDomainSummaryTable);
        return m.entrySet().iterator();        
    }
    
    public Iterator<Entry<String, Map<String, ClassDepCatAttrStats[]>>> getClassDepCatAttrSummary() {
        Map m = new TreeMap(classDepCatAttrSummaryTable);
        return m.entrySet().iterator();
    }
    
    public Iterator<Entry<String, ClassDepRealAttrStats[]>> getClassDepRealAttrSummary() {
        Map m = new TreeMap(classDepRealAttrSummaryTable);
        return m.entrySet().iterator();
    }
        
    private String[] findUniqueClassValues() {
        List<String> uniqVals = new ArrayList<String>();
        
        for (Instance inst : instances) {
            AttributeValue instClassVal = inst.properties.get(attributes[CLASS_INDEX]);
            if (!uniqVals.contains(instClassVal.getDisplayValue())) {
                uniqVals.add(instClassVal.getDisplayValue());
            }
        }
        
        // Sort uniqueClassVals
        if (attributes[CLASS_INDEX].getAttrType() == AttrType.R) {
            Collections.sort(uniqVals, new Comparator() {
                @Override
                public int compare(Object a, Object b) {
                    double A = Double.parseDouble(a.toString());
                    double B = Double.parseDouble(b.toString());
                    
                    if (A > B) { return 1; }
                    if (A < B) { return -1; }
                    return 0;
                }
            });
        }
        else {
            Collections.sort(uniqVals);
        }
        
        return uniqVals.toArray(new String[uniqVals.size()]);
    }
    
    private void initClassStats() {
        
        if (attributes[CLASS_INDEX].getAttrType() == AttrType.R) {
            List<Double> usedValues = new ArrayList<Double>();
            
            for (Instance inst : instances) {
                RealAttrValue instClassVal = (RealAttrValue)inst.properties.get(attributes[CLASS_INDEX]);
                if(instClassVal.ShouldIgnore()) { continue; }
                usedValues.add(instClassVal.getValue());
            }
            
            RealAttrDomainStats stats = new RealAttrDomainStats();
            stats.avg = roundToHundredths(calcAvg(
                    usedValues.toArray(new Double[usedValues.size()])));
            stats.stdDev = roundToHundredths(calcStdDev(
                usedValues.toArray(new Double[usedValues.size()])));
            stats.attrSig = 0.0;
            
            classStats = new RealAttrDomainStats[] { stats };
        }
        else {
            HashMap<String, Integer> freqTable = new HashMap<String, Integer>();
            int instUsed = 0;
            
            for (Instance inst : instances) {
                
                CatAttrValue instClassVal = ((CatAttrValue)inst.properties.get(attributes[CLASS_INDEX]));
                if(instClassVal.ShouldIgnore()) { continue; }
                instUsed++;
                
                if (!freqTable.containsKey(instClassVal.getValue())) {
                    freqTable.put(instClassVal.getValue(), 1);
                } else {
                    int old = freqTable.get(instClassVal.getValue());
                    freqTable.put(instClassVal.getValue(), old + 1);
                }
            }
            
            List<CatAttrDomainStats> statsList = new ArrayList<CatAttrDomainStats>();
            
            for (String uniqVal : freqTable.keySet()) {
                CatAttrDomainStats stats = new CatAttrDomainStats();
                stats.catAttrVal = uniqVal;
                stats.freq = freqTable.get(uniqVal);
                stats.predictability = roundToHundredths(
                        (double)freqTable.get(uniqVal) /
                        (double)instUsed);
                statsList.add(stats);
            }
            
            classStats = statsList.toArray(
                    new CatAttrDomainStats[statsList.size()]);
        }
    }
    
    private void calcCatAttrDomainStats() {
        for (Attribute attr : attributes) {
            
            // Skip class attribute and real attributes
            if (attr == attributes[CLASS_INDEX] || attr.getAttrType() != AttrType.C) { continue; }
            
            List<CatAttrDomainStats> domainStatsForAttr = new ArrayList<CatAttrDomainStats>();
            int instUsed = 0;
            
            // Create overallFreqTable
            HashMap<String, Integer> freqTable = new HashMap<String, Integer>();
            for (Instance inst : instances) {
                
                // Skip instances which are labeled ignore
                AttributeValue instValueForAttr = inst.properties.get(attr);
                if(instValueForAttr.ShouldIgnore()) { continue; }
                instUsed++;
                
                if (!freqTable.containsKey(instValueForAttr.getDisplayValue())) {
                    freqTable.put(instValueForAttr.getDisplayValue(), 1);
                } else {
                    int old = freqTable.get(instValueForAttr.getDisplayValue());
                    freqTable.put(instValueForAttr.getDisplayValue(), old + 1);
                }
            }
            
            for (String uniqVal : freqTable.keySet()) {
                CatAttrDomainStats stats = new CatAttrDomainStats();
                stats.catAttrVal = uniqVal;
                stats.freq = freqTable.get(uniqVal);
                stats.predictability = roundToHundredths(
                        (double)freqTable.get(uniqVal) /
                        (double)instUsed);
                domainStatsForAttr.add(stats);
            }
            
            // Custom sort method
            Collections.sort(domainStatsForAttr,
                    new Comparator() {
                        @Override
                        public int compare(Object a, Object b) {
                            String A = ((CatAttrDomainStats)a).catAttrVal;
                            String B = ((CatAttrDomainStats)b).catAttrVal;
                            return A.compareTo(B);
                        }                        
                    });
            
            catAttrDomainSummaryTable.put(attr.getAttrName(), 
                    domainStatsForAttr.toArray(
                        new CatAttrDomainStats[domainStatsForAttr.size()]));
        }
    }
    
    private void calcDepCatAttrStats() {
        String[] uniqClassValsPlusEmpty = new String[uniqueClassValues.length + 1];
        System.arraycopy(uniqueClassValues, 0, uniqClassValsPlusEmpty, 0, uniqueClassValues.length);
        uniqClassValsPlusEmpty[uniqClassValsPlusEmpty.length - 1] = "";
        
        for (String currentClassVal : uniqClassValsPlusEmpty) {
            HashMap<String, ClassDepCatAttrStats[]> classDepStats = new HashMap<String, ClassDepCatAttrStats[]>();
            
            for (Attribute attr : attributes) {
                
                List<ClassDepCatAttrStats> classDepStatsForAttr = new ArrayList<ClassDepCatAttrStats>();
                HashMap<String, Integer> overallFreqTable = new HashMap<String, Integer>();
                HashMap<String, Integer> matchingFreqTable = new HashMap<String, Integer>();
                int numMatchingClassInstances = 0;
                
                // Skip class attribute and real attributes
                if (attr == attributes[CLASS_INDEX] || attr.getAttrType() != AttrType.C) { continue; }
                
                for (Instance inst : instances) {
                    AttributeValue instValueForAttr = inst.properties.get(attr);
                    
                    if (!overallFreqTable.containsKey(instValueForAttr.getDisplayValue())) {
                        overallFreqTable.put(instValueForAttr.getDisplayValue(), 1);
                    } else {
                        int old = overallFreqTable.get(instValueForAttr.getDisplayValue());
                        overallFreqTable.put(instValueForAttr.getDisplayValue(), old + 1);
                    }
                    
                    // Skip instances which don't match the currentClassVal
                    String instValueForClass = inst.properties.get(attributes[CLASS_INDEX]).getDisplayValue();
                    if (!instValueForClass.equals(currentClassVal)) {
                        if (!matchingFreqTable.containsKey(instValueForAttr.getDisplayValue())) {
                            matchingFreqTable.put(instValueForAttr.getDisplayValue(), 0);
                        }
                        continue;
                    }
                    
                    numMatchingClassInstances++;
                    if (!matchingFreqTable.containsKey(instValueForAttr.getDisplayValue())) {
                        matchingFreqTable.put(instValueForAttr.getDisplayValue(), 1);
                    } else {
                        int old = matchingFreqTable.get(instValueForAttr.getDisplayValue());
                        matchingFreqTable.put(instValueForAttr.getDisplayValue(), old + 1);
                    }
                }

                for (String uniqVal : overallFreqTable.keySet()) {
                    ClassDepCatAttrStats stats = new ClassDepCatAttrStats();
                    stats.assocClassValue = uniqVal;
                    stats.freq = matchingFreqTable.get(uniqVal);
                    stats.predictability = roundToHundredths(
                            (double)matchingFreqTable.get(uniqVal) / 
                            (double)numMatchingClassInstances);
                    stats.predictiveness = roundToHundredths(
                            (double)matchingFreqTable.get(uniqVal) / 
                            (double)overallFreqTable.get(uniqVal));
                    classDepStatsForAttr.add(stats);
                }
                
                Collections.sort(classDepStatsForAttr,
                    new Comparator() {
                        @Override
                        public int compare(Object a, Object b) {
                            String A = ((ClassDepCatAttrStats)a).assocClassValue;
                            String B = ((ClassDepCatAttrStats)b).assocClassValue;
                            
                            // if (either is empty) say it's smaller
                            if (A.equals("")) { 
                                return 1; }
                            if (B.equals("")) { 
                                return -1; }
                            return A.compareTo(B);
                        }
                    });

                classDepStats.put(attr.getAttrName(), 
                        classDepStatsForAttr.toArray(
                            new ClassDepCatAttrStats[classDepStatsForAttr.size()]));
            }
            
            classDepCatAttrSummaryTable.put(currentClassVal, new TreeMap(classDepStats));
        }
    }
    
    private void calcRealAttrStats() {
        HashMap<Attribute, Double> minClassDepAvgTable = new HashMap<Attribute, Double>();
        HashMap<Attribute, Double> maxClassDepAvgTable = new HashMap<Attribute, Double>();
        
        // Calc Class Dependent Real Stats
        for (Attribute attr : attributes) {
            
            // Skip class attribute and cat attributes
            if (attr == attributes[CLASS_INDEX] || attr.getAttrType() != AttrType.R) { continue; }
            
            List<ClassDepRealAttrStats> statsList = new ArrayList<ClassDepRealAttrStats>();
            for (String currentClassVal : uniqueClassValues) {
                
                List<Double> usedValues = new ArrayList<Double>();
                for (Instance inst : instances) {

                    // Skip instances which are not part of this class
                    AttributeValue instValueForClass = inst.properties.get(attributes[CLASS_INDEX]);
                    if (!instValueForClass.getDisplayValue().equals(currentClassVal)) { continue; }
                    
                    // Skip instances which are labeled ignore
                    AttributeValue instValForAttr = inst.properties.get(attr);
                    if (instValForAttr.ShouldIgnore()) { continue; }
                    usedValues.add(Double.parseDouble(instValForAttr.getDisplayValue()));
                }

                ClassDepRealAttrStats stats = new ClassDepRealAttrStats();
                stats.assocClassValue = currentClassVal;
                stats.avg = roundToHundredths(calcAvg(
                        usedValues.toArray(new Double[usedValues.size()])));
                stats.stdDev = roundToHundredths(calcStdDev(
                        usedValues.toArray(new Double[usedValues.size()])));
                statsList.add(stats);
                
                // Add to min/max where applicable
                if (!minClassDepAvgTable.containsKey(attr) || 
                        stats.avg < minClassDepAvgTable.get(attr)) {
                    minClassDepAvgTable.put(attr, stats.avg);
                }
                if (!maxClassDepAvgTable.containsKey(attr) || 
                        stats.avg > maxClassDepAvgTable.get(attr)) {
                    maxClassDepAvgTable.put(attr, stats.avg);
                }
            }
            
            // Custom sort method
            Collections.sort(statsList,
                new Comparator() {
                    @Override
                    public int compare(Object a, Object b) {
                        String A = ((ClassDepRealAttrStats)a).assocClassValue;
                        String B = ((ClassDepRealAttrStats)b).assocClassValue;
                        return A.compareTo(B);
                    }               
                });
            
            classDepRealAttrSummaryTable.put(attr.getAttrName(), 
                    statsList.toArray(new ClassDepRealAttrStats[statsList.size()]));
        }
        
        
        // Calc Real Domain Stats
        for (Attribute attr : attributes) {
            
            // Skip class attribute and cat attributes
            if (attr == attributes[CLASS_INDEX] || attr.getAttrType() != AttrType.R) { continue; }
            
            List<Double> usedValues = new ArrayList<Double>();
            for (Instance inst : instances) {
                
                // Skip instances which are labeled ignore
                AttributeValue instValForAttr = inst.properties.get(attr);
                if (instValForAttr.ShouldIgnore()) { continue; }
                usedValues.add(Double.parseDouble(instValForAttr.getDisplayValue()));
            }
            
            RealAttrDomainStats stats = new RealAttrDomainStats();
            stats.avg = roundToHundredths(calcAvg(
                    usedValues.toArray(new Double[usedValues.size()])));
            stats.stdDev = roundToHundredths(calcStdDev(
                    usedValues.toArray(new Double[usedValues.size()])));
            stats.attrSig = roundToHundredths((maxClassDepAvgTable.get(attr) - 
                    minClassDepAvgTable.get(attr)) / stats.stdDev);
            realAttrDomainSummaryTable.put(attr.getAttrName(), stats);
        }
    }
        
    private double roundToHundredths (double dbl) {
        double d = (Math.round(100*dbl)) / 100.0;
        return d;
    }
    
    private double calcAvg (Double[] data) {
        int count = 0;
        double sum = 0;
        for (double val : data) {
            sum += val;
            count++;
        }
        return sum / count;
    }
    
    private double calcStdDev (Double[] data) {
       double avg = calcAvg(data);
       double sumOfSquares = 0;
       for (int i = 0; i < data.length; i++) {
           sumOfSquares += Math.pow(data[i] - avg, 2.0);
       }
       return Math.sqrt(sumOfSquares / (data.length - 1));
   }
}
