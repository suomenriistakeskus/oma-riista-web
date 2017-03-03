
package fi.riista.integration.lupahallinta.permitarea;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;


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
 *         &lt;element name="officialCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="nameSwedish" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="state" type="{http://riista.fi/integration/lupahallinta/export/permitarea}state"/&gt;
 *         &lt;element name="lastModified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="totalAreaSize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="waterAreaSize" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rhy" type="{http://riista.fi/integration/lupahallinta/export/permitarea}nameWithOfficialCode" maxOccurs="unbounded"/&gt;
 *         &lt;element name="hta" type="{http://riista.fi/integration/lupahallinta/export/permitarea}nameWithOfficialCode" maxOccurs="unbounded"/&gt;
 *         &lt;element name="partners" type="{http://riista.fi/integration/lupahallinta/export/permitarea}partner" maxOccurs="unbounded"/&gt;
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
    "officialCode",
    "nameFinnish",
    "nameSwedish",
    "state",
    "lastModified",
    "totalAreaSize",
    "waterAreaSize",
    "rhy",
    "hta",
    "partners"
})
@XmlRootElement(name = "permitArea", namespace = "http://riista.fi/integration/lupahallinta/export/permitarea")
public class LHPA_PermitArea {

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
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected LHPA_State state;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime lastModified;
    protected long totalAreaSize;
    protected long waterAreaSize;
    @XmlElement(required = true)
    protected List<LHPA_NameWithOfficialCode> rhy;
    @XmlElement(required = true)
    protected List<LHPA_NameWithOfficialCode> hta;
    @XmlElement(required = true)
    protected List<LHPA_Partner> partners;

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
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link LHPA_State }
     *     
     */
    public LHPA_State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link LHPA_State }
     *     
     */
    public void setState(LHPA_State value) {
        this.state = value;
    }

    /**
     * Gets the value of the lastModified property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Sets the value of the lastModified property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModified(LocalDateTime value) {
        this.lastModified = value;
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

    /**
     * Gets the value of the rhy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rhy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRhy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LHPA_NameWithOfficialCode }
     * 
     * 
     */
    public List<LHPA_NameWithOfficialCode> getRhy() {
        if (rhy == null) {
            rhy = new ArrayList<LHPA_NameWithOfficialCode>();
        }
        return this.rhy;
    }

    /**
     * Gets the value of the hta property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hta property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHta().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LHPA_NameWithOfficialCode }
     * 
     * 
     */
    public List<LHPA_NameWithOfficialCode> getHta() {
        if (hta == null) {
            hta = new ArrayList<LHPA_NameWithOfficialCode>();
        }
        return this.hta;
    }

    /**
     * Gets the value of the partners property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partners property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartners().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LHPA_Partner }
     * 
     * 
     */
    public List<LHPA_Partner> getPartners() {
        if (partners == null) {
            partners = new ArrayList<LHPA_Partner>();
        }
        return this.partners;
    }

    public void setRhy(List<LHPA_NameWithOfficialCode> value) {
        this.rhy = value;
    }

    public void setHta(List<LHPA_NameWithOfficialCode> value) {
        this.hta = value;
    }

    public void setPartners(List<LHPA_Partner> value) {
        this.partners = value;
    }

}
