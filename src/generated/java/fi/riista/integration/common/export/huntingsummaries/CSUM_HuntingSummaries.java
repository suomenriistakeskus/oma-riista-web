
package fi.riista.integration.common.export.huntingsummaries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="hunting-summary" type="{http://riista.fi/integration/common/export/2018/10}ClubHuntingSummary" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "huntingSummary"
})
@XmlRootElement(name = "hunting-summaries")
public class CSUM_HuntingSummaries implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "hunting-summary")
    protected List<CSUM_ClubHuntingSummary> huntingSummary;

    /**
     * Gets the value of the huntingSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the huntingSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHuntingSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CSUM_ClubHuntingSummary }
     * 
     * 
     */
    public List<CSUM_ClubHuntingSummary> getHuntingSummary() {
        if (huntingSummary == null) {
            huntingSummary = new ArrayList<CSUM_ClubHuntingSummary>();
        }
        return this.huntingSummary;
    }

    public CSUM_HuntingSummaries withHuntingSummary(CSUM_ClubHuntingSummary... values) {
        if (values!= null) {
            for (CSUM_ClubHuntingSummary value: values) {
                getHuntingSummary().add(value);
            }
        }
        return this;
    }

    public CSUM_HuntingSummaries withHuntingSummary(Collection<CSUM_ClubHuntingSummary> values) {
        if (values!= null) {
            getHuntingSummary().addAll(values);
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
            List<CSUM_ClubHuntingSummary> theHuntingSummary;
            theHuntingSummary = (((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty()))?this.getHuntingSummary():null);
            strategy.appendField(locator, this, "huntingSummary", buffer, theHuntingSummary, ((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty())));
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
        final CSUM_HuntingSummaries that = ((CSUM_HuntingSummaries) object);
        {
            List<CSUM_ClubHuntingSummary> lhsHuntingSummary;
            lhsHuntingSummary = (((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty()))?this.getHuntingSummary():null);
            List<CSUM_ClubHuntingSummary> rhsHuntingSummary;
            rhsHuntingSummary = (((that.huntingSummary!= null)&&(!that.huntingSummary.isEmpty()))?that.getHuntingSummary():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "huntingSummary", lhsHuntingSummary), LocatorUtils.property(thatLocator, "huntingSummary", rhsHuntingSummary), lhsHuntingSummary, rhsHuntingSummary, ((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty())), ((that.huntingSummary!= null)&&(!that.huntingSummary.isEmpty())))) {
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
            List<CSUM_ClubHuntingSummary> theHuntingSummary;
            theHuntingSummary = (((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty()))?this.getHuntingSummary():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "huntingSummary", theHuntingSummary), currentHashCode, theHuntingSummary, ((this.huntingSummary!= null)&&(!this.huntingSummary.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
