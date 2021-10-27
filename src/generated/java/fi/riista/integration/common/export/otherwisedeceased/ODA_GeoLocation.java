
package fi.riista.integration.common.export.otherwisedeceased;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *                 Location in ETRS-TM35FIN coordinate system.
 *                 Only estimated location, if noExactLocation is true.
 *             
 * 
 * <p>Java class for geoLocation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="geoLocation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="noExactLocation" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="latitude" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="longitude" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geoLocation", propOrder = {
    "noExactLocation",
    "latitude",
    "longitude"
})
public class ODA_GeoLocation implements Equals2, HashCode2, ToString2
{

    protected boolean noExactLocation;
    protected int latitude;
    protected int longitude;

    /**
     * Gets the value of the noExactLocation property.
     * 
     */
    public boolean isNoExactLocation() {
        return noExactLocation;
    }

    /**
     * Sets the value of the noExactLocation property.
     * 
     */
    public void setNoExactLocation(boolean value) {
        this.noExactLocation = value;
    }

    /**
     * Gets the value of the latitude property.
     * 
     */
    public int getLatitude() {
        return latitude;
    }

    /**
     * Sets the value of the latitude property.
     * 
     */
    public void setLatitude(int value) {
        this.latitude = value;
    }

    /**
     * Gets the value of the longitude property.
     * 
     */
    public int getLongitude() {
        return longitude;
    }

    /**
     * Sets the value of the longitude property.
     * 
     */
    public void setLongitude(int value) {
        this.longitude = value;
    }

    public ODA_GeoLocation withNoExactLocation(boolean value) {
        setNoExactLocation(value);
        return this;
    }

    public ODA_GeoLocation withLatitude(int value) {
        setLatitude(value);
        return this;
    }

    public ODA_GeoLocation withLongitude(int value) {
        setLongitude(value);
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
            boolean theNoExactLocation;
            theNoExactLocation = this.isNoExactLocation();
            strategy.appendField(locator, this, "noExactLocation", buffer, theNoExactLocation, true);
        }
        {
            int theLatitude;
            theLatitude = this.getLatitude();
            strategy.appendField(locator, this, "latitude", buffer, theLatitude, true);
        }
        {
            int theLongitude;
            theLongitude = this.getLongitude();
            strategy.appendField(locator, this, "longitude", buffer, theLongitude, true);
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
        final ODA_GeoLocation that = ((ODA_GeoLocation) object);
        {
            boolean lhsNoExactLocation;
            lhsNoExactLocation = this.isNoExactLocation();
            boolean rhsNoExactLocation;
            rhsNoExactLocation = that.isNoExactLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "noExactLocation", lhsNoExactLocation), LocatorUtils.property(thatLocator, "noExactLocation", rhsNoExactLocation), lhsNoExactLocation, rhsNoExactLocation, true, true)) {
                return false;
            }
        }
        {
            int lhsLatitude;
            lhsLatitude = this.getLatitude();
            int rhsLatitude;
            rhsLatitude = that.getLatitude();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "latitude", lhsLatitude), LocatorUtils.property(thatLocator, "latitude", rhsLatitude), lhsLatitude, rhsLatitude, true, true)) {
                return false;
            }
        }
        {
            int lhsLongitude;
            lhsLongitude = this.getLongitude();
            int rhsLongitude;
            rhsLongitude = that.getLongitude();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "longitude", lhsLongitude), LocatorUtils.property(thatLocator, "longitude", rhsLongitude), lhsLongitude, rhsLongitude, true, true)) {
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
            boolean theNoExactLocation;
            theNoExactLocation = this.isNoExactLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "noExactLocation", theNoExactLocation), currentHashCode, theNoExactLocation, true);
        }
        {
            int theLatitude;
            theLatitude = this.getLatitude();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "latitude", theLatitude), currentHashCode, theLatitude, true);
        }
        {
            int theLongitude;
            theLongitude = this.getLongitude();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "longitude", theLongitude), currentHashCode, theLongitude, true);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
