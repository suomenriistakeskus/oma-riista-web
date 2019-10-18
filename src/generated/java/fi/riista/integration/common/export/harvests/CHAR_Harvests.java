
package fi.riista.integration.common.export.harvests;

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
 *         &lt;element name="harvest" type="{http://riista.fi/integration/common/export/2018/10}Harvest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="specimen" type="{http://riista.fi/integration/common/export/2018/10}Specimen" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "harvest",
    "specimen"
})
@XmlRootElement(name = "harvests")
public class CHAR_Harvests implements Equals2, HashCode2, ToString2
{

    protected List<CHAR_Harvest> harvest;
    protected List<CHAR_Specimen> specimen;

    /**
     * Gets the value of the harvest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the harvest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHarvest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CHAR_Harvest }
     * 
     * 
     */
    public List<CHAR_Harvest> getHarvest() {
        if (harvest == null) {
            harvest = new ArrayList<CHAR_Harvest>();
        }
        return this.harvest;
    }

    /**
     * Gets the value of the specimen property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specimen property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecimen().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CHAR_Specimen }
     * 
     * 
     */
    public List<CHAR_Specimen> getSpecimen() {
        if (specimen == null) {
            specimen = new ArrayList<CHAR_Specimen>();
        }
        return this.specimen;
    }

    public CHAR_Harvests withHarvest(CHAR_Harvest... values) {
        if (values!= null) {
            for (CHAR_Harvest value: values) {
                getHarvest().add(value);
            }
        }
        return this;
    }

    public CHAR_Harvests withHarvest(Collection<CHAR_Harvest> values) {
        if (values!= null) {
            getHarvest().addAll(values);
        }
        return this;
    }

    public CHAR_Harvests withSpecimen(CHAR_Specimen... values) {
        if (values!= null) {
            for (CHAR_Specimen value: values) {
                getSpecimen().add(value);
            }
        }
        return this;
    }

    public CHAR_Harvests withSpecimen(Collection<CHAR_Specimen> values) {
        if (values!= null) {
            getSpecimen().addAll(values);
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
            List<CHAR_Harvest> theHarvest;
            theHarvest = (((this.harvest!= null)&&(!this.harvest.isEmpty()))?this.getHarvest():null);
            strategy.appendField(locator, this, "harvest", buffer, theHarvest, ((this.harvest!= null)&&(!this.harvest.isEmpty())));
        }
        {
            List<CHAR_Specimen> theSpecimen;
            theSpecimen = (((this.specimen!= null)&&(!this.specimen.isEmpty()))?this.getSpecimen():null);
            strategy.appendField(locator, this, "specimen", buffer, theSpecimen, ((this.specimen!= null)&&(!this.specimen.isEmpty())));
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
        final CHAR_Harvests that = ((CHAR_Harvests) object);
        {
            List<CHAR_Harvest> lhsHarvest;
            lhsHarvest = (((this.harvest!= null)&&(!this.harvest.isEmpty()))?this.getHarvest():null);
            List<CHAR_Harvest> rhsHarvest;
            rhsHarvest = (((that.harvest!= null)&&(!that.harvest.isEmpty()))?that.getHarvest():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "harvest", lhsHarvest), LocatorUtils.property(thatLocator, "harvest", rhsHarvest), lhsHarvest, rhsHarvest, ((this.harvest!= null)&&(!this.harvest.isEmpty())), ((that.harvest!= null)&&(!that.harvest.isEmpty())))) {
                return false;
            }
        }
        {
            List<CHAR_Specimen> lhsSpecimen;
            lhsSpecimen = (((this.specimen!= null)&&(!this.specimen.isEmpty()))?this.getSpecimen():null);
            List<CHAR_Specimen> rhsSpecimen;
            rhsSpecimen = (((that.specimen!= null)&&(!that.specimen.isEmpty()))?that.getSpecimen():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "specimen", lhsSpecimen), LocatorUtils.property(thatLocator, "specimen", rhsSpecimen), lhsSpecimen, rhsSpecimen, ((this.specimen!= null)&&(!this.specimen.isEmpty())), ((that.specimen!= null)&&(!that.specimen.isEmpty())))) {
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
            List<CHAR_Harvest> theHarvest;
            theHarvest = (((this.harvest!= null)&&(!this.harvest.isEmpty()))?this.getHarvest():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "harvest", theHarvest), currentHashCode, theHarvest, ((this.harvest!= null)&&(!this.harvest.isEmpty())));
        }
        {
            List<CHAR_Specimen> theSpecimen;
            theSpecimen = (((this.specimen!= null)&&(!this.specimen.isEmpty()))?this.getSpecimen():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "specimen", theSpecimen), currentHashCode, theSpecimen, ((this.specimen!= null)&&(!this.specimen.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
