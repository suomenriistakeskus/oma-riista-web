
package fi.riista.integration.luke_export.deerharvests;

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
 *         &lt;element name="permits" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Permit" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "permits"
})
@XmlRootElement(name = "permits", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
public class LED_Permits {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_Permit> permits;

    /**
     * Gets the value of the permits property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permits property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermits().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_Permit }
     * 
     * 
     */
    public List<LED_Permit> getPermits() {
        if (permits == null) {
            permits = new ArrayList<LED_Permit>();
        }
        return this.permits;
    }

    public void setPermits(List<LED_Permit> value) {
        this.permits = value;
    }

}
