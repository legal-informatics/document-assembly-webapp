package legal.documentassembly.bean;

import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import legal.documentassembly.EnvironmentProperties;

@XmlRootElement(name = "ExplanatoryMaterial")
public class ExplanatoryMaterial {
    
    private String id;
    private String title;
    private String content;

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name = "content")
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    public static ExplanatoryMaterial load(String filename) {
        ExplanatoryMaterial explanatoryMaterial = null;
        try {
            JAXBContext context = JAXBContext.newInstance(ExplanatoryMaterial.class);
            Unmarshaller um = context.createUnmarshaller();
            explanatoryMaterial = (ExplanatoryMaterial) um.unmarshal(new FileReader(EnvironmentProperties.input_folder + filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return explanatoryMaterial;
    }
    
}
