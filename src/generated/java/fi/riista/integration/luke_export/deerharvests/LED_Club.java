
package fi.riista.integration.luke_export.deerharvests;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Club complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Club"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="clubOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}geoLocation" minOccurs="0"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="groups" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Group" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="huntingSummary" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}HuntingSummary" minOccurs="0"/&gt;
 *         &lt;element name="overrides" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Overrides" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Club", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "clubOfficialCode",
    "nameFinnish",
    "geoLocation",
    "rhyOfficialCode",
    "groups",
    "huntingSummary",
    "overrides"
})
public class LED_Club {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String clubOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String nameFinnish;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected LED_GeoLocation geoLocation;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String rhyOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_Group> groups;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected LED_HuntingSummary huntingSummary;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected LED_Overrides overrides;

    /**
     * Gets the value of the clubOfficialCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClubOfficialCode() {
        return clubOfficialCode;
    }

    /**
     * Sets the value of the clubOfficialCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClubOfficialCode(String value) {
        this.clubOfficialCode = value;
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
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link LED_GeoLocation }
     *     
     */
    public LED_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_GeoLocation }
     *     
     */
    public void setGeoLocation(LED_GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the rhyOfficialCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    /**
     * Sets the value of the rhyOfficialCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRhyOfficialCode(String value) {
        this.rhyOfficialCode = value;
    }

    /**
     * Gets the value of the groups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the groups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_Group }
     * 
     * 
     */
    public List<LED_Group> getGroups() {
        if (groups == null) {
            groups = new ArrayList<LED_Group>();
        }
        return this.groups;
    }

    /**
     * Gets the value of the huntingSummary property.
     * 
     * @return
     *     possible object is
     *     {@link LED_HuntingSummary }
     *     
     */
    public LED_HuntingSummary getHuntingSummary() {
        return huntingSummary;
    }

    /**
     * Sets the value of the huntingSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_HuntingSummary }
     *     
     */
    public void setHuntingSummary(LED_HuntingSummary value) {
        this.huntingSummary = value;
    }

    /**
     * Gets the value of the overrides property.
     * 
     * @return
     *     possible object is
     *     {@link LED_Overrides }
     *     
     */
    public LED_Overrides getOverrides() {
        return overrides;
    }

    /**
     * Sets the value of the overrides property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_Overrides }
     *     
     */
    public void setOverrides(LED_Overrides value) {
        this.overrides = value;
    }

    public void setGroups(List<LED_Group> value) {
        this.groups = value;
    }

}
