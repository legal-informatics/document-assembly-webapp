package legal.documentassembly.util;

import legal.documentassembly.bean.Step;

public class ExerciseUtil {
    
    public static Object answerToObject(Step step) {
        if ("string".equals(step.getAnswerType())) {
            return step.getAnswer();
        } else if ("int".equals(step.getAnswerType())) {
            try {
                return Integer.parseInt(step.getAnswer());
            } catch (Exception e) { }
            return 0;
        } else if ("float".equals(step.getAnswerType())) {
            try {
                return Float.parseFloat(step.getAnswer());
            } catch (Exception e) { }
            return 0;
        }
        return "";
    }
    
}
