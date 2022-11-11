package legal.documentassembly.bean;

import java.util.HashMap;

public class RulebaseRelation {

    private String name;
    private HashMap<String, String> symbols = new HashMap<String, String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getSymbols() {
        return symbols;
    }

    public void setSymbols(HashMap<String, String> symbols) {
        this.symbols = symbols;
    }

    public boolean equals(RulebaseRelation relation) {
        if (!name.equals(relation.getName())) {
            return false;
        }
        if (symbols.size() != relation.getSymbols().size()) {
            return false;
        }
        for (String key : symbols.keySet()) {
            if (!relation.getSymbols().containsKey(key)) {
                return false;
            }
            if (!relation.getSymbols().get(key).equals(symbols.get(key))) {
                return false;
            }
        }
        return true;   
    }
    
    public boolean confirmedBy(RulebaseRelation relation) {
        if (!name.equals(relation.getName())) {
            return false;
        }
        for (String key : symbols.keySet()) {
            if (!relation.getSymbols().containsKey(key)) {
                return false;
            }
            if (!relation.getSymbols().get(key).equals(symbols.get(key))) {
                return false;
            }
        }
        return true;   
    }
    
}
