
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
 * <p>Java class for _Sivu_5Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_Sivu_5Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_5.1" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_5.1Type"/&gt;
 *         &lt;element name="_Asiakasnumero" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
@XmlType(name = "_Sivu_5Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "section_5_1",
    "huntingClubCode"
})
public class MooseDataCardPage5 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_5.1", required = true)
    protected MooseDataCardSection_5_1 section_5_1;
    @XmlElement(name = "_Asiakasnumero", required = true)
    protected String huntingClubCode;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardPage5() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardPage5 copying the state of another MooseDataCardPage5
     * 
     * @param _other
     *     The original MooseDataCardPage5 from which to copy state.
     */
    public MooseDataCardPage5(final MooseDataCardPage5 _other) {
        this.section_5_1 = ((_other.section_5_1 == null)?null:_other.section_5_1 .createCopy());
        this.huntingClubCode = _other.huntingClubCode;
    }

    /**
     * Instantiates a MooseDataCardPage5 copying the state of another MooseDataCardPage5
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardPage5 from which to copy state.
     */
    public MooseDataCardPage5(final MooseDataCardPage5 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree section_5_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_5_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_5_1PropertyTree!= null):((section_5_1PropertyTree == null)||(!section_5_1PropertyTree.isLeaf())))) {
            this.section_5_1 = ((_other.section_5_1 == null)?null:_other.section_5_1 .createCopy(section_5_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            this.huntingClubCode = _other.huntingClubCode;
        }
    }

    /**
     * Gets the value of the section_5_1 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_5_1 }
     *     
     */
    public MooseDataCardSection_5_1 getSection_5_1() {
        return section_5_1;
    }

    /**
     * Sets the value of the section_5_1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_5_1 }
     *     
     */
    public void setSection_5_1(MooseDataCardSection_5_1 value) {
        this.section_5_1 = value;
    }

    /**
     * Gets the value of the huntingClubCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHuntingClubCode() {
        return huntingClubCode;
    }

    /**
     * Sets the value of the huntingClubCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingClubCode(String value) {
        this.huntingClubCode = value;
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
            MooseDataCardSection_5_1 theSection_5_1;
            theSection_5_1 = this.getSection_5_1();
            strategy.appendField(locator, this, "section_5_1", buffer, theSection_5_1, (this.section_5_1 != null));
        }
        {
            String theHuntingClubCode;
            theHuntingClubCode = this.getHuntingClubCode();
            strategy.appendField(locator, this, "huntingClubCode", buffer, theHuntingClubCode, (this.huntingClubCode!= null));
        }
        return buffer;
    }

    public MooseDataCardPage5 withSection_5_1(MooseDataCardSection_5_1 value) {
        setSection_5_1(value);
        return this;
    }

    public MooseDataCardPage5 withHuntingClubCode(String value) {
        setHuntingClubCode(value);
        return this;
    }

    @Override
    public MooseDataCardPage5 clone() {
        final MooseDataCardPage5 _newObject;
        try {
            _newObject = ((MooseDataCardPage5) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_5_1 = ((this.section_5_1 == null)?null:this.section_5_1 .clone());
        return _newObject;
    }

    @Override
    public MooseDataCardPage5 createCopy() {
        final MooseDataCardPage5 _newObject;
        try {
            _newObject = ((MooseDataCardPage5) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_5_1 = ((this.section_5_1 == null)?null:this.section_5_1 .createCopy());
        _newObject.huntingClubCode = this.huntingClubCode;
        return _newObject;
    }

    @Override
    public MooseDataCardPage5 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardPage5 _newObject;
        try {
            _newObject = ((MooseDataCardPage5) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree section_5_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_5_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_5_1PropertyTree!= null):((section_5_1PropertyTree == null)||(!section_5_1PropertyTree.isLeaf())))) {
            _newObject.section_5_1 = ((this.section_5_1 == null)?null:this.section_5_1 .createCopy(section_5_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            _newObject.huntingClubCode = this.huntingClubCode;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardPage5 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardPage5 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardPage5 .Selector<MooseDataCardPage5 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardPage5 .Select _root() {
            return new MooseDataCardPage5 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private MooseDataCardSection_5_1 .Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>> section_5_1 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>> huntingClubCode = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.section_5_1 != null) {
                products.put("section_5_1", this.section_5_1 .init());
            }
            if (this.huntingClubCode!= null) {
                products.put("huntingClubCode", this.huntingClubCode.init());
            }
            return products;
        }

        public MooseDataCardSection_5_1 .Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>> section_5_1() {
            return ((this.section_5_1 == null)?this.section_5_1 = new MooseDataCardSection_5_1 .Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>>(this._root, this, "section_5_1"):this.section_5_1);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>> huntingClubCode() {
            return ((this.huntingClubCode == null)?this.huntingClubCode = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage5 .Selector<TRoot, TParent>>(this._root, this, "huntingClubCode"):this.huntingClubCode);
        }

    }

}
