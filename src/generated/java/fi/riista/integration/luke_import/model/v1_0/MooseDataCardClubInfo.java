
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
 * <p>Java class for _1.1Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_1.1Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_Pyyntiluvan_saajan_nimi" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Metsästysseura_tai_-seurue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Seurueen_koordinaatit" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Riistanhoitoyhdistys" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
@XmlType(name = "_1.1Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "permitHolderName",
    "huntingClubName",
    "huntingClubCoordinate",
    "rhyName"
})
public class MooseDataCardClubInfo implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_Pyyntiluvan_saajan_nimi", required = true)
    protected String permitHolderName;
    @XmlElement(name = "_Mets\u00e4stysseura_tai_-seurue", required = true)
    protected String huntingClubName;
    @XmlElement(name = "_Seurueen_koordinaatit", required = true)
    protected String huntingClubCoordinate;
    @XmlElement(name = "_Riistanhoitoyhdistys", required = true)
    protected String rhyName;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardClubInfo() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardClubInfo copying the state of another MooseDataCardClubInfo
     * 
     * @param _other
     *     The original MooseDataCardClubInfo from which to copy state.
     */
    public MooseDataCardClubInfo(final MooseDataCardClubInfo _other) {
        this.permitHolderName = _other.permitHolderName;
        this.huntingClubName = _other.huntingClubName;
        this.huntingClubCoordinate = _other.huntingClubCoordinate;
        this.rhyName = _other.rhyName;
    }

    /**
     * Instantiates a MooseDataCardClubInfo copying the state of another MooseDataCardClubInfo
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardClubInfo from which to copy state.
     */
    public MooseDataCardClubInfo(final MooseDataCardClubInfo _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree permitHolderNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("permitHolderName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(permitHolderNamePropertyTree!= null):((permitHolderNamePropertyTree == null)||(!permitHolderNamePropertyTree.isLeaf())))) {
            this.permitHolderName = _other.permitHolderName;
        }
        final PropertyTree huntingClubNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubNamePropertyTree!= null):((huntingClubNamePropertyTree == null)||(!huntingClubNamePropertyTree.isLeaf())))) {
            this.huntingClubName = _other.huntingClubName;
        }
        final PropertyTree huntingClubCoordinatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCoordinate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCoordinatePropertyTree!= null):((huntingClubCoordinatePropertyTree == null)||(!huntingClubCoordinatePropertyTree.isLeaf())))) {
            this.huntingClubCoordinate = _other.huntingClubCoordinate;
        }
        final PropertyTree rhyNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("rhyName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(rhyNamePropertyTree!= null):((rhyNamePropertyTree == null)||(!rhyNamePropertyTree.isLeaf())))) {
            this.rhyName = _other.rhyName;
        }
    }

    /**
     * Gets the value of the permitHolderName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitHolderName() {
        return permitHolderName;
    }

    /**
     * Sets the value of the permitHolderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitHolderName(String value) {
        this.permitHolderName = value;
    }

    /**
     * Gets the value of the huntingClubName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHuntingClubName() {
        return huntingClubName;
    }

    /**
     * Sets the value of the huntingClubName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingClubName(String value) {
        this.huntingClubName = value;
    }

    /**
     * Gets the value of the huntingClubCoordinate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHuntingClubCoordinate() {
        return huntingClubCoordinate;
    }

    /**
     * Sets the value of the huntingClubCoordinate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingClubCoordinate(String value) {
        this.huntingClubCoordinate = value;
    }

    /**
     * Gets the value of the rhyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRhyName() {
        return rhyName;
    }

    /**
     * Sets the value of the rhyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRhyName(String value) {
        this.rhyName = value;
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
            String thePermitHolderName;
            thePermitHolderName = this.getPermitHolderName();
            strategy.appendField(locator, this, "permitHolderName", buffer, thePermitHolderName, (this.permitHolderName!= null));
        }
        {
            String theHuntingClubName;
            theHuntingClubName = this.getHuntingClubName();
            strategy.appendField(locator, this, "huntingClubName", buffer, theHuntingClubName, (this.huntingClubName!= null));
        }
        {
            String theHuntingClubCoordinate;
            theHuntingClubCoordinate = this.getHuntingClubCoordinate();
            strategy.appendField(locator, this, "huntingClubCoordinate", buffer, theHuntingClubCoordinate, (this.huntingClubCoordinate!= null));
        }
        {
            String theRhyName;
            theRhyName = this.getRhyName();
            strategy.appendField(locator, this, "rhyName", buffer, theRhyName, (this.rhyName!= null));
        }
        return buffer;
    }

    public MooseDataCardClubInfo withPermitHolderName(String value) {
        setPermitHolderName(value);
        return this;
    }

    public MooseDataCardClubInfo withHuntingClubName(String value) {
        setHuntingClubName(value);
        return this;
    }

    public MooseDataCardClubInfo withHuntingClubCoordinate(String value) {
        setHuntingClubCoordinate(value);
        return this;
    }

    public MooseDataCardClubInfo withRhyName(String value) {
        setRhyName(value);
        return this;
    }

    @Override
    public MooseDataCardClubInfo clone() {
        final MooseDataCardClubInfo _newObject;
        try {
            _newObject = ((MooseDataCardClubInfo) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return _newObject;
    }

    @Override
    public MooseDataCardClubInfo createCopy() {
        final MooseDataCardClubInfo _newObject;
        try {
            _newObject = ((MooseDataCardClubInfo) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.permitHolderName = this.permitHolderName;
        _newObject.huntingClubName = this.huntingClubName;
        _newObject.huntingClubCoordinate = this.huntingClubCoordinate;
        _newObject.rhyName = this.rhyName;
        return _newObject;
    }

    @Override
    public MooseDataCardClubInfo createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardClubInfo _newObject;
        try {
            _newObject = ((MooseDataCardClubInfo) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree permitHolderNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("permitHolderName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(permitHolderNamePropertyTree!= null):((permitHolderNamePropertyTree == null)||(!permitHolderNamePropertyTree.isLeaf())))) {
            _newObject.permitHolderName = this.permitHolderName;
        }
        final PropertyTree huntingClubNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubNamePropertyTree!= null):((huntingClubNamePropertyTree == null)||(!huntingClubNamePropertyTree.isLeaf())))) {
            _newObject.huntingClubName = this.huntingClubName;
        }
        final PropertyTree huntingClubCoordinatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCoordinate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCoordinatePropertyTree!= null):((huntingClubCoordinatePropertyTree == null)||(!huntingClubCoordinatePropertyTree.isLeaf())))) {
            _newObject.huntingClubCoordinate = this.huntingClubCoordinate;
        }
        final PropertyTree rhyNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("rhyName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(rhyNamePropertyTree!= null):((rhyNamePropertyTree == null)||(!rhyNamePropertyTree.isLeaf())))) {
            _newObject.rhyName = this.rhyName;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardClubInfo copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardClubInfo copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardClubInfo.Selector<MooseDataCardClubInfo.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardClubInfo.Select _root() {
            return new MooseDataCardClubInfo.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> permitHolderName = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> huntingClubName = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> huntingClubCoordinate = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> rhyName = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.permitHolderName!= null) {
                products.put("permitHolderName", this.permitHolderName.init());
            }
            if (this.huntingClubName!= null) {
                products.put("huntingClubName", this.huntingClubName.init());
            }
            if (this.huntingClubCoordinate!= null) {
                products.put("huntingClubCoordinate", this.huntingClubCoordinate.init());
            }
            if (this.rhyName!= null) {
                products.put("rhyName", this.rhyName.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> permitHolderName() {
            return ((this.permitHolderName == null)?this.permitHolderName = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>>(this._root, this, "permitHolderName"):this.permitHolderName);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> huntingClubName() {
            return ((this.huntingClubName == null)?this.huntingClubName = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>>(this._root, this, "huntingClubName"):this.huntingClubName);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> huntingClubCoordinate() {
            return ((this.huntingClubCoordinate == null)?this.huntingClubCoordinate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>>(this._root, this, "huntingClubCoordinate"):this.huntingClubCoordinate);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>> rhyName() {
            return ((this.rhyName == null)?this.rhyName = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardClubInfo.Selector<TRoot, TParent>>(this._root, this, "rhyName"):this.rhyName);
        }

    }

}
