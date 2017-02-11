
package legal.documentassembly.bean;

import java.util.ArrayList;

public class Implication {
    
    private String id;
    private String headRelation;
    private ArrayList<String> bodyRelations = new ArrayList<String>();
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getHeadRelation() {
        return headRelation;
    }
    public void setHeadRelation(String headRelation) {
        this.headRelation = headRelation;
    }

    public ArrayList<String> getBodyRelations() {
        return bodyRelations;
    }
    public void setBodyRelations(ArrayList<String> bodyRelations) {
        this.bodyRelations = bodyRelations;
    }
    
}
