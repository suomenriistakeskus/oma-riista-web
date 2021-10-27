
package fi.riista.integration.common.export.permits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * <p>Java class for Permit complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Permit"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="permitYear" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/common/export/2018/10}geoLocation"/&gt;
 *         &lt;element name="originalPermitNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="permitDisplayName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="huntingFinished" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="derogationReasons" type="{http://dd.eionet.europa.eu/schemas/habides-2.0}reasonType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Permit", propOrder = {
    "permitNumber",
    "permitYear",
    "rhyOfficialCode",
    "geoLocation",
    "originalPermitNumber",
    "permitDisplayName",
    "huntingFinished",
    "derogationReasons"
})
public class CPER_Permit implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected String permitNumber;
    protected int permitYear;
    @XmlElement(required = true)
    protected String rhyOfficialCode;
    @XmlElement(required = true)
    protected CPER_GeoLocation geoLocation;
    protected String originalPermitNumber;
    @XmlElement(required = true)
    protected String permitDisplayName;
    protected boolean huntingFinished;
    protected List<String> derogationReasons;

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
     * Gets the value of the originalPermitNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalPermitNumber() {
        return originalPermitNumber;
    }

    /**
     * Sets the value of the originalPermitNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalPermitNumber(String value) {
        this.originalPermitNumber = value;
    }

    /**
     * Gets the value of the permitDisplayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitDisplayName() {
        return permitDisplayName;
    }

    /**
     * Sets the value of the permitDisplayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitDisplayName(String value) {
        this.permitDisplayName = value;
    }

    /**
     * Gets the value of the huntingFinished property.
     * 
     */
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    /**
     * Sets the value of the huntingFinished property.
     * 
     */
    public void setHuntingFinished(boolean value) {
        this.huntingFinished = value;
    }

    /**
     * Gets the value of the derogationReasons property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the derogationReasons property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDerogationReasons().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDerogationReasons() {
        if (derogationReasons == null) {
            derogationReasons = new ArrayList<String>();
        }
        return this.derogationReasons;
    }

    public CPER_Permit withPermitNumber(String value) {
        setPermitNumber(value);
        return this;
    }

    public CPER_Permit withPermitYear(int value) {
        setPermitYear(value);
        return this;
    }

    public CPER_Permit withRhyOfficialCode(String value) {
        setRhyOfficialCode(value);
        return this;
    }

    public CPER_Permit withGeoLocation(CPER_GeoLocation value) {
        setGeoLocation(value);
        return this;
    }

    public CPER_Permit withOriginalPermitNumber(String value) {
        setOriginalPermitNumber(value);
        return this;
    }

    public CPER_Permit withPermitDisplayName(String value) {
        setPermitDisplayName(value);
        return this;
    }

    public CPER_Permit withHuntingFinished(boolean value) {
        setHuntingFinished(value);
        return this;
    }

    public CPER_Permit withDerogationReasons(String... values) {
        if (values!= null) {
            for (String value: values) {
                getDerogationReasons().add(value);
            }
        }
        return this;
    }

    public CPER_Permit withDerogationReasons(Collection<String> values) {
        if (values!= null) {
            getDerogationReasons().addAll(values);
        }
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
            int thePermitYear;
            thePermitYear = this.getPermitYear();
            strategy.appendField(locator, this, "permitYear", buffer, thePermitYear, true);
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            strategy.appendField(locator, this, "rhyOfficialCode", buffer, theRhyOfficialCode, (this.rhyOfficialCode!= null));
        }
        {
            CPER_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            strategy.appendField(locator, this, "geoLocation", buffer, theGeoLocation, (this.geoLocation!= null));
        }
        {
            String theOriginalPermitNumber;
            theOriginalPermitNumber = this.getOriginalPermitNumber();
            strategy.appendField(locator, this, "originalPermitNumber", buffer, theOriginalPermitNumber, (this.originalPermitNumber!= null));
        }
        {
            String thePermitDisplayName;
            thePermitDisplayName = this.getPermitDisplayName();
            strategy.appendField(locator, this, "permitDisplayName", buffer, thePermitDisplayName, (this.permitDisplayName!= null));
        }
        {
            boolean theHuntingFinished;
            theHuntingFinished = this.isHuntingFinished();
            strategy.appendField(locator, this, "huntingFinished", buffer, theHuntingFinished, true);
        }
        {
            List<String> theDerogationReasons;
            theDerogationReasons = (((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty()))?this.getDerogationReasons():null);
            strategy.appendField(locator, this, "derogationReasons", buffer, theDerogationReasons, ((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty())));
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
        final CPER_Permit that = ((CPER_Permit) object);
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
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitYear", lhsPermitYear), LocatorUtils.property(thatLocator, "permitYear", rhsPermitYear), lhsPermitYear, rhsPermitYear, true, true)) {
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
            CPER_GeoLocation lhsGeoLocation;
            lhsGeoLocation = this.getGeoLocation();
            CPER_GeoLocation rhsGeoLocation;
            rhsGeoLocation = that.getGeoLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geoLocation", lhsGeoLocation), LocatorUtils.property(thatLocator, "geoLocation", rhsGeoLocation), lhsGeoLocation, rhsGeoLocation, (this.geoLocation!= null), (that.geoLocation!= null))) {
                return false;
            }
        }
        {
            String lhsOriginalPermitNumber;
            lhsOriginalPermitNumber = this.getOriginalPermitNumber();
            String rhsOriginalPermitNumber;
            rhsOriginalPermitNumber = that.getOriginalPermitNumber();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "originalPermitNumber", lhsOriginalPermitNumber), LocatorUtils.property(thatLocator, "originalPermitNumber", rhsOriginalPermitNumber), lhsOriginalPermitNumber, rhsOriginalPermitNumber, (this.originalPermitNumber!= null), (that.originalPermitNumber!= null))) {
                return false;
            }
        }
        {
            String lhsPermitDisplayName;
            lhsPermitDisplayName = this.getPermitDisplayName();
            String rhsPermitDisplayName;
            rhsPermitDisplayName = that.getPermitDisplayName();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitDisplayName", lhsPermitDisplayName), LocatorUtils.property(thatLocator, "permitDisplayName", rhsPermitDisplayName), lhsPermitDisplayName, rhsPermitDisplayName, (this.permitDisplayName!= null), (that.permitDisplayName!= null))) {
                return false;
            }
        }
        {
            boolean lhsHuntingFinished;
            lhsHuntingFinished = this.isHuntingFinished();
            boolean rhsHuntingFinished;
            rhsHuntingFinished = that.isHuntingFinished();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "huntingFinished", lhsHuntingFinished), LocatorUtils.property(thatLocator, "huntingFinished", rhsHuntingFinished), lhsHuntingFinished, rhsHuntingFinished, true, true)) {
                return false;
            }
        }
        {
            List<String> lhsDerogationReasons;
            lhsDerogationReasons = (((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty()))?this.getDerogationReasons():null);
            List<String> rhsDerogationReasons;
            rhsDerogationReasons = (((that.derogationReasons!= null)&&(!that.derogationReasons.isEmpty()))?that.getDerogationReasons():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogationReasons", lhsDerogationReasons), LocatorUtils.property(thatLocator, "derogationReasons", rhsDerogationReasons), lhsDerogationReasons, rhsDerogationReasons, ((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty())), ((that.derogationReasons!= null)&&(!that.derogationReasons.isEmpty())))) {
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
            int thePermitYear;
            thePermitYear = this.getPermitYear();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitYear", thePermitYear), currentHashCode, thePermitYear, true);
        }
        {
            String theRhyOfficialCode;
            theRhyOfficialCode = this.getRhyOfficialCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "rhyOfficialCode", theRhyOfficialCode), currentHashCode, theRhyOfficialCode, (this.rhyOfficialCode!= null));
        }
        {
            CPER_GeoLocation theGeoLocation;
            theGeoLocation = this.getGeoLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geoLocation", theGeoLocation), currentHashCode, theGeoLocation, (this.geoLocation!= null));
        }
        {
            String theOriginalPermitNumber;
            theOriginalPermitNumber = this.getOriginalPermitNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "originalPermitNumber", theOriginalPermitNumber), currentHashCode, theOriginalPermitNumber, (this.originalPermitNumber!= null));
        }
        {
            String thePermitDisplayName;
            thePermitDisplayName = this.getPermitDisplayName();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitDisplayName", thePermitDisplayName), currentHashCode, thePermitDisplayName, (this.permitDisplayName!= null));
        }
        {
            boolean theHuntingFinished;
            theHuntingFinished = this.isHuntingFinished();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "huntingFinished", theHuntingFinished), currentHashCode, theHuntingFinished, true);
        }
        {
            List<String> theDerogationReasons;
            theDerogationReasons = (((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty()))?this.getDerogationReasons():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogationReasons", theDerogationReasons), currentHashCode, theDerogationReasons, ((this.derogationReasons!= null)&&(!this.derogationReasons.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
