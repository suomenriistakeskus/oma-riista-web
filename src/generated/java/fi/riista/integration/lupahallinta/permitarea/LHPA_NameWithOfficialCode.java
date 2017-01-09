
package fi.riista.integration.lupahallinta.permitarea;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for nameWithOfficialCode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nameWithOfficialCode"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="officialCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameSwedish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="areaSize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameWithOfficialCode", namespace = "http://riista.fi/integration/lupahallinta/export/permitarea", propOrder = {
    "officialCode",
    "nameFinnish",
    "nameSwedish",
    "areaSize"
})
public class LHPA_NameWithOfficialCode {

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
    protected long areaSize;

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
     * Gets the value of the areaSize property.
     * 
     */
    public long getAreaSize() {
        return areaSize;
    }

    /**
     * Sets the value of the areaSize property.
     * 
     */
    public void setAreaSize(long value) {
        this.areaSize = value;
    }

}
