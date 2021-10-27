
package fi.riista.integration.common.export.otherwisedeceased;

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
 *         &lt;element name="deceasedAnimal" type="{http://riista.fi/integration/common/export/2018/10}DeceasedAnimal" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "deceasedAnimal"
})
@XmlRootElement(name = "deceasedAnimals")
public class ODA_DeceasedAnimals implements Equals2, HashCode2, ToString2
{

    protected List<ODA_DeceasedAnimal> deceasedAnimal;

    /**
     * Gets the value of the deceasedAnimal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deceasedAnimal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeceasedAnimal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ODA_DeceasedAnimal }
     * 
     * 
     */
    public List<ODA_DeceasedAnimal> getDeceasedAnimal() {
        if (deceasedAnimal == null) {
            deceasedAnimal = new ArrayList<ODA_DeceasedAnimal>();
        }
        return this.deceasedAnimal;
    }

    public ODA_DeceasedAnimals withDeceasedAnimal(ODA_DeceasedAnimal... values) {
        if (values!= null) {
            for (ODA_DeceasedAnimal value: values) {
                getDeceasedAnimal().add(value);
            }
        }
        return this;
    }

    public ODA_DeceasedAnimals withDeceasedAnimal(Collection<ODA_DeceasedAnimal> values) {
        if (values!= null) {
            getDeceasedAnimal().addAll(values);
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
            List<ODA_DeceasedAnimal> theDeceasedAnimal;
            theDeceasedAnimal = (((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty()))?this.getDeceasedAnimal():null);
            strategy.appendField(locator, this, "deceasedAnimal", buffer, theDeceasedAnimal, ((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty())));
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
        final ODA_DeceasedAnimals that = ((ODA_DeceasedAnimals) object);
        {
            List<ODA_DeceasedAnimal> lhsDeceasedAnimal;
            lhsDeceasedAnimal = (((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty()))?this.getDeceasedAnimal():null);
            List<ODA_DeceasedAnimal> rhsDeceasedAnimal;
            rhsDeceasedAnimal = (((that.deceasedAnimal!= null)&&(!that.deceasedAnimal.isEmpty()))?that.getDeceasedAnimal():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "deceasedAnimal", lhsDeceasedAnimal), LocatorUtils.property(thatLocator, "deceasedAnimal", rhsDeceasedAnimal), lhsDeceasedAnimal, rhsDeceasedAnimal, ((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty())), ((that.deceasedAnimal!= null)&&(!that.deceasedAnimal.isEmpty())))) {
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
            List<ODA_DeceasedAnimal> theDeceasedAnimal;
            theDeceasedAnimal = (((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty()))?this.getDeceasedAnimal():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "deceasedAnimal", theDeceasedAnimal), currentHashCode, theDeceasedAnimal, ((this.deceasedAnimal!= null)&&(!this.deceasedAnimal.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
