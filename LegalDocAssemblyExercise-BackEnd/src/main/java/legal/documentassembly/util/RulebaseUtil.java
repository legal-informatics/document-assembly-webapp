package legal.documentassembly.util;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.Implication;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class RulebaseUtil {

    public static ArrayList<Implication> retrieveImplications(String conclusion) {
        ArrayList<Implication> retVal = new ArrayList<Implication>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(EnvironmentProperties.input_folder + "rulebase.ruleml"); // EnvironmentProperties.reasoner_path + EnvironmentProperties.rulebase_filename);
            
            retrieveImplications(retVal, doc, conclusion);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    private static void retrieveImplications(ArrayList<Implication> implications, Document doc, String conclusion) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodelist = (NodeList) xpath.evaluate("//head//Rel[text()=\"" + conclusion + "\"]/ancestor::Implies/oid/Ind[text()]", doc, XPathConstants.NODESET); //   "//head//Rel[text()=\"" + conclusion + "\"]/ancestor::Implies/body//Rel[text()]"
            for (int i = 0; i < nodelist.getLength(); i++) {
                String ruleId = nodelist.item(i).getTextContent();
                boolean alreadyExist = false;
                for (Implication imp : implications)
                    if (imp.getId().equals(ruleId))
                        alreadyExist = true;
                if (!alreadyExist) {
                    NodeList nodelist2 = (NodeList) xpath.evaluate("//Implies[oid/Ind[text()=\"" + ruleId + "\"]]/body//Rel[text()]", doc, XPathConstants.NODESET);
                    Implication implication = new Implication();
                    implication.setId(ruleId);
                    implication.setHeadRelation(conclusion);
                    for (int j = 0; j < nodelist2.getLength(); j++) {
                        String premiseRelation = nodelist2.item(j).getTextContent();
                        implication.getBodyRelations().add(premiseRelation);
                        retrieveImplications(implications, doc, premiseRelation);
                    }
                    implications.add(implication);
                    // System.out.println("#" + ruleId + ":\t" + conclusion + "\t" + implication.getBodyRelations());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EnvironmentProperties.reasoner_path = "f:\\temp\\legalruleml\\reasoner\\";
        EnvironmentProperties.rulebase_filename = "rulebase.ruleml";
        ArrayList<Implication> implications = retrieveImplications("committed_art289para1");
        for (Implication imp: implications) {
            System.out.println(imp.getHeadRelation());
            for (String premise: imp.getBodyRelations())
                System.out.println("\t" + premise);
        }
        
        for (Implication imp: implications) {
            if (imp.getId().equals("cc_art289para1a"))
                imp.getBodyRelations().add("property_damage");
        }
        
        System.out.println("\n---------------\n");
        for (String confirmedConclusion: new String[] {"cc_art289para1a", "lorts_art43a", "lorts_art43b"}) {
            for (Implication imp: implications)
            if (confirmedConclusion.equals(imp.getId())) {
                // System.out.println("#" + imp.getId() + ":\t" + imp.getHeadRelation() + "\t" + imp.getBodyRelations());
                for (String premise: imp.getBodyRelations())
                    System.out.println(premise + " -> [" + imp.getId() + "]");
                if (imp.getBodyRelations().size() > 0)
                    System.out.println("[" + imp.getId() + "] -> " + imp.getHeadRelation());
            }
        }
        
        
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {

            HashMap<String, Object> createdElements = new HashMap<String, Object>();
            for (String confirmedConclusion : new String[]{"cc_art289para1a", "lorts_art43a", "lorts_art43b"}) {
                for (Implication imp : implications) {
                    if (confirmedConclusion.equals(imp.getId())) {
                        for (String premise : imp.getBodyRelations()) {
                            if (!createdElements.containsKey(premise)) {
                                createdElements.put(premise, graph.insertVertex(parent, null, premise, 20, 20, 80, 30));
                            }
                            if (!createdElements.containsKey(imp.getId())) {
                                createdElements.put(imp.getId(), graph.insertVertex(parent, null, imp.getId(), 20, 20, 80, 30, "shape=ellipse"));
                            }
                            graph.insertEdge(parent, null, "", createdElements.get(premise), createdElements.get(imp.getId()));
                        }
                        if (imp.getBodyRelations().size() > 0) {
                            if (!createdElements.containsKey(imp.getId())) {
                                createdElements.put(imp.getId(), graph.insertVertex(parent, null, imp.getId(), 20, 20, 80, 30, "shape=ellipse"));
                            }
                            if (!createdElements.containsKey(imp.getHeadRelation())) {
                                createdElements.put(imp.getHeadRelation(), graph.insertVertex(parent, null, imp.getHeadRelation(), 20, 20, 80, 30));
                            }
                            graph.insertEdge(parent, null, "", createdElements.get(imp.getId()), createdElements.get(imp.getHeadRelation()));
                        }
                    }
                }
            }

            mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
            layout.setOrientation(SwingConstants.WEST);
            layout.execute(parent);
        } finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        JFrame window = new JFrame();
        window.getContentPane().add(graphComponent);
        window.setVisible(true);
        
        try {
        BufferedImage image = mxCellRenderer.createBufferedImage(graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());
        mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
        //param.setCompressedText(new String[] { "mxGraphModel", erXmlString });
        FileOutputStream outputStream = new FileOutputStream("test.png");
        mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
        if (image != null) {
            encoder.encode(image);
            //return image;
        }
        outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        Graph newGraph = new Graph("new graph", true, false);
        Reader reader = new StringReader(testDot);
        Parser parser = new Parser(reader, new PrintWriter(System.out), newGraph);
        try {
            parser.parse();
        } catch (Exception e) {
           System.out.println(e);
        }
        newGraph.printGraph(System.out);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Create an instance of org.w3c.dom.Document
            // Document document = new DOMImplementation() createDocument(null, "svg", null);
            GrappaPanel grappaPanel = new GrappaPanel(newGraph);
            grappaPanel.setScaleToFit(true);

            JFrame window = new JFrame();
            window.add(grappaPanel);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLocation(10, 30);
            window.setSize(640, 480);
            window.setVisible(true);

//        grappaPanel.repaint();
            // Create an instance of the SVG Generator
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            // Render the GraphPanel into the SVG Graphics2D implementation
            grappaPanel.paint(svgGenerator);
            FileOutputStream fileout = new FileOutputStream("graph.svg");
            Writer out = new OutputStreamWriter(fileout, "UTF-8");
            svgGenerator.stream(out, true);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
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

}
