package legal.documentassembly.bean;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlType
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {

    private String text;
    private String ruleFact;
    private String templateFact;
    // private QuestionType answer;
    private String answer;
    private String answerType;
    private ArrayList<StepExplanation> stepExplanations = new ArrayList<>();


    public Step() {

    }

    public Step(String text) {
        this.text = text;
    }

    @XmlAttribute(name = "text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlAttribute(name = "ruleFact")
    public String getRuleFact() {
        return ruleFact;
    }

    public void setRuleFact(String ruleFact) {
        this.ruleFact = ruleFact;
    }

    @XmlAttribute(name = "templateFact")
    public String getTemplateFact() {
        return templateFact;
    }

    public void setTemplateFact(String templateFact) {
        this.templateFact = templateFact;
    }

    /*
	public QuestionType getAnswer() {
		return answer;
	}
	public void setAnswer(QuestionType answer) {
		this.answer = answer;
	}
     */
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @XmlAttribute(name = "answerType")
    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    @XmlElement(name = "StepExplanations")
    public ArrayList<StepExplanation> getStepExplanations() {
        return stepExplanations;
    }
    public void setStepExplanations(ArrayList<StepExplanation> stepExplanations) {
        this.stepExplanations = stepExplanations;
    }

}
