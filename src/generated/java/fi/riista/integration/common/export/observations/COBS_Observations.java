
package fi.riista.integration.common.export.observations;

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
 *         &lt;element name="observation" type="{http://riista.fi/integration/common/export/2018/10}Observation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="observationSpecimen" type="{http://riista.fi/integration/common/export/2018/10}ObservationSpecimen" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "observation",
    "observationSpecimen"
})
@XmlRootElement(name = "observations")
public class COBS_Observations implements Equals2, HashCode2, ToString2
{

    protected List<COBS_Observation> observation;
    protected List<COBS_ObservationSpecimen> observationSpecimen;

    /**
     * Gets the value of the observation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link COBS_Observation }
     * 
     * 
     */
    public List<COBS_Observation> getObservation() {
        if (observation == null) {
            observation = new ArrayList<COBS_Observation>();
        }
        return this.observation;
    }

    /**
     * Gets the value of the observationSpecimen property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observationSpecimen property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservationSpecimen().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link COBS_ObservationSpecimen }
     * 
     * 
     */
    public List<COBS_ObservationSpecimen> getObservationSpecimen() {
        if (observationSpecimen == null) {
            observationSpecimen = new ArrayList<COBS_ObservationSpecimen>();
        }
        return this.observationSpecimen;
    }

    public COBS_Observations withObservation(COBS_Observation... values) {
        if (values!= null) {
            for (COBS_Observation value: values) {
                getObservation().add(value);
            }
        }
        return this;
    }

    public COBS_Observations withObservation(Collection<COBS_Observation> values) {
        if (values!= null) {
            getObservation().addAll(values);
        }
        return this;
    }

    public COBS_Observations withObservationSpecimen(COBS_ObservationSpecimen... values) {
        if (values!= null) {
            for (COBS_ObservationSpecimen value: values) {
                getObservationSpecimen().add(value);
            }
        }
        return this;
    }

    public COBS_Observations withObservationSpecimen(Collection<COBS_ObservationSpecimen> values) {
        if (values!= null) {
            getObservationSpecimen().addAll(values);
        }
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
            List<COBS_Observation> theObservation;
            theObservation = (((this.observation!= null)&&(!this.observation.isEmpty()))?this.getObservation():null);
            strategy.appendField(locator, this, "observation", buffer, theObservation, ((this.observation!= null)&&(!this.observation.isEmpty())));
        }
        {
            List<COBS_ObservationSpecimen> theObservationSpecimen;
            theObservationSpecimen = (((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty()))?this.getObservationSpecimen():null);
            strategy.appendField(locator, this, "observationSpecimen", buffer, theObservationSpecimen, ((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty())));
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
        final COBS_Observations that = ((COBS_Observations) object);
        {
            List<COBS_Observation> lhsObservation;
            lhsObservation = (((this.observation!= null)&&(!this.observation.isEmpty()))?this.getObservation():null);
            List<COBS_Observation> rhsObservation;
            rhsObservation = (((that.observation!= null)&&(!that.observation.isEmpty()))?that.getObservation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "observation", lhsObservation), LocatorUtils.property(thatLocator, "observation", rhsObservation), lhsObservation, rhsObservation, ((this.observation!= null)&&(!this.observation.isEmpty())), ((that.observation!= null)&&(!that.observation.isEmpty())))) {
                return false;
            }
        }
        {
            List<COBS_ObservationSpecimen> lhsObservationSpecimen;
            lhsObservationSpecimen = (((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty()))?this.getObservationSpecimen():null);
            List<COBS_ObservationSpecimen> rhsObservationSpecimen;
            rhsObservationSpecimen = (((that.observationSpecimen!= null)&&(!that.observationSpecimen.isEmpty()))?that.getObservationSpecimen():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "observationSpecimen", lhsObservationSpecimen), LocatorUtils.property(thatLocator, "observationSpecimen", rhsObservationSpecimen), lhsObservationSpecimen, rhsObservationSpecimen, ((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty())), ((that.observationSpecimen!= null)&&(!that.observationSpecimen.isEmpty())))) {
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
            List<COBS_Observation> theObservation;
            theObservation = (((this.observation!= null)&&(!this.observation.isEmpty()))?this.getObservation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "observation", theObservation), currentHashCode, theObservation, ((this.observation!= null)&&(!this.observation.isEmpty())));
        }
        {
            List<COBS_ObservationSpecimen> theObservationSpecimen;
            theObservationSpecimen = (((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty()))?this.getObservationSpecimen():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "observationSpecimen", theObservationSpecimen), currentHashCode, theObservationSpecimen, ((this.observationSpecimen!= null)&&(!this.observationSpecimen.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
