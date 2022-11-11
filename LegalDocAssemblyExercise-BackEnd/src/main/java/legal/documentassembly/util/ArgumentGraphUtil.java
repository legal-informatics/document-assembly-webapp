package legal.documentassembly.util;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.Exercise;
import legal.documentassembly.bean.RulebaseImplication;
import legal.documentassembly.bean.ReasonerConclusion;
import legal.documentassembly.bean.RuleFact;
import legal.documentassembly.bean.RulebaseImplication;
import legal.documentassembly.bean.RulebaseRelation;
import legal.documentassembly.bean.Step;
import legal.documentassembly.bean.TemplateFact;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.io.JsonStringEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ArgumentGraphUtil {

    private static final Logger logger = Logger.getLogger(ArgumentGraphUtil.class);
    public static final String conclusionPropertiesIndent = "conclusionProperties";

    public static String buildArgumentGraph(Exercise exercise, String language) {
        logger.info("graph building started...");
        ArrayList<RulebaseImplication> implications = RulebaseUtil.retrieveImplications();
        ArrayList<String> conclusionsToConfirm = new ArrayList<String>();
        for (RulebaseImplication imp : implications) {
            conclusionsToConfirm.add(imp.getHeadRelation().getName());
        }
        ArrayList<ReasonerConclusion> confirmedConclusions = ReasonerUtil.confirmedConclusions(exercise, conclusionsToConfirm);

        ArrayList<String> nodes = new ArrayList<String>();
        ArrayList<String> boxNodes = new ArrayList<String>();
        ArrayList<String> edges = new ArrayList<String>();
        for (RulebaseImplication imp : implications) {
            boolean confirmedHead = false;
            for (ReasonerConclusion confirmedConclusion : confirmedConclusions) {
                if (imp.getHeadRelation().confirmedBy(confirmedConclusion.getRelation())) {
                    confirmedHead = true;
                }
            }
            boolean allPremisesConfirmed = true;
            for (RulebaseRelation premise : imp.getPremises()) {
                boolean confirmedPremise = false;
                for (ReasonerConclusion confirmedConclusion : confirmedConclusions) {
                    if (premise.confirmedBy(confirmedConclusion.getRelation())) {
                        confirmedPremise = true;
                    }
                }
                if (!confirmedPremise) {
                    allPremisesConfirmed = false;
                }
            }
            if (confirmedHead && allPremisesConfirmed && imp.getPremises().size() > 0) {  // should be drawn
                if (!nodes.contains(imp.getId())) {  // represent rule
                    nodes.add(imp.getId());
                }
                if (!nodes.contains(imp.getHeadRelation().getName())) {  // represent relation
                    nodes.add(imp.getHeadRelation().getName());
                    boxNodes.add(imp.getHeadRelation().getName());
                }
                String headEdge = "{\"from\": \"" + nodes.indexOf(imp.getId()) + "\", \"to\": \"" + nodes.indexOf(imp.getHeadRelation().getName()) + "\"}";
                if(!edges.contains(headEdge))
                    edges.add(headEdge);
                for (RulebaseRelation premise : imp.getPremises()) {
                    if (!nodes.contains(premise.getName())) {
                        nodes.add(premise.getName());
                        boxNodes.add(premise.getName());
                    }
                    if (!nodes.contains(imp.getId())) {
                        nodes.add(imp.getId());
                    }
                    String premiseEdge = "{\"from\": \"" + nodes.indexOf(premise.getName()) + "\", \"to\": \"" + nodes.indexOf(imp.getId()) + "\"}";
                    if(!edges.contains(premiseEdge))
                        edges.add(premiseEdge);
                }
            }
        }
        
        HashMap<String,Integer> ruleStepPairs = getRuleStepPairs(exercise, implications);
        String retVal = "{\"nodes\":[";
        for (int i = 0; i < nodes.size(); i++) {
            String label = nodes.get(i);
            label = translateLabel(label, language, confirmedConclusions);
            
            String shape = boxNodes.contains(nodes.get(i)) ? ", \"shape\": \"box\"" : "";
            String step = ruleStepPairs.containsKey(nodes.get(i)) ? ", \"step\": \"" + ruleStepPairs.get(nodes.get(i)) + "\"" : "";
            retVal += "{\"id\": \"" + i + "\", \"label\": \"" + label + "\""
                    + shape + step + "}"
                    + ((i < nodes.size() - 1) ? "," : "") + "\n";
        }
        retVal += "],\"edges\":[";
        for (int i = 0; i < edges.size(); i++) {
            retVal += edges.get(i) + ((i < edges.size() - 1) ? "," : "") + "\n";
        }
        retVal += "]}";
        logger.info("... graph building finished");
        return retVal;
    }
    
    private static String translateLabel(String label, String language, ArrayList<ReasonerConclusion> confirmedConclusions) {
        HashMap<String,String> dictionary = RulebaseUtil.getRulebaseLabels().get(language);
        label = dictionary.containsKey(label) ? dictionary.get(label) : label;
        for (ReasonerConclusion conclusion : confirmedConclusions) {  // find conclusion
            if (label.equals(conclusion.getRelation().getName())) {           // and find property
                for(String key: conclusion.getRelation().getSymbols().keySet()) {
                    String indent = label+":"+key+"("+conclusion.getRelation().getSymbols().get(key)+")";
                    if (dictionary.containsKey(indent))
                        label = dictionary.get(indent);
                }
            }
        }
        return label;
    }
    
    private static HashMap<String, Integer> getRuleStepPairs(Exercise exercise, ArrayList<RulebaseImplication> implications) {
        HashMap<String, Integer> retVal = new HashMap<String, Integer>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.input_folder + "rulebase.ruleml");
            XPath xpath = XPathFactory.newInstance().newXPath();
            for (RulebaseImplication implication : implications) {
                for (RulebaseRelation relation : implication.getPremises()) {
                    try {
                        NodeList nodelist = (NodeList) xpath.evaluate("//Implies[head/Atom/op/Rel[text()=\"" + relation.getName() + "\"]]/body//slot/Ind[not(ancestor::Implies/head//Var/text()=../Var/text())]/@uri", doc, XPathConstants.NODESET);  // "//Implies[head/Atom/op/Rel[text()=\"" + relation + "\"]]/body//slot/Ind[not(contains(string-join(ancestor::Implies/head//Var/text(),','),../Var/text()))]/@uri"
                        for (int i = 0; i < nodelist.getLength(); i++) {
                            String factWithNamespace = nodelist.item(i).getTextContent();
                            String factName = factWithNamespace.substring(EnvironmentProperties.rdf_namespace_prefix.length());
                            for (int j = 0; j < exercise.getSteps().size(); j++) {
                                Step step = exercise.getSteps().get(j);
                                // System.out.println("factName=" + factName + ", step.getRuleFact()=" + step.getRuleFact());
                                if (step.getRuleFact() != null && step.getRuleFact().equals(factName)) {
                                    retVal.put(relation.getName(), j);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static String localizeGraph(String graphString, String language) {
        HashMap<String,HashMap<String,String>> rulebaseLabels = RulebaseUtil.getRulebaseLabels();
        for (String key: rulebaseLabels.get(language).keySet()) {
            graphString = graphString.replaceAll(key, rulebaseLabels.get(language).get(key));
        }
        return graphString;
    }
    
    @Deprecated
    public static void buildArgumentGraph() {
        System.out.println("Generating argument graph...");
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(EnvironmentProperties.proof2caf_xsl_filename));
            transformer.transform(new StreamSource(EnvironmentProperties.reasoner_path + EnvironmentProperties.proof_filename), new StreamResult(new FileOutputStream(EnvironmentProperties.caf_filename)));
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Error while generating argument graph: " + e.getMessage());
        }
    }
    
    

}
