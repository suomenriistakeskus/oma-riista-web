
package fi.riista.integration.lupahallinta.permitarea;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for partner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="partner"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="officialCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameSwedish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="totalAreaSize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="waterAreaSize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partner", namespace = "http://riista.fi/integration/lupahallinta/export/permitarea", propOrder = {
    "officialCode",
    "nameFinnish",
    "nameSwedish",
    "totalAreaSize",
    "waterAreaSize"
})
public class LHPA_Partner {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String officialCode;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nameFinnish;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nameSwedish;
    protected long totalAreaSize;
    protected long waterAreaSize;

    /**
     * Gets the value of the officialCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficialCode() {
        return officialCode;
    }

    /**
     * Sets the value of the officialCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficialCode(String value) {
        this.officialCode = value;
    }

    /**
     * Gets the value of the nameFinnish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameFinnish() {
        return nameFinnish;
    }

    /**
     * Sets the value of the nameFinnish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameFinnish(String value) {
        this.nameFinnish = value;
    }

    /**
     * Gets the value of the nameSwedish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameSwedish() {
        return nameSwedish;
    }

    /**
     * Sets the value of the nameSwedish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameSwedish(String value) {
        this.nameSwedish = value;
    }

    /**
     * Gets the value of the totalAreaSize property.
     * 
     */
    public long getTotalAreaSize() {
        return totalAreaSize;
    }

    /**
     * Sets the value of the totalAreaSize property.
     * 
     */
    public void setTotalAreaSize(long value) {
        this.totalAreaSize = value;
    }

    /**
     * Gets the value of the waterAreaSize property.
     * 
     */
    public long getWaterAreaSize() {
        return waterAreaSize;
    }

    /**
     * Sets the value of the waterAreaSize property.
     * 
     */
    public void setWaterAreaSize(long value) {
        this.waterAreaSize = value;
    }

}
