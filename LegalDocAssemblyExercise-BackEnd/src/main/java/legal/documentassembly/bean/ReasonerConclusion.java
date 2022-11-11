package legal.documentassembly.bean;

public class ReasonerConclusion {

    private RulebaseRelation relation = new RulebaseRelation();
    private String status;

    public RulebaseRelation getRelation() {
        return relation;
    }
    public void setRelation(RulebaseRelation relation) {
        this.relation = relation;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isTrue() {
        return "definitely-proven-positive".equals(status) || "defeasibly-proven-positive".equals(status);
    }
}
