
package fi.riista.integration.habides.export.derogations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * <p>Java class for derogationJustificationsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="derogationJustificationsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="derogationJustification" type="{}derogationJustificationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "derogationJustificationsType", propOrder = {
    "derogationJustification"
})
public class DERO_DerogationJustificationsType implements Equals2, HashCode2, ToString2
{

    protected List<String> derogationJustification;

    /**
     * Gets the value of the derogationJustification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the derogationJustification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDerogationJustification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDerogationJustification() {
        if (derogationJustification == null) {
            derogationJustification = new ArrayList<String>();
        }
        return this.derogationJustification;
    }

    public DERO_DerogationJustificationsType withDerogationJustification(String... values) {
        if (values!= null) {
            for (String value: values) {
                getDerogationJustification().add(value);
            }
        }
        return this;
    }

    public DERO_DerogationJustificationsType withDerogationJustification(Collection<String> values) {
        if (values!= null) {
            getDerogationJustification().addAll(values);
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
            List<String> theDerogationJustification;
            theDerogationJustification = (((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty()))?this.getDerogationJustification():null);
            strategy.appendField(locator, this, "derogationJustification", buffer, theDerogationJustification, ((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty())));
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
        final DERO_DerogationJustificationsType that = ((DERO_DerogationJustificationsType) object);
        {
            List<String> lhsDerogationJustification;
            lhsDerogationJustification = (((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty()))?this.getDerogationJustification():null);
            List<String> rhsDerogationJustification;
            rhsDerogationJustification = (((that.derogationJustification!= null)&&(!that.derogationJustification.isEmpty()))?that.getDerogationJustification():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogationJustification", lhsDerogationJustification), LocatorUtils.property(thatLocator, "derogationJustification", rhsDerogationJustification), lhsDerogationJustification, rhsDerogationJustification, ((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty())), ((that.derogationJustification!= null)&&(!that.derogationJustification.isEmpty())))) {
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
            List<String> theDerogationJustification;
            theDerogationJustification = (((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty()))?this.getDerogationJustification():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogationJustification", theDerogationJustification), currentHashCode, theDerogationJustification, ((this.derogationJustification!= null)&&(!this.derogationJustification.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
