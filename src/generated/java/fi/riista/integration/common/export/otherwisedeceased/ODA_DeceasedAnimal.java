
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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


/**
 * <p>Java class for DeceasedAnimal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeceasedAnimal"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="deceasedAnimalId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/common/export/2018/10}gameAgeEnum"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/common/export/2018/10}gameGenderEnum"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation"/&gt;
 *         &lt;element name="cause" type="{http://riista.fi/integration/common/export/2018/10}deathCauseEnum"/&gt;
 *         &lt;element name="causeOther" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeceasedAnimal", propOrder = {
    "deceasedAnimalId",
    "gameSpeciesCode",
    "age",
    "gender",
    "pointOfTime",
    "geoLocation",
    "cause",
    "causeOther",
    "description"
})
public class ODA_DeceasedAnimal implements Equals2, HashCode2, ToString2
{

    protected long deceasedAnimalId;
    protected int gameSpeciesCode;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected ODA_GameAgeEnum age;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected ODA_GameGenderEnum gender;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(required = true)
    protected ODA_GeoLocation geoLocation;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected ODA_DeathCauseEnum cause;
    protected String causeOther;
    protected String description;

    /**
     * Gets the value of the deceasedAnimalId property.
     * 
     */
    public long getDeceasedAnimalId() {
        return deceasedAnimalId;
    }

    /**
     * Sets the value of the deceasedAnimalId property.
     * 
     */
    public void setDeceasedAnimalId(long value) {
        this.deceasedAnimalId = value;
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
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link ODA_GameAgeEnum }
     *     
     */
    public ODA_GameAgeEnum getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODA_GameAgeEnum }
     *     
     */
    public void setAge(ODA_GameAgeEnum value) {
        this.age = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link ODA_GameGenderEnum }
     *     
     */
    public ODA_GameGenderEnum getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODA_GameGenderEnum }
     *     
     */
    public void setGender(ODA_GameGenderEnum value) {
        this.gender = value;
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
     *     {@link ODA_GeoLocation }
     *     
     */
    public ODA_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODA_GeoLocation }
     *     
     */
    public void setGeoLocation(ODA_GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the cause property.
     * 
     * @return
     *     possible object is
     *     {@link ODA_DeathCauseEnum }
     *     
     */
    public ODA_DeathCauseEnum getCause() {
        return cause;
    }

    /**
     * Sets the value of the cause property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODA_DeathCauseEnum }
     *     
     */
    public void setCause(ODA_DeathCauseEnum value) {
        this.cause = value;
    }

    /**
     * Gets the value of the causeOther property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCauseOther() {
        return causeOther;
    }

    /**
     * Sets the value of the causeOther property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCauseOther(String value) {
        this.causeOther = value;
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

    public ODA_DeceasedAnimal withDeceasedAnimalId(long value) {
        setDeceasedAnimalId(value);
        return this;
    }

    public ODA_DeceasedAnimal withGameSpeciesCode(int value) {
        setGameSpeciesCode(value);
        return this;
    }

    public ODA_DeceasedAnimal withAge(ODA_GameAgeEnum value) {
        setAge(value);
        return this;
    }

    public ODA_DeceasedAnimal withGender(ODA_GameGenderEnum value) {
        setGender(value);
        return this;
    }

    public ODA_DeceasedAnimal withPointOfTime(LocalDateTime value) {
        setPointOfTime(value);
        return this;
    }

    public ODA_DeceasedAnimal withGeoLocation(ODA_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public ODA_DeceasedAnimal withCause(ODA_DeathCauseEnum value) {
        setCause(value);
        return this;
    }

    public ODA_DeceasedAnimal withCauseOther(String value) {
        setCauseOther(value);
        return this;
    }

    public ODA_DeceasedAnimal withDescription(String value) {
        setDescription(value);
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
            long theDeceasedAnimalId;
            theDeceasedAnimalId = this.getDeceasedAnimalId();
            strategy.appendField(locator, this, "deceasedAnimalId", buffer, theDeceasedAnimalId, true);
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, true);
        }
        {
            ODA_GameAgeEnum theAge;
            theAge = this.getAge();
            strategy.appendField(locator, this, "age", buffer, theAge, (this.age!= null));
        }
        {
            ODA_GameGenderEnum theGender;
            theGender = this.getGender();
            strategy.appendField(locator, this, "gender", buffer, theGender, (this.gender!= null));
        }
        {
            LocalDateTime thePointOfTime;
            thePointOfTime = this.getPointOfTime();
            strategy.appendField(locator, this, "pointOfTime", buffer, thePointOfTime, (this.pointOfTime!= null));
        }
        {
            ODA_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            ODA_DeathCauseEnum theCause;
            theCause = this.getCause();
            strategy.appendField(locator, this, "cause", buffer, theCause, (this.cause!= null));
        }
        {
            String theCauseOther;
            theCauseOther = this.getCauseOther();
            strategy.appendField(locator, this, "causeOther", buffer, theCauseOther, (this.causeOther!= null));
        }
        {
            String theDescription;
            theDescription = this.getDescription();
            strategy.appendField(locator, this, "description", buffer, theDescription, (this.description!= null));
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
        final ODA_DeceasedAnimal that = ((ODA_DeceasedAnimal) object);
        {
            long lhsDeceasedAnimalId;
            lhsDeceasedAnimalId = this.getDeceasedAnimalId();
            long rhsDeceasedAnimalId;
            rhsDeceasedAnimalId = that.getDeceasedAnimalId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "deceasedAnimalId", lhsDeceasedAnimalId), LocatorUtils.property(thatLocator, "deceasedAnimalId", rhsDeceasedAnimalId), lhsDeceasedAnimalId, rhsDeceasedAnimalId, true, true)) {
                return false;
            }
        }
        {
            int lhsGameSpeciesCode;
            lhsGameSpeciesCode = this.getGameSpeciesCode();
            int rhsGameSpeciesCode;
            rhsGameSpeciesCode = that.getGameSpeciesCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gameSpeciesCode", lhsGameSpeciesCode), LocatorUtils.property(thatLocator, "gameSpeciesCode", rhsGameSpeciesCode), lhsGameSpeciesCode, rhsGameSpeciesCode, true, true)) {
                return false;
            }
        }
        {
            ODA_GameAgeEnum lhsAge;
            lhsAge = this.getAge();
            ODA_GameAgeEnum rhsAge;
            rhsAge = that.getAge();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "age", lhsAge), LocatorUtils.property(thatLocator, "age", rhsAge), lhsAge, rhsAge, (this.age!= null), (that.age!= null))) {
                return false;
            }
        }
        {
            ODA_GameGenderEnum lhsGender;
            lhsGender = this.getGender();
            ODA_GameGenderEnum rhsGender;
            rhsGender = that.getGender();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gender", lhsGender), LocatorUtils.property(thatLocator, "gender", rhsGender), lhsGender, rhsGender, (this.gender!= null), (that.gender!= null))) {
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
            ODA_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            ODA_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
                return false;
            }
        }
        {
            ODA_DeathCauseEnum lhsCause;
            lhsCause = this.getCause();
            ODA_DeathCauseEnum rhsCause;
            rhsCause = that.getCause();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "cause", lhsCause), LocatorUtils.property(thatLocator, "cause", rhsCause), lhsCause, rhsCause, (this.cause!= null), (that.cause!= null))) {
                return false;
            }
        }
        {
            String lhsCauseOther;
            lhsCauseOther = this.getCauseOther();
            String rhsCauseOther;
            rhsCauseOther = that.getCauseOther();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "causeOther", lhsCauseOther), LocatorUtils.property(thatLocator, "causeOther", rhsCauseOther), lhsCauseOther, rhsCauseOther, (this.causeOther!= null), (that.causeOther!= null))) {
                return false;
            }
        }
        {
            String lhsDescription;
            lhsDescription = this.getDescription();
            String rhsDescription;
            rhsDescription = that.getDescription();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "description", lhsDescription), LocatorUtils.property(thatLocator, "description", rhsDescription), lhsDescription, rhsDescription, (this.description!= null), (that.description!= null))) {
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
            long theDeceasedAnimalId;
            theDeceasedAnimalId = this.getDeceasedAnimalId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "deceasedAnimalId", theDeceasedAnimalId), currentHashCode, theDeceasedAnimalId, true);
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, true);
        }
        {
            ODA_GameAgeEnum theAge;
            theAge = this.getAge();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "age", theAge), currentHashCode, theAge, (this.age!= null));
        }
        {
            ODA_GameGenderEnum theGender;
            theGender = this.getGender();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gender", theGender), currentHashCode, theGender, (this.gender!= null));
        }
        {
            LocalDateTime thePointOfTime;
            thePointOfTime = this.getPointOfTime();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "pointOfTime", thePointOfTime), currentHashCode, thePointOfTime, (this.pointOfTime!= null));
        }
        {
            ODA_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            ODA_DeathCauseEnum theCause;
            theCause = this.getCause();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "cause", theCause), currentHashCode, theCause, (this.cause!= null));
        }
        {
            String theCauseOther;
            theCauseOther = this.getCauseOther();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "causeOther", theCauseOther), currentHashCode, theCauseOther, (this.causeOther!= null));
        }
        {
            String theDescription;
            theDescription = this.getDescription();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "description", theDescription), currentHashCode, theDescription, (this.description!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
