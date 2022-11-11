package legal.documentassembly.util;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.RulebaseImplication;
import legal.documentassembly.bean.RulebaseRelation;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class RulebaseUtil {
    
    private static HashMap<String,HashMap<String,String>> rulebaseLabels = null;

    public static ArrayList<RulebaseImplication> retrieveImplications() {
        ArrayList<RulebaseImplication> retVal = new ArrayList<RulebaseImplication>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.input_folder + "rulebase.ruleml"); // EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename);
            
            NodeList nodelistImplication = (NodeList) xpath.evaluate(".//Implies", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodelistImplication.getLength(); i++) {
                RulebaseImplication implication = new RulebaseImplication();
                Object nodeImplication = nodelistImplication.item(i);
                NodeList nodelistRuleId = (NodeList) xpath.evaluate(".//oid/Ind", nodeImplication, XPathConstants.NODESET);
                if(nodelistRuleId.getLength() > 0) {
                    implication.setId(nodelistRuleId.item(0).getTextContent());
                    NodeList nodelistConclusion = (NodeList) xpath.evaluate(".//head//Rel", nodeImplication, XPathConstants.NODESET);
                    if(nodelistConclusion.getLength() > 0) {
                        implication.getHeadRelation().setName(nodelistConclusion.item(0).getTextContent());
                //System.out.println("RulebaseUtil: " + implication.getHeadRelation().getName() );

                        NodeList nodelistPremiseSymbolPairs = (NodeList) xpath.evaluate(".//head//slot[count(Ind)=2]", nodeImplication, XPathConstants.NODESET);
                        for (int j = 0; j < nodelistPremiseSymbolPairs.getLength(); j++) {
                            NodeList nodelistConclusionSymbolPair = (NodeList) xpath.evaluate("./Ind", nodelistPremiseSymbolPairs.item(j), XPathConstants.NODESET);
                            if(nodelistConclusionSymbolPair.getLength() == 2) {
                                String ind1 = nodelistConclusionSymbolPair.item(0).getTextContent(); // symbol name
                                String ind2 = nodelistConclusionSymbolPair.item(1).getTextContent(); // symbol value
                                implication.getHeadRelation().getSymbols().put(ind1, ind2);
                //System.out.println("\t (" + ind1 + "," + ind2 + ")");
                            }
                        }
                    }
                    NodeList nodelistPremises = (NodeList) xpath.evaluate(".//body//Atom", nodeImplication, XPathConstants.NODESET);
                    for (int j = 0; j < nodelistPremises.getLength(); j++) {
                        Object nodePremise = nodelistPremises.item(j);
                        NodeList nodelistPremiseRelation = (NodeList) xpath.evaluate(".//Rel[not(@uri)]", nodePremise, XPathConstants.NODESET);
                        if (nodelistPremiseRelation.getLength() > 0) {
                            RulebaseRelation premiseRelation = new RulebaseRelation();
                            premiseRelation.setName(nodelistPremiseRelation.item(0).getTextContent());
                //System.out.println("  " + premiseRelation.getName() );
                            NodeList nodelistPremiseSymbolPairs = (NodeList) xpath.evaluate(".//slot[count(Ind)=2]", nodePremise, XPathConstants.NODESET);
                            for (int k = 0; k < nodelistPremiseSymbolPairs.getLength(); k++) {
                                NodeList nodelistConclusionSymbolPair = (NodeList) xpath.evaluate("./Ind", nodelistPremiseSymbolPairs.item(k), XPathConstants.NODESET);
                                if(nodelistConclusionSymbolPair.getLength() == 2) {
                                    String ind1 = nodelistConclusionSymbolPair.item(0).getTextContent(); // symbol name
                                    String ind2 = nodelistConclusionSymbolPair.item(1).getTextContent(); // symbol value
                                    premiseRelation.getSymbols().put(ind1, ind2);
                //System.out.println("\t (" + ind1 + "," + ind2 + ")");
                                }
                            }
                            implication.getPremises().add(premiseRelation);
                        }
                    }
                }
                retVal.add(implication);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static Map<String, String> retrieveFactsForRulebase() {
        Map<String, String> facts = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodelist = (NodeList) xpath.evaluate("//*/@*[starts-with(.,'" + EnvironmentProperties.rdf_namespace_prefix + "')]", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodelist.getLength(); i++) {
                String factName = nodelist.item(i).getNodeValue();
                factName = factName.substring(EnvironmentProperties.rdf_namespace_prefix.length());
                if (!factName.equals("case")) {
                    facts.put(factName, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return facts;
    }

    public static ArrayList<String> retrieveGoals() {
        ArrayList<String> goals = new ArrayList<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodelist = (NodeList) xpath.evaluate("//*/head/Atom/op/Rel", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodelist.getLength(); i++) {
                String goal = nodelist.item(i).getTextContent();
                boolean alreadyFound = false;
                for (String item : goals) {
                    if (item.equals(goal)) {
                        alreadyFound = true;
                    }
                }
                if (!alreadyFound) {
                    goals.add(goal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goals;
    }
    
    public static HashMap<String,HashMap<String,String>> getRulebaseLabels() {
            if (rulebaseLabels == null) {
            rulebaseLabels = new HashMap<String,HashMap<String,String>>();
            try {
                JsonFactory jsonFactory = new JsonFactory();
                //jsonFactory.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
                JsonParser jsonParser = jsonFactory.createJsonParser(new File(EnvironmentProperties.input_folder + EnvironmentProperties.rulebase_labels_filename));
                //jsonParser.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
                String lang = null;
                JsonToken token;
                while ((token = jsonParser.nextToken()) != null) {
                    String fieldName = jsonParser.getCurrentName();
                    if (token == JsonToken.START_OBJECT && fieldName != null) {
                        lang = fieldName;
                        rulebaseLabels.put(lang, new HashMap<>());
                    }
                    if (token == JsonToken.VALUE_STRING && lang != null) {
                        rulebaseLabels.get(lang).put(fieldName, jsonParser.getText());
                    }
                }
                jsonParser.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rulebaseLabels;
    }

}
