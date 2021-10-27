
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
 * <p>Java class for PermitSpeciesAmount complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PermitSpeciesAmount"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="validityPeriod" type="{http://riista.fi/integration/common/export/2018/10}validityTimeInterval" maxOccurs="unbounded"/&gt;
 *         &lt;element name="gameSpeciesCode" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="restrictedAmountAdult" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/&gt;
 *         &lt;element name="restrictedAmountAdultMale" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermitSpeciesAmount", propOrder = {
    "permitNumber",
    "validityPeriod",
    "gameSpeciesCode",
    "amount",
    "restrictedAmountAdult",
    "restrictedAmountAdultMale"
})
public class CPER_PermitSpeciesAmount implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected String permitNumber;
    @XmlElement(required = true)
    protected List<CPER_ValidityTimeInterval> validityPeriod;
    protected int gameSpeciesCode;
    protected float amount;
    protected Float restrictedAmountAdult;
    protected Float restrictedAmountAdultMale;

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
     * Gets the value of the validityPeriod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the validityPeriod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValidityPeriod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CPER_ValidityTimeInterval }
     * 
     * 
     */
    public List<CPER_ValidityTimeInterval> getValidityPeriod() {
        if (validityPeriod == null) {
            validityPeriod = new ArrayList<CPER_ValidityTimeInterval>();
        }
        return this.validityPeriod;
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
    public float getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     */
    public void setAmount(float value) {
        this.amount = value;
    }

    /**
     * Gets the value of the restrictedAmountAdult property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRestrictedAmountAdult() {
        return restrictedAmountAdult;
    }

    /**
     * Sets the value of the restrictedAmountAdult property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRestrictedAmountAdult(Float value) {
        this.restrictedAmountAdult = value;
    }

    /**
     * Gets the value of the restrictedAmountAdultMale property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRestrictedAmountAdultMale() {
        return restrictedAmountAdultMale;
    }

    /**
     * Sets the value of the restrictedAmountAdultMale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRestrictedAmountAdultMale(Float value) {
        this.restrictedAmountAdultMale = value;
    }

    public CPER_PermitSpeciesAmount withPermitNumber(String value) {
        setPermitNumber(value);
        return this;
    }

    public CPER_PermitSpeciesAmount withValidityPeriod(CPER_ValidityTimeInterval... values) {
        if (values!= null) {
            for (CPER_ValidityTimeInterval value: values) {
                getValidityPeriod().add(value);
            }
        }
        return this;
    }

    public CPER_PermitSpeciesAmount withValidityPeriod(Collection<CPER_ValidityTimeInterval> values) {
        if (values!= null) {
            getValidityPeriod().addAll(values);
        }
        return this;
    }

    public CPER_PermitSpeciesAmount withGameSpeciesCode(int value) {
        setGameSpeciesCode(value);
        return this;
    }

    public CPER_PermitSpeciesAmount withAmount(float value) {
        setAmount(value);
        return this;
    }

    public CPER_PermitSpeciesAmount withRestrictedAmountAdult(Float value) {
        setRestrictedAmountAdult(value);
        return this;
    }

    public CPER_PermitSpeciesAmount withRestrictedAmountAdultMale(Float value) {
        setRestrictedAmountAdultMale(value);
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
            List<CPER_ValidityTimeInterval> theValidityPeriod;
            theValidityPeriod = (((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty()))?this.getValidityPeriod():null);
            strategy.appendField(locator, this, "validityPeriod", buffer, theValidityPeriod, ((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty())));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            strategy.appendField(locator, this, "gameSpeciesCode", buffer, theGameSpeciesCode, true);
        }
        {
            float theAmount;
            theAmount = this.getAmount();
            strategy.appendField(locator, this, "amount", buffer, theAmount, true);
        }
        {
            Float theRestrictedAmountAdult;
            theRestrictedAmountAdult = this.getRestrictedAmountAdult();
            strategy.appendField(locator, this, "restrictedAmountAdult", buffer, theRestrictedAmountAdult, (this.restrictedAmountAdult!= null));
        }
        {
            Float theRestrictedAmountAdultMale;
            theRestrictedAmountAdultMale = this.getRestrictedAmountAdultMale();
            strategy.appendField(locator, this, "restrictedAmountAdultMale", buffer, theRestrictedAmountAdultMale, (this.restrictedAmountAdultMale!= null));
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
        final CPER_PermitSpeciesAmount that = ((CPER_PermitSpeciesAmount) object);
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
            List<CPER_ValidityTimeInterval> lhsValidityPeriod;
            lhsValidityPeriod = (((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty()))?this.getValidityPeriod():null);
            List<CPER_ValidityTimeInterval> rhsValidityPeriod;
            rhsValidityPeriod = (((that.validityPeriod!= null)&&(!that.validityPeriod.isEmpty()))?that.getValidityPeriod():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "validityPeriod", lhsValidityPeriod), LocatorUtils.property(thatLocator, "validityPeriod", rhsValidityPeriod), lhsValidityPeriod, rhsValidityPeriod, ((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty())), ((that.validityPeriod!= null)&&(!that.validityPeriod.isEmpty())))) {
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
            float lhsAmount;
            lhsAmount = this.getAmount();
            float rhsAmount;
            rhsAmount = that.getAmount();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "amount", lhsAmount), LocatorUtils.property(thatLocator, "amount", rhsAmount), lhsAmount, rhsAmount, true, true)) {
                return false;
            }
        }
        {
            Float lhsRestrictedAmountAdult;
            lhsRestrictedAmountAdult = this.getRestrictedAmountAdult();
            Float rhsRestrictedAmountAdult;
            rhsRestrictedAmountAdult = that.getRestrictedAmountAdult();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "restrictedAmountAdult", lhsRestrictedAmountAdult), LocatorUtils.property(thatLocator, "restrictedAmountAdult", rhsRestrictedAmountAdult), lhsRestrictedAmountAdult, rhsRestrictedAmountAdult, (this.restrictedAmountAdult!= null), (that.restrictedAmountAdult!= null))) {
                return false;
            }
        }
        {
            Float lhsRestrictedAmountAdultMale;
            lhsRestrictedAmountAdultMale = this.getRestrictedAmountAdultMale();
            Float rhsRestrictedAmountAdultMale;
            rhsRestrictedAmountAdultMale = that.getRestrictedAmountAdultMale();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "restrictedAmountAdultMale", lhsRestrictedAmountAdultMale), LocatorUtils.property(thatLocator, "restrictedAmountAdultMale", rhsRestrictedAmountAdultMale), lhsRestrictedAmountAdultMale, rhsRestrictedAmountAdultMale, (this.restrictedAmountAdultMale!= null), (that.restrictedAmountAdultMale!= null))) {
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
            List<CPER_ValidityTimeInterval> theValidityPeriod;
            theValidityPeriod = (((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty()))?this.getValidityPeriod():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "validityPeriod", theValidityPeriod), currentHashCode, theValidityPeriod, ((this.validityPeriod!= null)&&(!this.validityPeriod.isEmpty())));
        }
        {
            int theGameSpeciesCode;
            theGameSpeciesCode = this.getGameSpeciesCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gameSpeciesCode", theGameSpeciesCode), currentHashCode, theGameSpeciesCode, true);
        }
        {
            float theAmount;
            theAmount = this.getAmount();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "amount", theAmount), currentHashCode, theAmount, true);
        }
        {
            Float theRestrictedAmountAdult;
            theRestrictedAmountAdult = this.getRestrictedAmountAdult();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "restrictedAmountAdult", theRestrictedAmountAdult), currentHashCode, theRestrictedAmountAdult, (this.restrictedAmountAdult!= null));
        }
        {
            Float theRestrictedAmountAdultMale;
            theRestrictedAmountAdultMale = this.getRestrictedAmountAdultMale();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "restrictedAmountAdultMale", theRestrictedAmountAdultMale), currentHashCode, theRestrictedAmountAdultMale, (this.restrictedAmountAdultMale!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
