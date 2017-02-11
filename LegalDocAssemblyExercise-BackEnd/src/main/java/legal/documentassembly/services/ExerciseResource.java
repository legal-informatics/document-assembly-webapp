package legal.documentassembly.services;

import java.io.File;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.Exercise;
import org.apache.log4j.Logger;

@Path("exercises")
public class ExerciseResource {

    private static final Logger logger = Logger.getLogger(ExerciseResource.class);

    @Context
    ServletContext servletContext;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Exercise> getExercises() {
        logger.debug("test " + EnvironmentProperties.input_folder);
        ArrayList<Exercise> retVal = new ArrayList();
        for (final File fileEntry : (new File(EnvironmentProperties.input_folder)).listFiles()) {
            String filename = fileEntry.getName();
            if (filename.startsWith("exercise_")) {
                retVal.add(Exercise.load(filename));
            }
        }
        return retVal;
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Exercise getExercise(@PathParam("name") String name) {
        for (final File fileEntry : (new File(EnvironmentProperties.input_folder)).listFiles()) {
            String filename = fileEntry.getName();
            if (filename.equals(name)) {
                Exercise exercise = Exercise.load(name); // fileEntry.getAbsolutePath()
                return exercise;
            }
        }
        return null;
    }

}
