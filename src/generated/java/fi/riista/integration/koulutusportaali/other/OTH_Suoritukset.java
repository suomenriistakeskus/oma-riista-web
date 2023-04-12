
package fi.riista.integration.koulutusportaali.other;

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
 *         &lt;element name="suoritus" type="{http://riista.fi/integration/koulutusportaali/other/2021/10}Suoritus" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "suoritus"
})
@XmlRootElement(name = "suoritukset")
public class OTH_Suoritukset implements Equals2, HashCode2, ToString2
{

    protected List<OTH_Suoritus> suoritus;

    /**
     * Gets the value of the suoritus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the suoritus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuoritus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OTH_Suoritus }
     * 
     * 
     */
    public List<OTH_Suoritus> getSuoritus() {
        if (suoritus == null) {
            suoritus = new ArrayList<OTH_Suoritus>();
        }
        return this.suoritus;
    }

    public OTH_Suoritukset withSuoritus(OTH_Suoritus... values) {
        if (values!= null) {
            for (OTH_Suoritus value: values) {
                getSuoritus().add(value);
            }
        }
        return this;
    }

    public OTH_Suoritukset withSuoritus(Collection<OTH_Suoritus> values) {
        if (values!= null) {
            getSuoritus().addAll(values);
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
            List<OTH_Suoritus> theSuoritus;
            theSuoritus = (((this.suoritus!= null)&&(!this.suoritus.isEmpty()))?this.getSuoritus():null);
            strategy.appendField(locator, this, "suoritus", buffer, theSuoritus, ((this.suoritus!= null)&&(!this.suoritus.isEmpty())));
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
        final OTH_Suoritukset that = ((OTH_Suoritukset) object);
        {
            List<OTH_Suoritus> lhsSuoritus;
            lhsSuoritus = (((this.suoritus!= null)&&(!this.suoritus.isEmpty()))?this.getSuoritus():null);
            List<OTH_Suoritus> rhsSuoritus;
            rhsSuoritus = (((that.suoritus!= null)&&(!that.suoritus.isEmpty()))?that.getSuoritus():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "suoritus", lhsSuoritus), LocatorUtils.property(thatLocator, "suoritus", rhsSuoritus), lhsSuoritus, rhsSuoritus, ((this.suoritus!= null)&&(!this.suoritus.isEmpty())), ((that.suoritus!= null)&&(!that.suoritus.isEmpty())))) {
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
            List<OTH_Suoritus> theSuoritus;
            theSuoritus = (((this.suoritus!= null)&&(!this.suoritus.isEmpty()))?this.getSuoritus():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "suoritus", theSuoritus), currentHashCode, theSuoritus, ((this.suoritus!= null)&&(!this.suoritus.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
