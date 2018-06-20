
package fi.riista.integration.luke_import.model.v1_0;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for _1.2Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_1.2Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_Sukunimi_Etunimi" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Henkilötunnus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Metsästäjänumero" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Lähiosoite" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Postinumero_ja_-toimipaikka" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Puhelin" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Sähköpostiosoite" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;anyAttribute namespace='http://www.abbyy.com/FlexiCapture/Schemas/Export/AdditionalFormData.xsd'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "_1.2Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "lastNameFirstName",
    "ssn",
    "hunterNumber",
    "streetAddress",
    "postalCodeAndCity",
    "phoneNumber",
    "emailAddress"
})
public class MooseDataCardContactPerson implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_Sukunimi_Etunimi", required = true)
    protected String lastNameFirstName;
    @XmlElement(name = "_Henkil\u00f6tunnus", required = true)
    protected String ssn;
    @XmlElement(name = "_Mets\u00e4st\u00e4j\u00e4numero", required = true)
    protected String hunterNumber;
    @XmlElement(name = "_L\u00e4hiosoite", required = true)
    protected String streetAddress;
    @XmlElement(name = "_Postinumero_ja_-toimipaikka", required = true)
    protected String postalCodeAndCity;
    @XmlElement(name = "_Puhelin", required = true)
    protected String phoneNumber;
    @XmlElement(name = "_S\u00e4hk\u00f6postiosoite", required = true)
    protected String emailAddress;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardContactPerson() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardContactPerson copying the state of another MooseDataCardContactPerson
     * 
     * @param _other
     *     The original MooseDataCardContactPerson from which to copy state.
     */
    public MooseDataCardContactPerson(final MooseDataCardContactPerson _other) {
        this.lastNameFirstName = _other.lastNameFirstName;
        this.ssn = _other.ssn;
        this.hunterNumber = _other.hunterNumber;
        this.streetAddress = _other.streetAddress;
        this.postalCodeAndCity = _other.postalCodeAndCity;
        this.phoneNumber = _other.phoneNumber;
        this.emailAddress = _other.emailAddress;
    }

    /**
     * Instantiates a MooseDataCardContactPerson copying the state of another MooseDataCardContactPerson
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardContactPerson from which to copy state.
     */
    public MooseDataCardContactPerson(final MooseDataCardContactPerson _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree lastNameFirstNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("lastNameFirstName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(lastNameFirstNamePropertyTree!= null):((lastNameFirstNamePropertyTree == null)||(!lastNameFirstNamePropertyTree.isLeaf())))) {
            this.lastNameFirstName = _other.lastNameFirstName;
        }
        final PropertyTree ssnPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("ssn"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(ssnPropertyTree!= null):((ssnPropertyTree == null)||(!ssnPropertyTree.isLeaf())))) {
            this.ssn = _other.ssn;
        }
        final PropertyTree hunterNumberPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("hunterNumber"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(hunterNumberPropertyTree!= null):((hunterNumberPropertyTree == null)||(!hunterNumberPropertyTree.isLeaf())))) {
            this.hunterNumber = _other.hunterNumber;
        }
        final PropertyTree streetAddressPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("streetAddress"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(streetAddressPropertyTree!= null):((streetAddressPropertyTree == null)||(!streetAddressPropertyTree.isLeaf())))) {
            this.streetAddress = _other.streetAddress;
        }
        final PropertyTree postalCodeAndCityPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("postalCodeAndCity"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(postalCodeAndCityPropertyTree!= null):((postalCodeAndCityPropertyTree == null)||(!postalCodeAndCityPropertyTree.isLeaf())))) {
            this.postalCodeAndCity = _other.postalCodeAndCity;
        }
        final PropertyTree phoneNumberPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("phoneNumber"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(phoneNumberPropertyTree!= null):((phoneNumberPropertyTree == null)||(!phoneNumberPropertyTree.isLeaf())))) {
            this.phoneNumber = _other.phoneNumber;
        }
        final PropertyTree emailAddressPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("emailAddress"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(emailAddressPropertyTree!= null):((emailAddressPropertyTree == null)||(!emailAddressPropertyTree.isLeaf())))) {
            this.emailAddress = _other.emailAddress;
        }
    }

    /**
     * Gets the value of the lastNameFirstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastNameFirstName() {
        return lastNameFirstName;
    }

    /**
     * Sets the value of the lastNameFirstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastNameFirstName(String value) {
        this.lastNameFirstName = value;
    }

    /**
     * Gets the value of the ssn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Sets the value of the ssn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSsn(String value) {
        this.ssn = value;
    }

    /**
     * Gets the value of the hunterNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHunterNumber() {
        return hunterNumber;
    }

    /**
     * Sets the value of the hunterNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHunterNumber(String value) {
        this.hunterNumber = value;
    }

    /**
     * Gets the value of the streetAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Sets the value of the streetAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreetAddress(String value) {
        this.streetAddress = value;
    }

    /**
     * Gets the value of the postalCodeAndCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostalCodeAndCity() {
        return postalCodeAndCity;
    }

    /**
     * Sets the value of the postalCodeAndCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostalCodeAndCity(String value) {
        this.postalCodeAndCity = value;
    }

    /**
     * Gets the value of the phoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
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
            String theLastNameFirstName;
            theLastNameFirstName = this.getLastNameFirstName();
            strategy.appendField(locator, this, "lastNameFirstName", buffer, theLastNameFirstName, (this.lastNameFirstName!= null));
        }
        {
            String theSsn;
            theSsn = this.getSsn();
            strategy.appendField(locator, this, "ssn", buffer, theSsn, (this.ssn!= null));
        }
        {
            String theHunterNumber;
            theHunterNumber = this.getHunterNumber();
            strategy.appendField(locator, this, "hunterNumber", buffer, theHunterNumber, (this.hunterNumber!= null));
        }
        {
            String theStreetAddress;
            theStreetAddress = this.getStreetAddress();
            strategy.appendField(locator, this, "streetAddress", buffer, theStreetAddress, (this.streetAddress!= null));
        }
        {
            String thePostalCodeAndCity;
            thePostalCodeAndCity = this.getPostalCodeAndCity();
            strategy.appendField(locator, this, "postalCodeAndCity", buffer, thePostalCodeAndCity, (this.postalCodeAndCity!= null));
        }
        {
            String thePhoneNumber;
            thePhoneNumber = this.getPhoneNumber();
            strategy.appendField(locator, this, "phoneNumber", buffer, thePhoneNumber, (this.phoneNumber!= null));
        }
        {
            String theEmailAddress;
            theEmailAddress = this.getEmailAddress();
            strategy.appendField(locator, this, "emailAddress", buffer, theEmailAddress, (this.emailAddress!= null));
        }
        return buffer;
    }

    public MooseDataCardContactPerson withLastNameFirstName(String value) {
        setLastNameFirstName(value);
        return this;
    }

    public MooseDataCardContactPerson withSsn(String value) {
        setSsn(value);
        return this;
    }

    public MooseDataCardContactPerson withHunterNumber(String value) {
        setHunterNumber(value);
        return this;
    }

    public MooseDataCardContactPerson withStreetAddress(String value) {
        setStreetAddress(value);
        return this;
    }

    public MooseDataCardContactPerson withPostalCodeAndCity(String value) {
        setPostalCodeAndCity(value);
        return this;
    }

    public MooseDataCardContactPerson withPhoneNumber(String value) {
        setPhoneNumber(value);
        return this;
    }

    public MooseDataCardContactPerson withEmailAddress(String value) {
        setEmailAddress(value);
        return this;
    }

    @Override
    public MooseDataCardContactPerson clone() {
        final MooseDataCardContactPerson _newObject;
        try {
            _newObject = ((MooseDataCardContactPerson) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return _newObject;
    }

    @Override
    public MooseDataCardContactPerson createCopy() {
        final MooseDataCardContactPerson _newObject;
        try {
            _newObject = ((MooseDataCardContactPerson) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.lastNameFirstName = this.lastNameFirstName;
        _newObject.ssn = this.ssn;
        _newObject.hunterNumber = this.hunterNumber;
        _newObject.streetAddress = this.streetAddress;
        _newObject.postalCodeAndCity = this.postalCodeAndCity;
        _newObject.phoneNumber = this.phoneNumber;
        _newObject.emailAddress = this.emailAddress;
        return _newObject;
    }

    @Override
    public MooseDataCardContactPerson createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardContactPerson _newObject;
        try {
            _newObject = ((MooseDataCardContactPerson) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree lastNameFirstNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("lastNameFirstName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(lastNameFirstNamePropertyTree!= null):((lastNameFirstNamePropertyTree == null)||(!lastNameFirstNamePropertyTree.isLeaf())))) {
            _newObject.lastNameFirstName = this.lastNameFirstName;
        }
        final PropertyTree ssnPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("ssn"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(ssnPropertyTree!= null):((ssnPropertyTree == null)||(!ssnPropertyTree.isLeaf())))) {
            _newObject.ssn = this.ssn;
        }
        final PropertyTree hunterNumberPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("hunterNumber"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(hunterNumberPropertyTree!= null):((hunterNumberPropertyTree == null)||(!hunterNumberPropertyTree.isLeaf())))) {
            _newObject.hunterNumber = this.hunterNumber;
        }
        final PropertyTree streetAddressPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("streetAddress"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(streetAddressPropertyTree!= null):((streetAddressPropertyTree == null)||(!streetAddressPropertyTree.isLeaf())))) {
            _newObject.streetAddress = this.streetAddress;
        }
        final PropertyTree postalCodeAndCityPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("postalCodeAndCity"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(postalCodeAndCityPropertyTree!= null):((postalCodeAndCityPropertyTree == null)||(!postalCodeAndCityPropertyTree.isLeaf())))) {
            _newObject.postalCodeAndCity = this.postalCodeAndCity;
        }
        final PropertyTree phoneNumberPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("phoneNumber"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(phoneNumberPropertyTree!= null):((phoneNumberPropertyTree == null)||(!phoneNumberPropertyTree.isLeaf())))) {
            _newObject.phoneNumber = this.phoneNumber;
        }
        final PropertyTree emailAddressPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("emailAddress"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(emailAddressPropertyTree!= null):((emailAddressPropertyTree == null)||(!emailAddressPropertyTree.isLeaf())))) {
            _newObject.emailAddress = this.emailAddress;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardContactPerson copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardContactPerson copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardContactPerson.Selector<MooseDataCardContactPerson.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardContactPerson.Select _root() {
            return new MooseDataCardContactPerson.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> lastNameFirstName = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> ssn = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> hunterNumber = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> streetAddress = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> postalCodeAndCity = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> phoneNumber = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> emailAddress = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.lastNameFirstName!= null) {
                products.put("lastNameFirstName", this.lastNameFirstName.init());
            }
            if (this.ssn!= null) {
                products.put("ssn", this.ssn.init());
            }
            if (this.hunterNumber!= null) {
                products.put("hunterNumber", this.hunterNumber.init());
            }
            if (this.streetAddress!= null) {
                products.put("streetAddress", this.streetAddress.init());
            }
            if (this.postalCodeAndCity!= null) {
                products.put("postalCodeAndCity", this.postalCodeAndCity.init());
            }
            if (this.phoneNumber!= null) {
                products.put("phoneNumber", this.phoneNumber.init());
            }
            if (this.emailAddress!= null) {
                products.put("emailAddress", this.emailAddress.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> lastNameFirstName() {
            return ((this.lastNameFirstName == null)?this.lastNameFirstName = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "lastNameFirstName"):this.lastNameFirstName);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> ssn() {
            return ((this.ssn == null)?this.ssn = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "ssn"):this.ssn);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> hunterNumber() {
            return ((this.hunterNumber == null)?this.hunterNumber = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "hunterNumber"):this.hunterNumber);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> streetAddress() {
            return ((this.streetAddress == null)?this.streetAddress = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "streetAddress"):this.streetAddress);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> postalCodeAndCity() {
            return ((this.postalCodeAndCity == null)?this.postalCodeAndCity = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "postalCodeAndCity"):this.postalCodeAndCity);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> phoneNumber() {
            return ((this.phoneNumber == null)?this.phoneNumber = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "phoneNumber"):this.phoneNumber);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>> emailAddress() {
            return ((this.emailAddress == null)?this.emailAddress = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardContactPerson.Selector<TRoot, TParent>>(this._root, this, "emailAddress"):this.emailAddress);
        }

    }

}
