package acz.output;

import acz.analyzer.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// This class makes the XML dump of all the stats generated for use by some
// other application
public class AczOutputWriter {
    private AttrClustAnalyzer analyzer;
    private Document doc;
    
    // XML TAGS
    private final String ROOT = "AczOutputFile";
    private final String NUM_CLASSES = "NumOfClasses";
    private final String CAT_DOM_SUM = "CatAttrDomainSummary";
    private final String CAT_ATTR_SUM = "CatAttrSummary";
    private final String CAT_ATTR_DOM_STATS = "CatAttrDomainStats";
    private final String REAL_DOM_SUM = "RealAttrDomainSummary";
    private final String REAL_ATTR_SUM = "RealAttributeSummary";
    private final String REAL_ATTR_DOM_STATS = "RealAttrDomainStats";
    private final String CLASS_DEP_REAL_SUM = "ClassDepRealAttrSummary";
    private final String CLASS_DEP_REAL_ATTR_SUM = "ClassDepRealAttrSummary";
    private final String CLASS_DEP_REAL_ATTR_STATS = "ClasDepRealAttrStats";
    private final String CLASS_DEP_CAT_SUM = "ClassDepCatAttrSummary";
    private final String CLASS_DEP_CAT_ATTR_SUM = "ClassDepCatAttrSummary";
    private final String CLASS_DEP_CAT_ATTR_STATS = "ClasDepCatAttrStats";
    private final String CLASS = "Class";
    
    public AczOutputWriter(AttrClustAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
    
    public void writeAanOutputFile (String outputLoc) throws ParserConfigurationException, 
            TransformerConfigurationException, UnsupportedEncodingException, FileNotFoundException, TransformerException {
            
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();

        // Root with date attribute
        Element rootElement = doc.createElement(ROOT);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        rootElement.setAttribute("date", dateFormat.format(new Date()));
        doc.appendChild(rootElement);

        // Add content
        rootElement.appendChild(getNumClasses());
        rootElement.appendChild(getDomainStatsForCatAttr());
        rootElement.appendChild(getDomainStatsForRealAttr());
        rootElement.appendChild(getClassDepStatsForRealAttr());
        rootElement.appendChild(getClassDepStatsForCatAttr());

        // write the content into xml file
        DOMSource source = new DOMSource(doc);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4); 
        
        Transformer transformer = transformerFactory.newTransformer();
        Result result = new StreamResult(new OutputStreamWriter(new FileOutputStream(outputLoc),"UTF-8"));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        
        transformer.transform(source, result);
    }
    
    private Element getNumClasses() {
            Element numClasses = doc.createElement(NUM_CLASSES);
            numClasses.appendChild(doc.createTextNode(
                    Integer.toString(analyzer.getNumClasses())));
            return numClasses;
    }
    
    private Element getDomainStatsForCatAttr() {
        Iterator<Entry<String, CatAttrDomainStats[]>> itr = analyzer.getCatAttrDomainSummary();
        Element catDomSum = doc.createElement(CAT_DOM_SUM);
        
        while(itr.hasNext()) {
            Entry<String, CatAttrDomainStats[]> entry = itr.next();
            
            Element catAttrSum = doc.createElement(CAT_ATTR_SUM);
            catAttrSum.setAttribute("attrName", entry.getKey());
            
            for(CatAttrDomainStats stats : entry.getValue()) {
                Element catAttrDomStats = doc.createElement(CAT_ATTR_DOM_STATS);
                catAttrDomStats.setAttribute("catAttrValue", stats.catAttrVal);
                catAttrDomStats.setAttribute("freq", String.valueOf(stats.freq));
                catAttrDomStats.setAttribute("predictability", String.valueOf(stats.predictability));
                
                catAttrSum.appendChild(catAttrDomStats);
            }
            
            catDomSum.appendChild(catAttrSum);
        }
        return catDomSum;
    }
    
    private Element getDomainStatsForRealAttr() {
        Iterator<Entry<String, RealAttrDomainStats>> itr = analyzer.getRealAttrDomainSummary();
        Element realDomSum = doc.createElement(REAL_DOM_SUM);
        
        while(itr.hasNext()) {
            Entry<String, RealAttrDomainStats> entry = itr.next();
            
            Element realAttrSum = doc.createElement(REAL_ATTR_SUM);
            realAttrSum.setAttribute("attrName", entry.getKey());
            
            RealAttrDomainStats stats = entry.getValue();
            Element realAttrDomStats = doc.createElement(REAL_ATTR_DOM_STATS);
            realAttrDomStats.setAttribute("avg", String.valueOf(stats.avg));
            realAttrDomStats.setAttribute("stdDev", String.valueOf(stats.stdDev));
            realAttrDomStats.setAttribute("attrSig", String.valueOf(stats.attrSig));
            
            realAttrSum.appendChild(realAttrDomStats);
            realDomSum.appendChild(realAttrSum);
        }
        return realDomSum;
    }
    
    private Element getClassDepStatsForCatAttr() {
        Iterator<Entry<String, Map<String, ClassDepCatAttrStats[]>>> classVaueItr = analyzer.getClassDepCatAttrSummary();
        
        Element classDepCatAttrStatsSum = doc.createElement(CLASS_DEP_CAT_SUM);
        
        while(classVaueItr.hasNext()) {
            Entry<String, Map<String, ClassDepCatAttrStats[]>> classEntry = classVaueItr.next();
            
            Element _class = doc.createElement(CLASS);
            _class.setAttribute("classValue", classEntry.getKey());
            _class.setAttribute("numInstance", 
                    String.valueOf(analyzer.getNumInstancesForClassValue(classEntry.getKey())));
            
            Iterator<Entry<String, ClassDepCatAttrStats[]>> attrItr = classEntry.getValue().entrySet().iterator();
            while(attrItr.hasNext()) {
                Entry<String, ClassDepCatAttrStats[]> attrEntry = attrItr.next();
                
                Element classDepCatAttrSum = doc.createElement(CLASS_DEP_CAT_ATTR_SUM);
                classDepCatAttrSum.setAttribute("attrName", attrEntry.getKey());                
                
                for (ClassDepCatAttrStats stats : attrEntry.getValue()) {
                    Element classDepCatAttrStats = doc.createElement(CLASS_DEP_CAT_ATTR_STATS);
                    classDepCatAttrStats.setAttribute("assocClassValue", stats.assocClassValue);
                    classDepCatAttrStats.setAttribute("freq", String.valueOf(stats.freq));
                    classDepCatAttrStats.setAttribute("predictability", String.valueOf(stats.predictability));
                    classDepCatAttrStats.setAttribute("predictiveness", String.valueOf(stats.predictiveness));
                    
                    classDepCatAttrSum.appendChild(classDepCatAttrStats);
                }
                
                _class.appendChild(classDepCatAttrSum);
            }
                
            classDepCatAttrStatsSum.appendChild(_class);
        }
        return classDepCatAttrStatsSum;
    }
    
    private Element getClassDepStatsForRealAttr() {
        Iterator<Entry<String, ClassDepRealAttrStats[]>> itr = analyzer.getClassDepRealAttrSummary();
        
        Element classDepRealSum = doc.createElement(CLASS_DEP_REAL_SUM);
        
        while(itr.hasNext()) {
            Entry<String, ClassDepRealAttrStats[]> entry = itr.next();
            
            Element classDepRealAttrSum = doc.createElement(CLASS_DEP_REAL_ATTR_SUM);
            classDepRealAttrSum.setAttribute("attrName", entry.getKey());
            
            for(ClassDepRealAttrStats stats : entry.getValue()) {
                
                Element classDepRealAttrStats = doc.createElement(CLASS_DEP_REAL_ATTR_STATS);
                classDepRealAttrStats.setAttribute("assocClassValue", stats.assocClassValue);
                classDepRealAttrStats.setAttribute("avg", String.valueOf(stats.avg));
                classDepRealAttrStats.setAttribute("stdDev", String.valueOf(stats.stdDev));
                
                classDepRealAttrSum.appendChild(classDepRealAttrStats);
            }
            
            classDepRealSum.appendChild(classDepRealAttrSum);
        }
        return classDepRealSum;
    }
}
