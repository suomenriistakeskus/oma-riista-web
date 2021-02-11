
package fi.riista.integration.common.export.srva;

import fi.riista.integration.support.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.HashCode2;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * <p>Java class for SRVAEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SRVAEvent"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="srvaEventId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rhyNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation"/&gt;
 *         &lt;element name="totalSpecimenAmount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="otherSpeciesDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="name" type="{http://riista.fi/integration/common/export/2018/10}SRVAEventName"/&gt;
 *         &lt;element name="eventType" type="{http://riista.fi/integration/common/export/2018/10}SRVAEventType"/&gt;
 *         &lt;element name="otherTypeDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="method" type="{http://riista.fi/integration/common/export/2018/10}SRVAMethod" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="otherMethodDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="result" type="{http://riista.fi/integration/common/export/2018/10}SRVAEventResult" minOccurs="0"/&gt;
 *         &lt;element name="numberOfParticipants" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfWorkHours" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SRVAEvent", propOrder = {
    "srvaEventId",
    "rhyNumber",
    "pointOfTime",
    "geoLocation",
    "totalSpecimenAmount",
    "gameSpeciesCode",
    "otherSpeciesDescription",
    "name",
    "eventType",
    "otherTypeDescription",
    "method",
    "otherMethodDescription",
    "result",
    "numberOfParticipants",
    "numberOfWorkHours"
})
public class CEV_SRVAEvent implements Equals2, HashCode2, ToString2
{

    protected long srvaEventId;
    @XmlElement(required = true)
    protected String rhyNumber;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(required = true)
    protected CEV_GeoLocation geoLocation;
    protected int totalSpecimenAmount;
    protected Integer gameSpeciesCode;
    protected String otherSpeciesDescription;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected CEV_SRVAEventName name;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected CEV_SRVAEventType eventType;
    protected String otherTypeDescription;
    @XmlSchemaType(name = "token")
    protected List<CEV_SRVAMethod> method;
    protected String otherMethodDescription;
    @XmlSchemaType(name = "token")
    protected CEV_SRVAEventResult result;
    protected Integer numberOfParticipants;
    protected Integer numberOfWorkHours;

    /**
     * Gets the value of the srvaEventId property.
     * 
     */
    public long getSrvaEventId() {
        return srvaEventId;
    }

    /**
     * Sets the value of the srvaEventId property.
     * 
     */
    public void setSrvaEventId(long value) {
        this.srvaEventId = value;
    }

    /**
     * Gets the value of the rhyNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRhyNumber() {
        return rhyNumber;
    }

    /**
     * Sets the value of the rhyNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRhyNumber(String value) {
        this.rhyNumber = value;
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
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_GeoLocation }
     *     
     */
    public CEV_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_GeoLocation }
     *     
     */
    public void setGeoLocation(CEV_GeoLocation value) {
        this.geoLocation = value;
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
     * Gets the value of the gameSpeciesCode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    /**
     * Sets the value of the gameSpeciesCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGameSpeciesCode(Integer value) {
        this.gameSpeciesCode = value;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_SRVAEventName }
     *     
     */
    public CEV_SRVAEventName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_SRVAEventName }
     *     
     */
    public void setName(CEV_SRVAEventName value) {
        this.name = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_SRVAEventType }
     *     
     */
    public CEV_SRVAEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_SRVAEventType }
     *     
     */
    public void setEventType(CEV_SRVAEventType value) {
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
     * {@link CEV_SRVAMethod }
     * 
     * 
     */
    public List<CEV_SRVAMethod> getMethod() {
        if (method == null) {
            method = new ArrayList<CEV_SRVAMethod>();
        }
        return this.method;
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
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_SRVAEventResult }
     *     
     */
    public CEV_SRVAEventResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_SRVAEventResult }
     *     
     */
    public void setResult(CEV_SRVAEventResult value) {
        this.result = value;
    }

    /**
     * Gets the value of the numberOfParticipants property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }

    /**
     * Sets the value of the numberOfParticipants property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfParticipants(Integer value) {
        this.numberOfParticipants = value;
    }

    /**
     * Gets the value of the numberOfWorkHours property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfWorkHours() {
        return numberOfWorkHours;
    }

    /**
     * Sets the value of the numberOfWorkHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfWorkHours(Integer value) {
        this.numberOfWorkHours = value;
    }

    public CEV_SRVAEvent withSrvaEventId(long value) {
        setSrvaEventId(value);
        return this;
    }

    public CEV_SRVAEvent withRhyNumber(String value) {
        setRhyNumber(value);
        return this;
    }

    public CEV_SRVAEvent withPointOfTime(LocalDateTime value) {
        setPointOfTime(value);
        return this;
    }

    public CEV_SRVAEvent withGeoLocation(CEV_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public CEV_SRVAEvent withTotalSpecimenAmount(int value) {
        setTotalSpecimenAmount(value);
        return this;
    }

    public CEV_SRVAEvent withGameSpeciesCode(Integer value) {
        setGameSpeciesCode(value);
        return this;
    }

    public CEV_SRVAEvent withOtherSpeciesDescription(String value) {
        setOtherSpeciesDescription(value);
        return this;
    }

    public CEV_SRVAEvent withName(CEV_SRVAEventName value) {
        setName(value);
        return this;
    }

    public CEV_SRVAEvent withEventType(CEV_SRVAEventType value) {
        setEventType(value);
        return this;
    }

    public CEV_SRVAEvent withOtherTypeDescription(String value) {
        setOtherTypeDescription(value);
        return this;
    }

    public CEV_SRVAEvent withMethod(CEV_SRVAMethod... values) {
        if (values!= null) {
            for (CEV_SRVAMethod value: values) {
                getMethod().add(value);
            }
        }
        return this;
    }

    public CEV_SRVAEvent withMethod(Collection<CEV_SRVAMethod> values) {
        if (values!= null) {
            getMethod().addAll(values);
        }
        return this;
    }

    public CEV_SRVAEvent withOtherMethodDescription(String value) {
        setOtherMethodDescription(value);
        return this;
    }

    public CEV_SRVAEvent withResult(CEV_SRVAEventResult value) {
        setResult(value);
        return this;
    }

    public CEV_SRVAEvent withNumberOfParticipants(Integer value) {
        setNumberOfParticipants(value);
        return this;
    }

    public CEV_SRVAEvent withNumberOfWorkHours(Integer value) {
        setNumberOfWorkHours(value);
        return this;
    }

    public String toString() {
        final ToStringStrategy2 strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        {
            long theSrvaEventId;
            theSrvaEventId = this.getSrvaEventId();
            strategy.appendField(locator, this, "srvaEventId", buffer, theSrvaEventId, true);
        }
        {
            String theRhyNumber;
            theRhyNumber = this.getRhyNumber();
            strategy.appendField(locator, this, "rhyNumber", buffer, theRhyNumber, (this.rhyNumber!= null));
        }
        {
            LocalDateTime thePointOfTime;
            thePointOfTime = this.getPointOfTime();
            strategy.appendField(locator, this, "pointOfTime", buffer, thePointOfTime, (this.pointOfTime!= null));
        }
        {
            CEV_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theTotalSpecimenAmount;
            theTotalSpecimenAmount = this.getTotalSpecimenAmount();
            strategy.appendField(locator, this, "totalSpecimenAmount", buffer, theTotalSpecimenAmount, true);
        }
        {
            Integer theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, (this.gameSpeciesCode!= null));
        }
        {
            String theOtherSpeciesDescription;
            theOtherSpeciesDescription = this.getOtherSpeciesDescription();
            strategy.appendField(locator, this, "otherSpeciesDescription", buffer, theOtherSpeciesDescription, (this.otherSpeciesDescription!= null));
        }
        {
            CEV_SRVAEventName theName;
            theName = this.getName();
            strategy.appendField(locator, this, "name", buffer, theName, (this.name!= null));
        }
        {
            CEV_SRVAEventType theEventType;
            theEventType = this.getEventType();
            strategy.appendField(locator, this, "eventType", buffer, theEventType, (this.eventType!= null));
        }
        {
            String theOtherTypeDescription;
            theOtherTypeDescription = this.getOtherTypeDescription();
            strategy.appendField(locator, this, "otherTypeDescription", buffer, theOtherTypeDescription, (this.otherTypeDescription!= null));
        }
        {
            List<CEV_SRVAMethod> theMethod;
            theMethod = (((this.method!= null)&&(!this.method.isEmpty()))?this.getMethod():null);
            strategy.appendField(locator, this, "method", buffer, theMethod, ((this.method!= null)&&(!this.method.isEmpty())));
        }
        {
            String theOtherMethodDescription;
            theOtherMethodDescription = this.getOtherMethodDescription();
            strategy.appendField(locator, this, "otherMethodDescription", buffer, theOtherMethodDescription, (this.otherMethodDescription!= null));
        }
        {
            CEV_SRVAEventResult theResult;
            theResult = this.getResult();
            strategy.appendField(locator, this, "result", buffer, theResult, (this.result!= null));
        }
        {
            Integer theNumberOfParticipants;
            theNumberOfParticipants = this.getNumberOfParticipants();
            strategy.appendField(locator, this, "numberOfParticipants", buffer, theNumberOfParticipants, (this.numberOfParticipants!= null));
        }
        {
            Integer theNumberOfWorkHours;
            theNumberOfWorkHours = this.getNumberOfWorkHours();
            strategy.appendField(locator, this, "numberOfWorkHours", buffer, theNumberOfWorkHours, (this.numberOfWorkHours!= null));
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final CEV_SRVAEvent that = ((CEV_SRVAEvent) object);
        {
            long lhsSrvaEventId;
            lhsSrvaEventId = this.getSrvaEventId();
            long rhsSrvaEventId;
            rhsSrvaEventId = that.getSrvaEventId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "srvaEventId", lhsSrvaEventId), LocatorUtils.property(thatLocator, "srvaEventId", rhsSrvaEventId), lhsSrvaEventId, rhsSrvaEventId, true, true)) {
                return false;
            }
        }
        {
            String lhsRhyNumber;
            lhsRhyNumber = this.getRhyNumber();
            String rhsRhyNumber;
            rhsRhyNumber = that.getRhyNumber();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "rhyNumber", lhsRhyNumber), LocatorUtils.property(thatLocator, "rhyNumber", rhsRhyNumber), lhsRhyNumber, rhsRhyNumber, (this.rhyNumber!= null), (that.rhyNumber!= null))) {
                return false;
            }
        }
        {
            LocalDateTime lhsPointOfTime;
            lhsPointOfTime = this.getPointOfTime();
            LocalDateTime rhsPointOfTime;
            rhsPointOfTime = that.getPointOfTime();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "pointOfTime", lhsPointOfTime), LocatorUtils.property(thatLocator, "pointOfTime", rhsPointOfTime), lhsPointOfTime, rhsPointOfTime, (this.pointOfTime!= null), (that.pointOfTime!= null))) {
                return false;
            }
        }
        {
            CEV_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            CEV_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
                return false;
            }
        }
        {
            int lhsTotalSpecimenAmount;
            lhsTotalSpecimenAmount = this.getTotalSpecimenAmount();
            int rhsTotalSpecimenAmount;
            rhsTotalSpecimenAmount = that.getTotalSpecimenAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "totalSpecimenAmount", lhsTotalSpecimenAmount), LocatorUtils.property(thatLocator, "totalSpecimenAmount", rhsTotalSpecimenAmount), lhsTotalSpecimenAmount, rhsTotalSpecimenAmount, true, true)) {
                return false;
            }
        }
        {
            Integer lhsGameSpeciesCode;
            lhsGameSpeciesCode = this.getGameSpeciesCode();
            Integer rhsGameSpeciesCode;
            rhsGameSpeciesCode = that.getGameSpeciesCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gameSpeciesCode", lhsGameSpeciesCode), LocatorUtils.property(thatLocator, "gameSpeciesCode", rhsGameSpeciesCode), lhsGameSpeciesCode, rhsGameSpeciesCode, (this.gameSpeciesCode!= null), (that.gameSpeciesCode!= null))) {
                return false;
            }
        }
        {
            String lhsOtherSpeciesDescription;
            lhsOtherSpeciesDescription = this.getOtherSpeciesDescription();
            String rhsOtherSpeciesDescription;
            rhsOtherSpeciesDescription = that.getOtherSpeciesDescription();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "otherSpeciesDescription", lhsOtherSpeciesDescription), LocatorUtils.property(thatLocator, "otherSpeciesDescription", rhsOtherSpeciesDescription), lhsOtherSpeciesDescription, rhsOtherSpeciesDescription, (this.otherSpeciesDescription!= null), (that.otherSpeciesDescription!= null))) {
                return false;
            }
        }
        {
            CEV_SRVAEventName lhsName;
            lhsName = this.getName();
            CEV_SRVAEventName rhsName;
            rhsName = that.getName();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "name", lhsName), LocatorUtils.property(thatLocator, "name", rhsName), lhsName, rhsName, (this.name!= null), (that.name!= null))) {
                return false;
            }
        }
        {
            CEV_SRVAEventType lhsEventType;
            lhsEventType = this.getEventType();
            CEV_SRVAEventType rhsEventType;
            rhsEventType = that.getEventType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "eventType", lhsEventType), LocatorUtils.property(thatLocator, "eventType", rhsEventType), lhsEventType, rhsEventType, (this.eventType!= null), (that.eventType!= null))) {
                return false;
            }
        }
        {
            String lhsOtherTypeDescription;
            lhsOtherTypeDescription = this.getOtherTypeDescription();
            String rhsOtherTypeDescription;
            rhsOtherTypeDescription = that.getOtherTypeDescription();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "otherTypeDescription", lhsOtherTypeDescription), LocatorUtils.property(thatLocator, "otherTypeDescription", rhsOtherTypeDescription), lhsOtherTypeDescription, rhsOtherTypeDescription, (this.otherTypeDescription!= null), (that.otherTypeDescription!= null))) {
                return false;
            }
        }
        {
            List<CEV_SRVAMethod> lhsMethod;
            lhsMethod = (((this.method!= null)&&(!this.method.isEmpty()))?this.getMethod():null);
            List<CEV_SRVAMethod> rhsMethod;
            rhsMethod = (((that.method!= null)&&(!that.method.isEmpty()))?that.getMethod():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "method", lhsMethod), LocatorUtils.property(thatLocator, "method", rhsMethod), lhsMethod, rhsMethod, ((this.method!= null)&&(!this.method.isEmpty())), ((that.method!= null)&&(!that.method.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsOtherMethodDescription;
            lhsOtherMethodDescription = this.getOtherMethodDescription();
            String rhsOtherMethodDescription;
            rhsOtherMethodDescription = that.getOtherMethodDescription();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "otherMethodDescription", lhsOtherMethodDescription), LocatorUtils.property(thatLocator, "otherMethodDescription", rhsOtherMethodDescription), lhsOtherMethodDescription, rhsOtherMethodDescription, (this.otherMethodDescription!= null), (that.otherMethodDescription!= null))) {
                return false;
            }
        }
        {
            CEV_SRVAEventResult lhsResult;
            lhsResult = this.getResult();
            CEV_SRVAEventResult rhsResult;
            rhsResult = that.getResult();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "result", lhsResult), LocatorUtils.property(thatLocator, "result", rhsResult), lhsResult, rhsResult, (this.result!= null), (that.result!= null))) {
                return false;
            }
        }
        {
            Integer lhsNumberOfParticipants;
            lhsNumberOfParticipants = this.getNumberOfParticipants();
            Integer rhsNumberOfParticipants;
            rhsNumberOfParticipants = that.getNumberOfParticipants();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "numberOfParticipants", lhsNumberOfParticipants), LocatorUtils.property(thatLocator, "numberOfParticipants", rhsNumberOfParticipants), lhsNumberOfParticipants, rhsNumberOfParticipants, (this.numberOfParticipants!= null), (that.numberOfParticipants!= null))) {
                return false;
            }
        }
        {
            Integer lhsNumberOfWorkHours;
            lhsNumberOfWorkHours = this.getNumberOfWorkHours();
            Integer rhsNumberOfWorkHours;
            rhsNumberOfWorkHours = that.getNumberOfWorkHours();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "numberOfWorkHours", lhsNumberOfWorkHours), LocatorUtils.property(thatLocator, "numberOfWorkHours", rhsNumberOfWorkHours), lhsNumberOfWorkHours, rhsNumberOfWorkHours, (this.numberOfWorkHours!= null), (that.numberOfWorkHours!= null))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            long theSrvaEventId;
            theSrvaEventId = this.getSrvaEventId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "srvaEventId", theSrvaEventId), currentHashCode, theSrvaEventId, true);
        }
        {
            String theRhyNumber;
            theRhyNumber = this.getRhyNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "rhyNumber", theRhyNumber), currentHashCode, theRhyNumber, (this.rhyNumber!= null));
        }
        {
            LocalDateTime thePointOfTime;
            thePointOfTime = this.getPointOfTime();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "pointOfTime", thePointOfTime), currentHashCode, thePointOfTime, (this.pointOfTime!= null));
        }
        {
            CEV_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theTotalSpecimenAmount;
            theTotalSpecimenAmount = this.getTotalSpecimenAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "totalSpecimenAmount", theTotalSpecimenAmount), currentHashCode, theTotalSpecimenAmount, true);
        }
        {
            Integer theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, (this.gameSpeciesCode!= null));
        }
        {
            String theOtherSpeciesDescription;
            theOtherSpeciesDescription = this.getOtherSpeciesDescription();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "otherSpeciesDescription", theOtherSpeciesDescription), currentHashCode, theOtherSpeciesDescription, (this.otherSpeciesDescription!= null));
        }
        {
            CEV_SRVAEventName theName;
            theName = this.getName();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "name", theName), currentHashCode, theName, (this.name!= null));
        }
        {
            CEV_SRVAEventType theEventType;
            theEventType = this.getEventType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "eventType", theEventType), currentHashCode, theEventType, (this.eventType!= null));
        }
        {
            String theOtherTypeDescription;
            theOtherTypeDescription = this.getOtherTypeDescription();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "otherTypeDescription", theOtherTypeDescription), currentHashCode, theOtherTypeDescription, (this.otherTypeDescription!= null));
        }
        {
            List<CEV_SRVAMethod> theMethod;
            theMethod = (((this.method!= null)&&(!this.method.isEmpty()))?this.getMethod():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "method", theMethod), currentHashCode, theMethod, ((this.method!= null)&&(!this.method.isEmpty())));
        }
        {
            String theOtherMethodDescription;
            theOtherMethodDescription = this.getOtherMethodDescription();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "otherMethodDescription", theOtherMethodDescription), currentHashCode, theOtherMethodDescription, (this.otherMethodDescription!= null));
        }
        {
            CEV_SRVAEventResult theResult;
            theResult = this.getResult();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "result", theResult), currentHashCode, theResult, (this.result!= null));
        }
        {
            Integer theNumberOfParticipants;
            theNumberOfParticipants = this.getNumberOfParticipants();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "numberOfParticipants", theNumberOfParticipants), currentHashCode, theNumberOfParticipants, (this.numberOfParticipants!= null));
        }
        {
            Integer theNumberOfWorkHours;
            theNumberOfWorkHours = this.getNumberOfWorkHours();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "numberOfWorkHours", theNumberOfWorkHours), currentHashCode, theNumberOfWorkHours, (this.numberOfWorkHours!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
