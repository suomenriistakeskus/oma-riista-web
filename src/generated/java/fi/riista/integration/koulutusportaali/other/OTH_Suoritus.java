
package fi.riista.integration.koulutusportaali.other;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;
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
 * <p>Java class for Suoritus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Suoritus"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="hetu" type="{http://riista.fi/integration/koulutusportaali/other/2021/10}HenkiloTunnus"/&gt;
 *           &lt;element name="metsastajaNumero" type="{http://riista.fi/integration/koulutusportaali/other/2021/10}MetsastajaNumero"/&gt;
 *           &lt;element name="omaRiistaPersonId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="tehtavaTyyppi" type="{http://riista.fi/integration/koulutusportaali/other/2021/10}TehtavaTyyppi"/&gt;
 *         &lt;element name="suoritusPvm" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Suoritus", propOrder = {
    "id",
    "hetu",
    "metsastajaNumero",
    "omaRiistaPersonId",
    "tehtavaTyyppi",
    "suoritusPvm"
})
public class OTH_Suoritus implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String hetu;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String metsastajaNumero;
    protected Long omaRiistaPersonId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected OTH_TehtavaTyyppi tehtavaTyyppi;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate suoritusPvm;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the hetu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHetu() {
        return hetu;
    }

    /**
     * Sets the value of the hetu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHetu(String value) {
        this.hetu = value;
    }

    /**
     * Gets the value of the metsastajaNumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetsastajaNumero() {
        return metsastajaNumero;
    }

    /**
     * Sets the value of the metsastajaNumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetsastajaNumero(String value) {
        this.metsastajaNumero = value;
    }

    /**
     * Gets the value of the omaRiistaPersonId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOmaRiistaPersonId() {
        return omaRiistaPersonId;
    }

    /**
     * Sets the value of the omaRiistaPersonId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOmaRiistaPersonId(Long value) {
        this.omaRiistaPersonId = value;
    }

    /**
     * Gets the value of the tehtavaTyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link OTH_TehtavaTyyppi }
     *     
     */
    public OTH_TehtavaTyyppi getTehtavaTyyppi() {
        return tehtavaTyyppi;
    }

    /**
     * Sets the value of the tehtavaTyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link OTH_TehtavaTyyppi }
     *     
     */
    public void setTehtavaTyyppi(OTH_TehtavaTyyppi value) {
        this.tehtavaTyyppi = value;
    }

    /**
     * Gets the value of the suoritusPvm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getSuoritusPvm() {
        return suoritusPvm;
    }

    /**
     * Sets the value of the suoritusPvm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuoritusPvm(LocalDate value) {
        this.suoritusPvm = value;
    }

    public OTH_Suoritus withId(String value) {
        setId(value);
        return this;
    }

    public OTH_Suoritus withHetu(String value) {
        setHetu(value);
        return this;
    }

    public OTH_Suoritus withMetsastajaNumero(String value) {
        setMetsastajaNumero(value);
        return this;
    }

    public OTH_Suoritus withOmaRiistaPersonId(Long value) {
        setOmaRiistaPersonId(value);
        return this;
    }

    public OTH_Suoritus withTehtavaTyyppi(OTH_TehtavaTyyppi value) {
        setTehtavaTyyppi(value);
        return this;
    }

    public OTH_Suoritus withSuoritusPvm(LocalDate value) {
        setSuoritusPvm(value);
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
            String theId;
            theId = this.getId();
            strategy.appendField(locator, this, "id", buffer, theId, (this.id!= null));
        }
        {
            String theHetu;
            theHetu = this.getHetu();
            strategy.appendField(locator, this, "hetu", buffer, theHetu, (this.hetu!= null));
        }
        {
            String theMetsastajaNumero;
            theMetsastajaNumero = this.getMetsastajaNumero();
            strategy.appendField(locator, this, "metsastajaNumero", buffer, theMetsastajaNumero, (this.metsastajaNumero!= null));
        }
        {
            Long theOmaRiistaPersonId;
            theOmaRiistaPersonId = this.getOmaRiistaPersonId();
            strategy.appendField(locator, this, "omaRiistaPersonId", buffer, theOmaRiistaPersonId, (this.omaRiistaPersonId!= null));
        }
        {
            OTH_TehtavaTyyppi theTehtavaTyyppi;
            theTehtavaTyyppi = this.getTehtavaTyyppi();
            strategy.appendField(locator, this, "tehtavaTyyppi", buffer, theTehtavaTyyppi, (this.tehtavaTyyppi!= null));
        }
        {
            LocalDate theSuoritusPvm;
            theSuoritusPvm = this.getSuoritusPvm();
            strategy.appendField(locator, this, "suoritusPvm", buffer, theSuoritusPvm, (this.suoritusPvm!= null));
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
        final OTH_Suoritus that = ((OTH_Suoritus) object);
        {
            String lhsId;
            lhsId = this.getId();
            String rhsId;
            rhsId = that.getId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsId), LocatorUtils.property(thatLocator, "id", rhsId), lhsId, rhsId, (this.id!= null), (that.id!= null))) {
                return false;
            }
        }
        {
            String lhsHetu;
            lhsHetu = this.getHetu();
            String rhsHetu;
            rhsHetu = that.getHetu();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "hetu", lhsHetu), LocatorUtils.property(thatLocator, "hetu", rhsHetu), lhsHetu, rhsHetu, (this.hetu!= null), (that.hetu!= null))) {
                return false;
            }
        }
        {
            String lhsMetsastajaNumero;
            lhsMetsastajaNumero = this.getMetsastajaNumero();
            String rhsMetsastajaNumero;
            rhsMetsastajaNumero = that.getMetsastajaNumero();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "metsastajaNumero", lhsMetsastajaNumero), LocatorUtils.property(thatLocator, "metsastajaNumero", rhsMetsastajaNumero), lhsMetsastajaNumero, rhsMetsastajaNumero, (this.metsastajaNumero!= null), (that.metsastajaNumero!= null))) {
                return false;
            }
        }
        {
            Long lhsOmaRiistaPersonId;
            lhsOmaRiistaPersonId = this.getOmaRiistaPersonId();
            Long rhsOmaRiistaPersonId;
            rhsOmaRiistaPersonId = that.getOmaRiistaPersonId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "omaRiistaPersonId", lhsOmaRiistaPersonId), LocatorUtils.property(thatLocator, "omaRiistaPersonId", rhsOmaRiistaPersonId), lhsOmaRiistaPersonId, rhsOmaRiistaPersonId, (this.omaRiistaPersonId!= null), (that.omaRiistaPersonId!= null))) {
                return false;
            }
        }
        {
            OTH_TehtavaTyyppi lhsTehtavaTyyppi;
            lhsTehtavaTyyppi = this.getTehtavaTyyppi();
            OTH_TehtavaTyyppi rhsTehtavaTyyppi;
            rhsTehtavaTyyppi = that.getTehtavaTyyppi();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "tehtavaTyyppi", lhsTehtavaTyyppi), LocatorUtils.property(thatLocator, "tehtavaTyyppi", rhsTehtavaTyyppi), lhsTehtavaTyyppi, rhsTehtavaTyyppi, (this.tehtavaTyyppi!= null), (that.tehtavaTyyppi!= null))) {
                return false;
            }
        }
        {
            LocalDate lhsSuoritusPvm;
            lhsSuoritusPvm = this.getSuoritusPvm();
            LocalDate rhsSuoritusPvm;
            rhsSuoritusPvm = that.getSuoritusPvm();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "suoritusPvm", lhsSuoritusPvm), LocatorUtils.property(thatLocator, "suoritusPvm", rhsSuoritusPvm), lhsSuoritusPvm, rhsSuoritusPvm, (this.suoritusPvm!= null), (that.suoritusPvm!= null))) {
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
            String theId;
            theId = this.getId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "id", theId), currentHashCode, theId, (this.id!= null));
        }
        {
            String theHetu;
            theHetu = this.getHetu();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "hetu", theHetu), currentHashCode, theHetu, (this.hetu!= null));
        }
        {
            String theMetsastajaNumero;
            theMetsastajaNumero = this.getMetsastajaNumero();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "metsastajaNumero", theMetsastajaNumero), currentHashCode, theMetsastajaNumero, (this.metsastajaNumero!= null));
        }
        {
            Long theOmaRiistaPersonId;
            theOmaRiistaPersonId = this.getOmaRiistaPersonId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "omaRiistaPersonId", theOmaRiistaPersonId), currentHashCode, theOmaRiistaPersonId, (this.omaRiistaPersonId!= null));
        }
        {
            OTH_TehtavaTyyppi theTehtavaTyyppi;
            theTehtavaTyyppi = this.getTehtavaTyyppi();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "tehtavaTyyppi", theTehtavaTyyppi), currentHashCode, theTehtavaTyyppi, (this.tehtavaTyyppi!= null));
        }
        {
            LocalDate theSuoritusPvm;
            theSuoritusPvm = this.getSuoritusPvm();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "suoritusPvm", theSuoritusPvm), currentHashCode, theSuoritusPvm, (this.suoritusPvm!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
