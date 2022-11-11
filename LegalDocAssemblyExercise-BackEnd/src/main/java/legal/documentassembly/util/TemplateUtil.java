package legal.documentassembly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import legal.documentassembly.bean.*;
import legal.documentassembly.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import toxgene.core.Engine;
import toxgene.interfaces.ToXgeneDocumentCollection;
import toxgene.interfaces.ToXgeneReporter;
import toxgene.interfaces.ToXgeneSession;
import toxgene.util.ToXgeneReporterImpl;

public class TemplateUtil {

    public static String buildDocument(Exercise exercise) {
        String retVal = "";
        System.out.println("Exporting facts for document building...");
        exportFacts(exercise);
        System.out.println("Building the document...");
        try {
            System.setProperty("ToXgene_home", EnvironmentProperties.lib_folder);
            boolean verbose = false;
            boolean showWarnings = true;
            ToXgeneReporter tgReporter = new ToXgeneReporterImpl(verbose, showWarnings);
            ToXgeneSession session = new ToXgeneSession();
            session.reporter = tgReporter;
            session.addNewLines = true;
            session.inputPath = EnvironmentProperties.temp_folder;
            toxgene.core.Engine tgEngine = new Engine();
            tgEngine.startSession(session);
            String templateFilename = EnvironmentProperties.input_folder + exercise.getTemplateFilename();
            tgEngine.parseTemplate(new FileInputStream(templateFilename));
            ToXgeneDocumentCollection tgCollection = (ToXgeneDocumentCollection) tgEngine.getToXgeneDocumentCollections().get(0);
            tgEngine.materialize(tgCollection, new PrintStream(EnvironmentProperties.document_filename, "UTF-8"));
            
            // FileReader fileReader = new FileReader(EnvironmentProperties.document_filename);
            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(EnvironmentProperties.document_filename), "UTF-8");
            String line;
            try (BufferedReader br = new BufferedReader(fileReader)) {
                while ((line = br.readLine()) != null) {
                    retVal += line;
                }
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    private static void updateFactFilename(String templateFilename) {
        try {
            FileReader fileReader = new FileReader(templateFilename);
            String line;
            String content = "";
            try (BufferedReader br = new BufferedReader(fileReader)) {
                while ((line = br.readLine()) != null) {
                    content += line;
                }
                fileReader.close();
                content = content.replaceAll("readFrom=\"(.*?)\"", "readFrom=\"//" + EnvironmentProperties.document_facts_filename.replaceAll("\\\\", "/") + "\"");
                FileWriter fileWriter = new FileWriter(templateFilename);
                fileWriter.write(content);
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportFacts(Exercise exercise) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element rootElement = doc.createElement("fact_list");
            doc.appendChild(rootElement);
            for (Step step : exercise.getSteps()) {
                // System.out.println(step.getText() + ": " + step.isUnderRevision());
                String valueText = "";
                if (step.getAnswer() != null && !step.isUnderRevision())
                    valueText = "<span id=\"fact-" + step.getTemplateFact() + "\" style=\"color: blue; cursor: pointer\">" + unicode2htmlEntities(step.getAnswer().toString()) + "</span>";
                else
                    valueText = "<span style=\"color:gray\">&lt;&lt;" + step.getTemplateFact() + "&gt;&gt;</span>";

                appendXmlFact(doc, rootElement, step.getTemplateFact(), valueText);
            }
            ArrayList<String> conclusionsToConfirm = new ArrayList<String>();
            for (RulebaseImplication imp : RulebaseUtil.retrieveImplications()) {
                conclusionsToConfirm.add(imp.getHeadRelation().getName());
            }
            ArrayList<ReasonerConclusion> confirmedConclusions = ReasonerUtil.confirmedConclusions(exercise, conclusionsToConfirm);
            for (ReasonerConclusion conclusion : confirmedConclusions) {
                String relationName = conclusion.getRelation().getName();
                //if (conclusion.isTrue()) {
                    appendXmlFact(doc, rootElement, relationName, conclusion.getStatus());
                    for (String key : conclusion.getRelation().getSymbols().keySet()) {
                        appendXmlFact(doc, rootElement, relationName + ":" + key, conclusion.getRelation().getSymbols().get(key));
                    }
                //}
                //appendXmlFact(doc, rootElement, conclusion, results.containsKey(conclusion) ? results.get(conclusion) : "");
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult file = new StreamResult(new File(EnvironmentProperties.document_facts_filename).toURI().getPath());  // without toURI().getPath() throws error FileNotFoundException
            transformer.transform(source, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    private static void appendXmlFact(Document doc, Element parentElement, String factName, String factValue) {
        Element factEl = doc.createElement("fact");
        Element nameEl = doc.createElement("name");
        nameEl.setTextContent(factName);
        Element valueEl = doc.createElement("value");
        valueEl.setTextContent(factValue);
        factEl.appendChild(nameEl);
        factEl.appendChild(valueEl);
        parentElement.appendChild(factEl);
    }

    public static Map<String, String> retrieveFactsForTemplate(String templateFilename) {
        HashMap<String, String> retVal = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(EnvironmentProperties.input_folder + templateFilename));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } finally {
                reader.close();
            }
            String regex = "where=\"EQ\\(\\[name\\]\\,\\'([a-zA-Z_$][a-zA-Z_$0-9]*)\\'\\)\""; //   "\\b(\\d{3})(\\d{3})(\\d{4})\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(stringBuilder.toString());
            while (matcher.find()) {
                retVal.put(matcher.group(1), "string");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    private static String unicode2htmlEntities(String text) {
        String retVal = "";
        for (char c: text.toCharArray()) {
            switch (c) {
                case '\u0160': retVal += "&#352;"; break; //Sh
                case '\u0161': retVal += "&#353;"; break; //sh
                case '\u017d': retVal += "&#381;"; break; //Zz
                case '\u017e': retVal += "&#382;"; break; //zz
                case '\u010c': retVal += "&#268;"; break; //Ch
                case '\u010d': retVal += "&#269;"; break; //ch
                case '\u0106': retVal += "&#262;"; break; //Cc
                case '\u0107': retVal += "&#263;"; break; //cc
                case '\u0110': retVal += "&#272;"; break; //Dj
                case '\u0111': retVal += "&#273;"; break; //dj
                default: retVal += c;
            }
        }
        return retVal;
    }

}
