package legal.documentassembly.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import legal.documentassembly.ExerciseEngine;
import legal.documentassembly.bean.Exercise;
import org.apache.log4j.Logger;

@Path("assembly")
public class AssemblyResource {
    private static final Logger logger = Logger.getLogger(AssemblyResource.class);
    private static ArrayList<ExerciseEngine> exercises = new ArrayList<>();
    
    @Context
    ServletContext servletContext;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Integer startAssembling(Exercise exercise) {
        Integer retVal = exercises.size();
        exercises.add(new ExerciseEngine(exercise));
        return retVal;
    }

    
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean updateAssemblingState(@PathParam("id") Integer id, Exercise exercise) {
        exercises.get(id).setExercise(exercise);
        return true;
    }
/*
    @OPTIONS
    @Path("{id}")
    public Response optionsMethod() {
        System.out.println("### Options method called ###");
        return Response
            .ok("")
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
            .header("Access-Control-Allow-Credentials", "true")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            .header("Access-Control-Max-Age", "1209600")
            .build();
    }
*/
    /*
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean endOfExercise() {
        return engine.endOfExercise();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Step getCurrentStep() {
        return engine.getCurrentStep();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String assignAnswer(String answer) {
        try {
            engine.assignAnswer(answer);
            engine.stepForward();
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void stepForward() {
        
    }
    */

    @GET
    @Path("{id}/indictment.xml")
    @Produces(MediaType.APPLICATION_XML)
    public String getDocument(@PathParam("id") Integer id) {
        // exercises.get(id).startReasoner();
        return exercises.get(id).exportDocument();
    }

    @GET
    @Path("{id}/indictment.png")
    @Produces("image/png")
    public Response getArgumentGraph(@PathParam("id") Integer id) {
        System.out.println("argument graph requested for assembly: " + id);
        BufferedImage image = exercises.get(id).buildArgumentGraph();
        if (image == null)
            image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] imageData = baos.toByteArray();
        // return Response.ok(imageData).build();
        return Response.ok(new ByteArrayInputStream(imageData)).build();
    }

    @GET
    @Path("{id}/explanatory/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer getExplanatory(@PathParam("id") Integer id) {
        ExerciseEngine exercise = exercises.get(id);
        return 1;
    }

    @GET
    @Path("{id}/explanatory/{num}")
    @Produces(MediaType.APPLICATION_XML)
    public String getExplanatory(@PathParam("id") Integer id, @PathParam("num") Integer num) {
        ExerciseEngine exercise = exercises.get(id);
        return "this is an explanatory material";
    }

}
