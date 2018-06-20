
package fi.riista.integration.lupahallinta.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GeoSijainti complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GeoSijainti"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Leveys" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="Pituus" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeoSijainti", propOrder = {
    "leveys",
    "pituus"
})
public class LH_GeoLocation {

    @XmlElement(name = "Leveys")
    protected int leveys;
    @XmlElement(name = "Pituus")
    protected int pituus;

    /**
     * Gets the value of the leveys property.
     * 
     */
    public int getLeveys() {
        return leveys;
    }

    /**
     * Sets the value of the leveys property.
     * 
     */
    public void setLeveys(int value) {
        this.leveys = value;
    }

    /**
     * Gets the value of the pituus property.
     * 
     */
    public int getPituus() {
        return pituus;
    }

    /**
     * Sets the value of the pituus property.
     * 
     */
    public void setPituus(int value) {
        this.pituus = value;
    }

}
