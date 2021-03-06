
package fi.riista.integration.koulutusportaali.jht;

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
 *         &lt;element name="suoritus" type="{http://riista.fi/integration/koulutusportaali/jht/2016/06}Suoritus" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "suoritus"
})
@XmlRootElement(name = "suoritukset", namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
public class JHT_Suoritukset {

    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
    protected List<JHT_Suoritus> suoritus;

    /**
     * Gets the value of the suoritus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the suoritus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuoritus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JHT_Suoritus }
     * 
     * 
     */
    public List<JHT_Suoritus> getSuoritus() {
        if (suoritus == null) {
            suoritus = new ArrayList<JHT_Suoritus>();
        }
        return this.suoritus;
    }

    public void setSuoritus(List<JHT_Suoritus> value) {
        this.suoritus = value;
    }

}
