
package fi.riista.integration.common.export.permits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permit" type="{http://riista.fi/integration/common/export/2018/10}Permit" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="permitSpeciesAmount" type="{http://riista.fi/integration/common/export/2018/10}PermitSpeciesAmount" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="permitPartner" type="{http://riista.fi/integration/common/export/2018/10}PermitPartner" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "permit",
    "permitSpeciesAmount",
    "permitPartner"
})
@XmlRootElement(name = "permits")
public class CPER_Permits implements Equals2, HashCode2, ToString2
{

    protected List<CPER_Permit> permit;
    protected List<CPER_PermitSpeciesAmount> permitSpeciesAmount;
    protected List<CPER_PermitPartner> permitPartner;

    /**
     * Gets the value of the permit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CPER_Permit }
     * 
     * 
     */
    public List<CPER_Permit> getPermit() {
        if (permit == null) {
            permit = new ArrayList<CPER_Permit>();
        }
        return this.permit;
    }

    /**
     * Gets the value of the permitSpeciesAmount property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permitSpeciesAmount property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermitSpeciesAmount().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CPER_PermitSpeciesAmount }
     * 
     * 
     */
    public List<CPER_PermitSpeciesAmount> getPermitSpeciesAmount() {
        if (permitSpeciesAmount == null) {
            permitSpeciesAmount = new ArrayList<CPER_PermitSpeciesAmount>();
        }
        return this.permitSpeciesAmount;
    }

    /**
     * Gets the value of the permitPartner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permitPartner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermitPartner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CPER_PermitPartner }
     * 
     * 
     */
    public List<CPER_PermitPartner> getPermitPartner() {
        if (permitPartner == null) {
            permitPartner = new ArrayList<CPER_PermitPartner>();
        }
        return this.permitPartner;
    }

    public CPER_Permits withPermit(CPER_Permit... values) {
        if (values!= null) {
            for (CPER_Permit value: values) {
                getPermit().add(value);
            }
        }
        return this;
    }

    public CPER_Permits withPermit(Collection<CPER_Permit> values) {
        if (values!= null) {
            getPermit().addAll(values);
        }
        return this;
    }

    public CPER_Permits withPermitSpeciesAmount(CPER_PermitSpeciesAmount... values) {
        if (values!= null) {
            for (CPER_PermitSpeciesAmount value: values) {
                getPermitSpeciesAmount().add(value);
            }
        }
        return this;
    }

    public CPER_Permits withPermitSpeciesAmount(Collection<CPER_PermitSpeciesAmount> values) {
        if (values!= null) {
            getPermitSpeciesAmount().addAll(values);
        }
        return this;
    }

    public CPER_Permits withPermitPartner(CPER_PermitPartner... values) {
        if (values!= null) {
            for (CPER_PermitPartner value: values) {
                getPermitPartner().add(value);
            }
        }
        return this;
    }

    public CPER_Permits withPermitPartner(Collection<CPER_PermitPartner> values) {
        if (values!= null) {
            getPermitPartner().addAll(values);
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
            List<CPER_Permit> thePermit;
            thePermit = (((this.permit!= null)&&(!this.permit.isEmpty()))?this.getPermit():null);
            strategy.appendField(locator, this, "permit", buffer, thePermit, ((this.permit!= null)&&(!this.permit.isEmpty())));
        }
        {
            List<CPER_PermitSpeciesAmount> thePermitSpeciesAmount;
            thePermitSpeciesAmount = (((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty()))?this.getPermitSpeciesAmount():null);
            strategy.appendField(locator, this, "permitSpeciesAmount", buffer, thePermitSpeciesAmount, ((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty())));
        }
        {
            List<CPER_PermitPartner> thePermitPartner;
            thePermitPartner = (((this.permitPartner!= null)&&(!this.permitPartner.isEmpty()))?this.getPermitPartner():null);
            strategy.appendField(locator, this, "permitPartner", buffer, thePermitPartner, ((this.permitPartner!= null)&&(!this.permitPartner.isEmpty())));
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
        final CPER_Permits that = ((CPER_Permits) object);
        {
            List<CPER_Permit> lhsPermit;
            lhsPermit = (((this.permit!= null)&&(!this.permit.isEmpty()))?this.getPermit():null);
            List<CPER_Permit> rhsPermit;
            rhsPermit = (((that.permit!= null)&&(!that.permit.isEmpty()))?that.getPermit():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permit", lhsPermit), LocatorUtils.property(thatLocator, "permit", rhsPermit), lhsPermit, rhsPermit, ((this.permit!= null)&&(!this.permit.isEmpty())), ((that.permit!= null)&&(!that.permit.isEmpty())))) {
                return false;
            }
        }
        {
            List<CPER_PermitSpeciesAmount> lhsPermitSpeciesAmount;
            lhsPermitSpeciesAmount = (((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty()))?this.getPermitSpeciesAmount():null);
            List<CPER_PermitSpeciesAmount> rhsPermitSpeciesAmount;
            rhsPermitSpeciesAmount = (((that.permitSpeciesAmount!= null)&&(!that.permitSpeciesAmount.isEmpty()))?that.getPermitSpeciesAmount():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitSpeciesAmount", lhsPermitSpeciesAmount), LocatorUtils.property(thatLocator, "permitSpeciesAmount", rhsPermitSpeciesAmount), lhsPermitSpeciesAmount, rhsPermitSpeciesAmount, ((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty())), ((that.permitSpeciesAmount!= null)&&(!that.permitSpeciesAmount.isEmpty())))) {
                return false;
            }
        }
        {
            List<CPER_PermitPartner> lhsPermitPartner;
            lhsPermitPartner = (((this.permitPartner!= null)&&(!this.permitPartner.isEmpty()))?this.getPermitPartner():null);
            List<CPER_PermitPartner> rhsPermitPartner;
            rhsPermitPartner = (((that.permitPartner!= null)&&(!that.permitPartner.isEmpty()))?that.getPermitPartner():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "permitPartner", lhsPermitPartner), LocatorUtils.property(thatLocator, "permitPartner", rhsPermitPartner), lhsPermitPartner, rhsPermitPartner, ((this.permitPartner!= null)&&(!this.permitPartner.isEmpty())), ((that.permitPartner!= null)&&(!that.permitPartner.isEmpty())))) {
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
            List<CPER_Permit> thePermit;
            thePermit = (((this.permit!= null)&&(!this.permit.isEmpty()))?this.getPermit():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permit", thePermit), currentHashCode, thePermit, ((this.permit!= null)&&(!this.permit.isEmpty())));
        }
        {
            List<CPER_PermitSpeciesAmount> thePermitSpeciesAmount;
            thePermitSpeciesAmount = (((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty()))?this.getPermitSpeciesAmount():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitSpeciesAmount", thePermitSpeciesAmount), currentHashCode, thePermitSpeciesAmount, ((this.permitSpeciesAmount!= null)&&(!this.permitSpeciesAmount.isEmpty())));
        }
        {
            List<CPER_PermitPartner> thePermitPartner;
            thePermitPartner = (((this.permitPartner!= null)&&(!this.permitPartner.isEmpty()))?this.getPermitPartner():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "permitPartner", thePermitPartner), currentHashCode, thePermitPartner, ((this.permitPartner!= null)&&(!this.permitPartner.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
