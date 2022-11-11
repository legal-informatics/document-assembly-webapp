package legal.documentassembly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import legal.documentassembly.bean.ReasonerConclusion;
import legal.documentassembly.bean.Step;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.NTripleWriter;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ReasonerUtil {
    
    private static final Logger logger = Logger.getLogger(ReasonerUtil.class);

    private static String previousAnswers = ""; // for optimization purposes
    private static String currentAnswers = "";
    
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

    private static void updateRdf(Exercise exercise) {
        previousAnswers = currentAnswers;
        currentAnswers = "";
        // cleaning fact list from previous exercise
        Model model = FileManager.get().loadModel(EnvironmentProperties.reasoner_path + "facts.n3"); // ModelFactory.createDefaultModel();
        for (Statement stmt: model.listStatements().toList()) {
            if (stmt.asTriple().getObject().isLiteral()) {
                RDFDatatype type = stmt.asTriple().getObject().getLiteralDatatype();
                if (type.equals(XSDDatatype.XSDint)) {
                    stmt.changeObject(model.createTypedLiteral("",XSDDatatype.XSDint));
                } else if (type.equals(XSDDatatype.XSDfloat)) {
                    stmt.changeObject(model.createTypedLiteral("",XSDDatatype.XSDfloat));
                } else if (type.equals(XSDDatatype.XSDstring))  {
                    stmt.changeObject("");
                }
            }
        }

        Resource caseA1 = model.getResource(EnvironmentProperties.rdf_case_namespace + "a1");
        for (Step step : exercise.getSteps()) {
            if (step.getRuleFact() != null && !step.getRuleFact().equals("")) {
                StmtIterator iter = model.listStatements(caseA1, model.createProperty(EnvironmentProperties.rdf_ontology_namespace + step.getRuleFact()), (RDFNode)null);
                if (iter.hasNext()) {
                    Statement statement = iter.nextStatement();
                    String answer = step.isUnderRevision() ? "" : step.getAnswer();
                    currentAnswers += answer;
                        if ("string".equals(step.getAnswerType())) {
                            statement.changeObject(answer);
                        } else if ("int".equals(step.getAnswerType())) {
                            try {
                                statement.changeLiteralObject(Integer.parseInt(answer));
                            } catch (Exception e) {
                                statement.changeObject(model.createTypedLiteral("",XSDDatatype.XSDint));
                            }
                        } else if ("float".equals(step.getAnswerType())) {
                            try {
                                statement.changeLiteralObject(Float.parseFloat(answer));
                            } catch (Exception e) {
                                statement.changeObject(model.createTypedLiteral("",XSDDatatype.XSDfloat));
                            }
                        } else if (step.getAnswerType().startsWith("enum")) {
                            String value = answer;
                            for (String language: RulebaseUtil.getRulebaseLabels().keySet()) {
                                for (String key: RulebaseUtil.getRulebaseLabels().get(language).keySet()) {
                                    if (RulebaseUtil.getRulebaseLabels().get(language).get(key).equals(answer)) {
                                        value = key;  // reverse translation through dictionaries
                                    }
                                }
                            }
                            statement.changeObject(value);
                        }
                    // }
                }
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            // fileOutputStream = new FileOutputStream(EnvironmentProperties.reasoner_path + "facts.n3" /*+ EnvironmentProperties.rdf_filename*/);
            // model.write(fileOutputStream, "N-TRIPLE");
            MyNTripleWriter myNTripleWriter = new MyNTripleWriter();
            myNTripleWriter.write(model, EnvironmentProperties.reasoner_path + "facts.n3");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            StreamResult result = new StreamResult(new File(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename).toURI().getPath());  // without toURI().getPath() throws error FileNotFoundException
            transformer.transform(source, result);
            
            // also update goals for optimized rulebase (file: rulebase-comp.bat)
            File precompiledFolder = new File(EnvironmentProperties.precompiled_rulebase_folder);
            if (precompiledFolder.exists()) {
                FileReader fileReader = new FileReader(EnvironmentProperties.reasoner_path + EnvironmentProperties.optimized_reasoner_script_filename);
                String line;
                String content = "";
                try (BufferedReader br = new BufferedReader(fileReader)) {
                    while ((line = br.readLine()) != null) {
                        Pattern r = Pattern.compile("\\((\\S+ \\S+ \\S+ \\S+) (.*)\\)");
                        Matcher m = r.matcher(line);
                        if (m.find()) {
                            line = "(" + m.group(1) + " " + goals + ")";
                        }
                        content += line + "\n";
                    }
                    fileReader.close();
                    FileWriter fileWriter = new FileWriter(EnvironmentProperties.reasoner_path + EnvironmentProperties.optimized_reasoner_script_filename);
                    fileWriter.write(content);
                    fileWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildRulebase() {
        try {
            File precompiledFolder = new File(EnvironmentProperties.precompiled_rulebase_folder);
            if (precompiledFolder.exists()) {  // optimized
                for (File file: precompiledFolder.listFiles()) {
                    Files.copy(file.toPath(), new File(EnvironmentProperties.reasoner_path + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                TransformerFactory tFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
                Transformer transformer = tFactory.newTransformer(new StreamSource(EnvironmentProperties.lrml2drdevice_xsl_filename));
                transformer.transform(new StreamSource(EnvironmentProperties.lrml_rulebase_filename), new StreamResult(new FileOutputStream(EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static synchronized boolean startReasoner() {
        logger.info("reasoner started...");
        if (!currentAnswers.equals(previousAnswers)) {  // no need for reasoning if data was not changed
            try {
                prepareClipsdos();
                String startCommand = EnvironmentProperties.start_reasoner_command;
                File precompiledFolder = new File(EnvironmentProperties.precompiled_rulebase_folder);
                if (precompiledFolder.exists()) {
                    startCommand = EnvironmentProperties.start_optimized_reasoner_command;
                }
                Process process = Runtime.getRuntime().exec("\"" + EnvironmentProperties.reasoner_path.replaceAll("/", "\\\\") + startCommand + "\"", null, new File(EnvironmentProperties.reasoner_path)); // exec on MS requires backslash as folder separator
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
                input.close();
                process.waitFor();
                if (process.exitValue() == 0) {
                    logger.info("...reasoner finished");
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info("...reasoner finished");
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

    public static synchronized ArrayList<ReasonerConclusion> confirmedConclusions(Exercise exercise, ArrayList<String> conclusions) {
        logger.info("Preparing rulebase for reasoner...");
        buildRulebase();
        logger.info("Setting reasoning goal...");
        HashSet<String> uniqueConclusions = new HashSet<String>(conclusions);
        String conclusionsStr = "";
        for (String conclusion: uniqueConclusions)
            conclusionsStr += (conclusionsStr.length()==0?"":" ") + conclusion;
        updateGoal(conclusionsStr);
        logger.info("Exporting facts for reasoning...");
        updateRdf(exercise);
        logger.info("Starting the reasoner...");
        startReasoner();
        logger.info("Retrieving reasoning result...");
        return retrievePositiveConclusions();
    }

    @Deprecated
    public static ArrayList<ReasonerConclusion> conclusionsResults(ArrayList<String> conclusions) {
        System.out.println("Preparing rulebase for reasoner...");
        buildRulebase();
        System.out.println("Setting reasoning goal...");
        HashSet<String> uniqueConclusions = new HashSet<String>(conclusions);
        String conclusionsStr = "";
        for (String conclusion: uniqueConclusions)
            conclusionsStr += (conclusionsStr.length()==0?"":" ") + conclusion;
        updateGoal(conclusionsStr);
        System.out.println("Starting the reasoner...");
        startReasoner();
        System.out.println("Retrieving reasoning result...");
        return retrieveConclusionsResults(conclusions);
    }
    
    private static ArrayList<ReasonerConclusion> retrievePositiveConclusions() {
        ArrayList<ReasonerConclusion> result = new ArrayList<ReasonerConclusion>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.result_filename);
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList)xpath.evaluate("//*[truthStatus/text()='definitely-proven-positive'] | //*[truthStatus/text()='defeasibly-proven-positive']", doc, XPathConstants.NODESET);
            for (int i=0; i<nodeList.getLength(); i++) {
                String conclusionName = nodeList.item(i).getNodeName().replace("export:", "");
                ReasonerConclusion reasonerConclusion = new ReasonerConclusion();
                reasonerConclusion.getRelation().setName(conclusionName);
                for (int j=0; j<nodeList.item(i).getChildNodes().getLength(); j++) {
                    Node child = nodeList.item(i).getChildNodes().item(j);
                    String childName = child.getNodeName();
                    childName = childName.substring(childName.indexOf(":")+1); // ommitts prefix, if any is present
                    String childValue = child.getTextContent();
                    reasonerConclusion.getRelation().getSymbols().put(childName, childValue);
                    if ("truthStatus".equals(childName)) {
                        reasonerConclusion.setStatus(childValue);
                    }
                }
                result.add(reasonerConclusion);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error while retrieving conclusions from proof: " + e.getMessage());
        }
        return result;
    }

    private static ArrayList<ReasonerConclusion> retrieveConclusionsResults(ArrayList<String> conclusions) {
        ArrayList<ReasonerConclusion> result = new ArrayList<ReasonerConclusion>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.reasoner_path + EnvironmentProperties.result_filename);
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList)xpath.evaluate("//*[truthStatus/text()]", doc, XPathConstants.NODESET);
            for (int i=0; i<nodeList.getLength(); i++) {
                String conclusionName = nodeList.item(i).getNodeName().replace("export:", "");
                ReasonerConclusion reasonerConclusion = new ReasonerConclusion();
                reasonerConclusion.getRelation().setName(conclusionName);
                for (int j=0; j<nodeList.item(i).getChildNodes().getLength(); j++) {
                    Node child = nodeList.item(i).getChildNodes().item(j);
                    String childName = child.getNodeName();
                    childName = childName.substring(childName.indexOf(":")+1); // ommitts prefix, if any is present
                    String childValue = child.getTextContent();
                    reasonerConclusion.getRelation().getSymbols().put(childName, childValue);
                    if ("truthStatus".equals(childName)) {
                        reasonerConclusion.setStatus(childValue);
                    }
                }
                result.add(reasonerConclusion);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error while retrieving conclusions from proof: " + e.getMessage());
        }
        return result;
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
