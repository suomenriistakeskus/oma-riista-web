//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.13 at 11:52:15 AM EEST 
//


package fi.riista.integration.luke_import.model.v1_0;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 * <p>Java class for _4.1Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_4.1Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_Urokset" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_UroksetType" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "_4.1Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "mooseMales"
})
public class MooseDataCardSection_4_1 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_Urokset")
    protected List<MooseDataCardMooseMale> mooseMales;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardSection_4_1() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardSection_4_1 copying the state of another MooseDataCardSection_4_1
     * 
     * @param _other
     *     The original MooseDataCardSection_4_1 from which to copy state.
     */
    public MooseDataCardSection_4_1(final MooseDataCardSection_4_1 _other) {
        if (_other.mooseMales == null) {
            this.mooseMales = null;
        } else {
            this.mooseMales = new ArrayList<MooseDataCardMooseMale>();
            for (MooseDataCardMooseMale _item: _other.mooseMales) {
                this.mooseMales.add(((_item == null)?null:_item.createCopy()));
            }
        }
    }

    /**
     * Instantiates a MooseDataCardSection_4_1 copying the state of another MooseDataCardSection_4_1
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardSection_4_1 from which to copy state.
     */
    public MooseDataCardSection_4_1(final MooseDataCardSection_4_1 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree mooseMalesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseMales"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseMalesPropertyTree!= null):((mooseMalesPropertyTree == null)||(!mooseMalesPropertyTree.isLeaf())))) {
            if (_other.mooseMales == null) {
                this.mooseMales = null;
            } else {
                this.mooseMales = new ArrayList<MooseDataCardMooseMale>();
                for (MooseDataCardMooseMale _item: _other.mooseMales) {
                    this.mooseMales.add(((_item == null)?null:_item.createCopy(mooseMalesPropertyTree, _propertyTreeUse)));
                }
            }
        }
    }

    /**
     * Gets the value of the mooseMales property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mooseMales property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMooseMales().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MooseDataCardMooseMale }
     * 
     * 
     */
    public List<MooseDataCardMooseMale> getMooseMales() {
        if (mooseMales == null) {
            mooseMales = new ArrayList<MooseDataCardMooseMale>();
        }
        return this.mooseMales;
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
            List<MooseDataCardMooseMale> theMooseMales;
            theMooseMales = (((this.mooseMales!= null)&&(!this.mooseMales.isEmpty()))?this.getMooseMales():null);
            strategy.appendField(locator, this, "mooseMales", buffer, theMooseMales, ((this.mooseMales!= null)&&(!this.mooseMales.isEmpty())));
        }
        return buffer;
    }

    public MooseDataCardSection_4_1 withMooseMales(MooseDataCardMooseMale... values) {
        if (values!= null) {
            for (MooseDataCardMooseMale value: values) {
                getMooseMales().add(value);
            }
        }
        return this;
    }

    public MooseDataCardSection_4_1 withMooseMales(Collection<MooseDataCardMooseMale> values) {
        if (values!= null) {
            getMooseMales().addAll(values);
        }
        return this;
    }

    @Override
    public MooseDataCardSection_4_1 clone() {
        final MooseDataCardSection_4_1 _newObject;
        try {
            _newObject = ((MooseDataCardSection_4_1) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (this.mooseMales == null) {
            _newObject.mooseMales = null;
        } else {
            _newObject.mooseMales = new ArrayList<MooseDataCardMooseMale>();
            for (MooseDataCardMooseMale _item: this.mooseMales) {
                _newObject.mooseMales.add(((_item == null)?null:_item.clone()));
            }
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_4_1 createCopy() {
        final MooseDataCardSection_4_1 _newObject;
        try {
            _newObject = ((MooseDataCardSection_4_1) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (this.mooseMales == null) {
            _newObject.mooseMales = null;
        } else {
            _newObject.mooseMales = new ArrayList<MooseDataCardMooseMale>();
            for (MooseDataCardMooseMale _item: this.mooseMales) {
                _newObject.mooseMales.add(((_item == null)?null:_item.createCopy()));
            }
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_4_1 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardSection_4_1 _newObject;
        try {
            _newObject = ((MooseDataCardSection_4_1) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree mooseMalesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseMales"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseMalesPropertyTree!= null):((mooseMalesPropertyTree == null)||(!mooseMalesPropertyTree.isLeaf())))) {
            if (this.mooseMales == null) {
                _newObject.mooseMales = null;
            } else {
                _newObject.mooseMales = new ArrayList<MooseDataCardMooseMale>();
                for (MooseDataCardMooseMale _item: this.mooseMales) {
                    _newObject.mooseMales.add(((_item == null)?null:_item.createCopy(mooseMalesPropertyTree, _propertyTreeUse)));
                }
            }
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_4_1 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardSection_4_1 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardSection_4_1 .Selector<MooseDataCardSection_4_1 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardSection_4_1 .Select _root() {
            return new MooseDataCardSection_4_1 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private MooseDataCardMooseMale.Selector<TRoot, MooseDataCardSection_4_1 .Selector<TRoot, TParent>> mooseMales = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.mooseMales!= null) {
                products.put("mooseMales", this.mooseMales.init());
            }
            return products;
        }

        public MooseDataCardMooseMale.Selector<TRoot, MooseDataCardSection_4_1 .Selector<TRoot, TParent>> mooseMales() {
            return ((this.mooseMales == null)?this.mooseMales = new MooseDataCardMooseMale.Selector<TRoot, MooseDataCardSection_4_1 .Selector<TRoot, TParent>>(this._root, this, "mooseMales"):this.mooseMales);
        }

    }

}