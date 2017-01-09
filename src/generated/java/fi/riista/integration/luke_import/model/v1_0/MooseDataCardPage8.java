//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.13 at 11:52:15 AM EEST 
//


package fi.riista.integration.luke_import.model.v1_0;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for _Sivu_8Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_Sivu_8Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_8.1" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_8.1Type"/&gt;
 *         &lt;element name="_8.2" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_8.2Type"/&gt;
 *         &lt;element name="_8.3" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_8.3Type"/&gt;
 *         &lt;element name="_8.4" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_8.4Type"/&gt;
 *         &lt;element name="_Organisaatiotunnus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Pyynti_päättynyt" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Vakuutan_tiedot_oikeiksi" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}CorrectnessAssurance" minOccurs="0"/&gt;
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
@XmlType(name = "_Sivu_8Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "section_8_1",
    "section_8_2",
    "section_8_3",
    "section_8_4",
    "huntingClubCode",
    "huntingEndDate",
    "correctnessAssurance"
})
public class MooseDataCardPage8 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_8.1", required = true)
    protected MooseDataCardSection_8_1 section_8_1;
    @XmlElement(name = "_8.2", required = true)
    protected MooseDataCardSection_8_2 section_8_2;
    @XmlElement(name = "_8.3", required = true)
    protected MooseDataCardSection_8_3 section_8_3;
    @XmlElement(name = "_8.4", required = true)
    protected MooseDataCardSection_8_4 section_8_4;
    @XmlElement(name = "_Organisaatiotunnus", required = true)
    protected String huntingClubCode;
    @XmlElement(name = "_Pyynti_p\u00e4\u00e4ttynyt", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate huntingEndDate;
    @XmlElement(name = "_Vakuutan_tiedot_oikeiksi")
    protected CorrectnessAssurance correctnessAssurance;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardPage8() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardPage8 copying the state of another MooseDataCardPage8
     * 
     * @param _other
     *     The original MooseDataCardPage8 from which to copy state.
     */
    public MooseDataCardPage8(final MooseDataCardPage8 _other) {
        this.section_8_1 = ((_other.section_8_1 == null)?null:_other.section_8_1 .createCopy());
        this.section_8_2 = ((_other.section_8_2 == null)?null:_other.section_8_2 .createCopy());
        this.section_8_3 = ((_other.section_8_3 == null)?null:_other.section_8_3 .createCopy());
        this.section_8_4 = ((_other.section_8_4 == null)?null:_other.section_8_4 .createCopy());
        this.huntingClubCode = _other.huntingClubCode;
        this.huntingEndDate = _other.huntingEndDate;
        this.correctnessAssurance = ((_other.correctnessAssurance == null)?null:_other.correctnessAssurance.createCopy());
    }

    /**
     * Instantiates a MooseDataCardPage8 copying the state of another MooseDataCardPage8
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardPage8 from which to copy state.
     */
    public MooseDataCardPage8(final MooseDataCardPage8 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree section_8_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_1PropertyTree!= null):((section_8_1PropertyTree == null)||(!section_8_1PropertyTree.isLeaf())))) {
            this.section_8_1 = ((_other.section_8_1 == null)?null:_other.section_8_1 .createCopy(section_8_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_2PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_2"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_2PropertyTree!= null):((section_8_2PropertyTree == null)||(!section_8_2PropertyTree.isLeaf())))) {
            this.section_8_2 = ((_other.section_8_2 == null)?null:_other.section_8_2 .createCopy(section_8_2PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_3PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_3"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_3PropertyTree!= null):((section_8_3PropertyTree == null)||(!section_8_3PropertyTree.isLeaf())))) {
            this.section_8_3 = ((_other.section_8_3 == null)?null:_other.section_8_3 .createCopy(section_8_3PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_4PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_4"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_4PropertyTree!= null):((section_8_4PropertyTree == null)||(!section_8_4PropertyTree.isLeaf())))) {
            this.section_8_4 = ((_other.section_8_4 == null)?null:_other.section_8_4 .createCopy(section_8_4PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            this.huntingClubCode = _other.huntingClubCode;
        }
        final PropertyTree huntingEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingEndDatePropertyTree!= null):((huntingEndDatePropertyTree == null)||(!huntingEndDatePropertyTree.isLeaf())))) {
            this.huntingEndDate = _other.huntingEndDate;
        }
        final PropertyTree correctnessAssurancePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("correctnessAssurance"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(correctnessAssurancePropertyTree!= null):((correctnessAssurancePropertyTree == null)||(!correctnessAssurancePropertyTree.isLeaf())))) {
            this.correctnessAssurance = ((_other.correctnessAssurance == null)?null:_other.correctnessAssurance.createCopy(correctnessAssurancePropertyTree, _propertyTreeUse));
        }
    }

    /**
     * Gets the value of the section_8_1 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_8_1 }
     *     
     */
    public MooseDataCardSection_8_1 getSection_8_1() {
        return section_8_1;
    }

    /**
     * Sets the value of the section_8_1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_8_1 }
     *     
     */
    public void setSection_8_1(MooseDataCardSection_8_1 value) {
        this.section_8_1 = value;
    }

    /**
     * Gets the value of the section_8_2 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_8_2 }
     *     
     */
    public MooseDataCardSection_8_2 getSection_8_2() {
        return section_8_2;
    }

    /**
     * Sets the value of the section_8_2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_8_2 }
     *     
     */
    public void setSection_8_2(MooseDataCardSection_8_2 value) {
        this.section_8_2 = value;
    }

    /**
     * Gets the value of the section_8_3 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_8_3 }
     *     
     */
    public MooseDataCardSection_8_3 getSection_8_3() {
        return section_8_3;
    }

    /**
     * Sets the value of the section_8_3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_8_3 }
     *     
     */
    public void setSection_8_3(MooseDataCardSection_8_3 value) {
        this.section_8_3 = value;
    }

    /**
     * Gets the value of the section_8_4 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_8_4 }
     *     
     */
    public MooseDataCardSection_8_4 getSection_8_4() {
        return section_8_4;
    }

    /**
     * Sets the value of the section_8_4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_8_4 }
     *     
     */
    public void setSection_8_4(MooseDataCardSection_8_4 value) {
        this.section_8_4 = value;
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
     * Gets the value of the huntingEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    /**
     * Sets the value of the huntingEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingEndDate(LocalDate value) {
        this.huntingEndDate = value;
    }

    /**
     * Gets the value of the correctnessAssurance property.
     * 
     * @return
     *     possible object is
     *     {@link CorrectnessAssurance }
     *     
     */
    public CorrectnessAssurance getCorrectnessAssurance() {
        return correctnessAssurance;
    }

    /**
     * Sets the value of the correctnessAssurance property.
     * 
     * @param value
     *     allowed object is
     *     {@link CorrectnessAssurance }
     *     
     */
    public void setCorrectnessAssurance(CorrectnessAssurance value) {
        this.correctnessAssurance = value;
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
            MooseDataCardSection_8_1 theSection_8_1;
            theSection_8_1 = this.getSection_8_1();
            strategy.appendField(locator, this, "section_8_1", buffer, theSection_8_1, (this.section_8_1 != null));
        }
        {
            MooseDataCardSection_8_2 theSection_8_2;
            theSection_8_2 = this.getSection_8_2();
            strategy.appendField(locator, this, "section_8_2", buffer, theSection_8_2, (this.section_8_2 != null));
        }
        {
            MooseDataCardSection_8_3 theSection_8_3;
            theSection_8_3 = this.getSection_8_3();
            strategy.appendField(locator, this, "section_8_3", buffer, theSection_8_3, (this.section_8_3 != null));
        }
        {
            MooseDataCardSection_8_4 theSection_8_4;
            theSection_8_4 = this.getSection_8_4();
            strategy.appendField(locator, this, "section_8_4", buffer, theSection_8_4, (this.section_8_4 != null));
        }
        {
            String theHuntingClubCode;
            theHuntingClubCode = this.getHuntingClubCode();
            strategy.appendField(locator, this, "huntingClubCode", buffer, theHuntingClubCode, (this.huntingClubCode!= null));
        }
        {
            LocalDate theHuntingEndDate;
            theHuntingEndDate = this.getHuntingEndDate();
            strategy.appendField(locator, this, "huntingEndDate", buffer, theHuntingEndDate, (this.huntingEndDate!= null));
        }
        {
            CorrectnessAssurance theCorrectnessAssurance;
            theCorrectnessAssurance = this.getCorrectnessAssurance();
            strategy.appendField(locator, this, "correctnessAssurance", buffer, theCorrectnessAssurance, (this.correctnessAssurance!= null));
        }
        return buffer;
    }

    public MooseDataCardPage8 withSection_8_1(MooseDataCardSection_8_1 value) {
        setSection_8_1(value);
        return this;
    }

    public MooseDataCardPage8 withSection_8_2(MooseDataCardSection_8_2 value) {
        setSection_8_2(value);
        return this;
    }

    public MooseDataCardPage8 withSection_8_3(MooseDataCardSection_8_3 value) {
        setSection_8_3(value);
        return this;
    }

    public MooseDataCardPage8 withSection_8_4(MooseDataCardSection_8_4 value) {
        setSection_8_4(value);
        return this;
    }

    public MooseDataCardPage8 withHuntingClubCode(String value) {
        setHuntingClubCode(value);
        return this;
    }

    public MooseDataCardPage8 withHuntingEndDate(LocalDate value) {
        setHuntingEndDate(value);
        return this;
    }

    public MooseDataCardPage8 withCorrectnessAssurance(CorrectnessAssurance value) {
        setCorrectnessAssurance(value);
        return this;
    }

    @Override
    public MooseDataCardPage8 clone() {
        final MooseDataCardPage8 _newObject;
        try {
            _newObject = ((MooseDataCardPage8) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_8_1 = ((this.section_8_1 == null)?null:this.section_8_1 .clone());
        _newObject.section_8_2 = ((this.section_8_2 == null)?null:this.section_8_2 .clone());
        _newObject.section_8_3 = ((this.section_8_3 == null)?null:this.section_8_3 .clone());
        _newObject.section_8_4 = ((this.section_8_4 == null)?null:this.section_8_4 .clone());
        _newObject.correctnessAssurance = ((this.correctnessAssurance == null)?null:this.correctnessAssurance.clone());
        return _newObject;
    }

    @Override
    public MooseDataCardPage8 createCopy() {
        final MooseDataCardPage8 _newObject;
        try {
            _newObject = ((MooseDataCardPage8) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_8_1 = ((this.section_8_1 == null)?null:this.section_8_1 .createCopy());
        _newObject.section_8_2 = ((this.section_8_2 == null)?null:this.section_8_2 .createCopy());
        _newObject.section_8_3 = ((this.section_8_3 == null)?null:this.section_8_3 .createCopy());
        _newObject.section_8_4 = ((this.section_8_4 == null)?null:this.section_8_4 .createCopy());
        _newObject.huntingClubCode = this.huntingClubCode;
        _newObject.huntingEndDate = this.huntingEndDate;
        _newObject.correctnessAssurance = ((this.correctnessAssurance == null)?null:this.correctnessAssurance.createCopy());
        return _newObject;
    }

    @Override
    public MooseDataCardPage8 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardPage8 _newObject;
        try {
            _newObject = ((MooseDataCardPage8) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree section_8_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_1PropertyTree!= null):((section_8_1PropertyTree == null)||(!section_8_1PropertyTree.isLeaf())))) {
            _newObject.section_8_1 = ((this.section_8_1 == null)?null:this.section_8_1 .createCopy(section_8_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_2PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_2"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_2PropertyTree!= null):((section_8_2PropertyTree == null)||(!section_8_2PropertyTree.isLeaf())))) {
            _newObject.section_8_2 = ((this.section_8_2 == null)?null:this.section_8_2 .createCopy(section_8_2PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_3PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_3"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_3PropertyTree!= null):((section_8_3PropertyTree == null)||(!section_8_3PropertyTree.isLeaf())))) {
            _newObject.section_8_3 = ((this.section_8_3 == null)?null:this.section_8_3 .createCopy(section_8_3PropertyTree, _propertyTreeUse));
        }
        final PropertyTree section_8_4PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_8_4"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_8_4PropertyTree!= null):((section_8_4PropertyTree == null)||(!section_8_4PropertyTree.isLeaf())))) {
            _newObject.section_8_4 = ((this.section_8_4 == null)?null:this.section_8_4 .createCopy(section_8_4PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            _newObject.huntingClubCode = this.huntingClubCode;
        }
        final PropertyTree huntingEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingEndDatePropertyTree!= null):((huntingEndDatePropertyTree == null)||(!huntingEndDatePropertyTree.isLeaf())))) {
            _newObject.huntingEndDate = this.huntingEndDate;
        }
        final PropertyTree correctnessAssurancePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("correctnessAssurance"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(correctnessAssurancePropertyTree!= null):((correctnessAssurancePropertyTree == null)||(!correctnessAssurancePropertyTree.isLeaf())))) {
            _newObject.correctnessAssurance = ((this.correctnessAssurance == null)?null:this.correctnessAssurance.createCopy(correctnessAssurancePropertyTree, _propertyTreeUse));
        }
        return _newObject;
    }

    @Override
    public MooseDataCardPage8 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardPage8 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardPage8 .Selector<MooseDataCardPage8 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardPage8 .Select _root() {
            return new MooseDataCardPage8 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private MooseDataCardSection_8_1 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_1 = null;
        private MooseDataCardSection_8_2 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_2 = null;
        private MooseDataCardSection_8_3 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_3 = null;
        private MooseDataCardSection_8_4 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_4 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> huntingClubCode = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> huntingEndDate = null;
        private CorrectnessAssurance.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> correctnessAssurance = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.section_8_1 != null) {
                products.put("section_8_1", this.section_8_1 .init());
            }
            if (this.section_8_2 != null) {
                products.put("section_8_2", this.section_8_2 .init());
            }
            if (this.section_8_3 != null) {
                products.put("section_8_3", this.section_8_3 .init());
            }
            if (this.section_8_4 != null) {
                products.put("section_8_4", this.section_8_4 .init());
            }
            if (this.huntingClubCode!= null) {
                products.put("huntingClubCode", this.huntingClubCode.init());
            }
            if (this.huntingEndDate!= null) {
                products.put("huntingEndDate", this.huntingEndDate.init());
            }
            if (this.correctnessAssurance!= null) {
                products.put("correctnessAssurance", this.correctnessAssurance.init());
            }
            return products;
        }

        public MooseDataCardSection_8_1 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_1() {
            return ((this.section_8_1 == null)?this.section_8_1 = new MooseDataCardSection_8_1 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "section_8_1"):this.section_8_1);
        }

        public MooseDataCardSection_8_2 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_2() {
            return ((this.section_8_2 == null)?this.section_8_2 = new MooseDataCardSection_8_2 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "section_8_2"):this.section_8_2);
        }

        public MooseDataCardSection_8_3 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_3() {
            return ((this.section_8_3 == null)?this.section_8_3 = new MooseDataCardSection_8_3 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "section_8_3"):this.section_8_3);
        }

        public MooseDataCardSection_8_4 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> section_8_4() {
            return ((this.section_8_4 == null)?this.section_8_4 = new MooseDataCardSection_8_4 .Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "section_8_4"):this.section_8_4);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> huntingClubCode() {
            return ((this.huntingClubCode == null)?this.huntingClubCode = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "huntingClubCode"):this.huntingClubCode);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> huntingEndDate() {
            return ((this.huntingEndDate == null)?this.huntingEndDate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "huntingEndDate"):this.huntingEndDate);
        }

        public CorrectnessAssurance.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>> correctnessAssurance() {
            return ((this.correctnessAssurance == null)?this.correctnessAssurance = new CorrectnessAssurance.Selector<TRoot, MooseDataCardPage8 .Selector<TRoot, TParent>>(this._root, this, "correctnessAssurance"):this.correctnessAssurance);
        }

    }

}