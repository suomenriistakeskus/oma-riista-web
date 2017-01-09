
package fi.riista.integration.luke_export.mooselikeharvests;

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
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}geoLocation" minOccurs="0"/&gt;
 *         &lt;element name="contactPerson" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Person" minOccurs="0"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="groups" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Group" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="huntingSummary" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}HuntingSummary" minOccurs="0"/&gt;
 *         &lt;element name="overrides" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Overrides" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Club", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "clubOfficialCode",
    "nameFinnish",
    "geoLocation",
    "contactPerson",
    "rhyOfficialCode",
    "groups",
    "huntingSummary",
    "overrides"
})
public class LEM_Club {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String clubOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String nameFinnish;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_GeoLocation geoLocation;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_Person contactPerson;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String rhyOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", nillable = true)
    protected List<LEM_Group> groups;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_HuntingSummary huntingSummary;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_Overrides overrides;

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
     *     {@link LEM_GeoLocation }
     *     
     */
    public LEM_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GeoLocation }
     *     
     */
    public void setGeoLocation(LEM_GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the contactPerson property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_Person }
     *     
     */
    public LEM_Person getContactPerson() {
        return contactPerson;
    }

    /**
     * Sets the value of the contactPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_Person }
     *     
     */
    public void setContactPerson(LEM_Person value) {
        this.contactPerson = value;
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
     * {@link LEM_Group }
     * 
     * 
     */
    public List<LEM_Group> getGroups() {
        if (groups == null) {
            groups = new ArrayList<LEM_Group>();
        }
        return this.groups;
    }

    /**
     * Gets the value of the huntingSummary property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_HuntingSummary }
     *     
     */
    public LEM_HuntingSummary getHuntingSummary() {
        return huntingSummary;
    }

    /**
     * Sets the value of the huntingSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_HuntingSummary }
     *     
     */
    public void setHuntingSummary(LEM_HuntingSummary value) {
        this.huntingSummary = value;
    }

    /**
     * Gets the value of the overrides property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_Overrides }
     *     
     */
    public LEM_Overrides getOverrides() {
        return overrides;
    }

    /**
     * Sets the value of the overrides property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_Overrides }
     *     
     */
    public void setOverrides(LEM_Overrides value) {
        this.overrides = value;
    }

}
