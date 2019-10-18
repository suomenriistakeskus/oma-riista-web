
package fi.riista.integration.metsastajarekisteri.jht;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="occupation" type="{http://riista.fi/integration/mr/jht/2018/10}occupation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "occupation"
})
@XmlRootElement(name = "jht", namespace = "http://riista.fi/integration/mr/jht/2018/10")
public class MR_JHT_Jht {

    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10")
    protected List<MR_JHT_Occupation> occupation;

    /**
     * Gets the value of the occupation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the occupation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOccupation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MR_JHT_Occupation }
     * 
     * 
     */
    public List<MR_JHT_Occupation> getOccupation() {
        if (occupation == null) {
            occupation = new ArrayList<MR_JHT_Occupation>();
        }
        return this.occupation;
    }

    public void setOccupation(List<MR_JHT_Occupation> value) {
        this.occupation = value;
    }

}
