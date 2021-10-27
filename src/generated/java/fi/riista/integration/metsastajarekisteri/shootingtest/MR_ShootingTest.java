
package fi.riista.integration.metsastajarekisteri.shootingtest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;
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


/**
 * <p>Java class for ShootingTest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ShootingTest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Type" type="{http://riista.fi/integration/mr/export/shootingTest}ShootingTestType"/&gt;
 *         &lt;element name="ValidityBegin" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="ValidityEnd" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="RHY" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="eventId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="participantId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *       &lt;attribute name="executionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShootingTest", propOrder = {
    "type",
    "validityBegin",
    "validityEnd",
    "rhy"
})
public class MR_ShootingTest implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "Type", required = true)
    @XmlSchemaType(name = "token")
    protected MR_ShootingTestType type;
    @XmlElement(name = "ValidityBegin", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate validityBegin;
    @XmlElement(name = "ValidityEnd", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate validityEnd;
    @XmlElement(name = "RHY", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String rhy;
    @XmlAttribute(name = "eventId", required = true)
    protected long eventId;
    @XmlAttribute(name = "participantId", required = true)
    protected long participantId;
    @XmlAttribute(name = "executionId", required = true)
    protected long executionId;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link MR_ShootingTestType }
     *     
     */
    public MR_ShootingTestType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link MR_ShootingTestType }
     *     
     */
    public void setType(MR_ShootingTestType value) {
        this.type = value;
    }

    /**
     * Gets the value of the validityBegin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getValidityBegin() {
        return validityBegin;
    }

    /**
     * Sets the value of the validityBegin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidityBegin(LocalDate value) {
        this.validityBegin = value;
    }

    /**
     * Gets the value of the validityEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getValidityEnd() {
        return validityEnd;
    }

    /**
     * Sets the value of the validityEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidityEnd(LocalDate value) {
        this.validityEnd = value;
    }

    /**
     * Gets the value of the rhy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRHY() {
        return rhy;
    }

    /**
     * Sets the value of the rhy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRHY(String value) {
        this.rhy = value;
    }

    /**
     * Gets the value of the eventId property.
     * 
     */
    public long getEventId() {
        return eventId;
    }

    /**
     * Sets the value of the eventId property.
     * 
     */
    public void setEventId(long value) {
        this.eventId = value;
    }

    /**
     * Gets the value of the participantId property.
     * 
     */
    public long getParticipantId() {
        return participantId;
    }

    /**
     * Sets the value of the participantId property.
     * 
     */
    public void setParticipantId(long value) {
        this.participantId = value;
    }

    /**
     * Gets the value of the executionId property.
     * 
     */
    public long getExecutionId() {
        return executionId;
    }

    /**
     * Sets the value of the executionId property.
     * 
     */
    public void setExecutionId(long value) {
        this.executionId = value;
    }

    public MR_ShootingTest withType(MR_ShootingTestType value) {
        setType(value);
        return this;
    }

    public MR_ShootingTest withValidityBegin(LocalDate value) {
        setValidityBegin(value);
        return this;
    }

    public MR_ShootingTest withValidityEnd(LocalDate value) {
        setValidityEnd(value);
        return this;
    }

    public MR_ShootingTest withRHY(String value) {
        setRHY(value);
        return this;
    }

    public MR_ShootingTest withEventId(long value) {
        setEventId(value);
        return this;
    }

    public MR_ShootingTest withParticipantId(long value) {
        setParticipantId(value);
        return this;
    }

    public MR_ShootingTest withExecutionId(long value) {
        setExecutionId(value);
        return this;
    }

    public String toString() {
        final ToStringStrategy2 strategy = JAXBToStringStrategy.INSTANCE2;
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
            MR_ShootingTestType theType;
            theType = this.getType();
            strategy.appendField(locator, this, "type", buffer, theType, (this.type!= null));
        }
        {
            LocalDate theValidityBegin;
            theValidityBegin = this.getValidityBegin();
            strategy.appendField(locator, this, "validityBegin", buffer, theValidityBegin, (this.validityBegin!= null));
        }
        {
            LocalDate theValidityEnd;
            theValidityEnd = this.getValidityEnd();
            strategy.appendField(locator, this, "validityEnd", buffer, theValidityEnd, (this.validityEnd!= null));
        }
        {
            String theRHY;
            theRHY = this.getRHY();
            strategy.appendField(locator, this, "rhy", buffer, theRHY, (this.rhy!= null));
        }
        {
            long theEventId;
            theEventId = this.getEventId();
            strategy.appendField(locator, this, "eventId", buffer, theEventId, true);
        }
        {
            long theParticipantId;
            theParticipantId = this.getParticipantId();
            strategy.appendField(locator, this, "participantId", buffer, theParticipantId, true);
        }
        {
            long theExecutionId;
            theExecutionId = this.getExecutionId();
            strategy.appendField(locator, this, "executionId", buffer, theExecutionId, true);
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
        final MR_ShootingTest that = ((MR_ShootingTest) object);
        {
            MR_ShootingTestType lhsType;
            lhsType = this.getType();
            MR_ShootingTestType rhsType;
            rhsType = that.getType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsType), LocatorUtils.property(thatLocator, "type", rhsType), lhsType, rhsType, (this.type!= null), (that.type!= null))) {
                return false;
            }
        }
        {
            LocalDate lhsValidityBegin;
            lhsValidityBegin = this.getValidityBegin();
            LocalDate rhsValidityBegin;
            rhsValidityBegin = that.getValidityBegin();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "validityBegin", lhsValidityBegin), LocatorUtils.property(thatLocator, "validityBegin", rhsValidityBegin), lhsValidityBegin, rhsValidityBegin, (this.validityBegin!= null), (that.validityBegin!= null))) {
                return false;
            }
        }
        {
            LocalDate lhsValidityEnd;
            lhsValidityEnd = this.getValidityEnd();
            LocalDate rhsValidityEnd;
            rhsValidityEnd = that.getValidityEnd();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "validityEnd", lhsValidityEnd), LocatorUtils.property(thatLocator, "validityEnd", rhsValidityEnd), lhsValidityEnd, rhsValidityEnd, (this.validityEnd!= null), (that.validityEnd!= null))) {
                return false;
            }
        }
        {
            String lhsRHY;
            lhsRHY = this.getRHY();
            String rhsRHY;
            rhsRHY = that.getRHY();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "rhy", lhsRHY), LocatorUtils.property(thatLocator, "rhy", rhsRHY), lhsRHY, rhsRHY, (this.rhy!= null), (that.rhy!= null))) {
                return false;
            }
        }
        {
            long lhsEventId;
            lhsEventId = this.getEventId();
            long rhsEventId;
            rhsEventId = that.getEventId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "eventId", lhsEventId), LocatorUtils.property(thatLocator, "eventId", rhsEventId), lhsEventId, rhsEventId, true, true)) {
                return false;
            }
        }
        {
            long lhsParticipantId;
            lhsParticipantId = this.getParticipantId();
            long rhsParticipantId;
            rhsParticipantId = that.getParticipantId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "participantId", lhsParticipantId), LocatorUtils.property(thatLocator, "participantId", rhsParticipantId), lhsParticipantId, rhsParticipantId, true, true)) {
                return false;
            }
        }
        {
            long lhsExecutionId;
            lhsExecutionId = this.getExecutionId();
            long rhsExecutionId;
            rhsExecutionId = that.getExecutionId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "executionId", lhsExecutionId), LocatorUtils.property(thatLocator, "executionId", rhsExecutionId), lhsExecutionId, rhsExecutionId, true, true)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE2;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            MR_ShootingTestType theType;
            theType = this.getType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "type", theType), currentHashCode, theType, (this.type!= null));
        }
        {
            LocalDate theValidityBegin;
            theValidityBegin = this.getValidityBegin();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "validityBegin", theValidityBegin), currentHashCode, theValidityBegin, (this.validityBegin!= null));
        }
        {
            LocalDate theValidityEnd;
            theValidityEnd = this.getValidityEnd();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "validityEnd", theValidityEnd), currentHashCode, theValidityEnd, (this.validityEnd!= null));
        }
        {
            String theRHY;
            theRHY = this.getRHY();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "rhy", theRHY), currentHashCode, theRHY, (this.rhy!= null));
        }
        {
            long theEventId;
            theEventId = this.getEventId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "eventId", theEventId), currentHashCode, theEventId, true);
        }
        {
            long theParticipantId;
            theParticipantId = this.getParticipantId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "participantId", theParticipantId), currentHashCode, theParticipantId, true);
        }
        {
            long theExecutionId;
            theExecutionId = this.getExecutionId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "executionId", theExecutionId), currentHashCode, theExecutionId, true);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
