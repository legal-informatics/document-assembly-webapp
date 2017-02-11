package legal.documentassembly.services;

import java.io.File;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import legal.documentassembly.EnvironmentProperties;
import legal.documentassembly.bean.ExplanatoryMaterial;
import org.apache.log4j.Logger;

@Path("explanatoryMaterial")
public class ExplanatoryMaterialResource {

    private static final Logger logger = Logger.getLogger(ExerciseResource.class);

    @Context
    ServletContext servletContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ExplanatoryMaterial> getExplanatoryMaterials() {
        ArrayList<ExplanatoryMaterial> retVal = new ArrayList();
        for (final File fileEntry : (new File(EnvironmentProperties.input_folder)).listFiles()) {
            String filename = fileEntry.getName();
            if (filename.startsWith("explanatory_")) {
                retVal.add(ExplanatoryMaterial.load(filename));
            }
        }
        return retVal;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExplanatoryMaterial getExplanatoryMaterial(@PathParam("id") String id) {
        for (final File fileEntry : (new File(EnvironmentProperties.input_folder)).listFiles()) {
            String filename = fileEntry.getName();
            if (filename.equals(id)) {
                ExplanatoryMaterial explanatoryMaterial = ExplanatoryMaterial.load(id); // fileEntry.getAbsolutePath()
                return explanatoryMaterial;
            }
        }
        return null;
    }

}
