
package fi.riista.integration.common.export.permits;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 * <p>Java class for PermitPartner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermitPartner"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="clubOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation" minOccurs="0"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermitPartner", propOrder = {
    "permitNumber",
    "clubOfficialCode",
    "nameFinnish",
    "geoLocation",
    "rhyOfficialCode"
})
public class CPER_PermitPartner implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected String permitNumber;
    @XmlElement(required = true)
    protected String clubOfficialCode;
    @XmlElement(required = true)
    protected String nameFinnish;
    protected CPER_GeoLocation geoLocation;
    @XmlElement(required = true)
    protected String rhyOfficialCode;

    /**
     * Gets the value of the permitNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitNumber() {
        return permitNumber;
    }

    /**
     * Sets the value of the permitNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitNumber(String value) {
        this.permitNumber = value;
    }

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
     *     {@link CPER_GeoLocation }
     *     
     */
    public CPER_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CPER_GeoLocation }
     *     
     */
    public void setGeoLocation(CPER_GeoLocation value) {
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

    public CPER_PermitPartner withPermitNumber(String value) {
        setPermitNumber(value);
        return this;
    }

    public CPER_PermitPartner withClubOfficialCode(String value) {
        setClubOfficialCode(value);
        return this;
    }

    public CPER_PermitPartner withNameFinnish(String value) {
        setNameFinnish(value);
        return this;
    }

    public CPER_PermitPartner withGeoLocation(CPER_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public CPER_PermitPartner withRhyOfficialCode(String value) {
        setRhyOfficialCode(value);
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
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            strategy.appendField(locator, this, "permitNumber", buffer, thePermitNumber, (this.permitNumber!= null));
        }
        {
            String theClubOfficialCode;
            theClubOfficialCode = this.getClubOfficialCode();
            strategy.appendField(locator, this, "clubOfficialCode", buffer, theClubOfficialCode, (this.clubOfficialCode!= null));
        }
        {
            String theNameFinnish;
            theNameFinnish = this.getNameFinnish();
            strategy.appendField(locator, this, "nameFinnish", buffer, theNameFinnish, (this.nameFinnish!= null));
        }
        {
            CPER_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            strategy.appendField(locator, this, "rhyOfficialCode", buffer, theRhyOfficialCode, (this.rhyOfficialCode!= null));
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
        final CPER_PermitPartner that = ((CPER_PermitPartner) object);
        {
            String lhsPermitNumber;
            lhsPermitNumber = this.getPermitNumber();
            String rhsPermitNumber;
            rhsPermitNumber = that.getPermitNumber();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitNumber", lhsPermitNumber), LocatorUtils.property(thatLocator, "permitNumber", rhsPermitNumber), lhsPermitNumber, rhsPermitNumber, (this.permitNumber!= null), (that.permitNumber!= null))) {
                return false;
            }
        }
        {
            String lhsClubOfficialCode;
            lhsClubOfficialCode = this.getClubOfficialCode();
            String rhsClubOfficialCode;
            rhsClubOfficialCode = that.getClubOfficialCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "clubOfficialCode", lhsClubOfficialCode), LocatorUtils.property(thatLocator, "clubOfficialCode", rhsClubOfficialCode), lhsClubOfficialCode, rhsClubOfficialCode, (this.clubOfficialCode!= null), (that.clubOfficialCode!= null))) {
                return false;
            }
        }
        {
            String lhsNameFinnish;
            lhsNameFinnish = this.getNameFinnish();
            String rhsNameFinnish;
            rhsNameFinnish = that.getNameFinnish();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "nameFinnish", lhsNameFinnish), LocatorUtils.property(thatLocator, "nameFinnish", rhsNameFinnish), lhsNameFinnish, rhsNameFinnish, (this.nameFinnish!= null), (that.nameFinnish!= null))) {
                return false;
            }
        }
        {
            CPER_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            CPER_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
                return false;
            }
        }
        {
            String lhsRhyOfficialCode;
            lhsRhyOfficialCode = this.getRhyOfficialCode();
            String rhsRhyOfficialCode;
            rhsRhyOfficialCode = that.getRhyOfficialCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "rhyOfficialCode", lhsRhyOfficialCode), LocatorUtils.property(thatLocator, "rhyOfficialCode", rhsRhyOfficialCode), lhsRhyOfficialCode, rhsRhyOfficialCode, (this.rhyOfficialCode!= null), (that.rhyOfficialCode!= null))) {
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
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitNumber", thePermitNumber), currentHashCode, thePermitNumber, (this.permitNumber!= null));
        }
        {
            String theClubOfficialCode;
            theClubOfficialCode = this.getClubOfficialCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "clubOfficialCode", theClubOfficialCode), currentHashCode, theClubOfficialCode, (this.clubOfficialCode!= null));
        }
        {
            String theNameFinnish;
            theNameFinnish = this.getNameFinnish();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "nameFinnish", theNameFinnish), currentHashCode, theNameFinnish, (this.nameFinnish!= null));
        }
        {
            CPER_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "rhyOfficialCode", theRhyOfficialCode), currentHashCode, theRhyOfficialCode, (this.rhyOfficialCode!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
