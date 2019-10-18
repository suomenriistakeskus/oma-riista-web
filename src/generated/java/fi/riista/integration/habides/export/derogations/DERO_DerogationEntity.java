
package fi.riista.integration.habides.export.derogations;

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
 * 
 *                 at least one of the 6 first alternatives / or the last alternative (no figure can be provided) must be
 *                 filled in
 *             
 * 
 * <p>Java class for derogationEntity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="derogationEntity"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="individuals" type="{}nullableLong" minOccurs="0"/&gt;
 *         &lt;element name="eggs" type="{}nullableLong" minOccurs="0"/&gt;
 *         &lt;element name="nests" type="{}nullableLong" minOccurs="0"/&gt;
 *         &lt;element name="breeding" type="{}nullableLong" minOccurs="0"/&gt;
 *         &lt;element name="resting" type="{}nullableLong" minOccurs="0"/&gt;
 *         &lt;element name="otherType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="noFigureProvided" type="{}nullableBoolean" minOccurs="0"/&gt;
 *         &lt;element name="licensedJustification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "derogationEntity", propOrder = {
    "individuals",
    "eggs",
    "nests",
    "breeding",
    "resting",
    "otherType",
    "noFigureProvided",
    "licensedJustification"
})
public class DERO_DerogationEntity implements Equals2, HashCode2, ToString2
{

    @XmlSchemaType(name = "anySimpleType")
    protected String individuals;
    @XmlSchemaType(name = "anySimpleType")
    protected String eggs;
    @XmlSchemaType(name = "anySimpleType")
    protected String nests;
    @XmlSchemaType(name = "anySimpleType")
    protected String breeding;
    @XmlSchemaType(name = "anySimpleType")
    protected String resting;
    protected String otherType;
    @XmlSchemaType(name = "anySimpleType")
    protected String noFigureProvided;
    protected String licensedJustification;

    /**
     * Gets the value of the individuals property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndividuals() {
        return individuals;
    }

    /**
     * Sets the value of the individuals property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndividuals(String value) {
        this.individuals = value;
    }

    /**
     * Gets the value of the eggs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEggs() {
        return eggs;
    }

    /**
     * Sets the value of the eggs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEggs(String value) {
        this.eggs = value;
    }

    /**
     * Gets the value of the nests property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNests() {
        return nests;
    }

    /**
     * Sets the value of the nests property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNests(String value) {
        this.nests = value;
    }

    /**
     * Gets the value of the breeding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBreeding() {
        return breeding;
    }

    /**
     * Sets the value of the breeding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBreeding(String value) {
        this.breeding = value;
    }

    /**
     * Gets the value of the resting property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResting() {
        return resting;
    }

    /**
     * Sets the value of the resting property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResting(String value) {
        this.resting = value;
    }

    /**
     * Gets the value of the otherType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherType() {
        return otherType;
    }

    /**
     * Sets the value of the otherType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherType(String value) {
        this.otherType = value;
    }

    /**
     * Gets the value of the noFigureProvided property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoFigureProvided() {
        return noFigureProvided;
    }

    /**
     * Sets the value of the noFigureProvided property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoFigureProvided(String value) {
        this.noFigureProvided = value;
    }

    /**
     * Gets the value of the licensedJustification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicensedJustification() {
        return licensedJustification;
    }

    /**
     * Sets the value of the licensedJustification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicensedJustification(String value) {
        this.licensedJustification = value;
    }

    public DERO_DerogationEntity withIndividuals(String value) {
        setIndividuals(value);
        return this;
    }

    public DERO_DerogationEntity withEggs(String value) {
        setEggs(value);
        return this;
    }

    public DERO_DerogationEntity withNests(String value) {
        setNests(value);
        return this;
    }

    public DERO_DerogationEntity withBreeding(String value) {
        setBreeding(value);
        return this;
    }

    public DERO_DerogationEntity withResting(String value) {
        setResting(value);
        return this;
    }

    public DERO_DerogationEntity withOtherType(String value) {
        setOtherType(value);
        return this;
    }

    public DERO_DerogationEntity withNoFigureProvided(String value) {
        setNoFigureProvided(value);
        return this;
    }

    public DERO_DerogationEntity withLicensedJustification(String value) {
        setLicensedJustification(value);
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
            String theIndividuals;
            theIndividuals = this.getIndividuals();
            strategy.appendField(locator, this, "individuals", buffer, theIndividuals, (this.individuals!= null));
        }
        {
            String theEggs;
            theEggs = this.getEggs();
            strategy.appendField(locator, this, "eggs", buffer, theEggs, (this.eggs!= null));
        }
        {
            String theNests;
            theNests = this.getNests();
            strategy.appendField(locator, this, "nests", buffer, theNests, (this.nests!= null));
        }
        {
            String theBreeding;
            theBreeding = this.getBreeding();
            strategy.appendField(locator, this, "breeding", buffer, theBreeding, (this.breeding!= null));
        }
        {
            String theResting;
            theResting = this.getResting();
            strategy.appendField(locator, this, "resting", buffer, theResting, (this.resting!= null));
        }
        {
            String theOtherType;
            theOtherType = this.getOtherType();
            strategy.appendField(locator, this, "otherType", buffer, theOtherType, (this.otherType!= null));
        }
        {
            String theNoFigureProvided;
            theNoFigureProvided = this.getNoFigureProvided();
            strategy.appendField(locator, this, "noFigureProvided", buffer, theNoFigureProvided, (this.noFigureProvided!= null));
        }
        {
            String theLicensedJustification;
            theLicensedJustification = this.getLicensedJustification();
            strategy.appendField(locator, this, "licensedJustification", buffer, theLicensedJustification, (this.licensedJustification!= null));
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
        final DERO_DerogationEntity that = ((DERO_DerogationEntity) object);
        {
            String lhsIndividuals;
            lhsIndividuals = this.getIndividuals();
            String rhsIndividuals;
            rhsIndividuals = that.getIndividuals();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "individuals", lhsIndividuals), LocatorUtils.property(thatLocator, "individuals", rhsIndividuals), lhsIndividuals, rhsIndividuals, (this.individuals!= null), (that.individuals!= null))) {
                return false;
            }
        }
        {
            String lhsEggs;
            lhsEggs = this.getEggs();
            String rhsEggs;
            rhsEggs = that.getEggs();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "eggs", lhsEggs), LocatorUtils.property(thatLocator, "eggs", rhsEggs), lhsEggs, rhsEggs, (this.eggs!= null), (that.eggs!= null))) {
                return false;
            }
        }
        {
            String lhsNests;
            lhsNests = this.getNests();
            String rhsNests;
            rhsNests = that.getNests();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "nests", lhsNests), LocatorUtils.property(thatLocator, "nests", rhsNests), lhsNests, rhsNests, (this.nests!= null), (that.nests!= null))) {
                return false;
            }
        }
        {
            String lhsBreeding;
            lhsBreeding = this.getBreeding();
            String rhsBreeding;
            rhsBreeding = that.getBreeding();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "breeding", lhsBreeding), LocatorUtils.property(thatLocator, "breeding", rhsBreeding), lhsBreeding, rhsBreeding, (this.breeding!= null), (that.breeding!= null))) {
                return false;
            }
        }
        {
            String lhsResting;
            lhsResting = this.getResting();
            String rhsResting;
            rhsResting = that.getResting();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "resting", lhsResting), LocatorUtils.property(thatLocator, "resting", rhsResting), lhsResting, rhsResting, (this.resting!= null), (that.resting!= null))) {
                return false;
            }
        }
        {
            String lhsOtherType;
            lhsOtherType = this.getOtherType();
            String rhsOtherType;
            rhsOtherType = that.getOtherType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "otherType", lhsOtherType), LocatorUtils.property(thatLocator, "otherType", rhsOtherType), lhsOtherType, rhsOtherType, (this.otherType!= null), (that.otherType!= null))) {
                return false;
            }
        }
        {
            String lhsNoFigureProvided;
            lhsNoFigureProvided = this.getNoFigureProvided();
            String rhsNoFigureProvided;
            rhsNoFigureProvided = that.getNoFigureProvided();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "noFigureProvided", lhsNoFigureProvided), LocatorUtils.property(thatLocator, "noFigureProvided", rhsNoFigureProvided), lhsNoFigureProvided, rhsNoFigureProvided, (this.noFigureProvided!= null), (that.noFigureProvided!= null))) {
                return false;
            }
        }
        {
            String lhsLicensedJustification;
            lhsLicensedJustification = this.getLicensedJustification();
            String rhsLicensedJustification;
            rhsLicensedJustification = that.getLicensedJustification();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "licensedJustification", lhsLicensedJustification), LocatorUtils.property(thatLocator, "licensedJustification", rhsLicensedJustification), lhsLicensedJustification, rhsLicensedJustification, (this.licensedJustification!= null), (that.licensedJustification!= null))) {
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
            String theIndividuals;
            theIndividuals = this.getIndividuals();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "individuals", theIndividuals), currentHashCode, theIndividuals, (this.individuals!= null));
        }
        {
            String theEggs;
            theEggs = this.getEggs();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "eggs", theEggs), currentHashCode, theEggs, (this.eggs!= null));
        }
        {
            String theNests;
            theNests = this.getNests();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "nests", theNests), currentHashCode, theNests, (this.nests!= null));
        }
        {
            String theBreeding;
            theBreeding = this.getBreeding();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "breeding", theBreeding), currentHashCode, theBreeding, (this.breeding!= null));
        }
        {
            String theResting;
            theResting = this.getResting();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "resting", theResting), currentHashCode, theResting, (this.resting!= null));
        }
        {
            String theOtherType;
            theOtherType = this.getOtherType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "otherType", theOtherType), currentHashCode, theOtherType, (this.otherType!= null));
        }
        {
            String theNoFigureProvided;
            theNoFigureProvided = this.getNoFigureProvided();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "noFigureProvided", theNoFigureProvided), currentHashCode, theNoFigureProvided, (this.noFigureProvided!= null));
        }
        {
            String theLicensedJustification;
            theLicensedJustification = this.getLicensedJustification();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "licensedJustification", theLicensedJustification), currentHashCode, theLicensedJustification, (this.licensedJustification!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
