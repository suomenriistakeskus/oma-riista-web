
package fi.riista.integration.luke_export.deerharvests;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;


/**
 * <p>Java class for Observation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Observation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}geoLocation"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="huntingType" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}DeerHuntingType" minOccurs="0"/&gt;
 *         &lt;element name="observationType" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}observationType"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="gameSpeciesNameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="mooselikeMaleAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="mooseLikeFemaleAndCalfs" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}FemaleAndCalfs" maxOccurs="5" minOccurs="0"/&gt;
 *         &lt;element name="mooselikeSolitaryCalfAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="mooselikeUnknownSpecimenAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Observation", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "geoLocation",
    "pointOfTime",
    "huntingType",
    "observationType",
    "gameSpeciesCode",
    "gameSpeciesNameFinnish",
    "mooselikeMaleAmount",
    "mooseLikeFemaleAndCalfs",
    "mooselikeSolitaryCalfAmount",
    "mooselikeUnknownSpecimenAmount"
})
public class LED_Observation {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected LED_GeoLocation geoLocation;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected LED_DeerHuntingType huntingType;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    @XmlSchemaType(name = "token")
    protected LED_ObservationType observationType;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected int gameSpeciesCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String gameSpeciesNameFinnish;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer mooselikeMaleAmount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_FemaleAndCalfs> mooseLikeFemaleAndCalfs;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer mooselikeSolitaryCalfAmount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer mooselikeUnknownSpecimenAmount;

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
     * Gets the value of the pointOfTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    /**
     * Sets the value of the pointOfTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointOfTime(LocalDateTime value) {
        this.pointOfTime = value;
    }

    /**
     * Gets the value of the huntingType property.
     * 
     * @return
     *     possible object is
     *     {@link LED_DeerHuntingType }
     *     
     */
    public LED_DeerHuntingType getHuntingType() {
        return huntingType;
    }

    /**
     * Sets the value of the huntingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_DeerHuntingType }
     *     
     */
    public void setHuntingType(LED_DeerHuntingType value) {
        this.huntingType = value;
    }

    /**
     * Gets the value of the observationType property.
     * 
     * @return
     *     possible object is
     *     {@link LED_ObservationType }
     *     
     */
    public LED_ObservationType getObservationType() {
        return observationType;
    }

    /**
     * Sets the value of the observationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_ObservationType }
     *     
     */
    public void setObservationType(LED_ObservationType value) {
        this.observationType = value;
    }

    /**
     * Gets the value of the gameSpeciesCode property.
     * 
     */
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    /**
     * Sets the value of the gameSpeciesCode property.
     * 
     */
    public void setGameSpeciesCode(int value) {
        this.gameSpeciesCode = value;
    }

    /**
     * Gets the value of the gameSpeciesNameFinnish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGameSpeciesNameFinnish() {
        return gameSpeciesNameFinnish;
    }

    /**
     * Sets the value of the gameSpeciesNameFinnish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGameSpeciesNameFinnish(String value) {
        this.gameSpeciesNameFinnish = value;
    }

    /**
     * Gets the value of the mooselikeMaleAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    /**
     * Sets the value of the mooselikeMaleAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMooselikeMaleAmount(Integer value) {
        this.mooselikeMaleAmount = value;
    }

    /**
     * Gets the value of the mooseLikeFemaleAndCalfs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mooseLikeFemaleAndCalfs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMooseLikeFemaleAndCalfs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_FemaleAndCalfs }
     * 
     * 
     */
    public List<LED_FemaleAndCalfs> getMooseLikeFemaleAndCalfs() {
        if (mooseLikeFemaleAndCalfs == null) {
            mooseLikeFemaleAndCalfs = new ArrayList<LED_FemaleAndCalfs>();
        }
        return this.mooseLikeFemaleAndCalfs;
    }

    /**
     * Gets the value of the mooselikeSolitaryCalfAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMooselikeSolitaryCalfAmount() {
        return mooselikeSolitaryCalfAmount;
    }

    /**
     * Sets the value of the mooselikeSolitaryCalfAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMooselikeSolitaryCalfAmount(Integer value) {
        this.mooselikeSolitaryCalfAmount = value;
    }

    /**
     * Gets the value of the mooselikeUnknownSpecimenAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMooselikeUnknownSpecimenAmount() {
        return mooselikeUnknownSpecimenAmount;
    }

    /**
     * Sets the value of the mooselikeUnknownSpecimenAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMooselikeUnknownSpecimenAmount(Integer value) {
        this.mooselikeUnknownSpecimenAmount = value;
    }

    public void setMooseLikeFemaleAndCalfs(List<LED_FemaleAndCalfs> value) {
        this.mooseLikeFemaleAndCalfs = value;
    }

}
