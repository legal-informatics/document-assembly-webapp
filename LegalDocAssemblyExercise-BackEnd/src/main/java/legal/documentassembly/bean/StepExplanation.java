package legal.documentassembly.bean;

import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import legal.documentassembly.EnvironmentProperties;

@XmlRootElement(name = "StepExplanation")
public class StepExplanation {
    
    private String ref;

    @XmlAttribute(name = "ref")
    public String getRef() {
        return ref;
    }
    public void setRef(String ref) {
        this.ref = ref;
    }

}
