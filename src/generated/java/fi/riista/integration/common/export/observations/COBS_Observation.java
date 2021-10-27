
package fi.riista.integration.common.export.observations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * <p>Java class for Observation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Observation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="observationId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rhyNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="observationType" type="{http://riista.fi/integration/common/export/2018/10}observationType"/&gt;
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="maleAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="femaleAndCalfs" type="{http://riista.fi/integration/common/export/2018/10}FemaleAndCalfs" maxOccurs="5" minOccurs="0"/&gt;
 *         &lt;element name="solitaryCalfAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="unknownSpecimenAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Observation", propOrder = {
    "observationId",
    "rhyNumber",
    "pointOfTime",
    "geoLocation",
    "gameSpeciesCode",
    "observationType",
    "amount",
    "maleAmount",
    "femaleAndCalfs",
    "solitaryCalfAmount",
    "unknownSpecimenAmount"
})
public class COBS_Observation implements Equals2, HashCode2, ToString2
{

    protected long observationId;
    @XmlElement(required = true)
    protected String rhyNumber;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(required = true)
    protected COBS_GeoLocation geoLocation;
    protected int gameSpeciesCode;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected COBS_ObservationType observationType;
    protected Integer amount;
    protected Integer maleAmount;
    @XmlElement(nillable = true)
    protected List<COBS_FemaleAndCalfs> femaleAndCalfs;
    protected Integer solitaryCalfAmount;
    protected Integer unknownSpecimenAmount;

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
     *     {@link COBS_GeoLocation }
     *     
     */
    public COBS_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_GeoLocation }
     *     
     */
    public void setGeoLocation(COBS_GeoLocation value) {
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
     * Gets the value of the observationType property.
     * 
     * @return
     *     possible object is
     *     {@link COBS_ObservationType }
     *     
     */
    public COBS_ObservationType getObservationType() {
        return observationType;
    }

    /**
     * Sets the value of the observationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link COBS_ObservationType }
     *     
     */
    public void setObservationType(COBS_ObservationType value) {
        this.observationType = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAmount(Integer value) {
        this.amount = value;
    }

    /**
     * Gets the value of the maleAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaleAmount() {
        return maleAmount;
    }

    /**
     * Sets the value of the maleAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaleAmount(Integer value) {
        this.maleAmount = value;
    }

    /**
     * Gets the value of the femaleAndCalfs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the femaleAndCalfs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFemaleAndCalfs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link COBS_FemaleAndCalfs }
     * 
     * 
     */
    public List<COBS_FemaleAndCalfs> getFemaleAndCalfs() {
        if (femaleAndCalfs == null) {
            femaleAndCalfs = new ArrayList<COBS_FemaleAndCalfs>();
        }
        return this.femaleAndCalfs;
    }

    /**
     * Gets the value of the solitaryCalfAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSolitaryCalfAmount() {
        return solitaryCalfAmount;
    }

    /**
     * Sets the value of the solitaryCalfAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSolitaryCalfAmount(Integer value) {
        this.solitaryCalfAmount = value;
    }

    /**
     * Gets the value of the unknownSpecimenAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUnknownSpecimenAmount() {
        return unknownSpecimenAmount;
    }

    /**
     * Sets the value of the unknownSpecimenAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUnknownSpecimenAmount(Integer value) {
        this.unknownSpecimenAmount = value;
    }

    public COBS_Observation withObservationId(long value) {
        setObservationId(value);
        return this;
    }

    public COBS_Observation withRhyNumber(String value) {
        setRhyNumber(value);
        return this;
    }

    public COBS_Observation withPointOfTime(LocalDateTime value) {
        setPointOfTime(value);
        return this;
    }

    public COBS_Observation withGeoLocation(COBS_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public COBS_Observation withGameSpeciesCode(int value) {
        setGameSpeciesCode(value);
        return this;
    }

    public COBS_Observation withObservationType(COBS_ObservationType value) {
        setObservationType(value);
        return this;
    }

    public COBS_Observation withAmount(Integer value) {
        setAmount(value);
        return this;
    }

    public COBS_Observation withMaleAmount(Integer value) {
        setMaleAmount(value);
        return this;
    }

    public COBS_Observation withFemaleAndCalfs(COBS_FemaleAndCalfs... values) {
        if (values!= null) {
            for (COBS_FemaleAndCalfs value: values) {
                getFemaleAndCalfs().add(value);
            }
        }
        return this;
    }

    public COBS_Observation withFemaleAndCalfs(Collection<COBS_FemaleAndCalfs> values) {
        if (values!= null) {
            getFemaleAndCalfs().addAll(values);
        }
        return this;
    }

    public COBS_Observation withSolitaryCalfAmount(Integer value) {
        setSolitaryCalfAmount(value);
        return this;
    }

    public COBS_Observation withUnknownSpecimenAmount(Integer value) {
        setUnknownSpecimenAmount(value);
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
            COBS_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, true);
        }
        {
            COBS_ObservationType theObservationType;
            theObservationType = this.getObservationType();
            strategy.appendField(locator, this, "observationType", buffer, theObservationType, (this.observationType!= null));
        }
        {
            Integer theAmount;
            theAmount = this.getAmount();
            strategy.appendField(locator, this, "amount", buffer, theAmount, (this.amount!= null));
        }
        {
            Integer theMaleAmount;
            theMaleAmount = this.getMaleAmount();
            strategy.appendField(locator, this, "maleAmount", buffer, theMaleAmount, (this.maleAmount!= null));
        }
        {
            List<COBS_FemaleAndCalfs> theFemaleAndCalfs;
            theFemaleAndCalfs = (((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty()))?this.getFemaleAndCalfs():null);
            strategy.appendField(locator, this, "femaleAndCalfs", buffer, theFemaleAndCalfs, ((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty())));
        }
        {
            Integer theSolitaryCalfAmount;
            theSolitaryCalfAmount = this.getSolitaryCalfAmount();
            strategy.appendField(locator, this, "solitaryCalfAmount", buffer, theSolitaryCalfAmount, (this.solitaryCalfAmount!= null));
        }
        {
            Integer theUnknownSpecimenAmount;
            theUnknownSpecimenAmount = this.getUnknownSpecimenAmount();
            strategy.appendField(locator, this, "unknownSpecimenAmount", buffer, theUnknownSpecimenAmount, (this.unknownSpecimenAmount!= null));
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
        final COBS_Observation that = ((COBS_Observation) object);
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
            COBS_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            COBS_GeoLocation rhsGeoLocation;
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
            COBS_ObservationType lhsObservationType;
            lhsObservationType = this.getObservationType();
            COBS_ObservationType rhsObservationType;
            rhsObservationType = that.getObservationType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "observationType", lhsObservationType), LocatorUtils.property(thatLocator, "observationType", rhsObservationType), lhsObservationType, rhsObservationType, (this.observationType!= null), (that.observationType!= null))) {
                return false;
            }
        }
        {
            Integer lhsAmount;
            lhsAmount = this.getAmount();
            Integer rhsAmount;
            rhsAmount = that.getAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "amount", lhsAmount), LocatorUtils.property(thatLocator, "amount", rhsAmount), lhsAmount, rhsAmount, (this.amount!= null), (that.amount!= null))) {
                return false;
            }
        }
        {
            Integer lhsMaleAmount;
            lhsMaleAmount = this.getMaleAmount();
            Integer rhsMaleAmount;
            rhsMaleAmount = that.getMaleAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "maleAmount", lhsMaleAmount), LocatorUtils.property(thatLocator, "maleAmount", rhsMaleAmount), lhsMaleAmount, rhsMaleAmount, (this.maleAmount!= null), (that.maleAmount!= null))) {
                return false;
            }
        }
        {
            List<COBS_FemaleAndCalfs> lhsFemaleAndCalfs;
            lhsFemaleAndCalfs = (((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty()))?this.getFemaleAndCalfs():null);
            List<COBS_FemaleAndCalfs> rhsFemaleAndCalfs;
            rhsFemaleAndCalfs = (((that.femaleAndCalfs!= null)&&(!that.femaleAndCalfs.isEmpty()))?that.getFemaleAndCalfs():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "femaleAndCalfs", lhsFemaleAndCalfs), LocatorUtils.property(thatLocator, "femaleAndCalfs", rhsFemaleAndCalfs), lhsFemaleAndCalfs, rhsFemaleAndCalfs, ((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty())), ((that.femaleAndCalfs!= null)&&(!that.femaleAndCalfs.isEmpty())))) {
                return false;
            }
        }
        {
            Integer lhsSolitaryCalfAmount;
            lhsSolitaryCalfAmount = this.getSolitaryCalfAmount();
            Integer rhsSolitaryCalfAmount;
            rhsSolitaryCalfAmount = that.getSolitaryCalfAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "solitaryCalfAmount", lhsSolitaryCalfAmount), LocatorUtils.property(thatLocator, "solitaryCalfAmount", rhsSolitaryCalfAmount), lhsSolitaryCalfAmount, rhsSolitaryCalfAmount, (this.solitaryCalfAmount!= null), (that.solitaryCalfAmount!= null))) {
                return false;
            }
        }
        {
            Integer lhsUnknownSpecimenAmount;
            lhsUnknownSpecimenAmount = this.getUnknownSpecimenAmount();
            Integer rhsUnknownSpecimenAmount;
            rhsUnknownSpecimenAmount = that.getUnknownSpecimenAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "unknownSpecimenAmount", lhsUnknownSpecimenAmount), LocatorUtils.property(thatLocator, "unknownSpecimenAmount", rhsUnknownSpecimenAmount), lhsUnknownSpecimenAmount, rhsUnknownSpecimenAmount, (this.unknownSpecimenAmount!= null), (that.unknownSpecimenAmount!= null))) {
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
            COBS_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, true);
        }
        {
            COBS_ObservationType theObservationType;
            theObservationType = this.getObservationType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "observationType", theObservationType), currentHashCode, theObservationType, (this.observationType!= null));
        }
        {
            Integer theAmount;
            theAmount = this.getAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "amount", theAmount), currentHashCode, theAmount, (this.amount!= null));
        }
        {
            Integer theMaleAmount;
            theMaleAmount = this.getMaleAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "maleAmount", theMaleAmount), currentHashCode, theMaleAmount, (this.maleAmount!= null));
        }
        {
            List<COBS_FemaleAndCalfs> theFemaleAndCalfs;
            theFemaleAndCalfs = (((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty()))?this.getFemaleAndCalfs():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "femaleAndCalfs", theFemaleAndCalfs), currentHashCode, theFemaleAndCalfs, ((this.femaleAndCalfs!= null)&&(!this.femaleAndCalfs.isEmpty())));
        }
        {
            Integer theSolitaryCalfAmount;
            theSolitaryCalfAmount = this.getSolitaryCalfAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "solitaryCalfAmount", theSolitaryCalfAmount), currentHashCode, theSolitaryCalfAmount, (this.solitaryCalfAmount!= null));
        }
        {
            Integer theUnknownSpecimenAmount;
            theUnknownSpecimenAmount = this.getUnknownSpecimenAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "unknownSpecimenAmount", theUnknownSpecimenAmount), currentHashCode, theUnknownSpecimenAmount, (this.unknownSpecimenAmount!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
