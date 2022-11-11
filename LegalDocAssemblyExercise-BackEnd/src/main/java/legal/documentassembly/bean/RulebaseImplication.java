package legal.documentassembly.bean;

import java.util.ArrayList;

public class RulebaseImplication {

    private String id;
    private RulebaseRelation headRelation = new RulebaseRelation();
    private ArrayList<RulebaseRelation> premises = new ArrayList<RulebaseRelation>();
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public RulebaseRelation getHeadRelation() {
        return headRelation;
    }
    public void setHeadRelation(RulebaseRelation headRelation) {
        this.headRelation = headRelation;
    }

    public ArrayList<RulebaseRelation> getPremises() {
        return premises;
    }
    public void setPremises(ArrayList<RulebaseRelation> premises) {
        this.premises = premises;
    }

}
