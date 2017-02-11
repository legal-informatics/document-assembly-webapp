package legal.documentassembly.util;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.Exercise;
import legal.documentassembly.bean.Implication;
import legal.documentassembly.bean.Step;

public class ArgumentGraphUtil {

    public static BufferedImage buildArgumentGraph(Exercise exercise) {
        ArrayList<Implication> implications = RulebaseUtil.retrieveImplications(exercise.getRulebaseGoal());
        ArrayList<String> conclusionsToConfirm = new ArrayList<String>();
        for (Implication imp : implications) {
            conclusionsToConfirm.add(imp.getHeadRelation());
        }
        ReasonerUtil.updateRdf(exercise);
        //ArrayList<String> confirmedConclusions = ReasonerUtil.conclusionsConfirmedByRules(conclusionsToConfirm);
        ArrayList<String> confirmedConclusions = ReasonerUtil.confirmedConclusions(conclusionsToConfirm);
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            HashMap<String, Object> createdElements = new HashMap<String, Object>();
            for (Implication imp : implications) {
                boolean confirmedHead = false;
                for (String confirmedConclusion : confirmedConclusions) {
                    if (imp.getHeadRelation().equals(confirmedConclusion)) {
                        confirmedHead = true;
                    }
                }
                boolean allPremisesConfirmed = true;
                for (String premise : imp.getBodyRelations()) {
                    boolean confirmedPremise = false;
                    for (String confirmedConclusion : confirmedConclusions) {
                        if (premise.equals(confirmedConclusion)) {
                            confirmedPremise = true;
                        }
                    }
                    if (!confirmedPremise) {
                        allPremisesConfirmed = false;
                    }
                }
                if (confirmedHead && allPremisesConfirmed && imp.getBodyRelations().size() > 0) {  // should be drawn
                    if (!createdElements.containsKey(imp.getId())) {
                        createdElements.put(imp.getId(), graph.insertVertex(parent, null, imp.getId(), 20, 20, 120, 40, "shape=ellipse"));
                    }
                    if (!createdElements.containsKey(imp.getHeadRelation())) {
                        createdElements.put(imp.getHeadRelation(), graph.insertVertex(parent, null, imp.getHeadRelation(), 20, 20, 120, 30));
                    }
                    graph.insertEdge(parent, null, "", createdElements.get(imp.getId()), createdElements.get(imp.getHeadRelation()));
                    for (String premise : imp.getBodyRelations()) {
                        if (!createdElements.containsKey(premise)) {
                            createdElements.put(premise, graph.insertVertex(parent, null, premise, 20, 20, 120, 30));
                        }
                        if (!createdElements.containsKey(imp.getId())) {
                            createdElements.put(imp.getId(), graph.insertVertex(parent, null, imp.getId(), 20, 20, 120, 40, "shape=ellipse"));
                        }
                        graph.insertEdge(parent, null, "", createdElements.get(premise), createdElements.get(imp.getId()));
                    }
                }
            }
            mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
            layout.setOrientation(SwingConstants.WEST);
            layout.execute(parent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        BufferedImage image = null;
        try {
            image = mxCellRenderer.createBufferedImage(graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null); // , graphComponent.getCanvas());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
    
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
    
    
    
    
    public static void main(String[] args) {
        EnvironmentProperties.input_folder = "src\\main\\resources\\input\\";
        EnvironmentProperties.proof_filename = "proof.ruleml";
        EnvironmentProperties.result_filename = "export.rdf";
        EnvironmentProperties.reasoner_path = "f:\\temp\\legalruleml\\reasoner\\";
        EnvironmentProperties.rulebase_filename = "rulebase.ruleml";
        EnvironmentProperties.clipsdos_filename = EnvironmentProperties.reasoner_path + "clipsdos\\clipsdos.exe";
        EnvironmentProperties.clipsdos32_filename = EnvironmentProperties.reasoner_path + "clipsdos\\clipsdos32.exe";
        EnvironmentProperties.clipsdos64_filename = EnvironmentProperties.reasoner_path + "clipsdos\\clipsdos64.exe";
        EnvironmentProperties.start_reasoner_command = "start.bat";

        Exercise exercise = Exercise.load("exercise_art297para2.xml");
        /*
        for (Step step: exercise.getSteps()) {
            step.setAnswer(readValue(step.getText()));
        }
*/
        BufferedImage image = buildArgumentGraph(exercise);
        File outputfile = new File(EnvironmentProperties.reasoner_path +  "argument_graph.png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    	private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

	public static String readValue(String prompt) {
		String retVal = "";
		System.out.print(prompt + ": ");
		try {
			retVal = bufferedReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}


}
