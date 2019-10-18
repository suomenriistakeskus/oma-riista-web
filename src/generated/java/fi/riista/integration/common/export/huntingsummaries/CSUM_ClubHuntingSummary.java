
package fi.riista.integration.common.export.huntingsummaries;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Permit partner's summary for a hunting permit.
 * 
 * <p>Java class for ClubHuntingSummary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClubHuntingSummary"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="permitYear" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="clubOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="clubNameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="huntingEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation" minOccurs="0"/&gt;
 *         &lt;element name="totalLandAreaSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="areaLandEffectiveSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="remainingPopulationInTotalLandArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="remainingPopulationInEffectiveLandArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClubHuntingSummary", propOrder = {
    "permitNumber",
        "permitYear",
    "clubOfficialCode",
    "clubNameFinnish",
    "gameSpeciesCode",
    "rhyOfficialCode",
    "huntingEndDate",
    "geoLocation",
    "totalLandAreaSize",
    "areaLandEffectiveSize",
    "remainingPopulationInTotalLandArea",
    "remainingPopulationInEffectiveLandArea"
})
public class CSUM_ClubHuntingSummary implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected String permitNumber;
    protected int permitYear;
    @XmlElement(required = true)
    protected String clubOfficialCode;
    @XmlElement(required = true)
    protected String clubNameFinnish;
    protected int gameSpeciesCode;
    @XmlElement(required = true)
    protected String rhyOfficialCode;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate huntingEndDate;
    protected CSUM_GeoLocation geoLocation;
    protected Integer totalLandAreaSize;
    protected Integer areaLandEffectiveSize;
    protected Integer remainingPopulationInTotalLandArea;
    protected Integer remainingPopulationInEffectiveLandArea;

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
     * Gets the value of the permitYear property.
     * 
     */
    public int getPermitYear() {
        return permitYear;
    }

    /**
     * Sets the value of the permitYear property.
     * 
     */
    public void setPermitYear(int value) {
        this.permitYear = value;
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
     * Gets the value of the clubNameFinnish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClubNameFinnish() {
        return clubNameFinnish;
    }

    /**
     * Sets the value of the clubNameFinnish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClubNameFinnish(String value) {
        this.clubNameFinnish = value;
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
     * Gets the value of the huntingEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    /**
     * Sets the value of the huntingEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingEndDate(LocalDate value) {
        this.huntingEndDate = value;
    }

    /**
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link CSUM_GeoLocation }
     *     
     */
    public CSUM_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CSUM_GeoLocation }
     *     
     */
    public void setGeoLocation(CSUM_GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the totalLandAreaSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalLandAreaSize() {
        return totalLandAreaSize;
    }

    /**
     * Sets the value of the totalLandAreaSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalLandAreaSize(Integer value) {
        this.totalLandAreaSize = value;
    }

    /**
     * Gets the value of the areaLandEffectiveSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAreaLandEffectiveSize() {
        return areaLandEffectiveSize;
    }

    /**
     * Sets the value of the areaLandEffectiveSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAreaLandEffectiveSize(Integer value) {
        this.areaLandEffectiveSize = value;
    }

    /**
     * Gets the value of the remainingPopulationInTotalLandArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRemainingPopulationInTotalLandArea() {
        return remainingPopulationInTotalLandArea;
    }

    /**
     * Sets the value of the remainingPopulationInTotalLandArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRemainingPopulationInTotalLandArea(Integer value) {
        this.remainingPopulationInTotalLandArea = value;
    }

    /**
     * Gets the value of the remainingPopulationInEffectiveLandArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRemainingPopulationInEffectiveLandArea() {
        return remainingPopulationInEffectiveLandArea;
    }

    /**
     * Sets the value of the remainingPopulationInEffectiveLandArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRemainingPopulationInEffectiveLandArea(Integer value) {
        this.remainingPopulationInEffectiveLandArea = value;
    }

    public CSUM_ClubHuntingSummary withPermitNumber(String value) {
        setPermitNumber(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withPermitYear(int value) {
        setPermitYear(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withClubOfficialCode(String value) {
        setClubOfficialCode(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withClubNameFinnish(String value) {
        setClubNameFinnish(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withGameSpeciesCode(int value) {
        setGameSpeciesCode(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withRhyOfficialCode(String value) {
        setRhyOfficialCode(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withHuntingEndDate(LocalDate value) {
        setHuntingEndDate(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withGeoLocation(CSUM_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withTotalLandAreaSize(Integer value) {
        setTotalLandAreaSize(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withAreaLandEffectiveSize(Integer value) {
        setAreaLandEffectiveSize(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withRemainingPopulationInTotalLandArea(Integer value) {
        setRemainingPopulationInTotalLandArea(value);
        return this;
    }

    public CSUM_ClubHuntingSummary withRemainingPopulationInEffectiveLandArea(Integer value) {
        setRemainingPopulationInEffectiveLandArea(value);
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
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            strategy.appendField(locator, this, "permitNumber", buffer, thePermitNumber, (this.permitNumber!= null));
        }
        {
            int thePermitYear;
            thePermitYear = this.getPermitYear();
            strategy.appendField(locator, this, "permitYear", buffer, thePermitYear, true);
        }
        {
            String theClubOfficialCode;
            theClubOfficialCode = this.getClubOfficialCode();
            strategy.appendField(locator, this, "clubOfficialCode", buffer, theClubOfficialCode, (this.clubOfficialCode!= null));
        }
        {
            String theClubNameFinnish;
            theClubNameFinnish = this.getClubNameFinnish();
            strategy.appendField(locator, this, "clubNameFinnish", buffer, theClubNameFinnish, (this.clubNameFinnish!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, true);
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            strategy.appendField(locator, this, "rhyOfficialCode", buffer, theRhyOfficialCode, (this.rhyOfficialCode!= null));
        }
        {
            LocalDate theHuntingEndDate;
            theHuntingEndDate = this.getHuntingEndDate();
            strategy.appendField(locator, this, "huntingEndDate", buffer, theHuntingEndDate, (this.huntingEndDate!= null));
        }
        {
            CSUM_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            Integer theTotalLandAreaSize;
            theTotalLandAreaSize = this.getTotalLandAreaSize();
            strategy.appendField(locator, this, "totalLandAreaSize", buffer, theTotalLandAreaSize, (this.totalLandAreaSize!= null));
        }
        {
            Integer theAreaLandEffectiveSize;
            theAreaLandEffectiveSize = this.getAreaLandEffectiveSize();
            strategy.appendField(locator, this, "areaLandEffectiveSize", buffer, theAreaLandEffectiveSize, (this.areaLandEffectiveSize!= null));
        }
        {
            Integer theRemainingPopulationInTotalLandArea;
            theRemainingPopulationInTotalLandArea = this.getRemainingPopulationInTotalLandArea();
            strategy.appendField(locator, this, "remainingPopulationInTotalLandArea", buffer, theRemainingPopulationInTotalLandArea, (this.remainingPopulationInTotalLandArea!= null));
        }
        {
            Integer theRemainingPopulationInEffectiveLandArea;
            theRemainingPopulationInEffectiveLandArea = this.getRemainingPopulationInEffectiveLandArea();
            strategy.appendField(locator, this, "remainingPopulationInEffectiveLandArea", buffer, theRemainingPopulationInEffectiveLandArea, (this.remainingPopulationInEffectiveLandArea!= null));
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
        final CSUM_ClubHuntingSummary that = ((CSUM_ClubHuntingSummary) object);
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
            int lhsPermitYear;
            lhsPermitYear = this.getPermitYear();
            int rhsPermitYear;
            rhsPermitYear = that.getPermitYear();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitYear", lhsPermitYear),
                    LocatorUtils.property(thatLocator, "permitYear", rhsPermitYear), lhsPermitYear, rhsPermitYear,
                    true, true)) {
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
            String lhsClubNameFinnish;
            lhsClubNameFinnish = this.getClubNameFinnish();
            String rhsClubNameFinnish;
            rhsClubNameFinnish = that.getClubNameFinnish();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "clubNameFinnish", lhsClubNameFinnish), LocatorUtils.property(thatLocator, "clubNameFinnish", rhsClubNameFinnish), lhsClubNameFinnish, rhsClubNameFinnish, (this.clubNameFinnish!= null), (that.clubNameFinnish!= null))) {
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
            String lhsRhyOfficialCode;
            lhsRhyOfficialCode = this.getRhyOfficialCode();
            String rhsRhyOfficialCode;
            rhsRhyOfficialCode = that.getRhyOfficialCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "rhyOfficialCode", lhsRhyOfficialCode), LocatorUtils.property(thatLocator, "rhyOfficialCode", rhsRhyOfficialCode), lhsRhyOfficialCode, rhsRhyOfficialCode, (this.rhyOfficialCode!= null), (that.rhyOfficialCode!= null))) {
                return false;
            }
        }
        {
            LocalDate lhsHuntingEndDate;
            lhsHuntingEndDate = this.getHuntingEndDate();
            LocalDate rhsHuntingEndDate;
            rhsHuntingEndDate = that.getHuntingEndDate();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "huntingEndDate", lhsHuntingEndDate), LocatorUtils.property(thatLocator, "huntingEndDate", rhsHuntingEndDate), lhsHuntingEndDate, rhsHuntingEndDate, (this.huntingEndDate!= null), (that.huntingEndDate!= null))) {
                return false;
            }
        }
        {
            CSUM_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            CSUM_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
                return false;
            }
        }
        {
            Integer lhsTotalLandAreaSize;
            lhsTotalLandAreaSize = this.getTotalLandAreaSize();
            Integer rhsTotalLandAreaSize;
            rhsTotalLandAreaSize = that.getTotalLandAreaSize();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "totalLandAreaSize", lhsTotalLandAreaSize), LocatorUtils.property(thatLocator, "totalLandAreaSize", rhsTotalLandAreaSize), lhsTotalLandAreaSize, rhsTotalLandAreaSize, (this.totalLandAreaSize!= null), (that.totalLandAreaSize!= null))) {
                return false;
            }
        }
        {
            Integer lhsAreaLandEffectiveSize;
            lhsAreaLandEffectiveSize = this.getAreaLandEffectiveSize();
            Integer rhsAreaLandEffectiveSize;
            rhsAreaLandEffectiveSize = that.getAreaLandEffectiveSize();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "areaLandEffectiveSize", lhsAreaLandEffectiveSize), LocatorUtils.property(thatLocator, "areaLandEffectiveSize", rhsAreaLandEffectiveSize), lhsAreaLandEffectiveSize, rhsAreaLandEffectiveSize, (this.areaLandEffectiveSize!= null), (that.areaLandEffectiveSize!= null))) {
                return false;
            }
        }
        {
            Integer lhsRemainingPopulationInTotalLandArea;
            lhsRemainingPopulationInTotalLandArea = this.getRemainingPopulationInTotalLandArea();
            Integer rhsRemainingPopulationInTotalLandArea;
            rhsRemainingPopulationInTotalLandArea = that.getRemainingPopulationInTotalLandArea();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "remainingPopulationInTotalLandArea", lhsRemainingPopulationInTotalLandArea), LocatorUtils.property(thatLocator, "remainingPopulationInTotalLandArea", rhsRemainingPopulationInTotalLandArea), lhsRemainingPopulationInTotalLandArea, rhsRemainingPopulationInTotalLandArea, (this.remainingPopulationInTotalLandArea!= null), (that.remainingPopulationInTotalLandArea!= null))) {
                return false;
            }
        }
        {
            Integer lhsRemainingPopulationInEffectiveLandArea;
            lhsRemainingPopulationInEffectiveLandArea = this.getRemainingPopulationInEffectiveLandArea();
            Integer rhsRemainingPopulationInEffectiveLandArea;
            rhsRemainingPopulationInEffectiveLandArea = that.getRemainingPopulationInEffectiveLandArea();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "remainingPopulationInEffectiveLandArea", lhsRemainingPopulationInEffectiveLandArea), LocatorUtils.property(thatLocator, "remainingPopulationInEffectiveLandArea", rhsRemainingPopulationInEffectiveLandArea), lhsRemainingPopulationInEffectiveLandArea, rhsRemainingPopulationInEffectiveLandArea, (this.remainingPopulationInEffectiveLandArea!= null), (that.remainingPopulationInEffectiveLandArea!= null))) {
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
            String thePermitNumber;
            thePermitNumber = this.getPermitNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitNumber", thePermitNumber), currentHashCode, thePermitNumber, (this.permitNumber!= null));
        }
        {
            int thePermitYear;
            thePermitYear = this.getPermitYear();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitYear", thePermitYear), currentHashCode, thePermitYear, true);
        }
        {
            String theClubOfficialCode;
            theClubOfficialCode = this.getClubOfficialCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "clubOfficialCode", theClubOfficialCode), currentHashCode, theClubOfficialCode, (this.clubOfficialCode!= null));
        }
        {
            String theClubNameFinnish;
            theClubNameFinnish = this.getClubNameFinnish();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "clubNameFinnish", theClubNameFinnish), currentHashCode, theClubNameFinnish, (this.clubNameFinnish!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, true);
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "rhyOfficialCode", theRhyOfficialCode), currentHashCode, theRhyOfficialCode, (this.rhyOfficialCode!= null));
        }
        {
            LocalDate theHuntingEndDate;
            theHuntingEndDate = this.getHuntingEndDate();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "huntingEndDate", theHuntingEndDate), currentHashCode, theHuntingEndDate, (this.huntingEndDate!= null));
        }
        {
            CSUM_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            Integer theTotalLandAreaSize;
            theTotalLandAreaSize = this.getTotalLandAreaSize();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "totalLandAreaSize", theTotalLandAreaSize), currentHashCode, theTotalLandAreaSize, (this.totalLandAreaSize!= null));
        }
        {
            Integer theAreaLandEffectiveSize;
            theAreaLandEffectiveSize = this.getAreaLandEffectiveSize();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "areaLandEffectiveSize", theAreaLandEffectiveSize), currentHashCode, theAreaLandEffectiveSize, (this.areaLandEffectiveSize!= null));
        }
        {
            Integer theRemainingPopulationInTotalLandArea;
            theRemainingPopulationInTotalLandArea = this.getRemainingPopulationInTotalLandArea();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "remainingPopulationInTotalLandArea", theRemainingPopulationInTotalLandArea), currentHashCode, theRemainingPopulationInTotalLandArea, (this.remainingPopulationInTotalLandArea!= null));
        }
        {
            Integer theRemainingPopulationInEffectiveLandArea;
            theRemainingPopulationInEffectiveLandArea = this.getRemainingPopulationInEffectiveLandArea();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "remainingPopulationInEffectiveLandArea", theRemainingPopulationInEffectiveLandArea), currentHashCode, theRemainingPopulationInEffectiveLandArea, (this.remainingPopulationInEffectiveLandArea!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
