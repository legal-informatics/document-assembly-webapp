package legal.documentassembly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import legal.documentassembly.EnvironmentProperties;

import legal.documentassembly.bean.Exercise;
import legal.documentassembly.bean.RuleFact;
import legal.documentassembly.bean.Step;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ReasonerUtil {

    public static String getConclusion(Exercise exercise) {
        System.out.println("Exporting facts for reasoning...");
        updateRdf(exercise);
        System.out.println("Preparing rulebase for reasoner...");
        buildRulebase(/* exercise */);
        System.out.println("Setting reasoning goal...");
        updateGoal(exercise.getRulebaseGoal());
        System.out.println("Starting the reasoner...");
        startReasoner();
        System.out.println("Retrieving reasoning result...");
        return retrieveResult();
    }

    public static void updateRdf(Exercise exercise) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.rdf_filename);
            for (Step step : exercise.getSteps()) {
                String rdfProperty = "";
                for (RuleFact ruleFact : exercise.getRuleFacts()) {
                    if (ruleFact.getName().equals(step.getRuleFact())) {
                        rdfProperty = ruleFact.getName();
                    }
                }
                Node node = doc.getElementsByTagName(EnvironmentProperties.rdf_namespace_prefix + rdfProperty).item(0);
                if (node != null) {
                    node.setTextContent(step.getAnswer());
                    if (node.hasAttributes()) {
                        Node dataTypeAttribute = node.getAttributes().getNamedItem("rdf:datatype");
                        if (dataTypeAttribute.getTextContent().contains("float")) {  // reasoner requires decimal point for float data type
                            try {
                                node.setTextContent( String.format("%.2f", Float.parseFloat(step.getAnswer())) );
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(EnvironmentProperties.reasoner_path + EnvironmentProperties.rdf_filename));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateGoal(String goals) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename);
            Node node = doc.getElementsByTagName("RuleML").item(0);
            node.getAttributes().getNamedItem("rdf_export_classes").setNodeValue(goals);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildRulebase(/* Exercise exercise */) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            Transformer transformer = tFactory.newTransformer(new StreamSource(EnvironmentProperties.lrml2drdevice_xsl_filename));
            transformer.transform(new StreamSource(EnvironmentProperties.lrml_rulebase_filename), new StreamResult(new FileOutputStream(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static synchronized boolean startReasoner() {
        try {
            prepareClipsdos();
            Process process = Runtime.getRuntime().exec(EnvironmentProperties.reasoner_path + EnvironmentProperties.start_reasoner_command, null, new File(EnvironmentProperties.reasoner_path));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            process.waitFor();
            if (process.exitValue() == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String retrieveResult() {
        String result = "";
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.result_filename);
            XPath xpath = XPathFactory.newInstance().newXPath();
            result = (String) xpath.evaluate("//*[local-name()='truthStatus']/text()", doc, XPathConstants.STRING);
            System.out.println(result);
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error while retrieving reasoning result: " + e.getMessage());
        }
        return result;
    }

    public static void prepareClipsdos() throws IOException {
        String inputFile = EnvironmentProperties.clipsdos32_filename;
        if (System.getenv("ProgramFiles(x86)") != null) { // 64-bit OS
            inputFile = EnvironmentProperties.clipsdos64_filename;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(inputFile);
            os = new FileOutputStream(EnvironmentProperties.clipsdos_filename);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

    //---------------------------- verifying confirmed conclusions

    public static ArrayList<String> confirmedConclusions(ArrayList<String> conclusions) {
    //    System.out.println("Preparing rulebase for reasoner...");
    //    buildRulebase();
        System.out.println("Setting reasoning goal...");
        String conclusionsStr = "";
        for (String conclusion: conclusions)
            conclusionsStr += (conclusionsStr.length()==0?"":" ") + conclusion;
        updateGoal(conclusionsStr);
        System.out.println("Starting the reasoner...");
        startReasoner();
        System.out.println("Retrieving reasoning result...");
        return retrievePositiveConclusions();
    }
    
    private static ArrayList<String> retrievePositiveConclusions() {
        HashSet<String> result = new HashSet<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.result_filename);
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList)xpath.evaluate("//*[truthStatus/text()='definitely-proven-positive'] | //*[truthStatus/text()='defeasibly-proven-positive']", doc, XPathConstants.NODESET);
            for (int i=0; i<nodeList.getLength(); i++) {
                String conclusionName = nodeList.item(i).getNodeName().replace("export:", "");
                result.add(conclusionName);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error while retrieving conclusions from proof: " + e.getMessage());
        }
        return new ArrayList(result);

        
    }

    @Deprecated
    public static ArrayList<String> conclusionsConfirmedByRules(ArrayList<String> conclusions) {
        System.out.println("Preparing rulebase for reasoner...");
    //    buildRulebase();
        System.out.println("Setting reasoning goal...");
        String conclusionsStr = "";
        for (String conclusion: conclusions)
            conclusionsStr += (conclusionsStr.length()==0?"":" ") + conclusion;
    //    updateGoal(conclusionsStr);
        System.out.println("Starting the reasoner...");
    //    startReasoner();
        System.out.println("Retrieving reasoning result...");
        return retrieveRuleNamesFromProof();
    }
    
    @Deprecated
    private static ArrayList<String> retrieveRuleNamesFromProof() {
        HashSet<String> result = new HashSet<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        //domFactory.setValidating(true);
        // domFactory.setExpandEntityReferences(false);
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            
            String xmlContents = new String(Files.readAllBytes(Paths.get(EnvironmentProperties.reasoner_path + EnvironmentProperties.proof_filename)), "UTF-8");
            // Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.proof_filename);
            Document doc = builder.parse(new InputSource(new StringReader(xmlContents.replaceAll("&rulebase;", ""))));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList)xpath.evaluate("//supportive_rule/rule_ref[@rule]", doc, XPathConstants.NODESET);
            for (int i=0; i<nodeList.getLength(); i++) {
                String ruleName = nodeList.item(i).getAttributes().getNamedItem("rule").getNodeValue();
                System.out.println("ruleName: " + ruleName);
                result.add(ruleName);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error while retrieving rule names from proof: " + e.getMessage());
        }
        return new ArrayList(result);
    }
    
    
}
