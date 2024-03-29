
package fi.riista.integration.srva.rvr;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.DateTimeAdapter;
import org.joda.time.DateTime;


/**
 * <p>Java class for srvaEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="srvaEvent"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rev" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="eventName" type="{http://riista.fi/integration/srva/rvr}srvaEventNameEnum"/&gt;
 *         &lt;element name="eventType" type="{http://riista.fi/integration/srva/rvr}srvaEventTypeEnum"/&gt;
 *         &lt;element name="otherTypeDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="gameSpeciesOfficialCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *           &lt;element name="otherSpeciesDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="gameSpeciesHumanReadableName" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="totalSpecimenAmount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="specimens" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="specimen" type="{http://riista.fi/integration/srva/rvr}srvaSpecimen" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/srva/rvr}geoLocation"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="rhyHumanReadableName" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="methods" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="method" type="{http://riista.fi/integration/srva/rvr}srvaMethodEnum" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="otherMethodDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="eventResult" type="{http://riista.fi/integration/srva/rvr}srvaResultEnum" minOccurs="0"/&gt;
 *         &lt;element name="personCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="timeSpent" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="deportationOrderNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="eventTypeDetail" type="{http://riista.fi/integration/srva/rvr}srvaEventTypeDetailsEnum" minOccurs="0"/&gt;
 *         &lt;element name="otherEventTypeDetailDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="eventResultDetail" type="{http://riista.fi/integration/srva/rvr}srvaEventResultDetailsEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "srvaEvent", namespace = "http://riista.fi/integration/srva/rvr", propOrder = {
    "id",
    "rev",
    "eventName",
    "eventType",
    "otherTypeDescription",
    "gameSpeciesOfficialCode",
    "otherSpeciesDescription",
    "gameSpeciesHumanReadableName",
    "totalSpecimenAmount",
    "specimens",
    "pointOfTime",
    "geoLocation",
    "rhyOfficialCode",
    "rhyHumanReadableName",
    "methods",
    "otherMethodDescription",
    "description",
    "eventResult",
    "personCount",
    "timeSpent",
    "deportationOrderNumber",
    "eventTypeDetail",
    "otherEventTypeDetailDescription",
    "eventResultDetail"
})
public class RVR_SrvaEvent {

    protected long id;
    protected int rev;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected RVR_SrvaEventNameEnum eventName;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected RVR_SrvaEventTypeEnum eventType;
    protected String otherTypeDescription;
    protected Integer gameSpeciesOfficialCode;
    protected String otherSpeciesDescription;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String gameSpeciesHumanReadableName;
    protected int totalSpecimenAmount;
    protected RVR_SrvaEvent.RVR_Specimens specimens;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime pointOfTime;
    @XmlElement(required = true)
    protected RVR_GeoLocation geoLocation;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String rhyOfficialCode;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String rhyHumanReadableName;
    protected RVR_SrvaEvent.RVR_Methods methods;
    protected String otherMethodDescription;
    protected String description;
    @XmlSchemaType(name = "token")
    protected RVR_SrvaResultEnum eventResult;
    protected Integer personCount;
    protected Integer timeSpent;
    protected String deportationOrderNumber;
    @XmlSchemaType(name = "token")
    protected RVR_SrvaEventTypeDetailsEnum eventTypeDetail;
    protected String otherEventTypeDetailDescription;
    @XmlSchemaType(name = "token")
    protected RVR_SrvaEventResultDetailsEnum eventResultDetail;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the rev property.
     * 
     */
    public int getRev() {
        return rev;
    }

    /**
     * Sets the value of the rev property.
     * 
     */
    public void setRev(int value) {
        this.rev = value;
    }

    /**
     * Gets the value of the eventName property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEventNameEnum }
     *     
     */
    public RVR_SrvaEventNameEnum getEventName() {
        return eventName;
    }

    /**
     * Sets the value of the eventName property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEventNameEnum }
     *     
     */
    public void setEventName(RVR_SrvaEventNameEnum value) {
        this.eventName = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEventTypeEnum }
     *     
     */
    public RVR_SrvaEventTypeEnum getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEventTypeEnum }
     *     
     */
    public void setEventType(RVR_SrvaEventTypeEnum value) {
        this.eventType = value;
    }

    /**
     * Gets the value of the otherTypeDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherTypeDescription() {
        return otherTypeDescription;
    }

    /**
     * Sets the value of the otherTypeDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherTypeDescription(String value) {
        this.otherTypeDescription = value;
    }

    /**
     * Gets the value of the gameSpeciesOfficialCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGameSpeciesOfficialCode() {
        return gameSpeciesOfficialCode;
    }

    /**
     * Sets the value of the gameSpeciesOfficialCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGameSpeciesOfficialCode(Integer value) {
        this.gameSpeciesOfficialCode = value;
    }

    /**
     * Gets the value of the otherSpeciesDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherSpeciesDescription() {
        return otherSpeciesDescription;
    }

    /**
     * Sets the value of the otherSpeciesDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherSpeciesDescription(String value) {
        this.otherSpeciesDescription = value;
    }

    /**
     * Gets the value of the gameSpeciesHumanReadableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGameSpeciesHumanReadableName() {
        return gameSpeciesHumanReadableName;
    }

    /**
     * Sets the value of the gameSpeciesHumanReadableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGameSpeciesHumanReadableName(String value) {
        this.gameSpeciesHumanReadableName = value;
    }

    /**
     * Gets the value of the totalSpecimenAmount property.
     * 
     */
    public int getTotalSpecimenAmount() {
        return totalSpecimenAmount;
    }

    /**
     * Sets the value of the totalSpecimenAmount property.
     * 
     */
    public void setTotalSpecimenAmount(int value) {
        this.totalSpecimenAmount = value;
    }

    /**
     * Gets the value of the specimens property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEvent.RVR_Specimens }
     *     
     */
    public RVR_SrvaEvent.RVR_Specimens getSpecimens() {
        return specimens;
    }

    /**
     * Sets the value of the specimens property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEvent.RVR_Specimens }
     *     
     */
    public void setSpecimens(RVR_SrvaEvent.RVR_Specimens value) {
        this.specimens = value;
    }

    /**
     * Gets the value of the pointOfTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DateTime getPointOfTime() {
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
    public void setPointOfTime(DateTime value) {
        this.pointOfTime = value;
    }

    /**
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_GeoLocation }
     *     
     */
    public RVR_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_GeoLocation }
     *     
     */
    public void setGeoLocation(RVR_GeoLocation value) {
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
     * Gets the value of the rhyHumanReadableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRhyHumanReadableName() {
        return rhyHumanReadableName;
    }

    /**
     * Sets the value of the rhyHumanReadableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRhyHumanReadableName(String value) {
        this.rhyHumanReadableName = value;
    }

    /**
     * Gets the value of the methods property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEvent.RVR_Methods }
     *     
     */
    public RVR_SrvaEvent.RVR_Methods getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEvent.RVR_Methods }
     *     
     */
    public void setMethods(RVR_SrvaEvent.RVR_Methods value) {
        this.methods = value;
    }

    /**
     * Gets the value of the otherMethodDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherMethodDescription() {
        return otherMethodDescription;
    }

    /**
     * Sets the value of the otherMethodDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherMethodDescription(String value) {
        this.otherMethodDescription = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the eventResult property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaResultEnum }
     *     
     */
    public RVR_SrvaResultEnum getEventResult() {
        return eventResult;
    }

    /**
     * Sets the value of the eventResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaResultEnum }
     *     
     */
    public void setEventResult(RVR_SrvaResultEnum value) {
        this.eventResult = value;
    }

    /**
     * Gets the value of the personCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPersonCount() {
        return personCount;
    }

    /**
     * Sets the value of the personCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPersonCount(Integer value) {
        this.personCount = value;
    }

    /**
     * Gets the value of the timeSpent property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTimeSpent() {
        return timeSpent;
    }

    /**
     * Sets the value of the timeSpent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimeSpent(Integer value) {
        this.timeSpent = value;
    }

    /**
     * Gets the value of the deportationOrderNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeportationOrderNumber() {
        return deportationOrderNumber;
    }

    /**
     * Sets the value of the deportationOrderNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeportationOrderNumber(String value) {
        this.deportationOrderNumber = value;
    }

    /**
     * Gets the value of the eventTypeDetail property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEventTypeDetailsEnum }
     *     
     */
    public RVR_SrvaEventTypeDetailsEnum getEventTypeDetail() {
        return eventTypeDetail;
    }

    /**
     * Sets the value of the eventTypeDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEventTypeDetailsEnum }
     *     
     */
    public void setEventTypeDetail(RVR_SrvaEventTypeDetailsEnum value) {
        this.eventTypeDetail = value;
    }

    /**
     * Gets the value of the otherEventTypeDetailDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherEventTypeDetailDescription() {
        return otherEventTypeDetailDescription;
    }

    /**
     * Sets the value of the otherEventTypeDetailDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherEventTypeDetailDescription(String value) {
        this.otherEventTypeDetailDescription = value;
    }

    /**
     * Gets the value of the eventResultDetail property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_SrvaEventResultDetailsEnum }
     *     
     */
    public RVR_SrvaEventResultDetailsEnum getEventResultDetail() {
        return eventResultDetail;
    }

    /**
     * Sets the value of the eventResultDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_SrvaEventResultDetailsEnum }
     *     
     */
    public void setEventResultDetail(RVR_SrvaEventResultDetailsEnum value) {
        this.eventResultDetail = value;
    }


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
     *         &lt;element name="method" type="{http://riista.fi/integration/srva/rvr}srvaMethodEnum" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "method"
    })
    public static class RVR_Methods {

        @XmlSchemaType(name = "token")
        protected List<RVR_SrvaMethodEnum> method;

        /**
         * Gets the value of the method property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the method property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMethod().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RVR_SrvaMethodEnum }
         * 
         * 
         */
        public List<RVR_SrvaMethodEnum> getMethod() {
            if (method == null) {
                method = new ArrayList<RVR_SrvaMethodEnum>();
            }
            return this.method;
        }

        public void setMethod(List<RVR_SrvaMethodEnum> value) {
            this.method = value;
        }

    }


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
     *         &lt;element name="specimen" type="{http://riista.fi/integration/srva/rvr}srvaSpecimen" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "specimen"
    })
    public static class RVR_Specimens {

        protected List<RVR_SrvaSpecimen> specimen;

        /**
         * Gets the value of the specimen property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the specimen property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSpecimen().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RVR_SrvaSpecimen }
         * 
         * 
         */
        public List<RVR_SrvaSpecimen> getSpecimen() {
            if (specimen == null) {
                specimen = new ArrayList<RVR_SrvaSpecimen>();
            }
            return this.specimen;
        }

        public void setSpecimen(List<RVR_SrvaSpecimen> value) {
            this.specimen = value;
        }

    }

}
