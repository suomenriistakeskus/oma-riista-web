
package fi.riista.integration.habides.export.derogations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
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
 *         &lt;element ref="{}derogation" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="country" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="userIdentity" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="labelLanguage" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang use="required""/&gt;
 *       &lt;anyAttribute processContents='lax' namespace='http://www.w3.org/XML/1998/namespace'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "derogation"
})
@XmlRootElement(name = "derogations")
public class DERO_Derogations implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected List<DERO_Derogation> derogation;
    @XmlAttribute(name = "country")
    protected String country;
    @XmlAttribute(name = "userIdentity")
    protected String userIdentity;
    @XmlAttribute(name = "labelLanguage")
    protected String labelLanguage;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    protected String lang;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the derogation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the derogation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDerogation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DERO_Derogation }
     * 
     * 
     */
    public List<DERO_Derogation> getDerogation() {
        if (derogation == null) {
            derogation = new ArrayList<DERO_Derogation>();
        }
        return this.derogation;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the userIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserIdentity() {
        return userIdentity;
    }

    /**
     * Sets the value of the userIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserIdentity(String value) {
        this.userIdentity = value;
    }

    /**
     * Gets the value of the labelLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelLanguage() {
        return labelLanguage;
    }

    /**
     * Sets the value of the labelLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelLanguage(String value) {
        this.labelLanguage = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public DERO_Derogations withDerogation(DERO_Derogation... values) {
        if (values!= null) {
            for (DERO_Derogation value: values) {
                getDerogation().add(value);
            }
        }
        return this;
    }

    public DERO_Derogations withDerogation(Collection<DERO_Derogation> values) {
        if (values!= null) {
            getDerogation().addAll(values);
        }
        return this;
    }

    public DERO_Derogations withCountry(String value) {
        setCountry(value);
        return this;
    }

    public DERO_Derogations withUserIdentity(String value) {
        setUserIdentity(value);
        return this;
    }

    public DERO_Derogations withLabelLanguage(String value) {
        setLabelLanguage(value);
        return this;
    }

    public DERO_Derogations withLang(String value) {
        setLang(value);
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
            List<DERO_Derogation> theDerogation;
            theDerogation = (((this.derogation!= null)&&(!this.derogation.isEmpty()))?this.getDerogation():null);
            strategy.appendField(locator, this, "derogation", buffer, theDerogation, ((this.derogation!= null)&&(!this.derogation.isEmpty())));
        }
        {
            String theCountry;
            theCountry = this.getCountry();
            strategy.appendField(locator, this, "country", buffer, theCountry, (this.country!= null));
        }
        {
            String theUserIdentity;
            theUserIdentity = this.getUserIdentity();
            strategy.appendField(locator, this, "userIdentity", buffer, theUserIdentity, (this.userIdentity!= null));
        }
        {
            String theLabelLanguage;
            theLabelLanguage = this.getLabelLanguage();
            strategy.appendField(locator, this, "labelLanguage", buffer, theLabelLanguage, (this.labelLanguage!= null));
        }
        {
            String theLang;
            theLang = this.getLang();
            strategy.appendField(locator, this, "lang", buffer, theLang, (this.lang!= null));
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
        final DERO_Derogations that = ((DERO_Derogations) object);
        {
            List<DERO_Derogation> lhsDerogation;
            lhsDerogation = (((this.derogation!= null)&&(!this.derogation.isEmpty()))?this.getDerogation():null);
            List<DERO_Derogation> rhsDerogation;
            rhsDerogation = (((that.derogation!= null)&&(!that.derogation.isEmpty()))?that.getDerogation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogation", lhsDerogation), LocatorUtils.property(thatLocator, "derogation", rhsDerogation), lhsDerogation, rhsDerogation, ((this.derogation!= null)&&(!this.derogation.isEmpty())), ((that.derogation!= null)&&(!that.derogation.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsCountry;
            lhsCountry = this.getCountry();
            String rhsCountry;
            rhsCountry = that.getCountry();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "country", lhsCountry), LocatorUtils.property(thatLocator, "country", rhsCountry), lhsCountry, rhsCountry, (this.country!= null), (that.country!= null))) {
                return false;
            }
        }
        {
            String lhsUserIdentity;
            lhsUserIdentity = this.getUserIdentity();
            String rhsUserIdentity;
            rhsUserIdentity = that.getUserIdentity();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "userIdentity", lhsUserIdentity), LocatorUtils.property(thatLocator, "userIdentity", rhsUserIdentity), lhsUserIdentity, rhsUserIdentity, (this.userIdentity!= null), (that.userIdentity!= null))) {
                return false;
            }
        }
        {
            String lhsLabelLanguage;
            lhsLabelLanguage = this.getLabelLanguage();
            String rhsLabelLanguage;
            rhsLabelLanguage = that.getLabelLanguage();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "labelLanguage", lhsLabelLanguage), LocatorUtils.property(thatLocator, "labelLanguage", rhsLabelLanguage), lhsLabelLanguage, rhsLabelLanguage, (this.labelLanguage!= null), (that.labelLanguage!= null))) {
                return false;
            }
        }
        {
            String lhsLang;
            lhsLang = this.getLang();
            String rhsLang;
            rhsLang = that.getLang();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "lang", lhsLang), LocatorUtils.property(thatLocator, "lang", rhsLang), lhsLang, rhsLang, (this.lang!= null), (that.lang!= null))) {
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
            List<DERO_Derogation> theDerogation;
            theDerogation = (((this.derogation!= null)&&(!this.derogation.isEmpty()))?this.getDerogation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogation", theDerogation), currentHashCode, theDerogation, ((this.derogation!= null)&&(!this.derogation.isEmpty())));
        }
        {
            String theCountry;
            theCountry = this.getCountry();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "country", theCountry), currentHashCode, theCountry, (this.country!= null));
        }
        {
            String theUserIdentity;
            theUserIdentity = this.getUserIdentity();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "userIdentity", theUserIdentity), currentHashCode, theUserIdentity, (this.userIdentity!= null));
        }
        {
            String theLabelLanguage;
            theLabelLanguage = this.getLabelLanguage();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "labelLanguage", theLabelLanguage), currentHashCode, theLabelLanguage, (this.labelLanguage!= null));
        }
        {
            String theLang;
            theLang = this.getLang();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "lang", theLang), currentHashCode, theLang, (this.lang!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
