
package fi.riista.integration.common.export.observations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
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
 * <p>Java class for ObservationSpecimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObservationSpecimen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="observationId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/common/export/2018/10}gameGender" minOccurs="0"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/common/export/2018/10}observedGameAge" minOccurs="0"/&gt;
 *         &lt;element name="state" type="{http://riista.fi/integration/common/export/2018/10}observedGameState" minOccurs="0"/&gt;
 *         &lt;element name="marking" type="{http://riista.fi/integration/common/export/2018/10}gameMarking" minOccurs="0"/&gt;
 *         &lt;element name="widthOfPaw" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="lengthOfPaw" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationSpecimen", propOrder = {
    "observationId",
    "gender",
    "age",
    "state",
    "marking",
    "widthOfPaw",
    "lengthOfPaw"
})
public class COBS_ObservationSpecimen implements Equals2, HashCode2, ToString2
{

    protected long observationId;
    @XmlSchemaType(name = "token")
    protected COBS_GameGender gender;
    @XmlSchemaType(name = "token")
    protected COBS_ObservedGameAge age;
    @XmlSchemaType(name = "token")
    protected COBS_ObservedGameState state;
    @XmlSchemaType(name = "token")
    protected COBS_GameMarking marking;
    protected Double widthOfPaw;
    protected Double lengthOfPaw;

    /**
     * Gets the value of the observationId property.
     * 
     */
    public long getObservationId() {
        return observationId;
    }

    /**
     * Sets the value of the observationId property.
     * 
     */
    public void setObservationId(long value) {
        this.observationId = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link COBS_GameGender }
     *     
     */
    public COBS_GameGender getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_GameGender }
     *     
     */
    public void setGender(COBS_GameGender value) {
        this.gender = value;
    }

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link COBS_ObservedGameAge }
     *     
     */
    public COBS_ObservedGameAge getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_ObservedGameAge }
     *     
     */
    public void setAge(COBS_ObservedGameAge value) {
        this.age = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link COBS_ObservedGameState }
     *     
     */
    public COBS_ObservedGameState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_ObservedGameState }
     *     
     */
    public void setState(COBS_ObservedGameState value) {
        this.state = value;
    }

    /**
     * Gets the value of the marking property.
     * 
     * @return
     *     possible object is
     *     {@link COBS_GameMarking }
     *     
     */
    public COBS_GameMarking getMarking() {
        return marking;
    }

    /**
     * Sets the value of the marking property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_GameMarking }
     *     
     */
    public void setMarking(COBS_GameMarking value) {
        this.marking = value;
    }

    /**
     * Gets the value of the widthOfPaw property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidthOfPaw() {
        return widthOfPaw;
    }

    /**
     * Sets the value of the widthOfPaw property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWidthOfPaw(Double value) {
        this.widthOfPaw = value;
    }

    /**
     * Gets the value of the lengthOfPaw property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLengthOfPaw() {
        return lengthOfPaw;
    }

    /**
     * Sets the value of the lengthOfPaw property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLengthOfPaw(Double value) {
        this.lengthOfPaw = value;
    }

    public COBS_ObservationSpecimen withObservationId(long value) {
        setObservationId(value);
        return this;
    }

    public COBS_ObservationSpecimen withGender(COBS_GameGender value) {
        setGender(value);
        return this;
    }

    public COBS_ObservationSpecimen withAge(COBS_ObservedGameAge value) {
        setAge(value);
        return this;
    }

    public COBS_ObservationSpecimen withState(COBS_ObservedGameState value) {
        setState(value);
        return this;
    }

    public COBS_ObservationSpecimen withMarking(COBS_GameMarking value) {
        setMarking(value);
        return this;
    }

    public COBS_ObservationSpecimen withWidthOfPaw(Double value) {
        setWidthOfPaw(value);
        return this;
    }

    public COBS_ObservationSpecimen withLengthOfPaw(Double value) {
        setLengthOfPaw(value);
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
            long theObservationId;
            theObservationId = this.getObservationId();
            strategy.appendField(locator, this, "observationId", buffer, theObservationId, true);
        }
        {
            COBS_GameGender theGender;
            theGender = this.getGender();
            strategy.appendField(locator, this, "gender", buffer, theGender, (this.gender!= null));
        }
        {
            COBS_ObservedGameAge theAge;
            theAge = this.getAge();
            strategy.appendField(locator, this, "age", buffer, theAge, (this.age!= null));
        }
        {
            COBS_ObservedGameState theState;
            theState = this.getState();
            strategy.appendField(locator, this, "state", buffer, theState, (this.state!= null));
        }
        {
            COBS_GameMarking theMarking;
            theMarking = this.getMarking();
            strategy.appendField(locator, this, "marking", buffer, theMarking, (this.marking!= null));
        }
        {
            Double theWidthOfPaw;
            theWidthOfPaw = this.getWidthOfPaw();
            strategy.appendField(locator, this, "widthOfPaw", buffer, theWidthOfPaw, (this.widthOfPaw!= null));
        }
        {
            Double theLengthOfPaw;
            theLengthOfPaw = this.getLengthOfPaw();
            strategy.appendField(locator, this, "lengthOfPaw", buffer, theLengthOfPaw, (this.lengthOfPaw!= null));
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
        final COBS_ObservationSpecimen that = ((COBS_ObservationSpecimen) object);
        {
            long lhsObservationId;
            lhsObservationId = this.getObservationId();
            long rhsObservationId;
            rhsObservationId = that.getObservationId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "observationId", lhsObservationId), LocatorUtils.property(thatLocator, "observationId", rhsObservationId), lhsObservationId, rhsObservationId, true, true)) {
                return false;
            }
        }
        {
            COBS_GameGender lhsGender;
            lhsGender = this.getGender();
            COBS_GameGender rhsGender;
            rhsGender = that.getGender();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gender", lhsGender), LocatorUtils.property(thatLocator, "gender", rhsGender), lhsGender, rhsGender, (this.gender!= null), (that.gender!= null))) {
                return false;
            }
        }
        {
            COBS_ObservedGameAge lhsAge;
            lhsAge = this.getAge();
            COBS_ObservedGameAge rhsAge;
            rhsAge = that.getAge();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "age", lhsAge), LocatorUtils.property(thatLocator, "age", rhsAge), lhsAge, rhsAge, (this.age!= null), (that.age!= null))) {
                return false;
            }
        }
        {
            COBS_ObservedGameState lhsState;
            lhsState = this.getState();
            COBS_ObservedGameState rhsState;
            rhsState = that.getState();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "state", lhsState), LocatorUtils.property(thatLocator, "state", rhsState), lhsState, rhsState, (this.state!= null), (that.state!= null))) {
                return false;
            }
        }
        {
            COBS_GameMarking lhsMarking;
            lhsMarking = this.getMarking();
            COBS_GameMarking rhsMarking;
            rhsMarking = that.getMarking();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "marking", lhsMarking), LocatorUtils.property(thatLocator, "marking", rhsMarking), lhsMarking, rhsMarking, (this.marking!= null), (that.marking!= null))) {
                return false;
            }
        }
        {
            Double lhsWidthOfPaw;
            lhsWidthOfPaw = this.getWidthOfPaw();
            Double rhsWidthOfPaw;
            rhsWidthOfPaw = that.getWidthOfPaw();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "widthOfPaw", lhsWidthOfPaw), LocatorUtils.property(thatLocator, "widthOfPaw", rhsWidthOfPaw), lhsWidthOfPaw, rhsWidthOfPaw, (this.widthOfPaw!= null), (that.widthOfPaw!= null))) {
                return false;
            }
        }
        {
            Double lhsLengthOfPaw;
            lhsLengthOfPaw = this.getLengthOfPaw();
            Double rhsLengthOfPaw;
            rhsLengthOfPaw = that.getLengthOfPaw();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "lengthOfPaw", lhsLengthOfPaw), LocatorUtils.property(thatLocator, "lengthOfPaw", rhsLengthOfPaw), lhsLengthOfPaw, rhsLengthOfPaw, (this.lengthOfPaw!= null), (that.lengthOfPaw!= null))) {
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
            long theObservationId;
            theObservationId = this.getObservationId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "observationId", theObservationId), currentHashCode, theObservationId, true);
        }
        {
            COBS_GameGender theGender;
            theGender = this.getGender();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gender", theGender), currentHashCode, theGender, (this.gender!= null));
        }
        {
            COBS_ObservedGameAge theAge;
            theAge = this.getAge();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "age", theAge), currentHashCode, theAge, (this.age!= null));
        }
        {
            COBS_ObservedGameState theState;
            theState = this.getState();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "state", theState), currentHashCode, theState, (this.state!= null));
        }
        {
            COBS_GameMarking theMarking;
            theMarking = this.getMarking();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "marking", theMarking), currentHashCode, theMarking, (this.marking!= null));
        }
        {
            Double theWidthOfPaw;
            theWidthOfPaw = this.getWidthOfPaw();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "widthOfPaw", theWidthOfPaw), currentHashCode, theWidthOfPaw, (this.widthOfPaw!= null));
        }
        {
            Double theLengthOfPaw;
            theLengthOfPaw = this.getLengthOfPaw();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "lengthOfPaw", theLengthOfPaw), currentHashCode, theLengthOfPaw, (this.lengthOfPaw!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
