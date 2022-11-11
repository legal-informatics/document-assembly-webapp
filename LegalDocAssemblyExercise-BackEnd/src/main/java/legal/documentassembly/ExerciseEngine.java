package legal.documentassembly;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import legal.documentassembly.bean.*;
import legal.documentassembly.util.*;

public class ExerciseEngine {

    private Exercise exercise = null;

    private String exercise_filename;
    private int currentStepIndex;

    public ExerciseEngine(String exercise_filename) {
        this.exercise_filename = exercise_filename;
        exercise = Exercise.load(exercise_filename);
        currentStepIndex = 0;
    }

    public ExerciseEngine(Exercise exercise) {
        this.exercise = exercise;
        currentStepIndex = 0;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
        currentStepIndex = 0;
    }

    public void startReasoner() {
        ReasonerUtil.getConclusion(exercise);
    }

    public String buildArgumentGraph(String language) {
        return ArgumentGraphUtil.buildArgumentGraph(exercise, language);
    }

    public String exportDocument() {
        return TemplateUtil.buildDocument(exercise);
    }

    public boolean hasNextStep() {
        return currentStepIndex + 1 < exercise.getSteps().size();
    }

    public Step getCurrentStep() {
        return exercise.getSteps().get(currentStepIndex);
    }

    public boolean stepForward() {
        currentStepIndex++;
        if (endOfExercise()) {
            return false;
        }
        return true;
    }

    public boolean stepBackward() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            return true;
        }
        return false;
    }

    public boolean endOfExercise() {
        return currentStepIndex >= exercise.getSteps().size();
    }

    public void assignAnswer(String answer) throws Exception {
        Step currentStep = exercise.getSteps().get(currentStepIndex);
        String answerType = currentStep.getAnswerType();
        if ("int".equals(answerType)) {
            try {
                Integer.parseInt(answer);
            } catch (Exception e) {
                throw new Exception("Wrong format, expected integer");
            }
        } else if ("float".equals(answerType)) {
            try {
                Float.parseFloat(answer);
            } catch (Exception e) {
                throw new Exception("Wrong format, expected float");
            }
        } else if ("date".equals(answerType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy.");
                sdf.parse(answer);
            } catch (Exception e) {
                throw new Exception("Wrong format, expected dd.mm.yyyy.");
            }
        } else if ("time".equals(answerType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                sdf.parse(answer);
            } catch (Exception e) {
                throw new Exception("Wrong format, expected hh:mm");
            }
        }
        currentStep.setAnswer(answer);
    }

}
