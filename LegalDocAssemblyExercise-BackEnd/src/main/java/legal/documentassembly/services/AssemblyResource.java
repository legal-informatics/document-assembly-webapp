package legal.documentassembly.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.ExerciseEngine;
import legal.documentassembly.bean.Exercise;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
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
        logger.debug("updating assembly...");
        exercises.get(id).setExercise(exercise);
        return true;
    }

    @GET
    @Path("{id}/indictment.xml")
    @Produces(MediaType.APPLICATION_XML)
    public String getDocument(@PathParam("id") Integer id) {
        logger.debug("document request...");
        // exercises.get(id).startReasoner();
        String response = exercises.get(id).exportDocument();
        logger.debug("...document response");
        return response;
    }

    @GET
    @Path("{id}/indictment.akn")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAkomaNtosoDocument(@PathParam("id") Integer id) {
        logger.debug("document request...");
        // exercises.get(id).startReasoner();
        String response = exercises.get(id).exportDocument();
        logger.debug("...document response");
        return Response.ok().header("Content-Disposition", "attachment; filename=\"indictment.akn\"").entity(response).build();
    }
    
    @GET
    @Path("{id}/indictment.pdf")
    @Produces("application/pdf")
    public Response getPdfDocument(@PathParam("id") Integer id) {
        try {
            String akomaNtosoDocument = exercises.get(id).exportDocument();
            FopFactory fopFactory = FopFactory.newInstance(new File(EnvironmentProperties.converter_folder + "fopConfig.xml"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(
                    new StreamSource(EnvironmentProperties.converter_folder + "xml2fop.xsl"));
            Result res = new SAXResult(fop.getDefaultHandler());
            Source src = new StreamSource( new StringReader(akomaNtosoDocument) );
            transformer.transform(src, res);

            return Response.ok().header("Content-Disposition", "attachment; filename=\"indictment.pdf\"")
                    .entity(out.toByteArray()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
        // return Response.ok().build();
    }
    
    @GET
    @Path("{id}/indictment.rtf")
    @Produces("text/rtf")
    public Response getRtfDocument(@PathParam("id") Integer id) {
        try {
            String akomaNtosoDocument = exercises.get(id).exportDocument();
            FopFactory fopFactory = FopFactory.newInstance(new File(EnvironmentProperties.converter_folder + "fopConfig.xml"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_RTF, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(
                    new StreamSource(EnvironmentProperties.converter_folder + "xml2fop.xsl"));
            Result res = new SAXResult(fop.getDefaultHandler());
            Source src = new StreamSource( new StringReader(akomaNtosoDocument) );
            transformer.transform(src, res);

            return Response.ok().header("Content-Disposition", "attachment; filename=\"indictment.rtf\"")
                    .entity(out.toByteArray()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
        // return Response.ok().build();
    }

    @GET
    @Path("{id}/indictment_{language}.vis")
    @Produces(MediaType.APPLICATION_JSON)
    public String getArgumentGraph(@PathParam("id") Integer id, @PathParam("language") String language) {
        logger.debug("graph request...");
        String graphString = exercises.get(id).buildArgumentGraph(language);
        logger.debug("...graph response");
        return graphString;
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
