package legal.documentassembly.bean;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import legal.documentassembly.EnvironmentProperties;

@XmlRootElement(name = "Exercise")
public class Exercise {

    private String name;
    private String rulebaseGoal;
    private String templateFilename;
    private ArrayList<Step> steps = new ArrayList<>();

    private ArrayList<RuleFact> ruleFacts = new ArrayList<>();
    private ArrayList<TemplateFact> templateFacts = new ArrayList<>();

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "rulebaseGoal")
    public String getRulebaseGoal() {
        return rulebaseGoal;
    }

    public void setRulebaseGoal(String rulebaseGoal) {
        this.rulebaseGoal = rulebaseGoal;
    }

    @XmlAttribute(name = "templateFilename")
    public String getTemplateFilename() {
        return templateFilename;
    }

    public void setTemplateFilename(String templateFilename) {
        this.templateFilename = templateFilename;
    }

    @XmlElementWrapper(name = "Steps")
    @XmlElement(name = "Step")
    public ArrayList<Step> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }

    @XmlElementWrapper(name = "RuleFacts")
    @XmlElement(name = "RuleFact")
    public ArrayList<RuleFact> getRuleFacts() {
        return ruleFacts;
    }

    public void setRuleFacts(ArrayList<RuleFact> ruleFacts) {
        this.ruleFacts = ruleFacts;
    }

    @XmlElementWrapper(name = "TemplateFacts")
    @XmlElement(name = "TemplateFact")
    public ArrayList<TemplateFact> getTemplateFacts() {
        return templateFacts;
    }

    public void setTemplateFacts(ArrayList<TemplateFact> templateFacts) {
        this.templateFacts = templateFacts;
    }

    public static Exercise load(String filename) {
        Exercise exercise = null;
        try {
            JAXBContext context = JAXBContext.newInstance(Exercise.class);
            Unmarshaller um = context.createUnmarshaller();
            exercise = (Exercise) um.unmarshal(new FileReader(EnvironmentProperties.input_folder + filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exercise;
    }

    public void save(String filename) {
        try {
            JAXBContext context = JAXBContext.newInstance(Exercise.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
