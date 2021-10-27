
package fi.riista.integration.common.export.srva;

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
 *         &lt;element name="srvaEvent" type="{http://riista.fi/integration/common/export/2018/10}SRVAEvent" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="srvaSpecimen" type="{http://riista.fi/integration/common/export/2018/10}SRVASpecimen" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "srvaEvent",
    "srvaSpecimen"
})
@XmlRootElement(name = "srvaEvents")
public class CEV_SrvaEvents implements Equals2, HashCode2, ToString2
{

    protected List<CEV_SRVAEvent> srvaEvent;
    protected List<CEV_SRVASpecimen> srvaSpecimen;

    /**
     * Gets the value of the srvaEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the srvaEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSrvaEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CEV_SRVAEvent }
     * 
     * 
     */
    public List<CEV_SRVAEvent> getSrvaEvent() {
        if (srvaEvent == null) {
            srvaEvent = new ArrayList<CEV_SRVAEvent>();
        }
        return this.srvaEvent;
    }

    /**
     * Gets the value of the srvaSpecimen property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the srvaSpecimen property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSrvaSpecimen().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CEV_SRVASpecimen }
     * 
     * 
     */
    public List<CEV_SRVASpecimen> getSrvaSpecimen() {
        if (srvaSpecimen == null) {
            srvaSpecimen = new ArrayList<CEV_SRVASpecimen>();
        }
        return this.srvaSpecimen;
    }

    public CEV_SrvaEvents withSrvaEvent(CEV_SRVAEvent... values) {
        if (values!= null) {
            for (CEV_SRVAEvent value: values) {
                getSrvaEvent().add(value);
            }
        }
        return this;
    }

    public CEV_SrvaEvents withSrvaEvent(Collection<CEV_SRVAEvent> values) {
        if (values!= null) {
            getSrvaEvent().addAll(values);
        }
        return this;
    }

    public CEV_SrvaEvents withSrvaSpecimen(CEV_SRVASpecimen... values) {
        if (values!= null) {
            for (CEV_SRVASpecimen value: values) {
                getSrvaSpecimen().add(value);
            }
        }
        return this;
    }

    public CEV_SrvaEvents withSrvaSpecimen(Collection<CEV_SRVASpecimen> values) {
        if (values!= null) {
            getSrvaSpecimen().addAll(values);
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
            List<CEV_SRVAEvent> theSrvaEvent;
            theSrvaEvent = (((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty()))?this.getSrvaEvent():null);
            strategy.appendField(locator, this, "srvaEvent", buffer, theSrvaEvent, ((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty())));
        }
        {
            List<CEV_SRVASpecimen> theSrvaSpecimen;
            theSrvaSpecimen = (((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty()))?this.getSrvaSpecimen():null);
            strategy.appendField(locator, this, "srvaSpecimen", buffer, theSrvaSpecimen, ((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty())));
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
        final CEV_SrvaEvents that = ((CEV_SrvaEvents) object);
        {
            List<CEV_SRVAEvent> lhsSrvaEvent;
            lhsSrvaEvent = (((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty()))?this.getSrvaEvent():null);
            List<CEV_SRVAEvent> rhsSrvaEvent;
            rhsSrvaEvent = (((that.srvaEvent!= null)&&(!that.srvaEvent.isEmpty()))?that.getSrvaEvent():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "srvaEvent", lhsSrvaEvent), LocatorUtils.property(thatLocator, "srvaEvent", rhsSrvaEvent), lhsSrvaEvent, rhsSrvaEvent, ((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty())), ((that.srvaEvent!= null)&&(!that.srvaEvent.isEmpty())))) {
                return false;
            }
        }
        {
            List<CEV_SRVASpecimen> lhsSrvaSpecimen;
            lhsSrvaSpecimen = (((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty()))?this.getSrvaSpecimen():null);
            List<CEV_SRVASpecimen> rhsSrvaSpecimen;
            rhsSrvaSpecimen = (((that.srvaSpecimen!= null)&&(!that.srvaSpecimen.isEmpty()))?that.getSrvaSpecimen():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "srvaSpecimen", lhsSrvaSpecimen), LocatorUtils.property(thatLocator, "srvaSpecimen", rhsSrvaSpecimen), lhsSrvaSpecimen, rhsSrvaSpecimen, ((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty())), ((that.srvaSpecimen!= null)&&(!that.srvaSpecimen.isEmpty())))) {
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
            List<CEV_SRVAEvent> theSrvaEvent;
            theSrvaEvent = (((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty()))?this.getSrvaEvent():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "srvaEvent", theSrvaEvent), currentHashCode, theSrvaEvent, ((this.srvaEvent!= null)&&(!this.srvaEvent.isEmpty())));
        }
        {
            List<CEV_SRVASpecimen> theSrvaSpecimen;
            theSrvaSpecimen = (((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty()))?this.getSrvaSpecimen():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "srvaSpecimen", theSrvaSpecimen), currentHashCode, theSrvaSpecimen, ((this.srvaSpecimen!= null)&&(!this.srvaSpecimen.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
