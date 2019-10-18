
package fi.riista.integration.common.export.harvests;

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
 * <p>Java class for Harvest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Harvest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="harvestId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rhyNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="officialHarvest" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Harvest", propOrder = {
    "harvestId",
    "rhyNumber",
    "pointOfTime",
    "geoLocation",
    "gameSpeciesCode",
    "amount",
    "officialHarvest",
    "permitNumber"
})
public class CHAR_Harvest implements Equals2, HashCode2, ToString2
{

    protected long harvestId;
    @XmlElement(required = true)
    protected String rhyNumber;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(required = true)
    protected CHAR_GeoLocation geoLocation;
    protected int gameSpeciesCode;
    protected int amount;
    protected boolean officialHarvest;
    protected String permitNumber;

    /**
     * Gets the value of the harvestId property.
     * 
     */
    public long getHarvestId() {
        return harvestId;
    }

    /**
     * Sets the value of the harvestId property.
     * 
     */
    public void setHarvestId(long value) {
        this.harvestId = value;
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
     *     {@link CHAR_GeoLocation }
     *     
     */
    public CHAR_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CHAR_GeoLocation }
     *     
     */
    public void setGeoLocation(CHAR_GeoLocation value) {
        this.geoLocation = value;
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
     * Gets the value of the amount property.
     * 
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     */
    public void setAmount(int value) {
        this.amount = value;
    }

    /**
     * Gets the value of the officialHarvest property.
     * 
     */
    public boolean isOfficialHarvest() {
        return officialHarvest;
    }

    /**
     * Sets the value of the officialHarvest property.
     * 
     */
    public void setOfficialHarvest(boolean value) {
        this.officialHarvest = value;
    }

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

    public CHAR_Harvest withHarvestId(long value) {
        setHarvestId(value);
        return this;
    }

    public CHAR_Harvest withRhyNumber(String value) {
        setRhyNumber(value);
        return this;
    }

    public CHAR_Harvest withPointOfTime(LocalDateTime value) {
        setPointOfTime(value);
        return this;
    }

    public CHAR_Harvest withGeoLocation(CHAR_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public CHAR_Harvest withGameSpeciesCode(int value) {
        setGameSpeciesCode(value);
        return this;
    }

    public CHAR_Harvest withAmount(int value) {
        setAmount(value);
        return this;
    }

    public CHAR_Harvest withOfficialHarvest(boolean value) {
        setOfficialHarvest(value);
        return this;
    }

    public CHAR_Harvest withPermitNumber(String value) {
        setPermitNumber(value);
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
            long theHarvestId;
            theHarvestId = this.getHarvestId();
            strategy.appendField(locator, this, "harvestId", buffer, theHarvestId, true);
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
            CHAR_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, true);
        }
        {
            int theAmount;
            theAmount = this.getAmount();
            strategy.appendField(locator, this, "amount", buffer, theAmount, true);
        }
        {
            boolean theOfficialHarvest;
            theOfficialHarvest = this.isOfficialHarvest();
            strategy.appendField(locator, this, "officialHarvest", buffer, theOfficialHarvest, true);
        }
        {
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            strategy.appendField(locator, this, "permitNumber", buffer, thePermitNumber, (this.permitNumber!= null));
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
        final CHAR_Harvest that = ((CHAR_Harvest) object);
        {
            long lhsHarvestId;
            lhsHarvestId = this.getHarvestId();
            long rhsHarvestId;
            rhsHarvestId = that.getHarvestId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "harvestId", lhsHarvestId), LocatorUtils.property(thatLocator, "harvestId", rhsHarvestId), lhsHarvestId, rhsHarvestId, true, true)) {
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
            CHAR_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            CHAR_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
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
            int lhsAmount;
            lhsAmount = this.getAmount();
            int rhsAmount;
            rhsAmount = that.getAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "amount", lhsAmount), LocatorUtils.property(thatLocator, "amount", rhsAmount), lhsAmount, rhsAmount, true, true)) {
                return false;
            }
        }
        {
            boolean lhsOfficialHarvest;
            lhsOfficialHarvest = this.isOfficialHarvest();
            boolean rhsOfficialHarvest;
            rhsOfficialHarvest = that.isOfficialHarvest();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "officialHarvest", lhsOfficialHarvest), LocatorUtils.property(thatLocator, "officialHarvest", rhsOfficialHarvest), lhsOfficialHarvest, rhsOfficialHarvest, true, true)) {
                return false;
            }
        }
        {
            String lhsPermitNumber;
            lhsPermitNumber = this.getPermitNumber();
            String rhsPermitNumber;
            rhsPermitNumber = that.getPermitNumber();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitNumber", lhsPermitNumber), LocatorUtils.property(thatLocator, "permitNumber", rhsPermitNumber), lhsPermitNumber, rhsPermitNumber, (this.permitNumber!= null), (that.permitNumber!= null))) {
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
            long theHarvestId;
            theHarvestId = this.getHarvestId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "harvestId", theHarvestId), currentHashCode, theHarvestId, true);
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
            CHAR_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, true);
        }
        {
            int theAmount;
            theAmount = this.getAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "amount", theAmount), currentHashCode, theAmount, true);
        }
        {
            boolean theOfficialHarvest;
            theOfficialHarvest = this.isOfficialHarvest();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "officialHarvest", theOfficialHarvest), currentHashCode, theOfficialHarvest, true);
        }
        {
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitNumber", thePermitNumber), currentHashCode, thePermitNumber, (this.permitNumber!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
