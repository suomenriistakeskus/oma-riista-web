
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
 * <p>Java class for _8.3Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_8.3Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_Hukkuneet" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Karhun_tappamat" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Suden_tappamat" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Kolarissa_kuolleet" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Salakaadetut" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Kiimatappelussa_kuolleet" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Nälkiintyneet" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Muu_syy" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Yhteensä" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Muu_syy_2" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
@XmlType(name = "_8.3Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "numberOfDrownedMooses",
    "numberOfMoosesKilledByBear",
    "numberOfMoosesKilledByWolf",
    "numberOfMoosesKilledInTrafficAccident",
    "numberOfMoosesKilledInPoaching",
    "numberOfMoosesKilledInRutFight",
    "numberOfStarvedMooses",
    "numberOfMoosesDeceasedByOtherReason",
    "totalNumberOfDeceasedMooses",
    "explanationForOtherReason"
})
public class MooseDataCardSection_8_3 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_Hukkuneet", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfDrownedMooses;
    @XmlElement(name = "_Karhun_tappamat", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesKilledByBear;
    @XmlElement(name = "_Suden_tappamat", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesKilledByWolf;
    @XmlElement(name = "_Kolarissa_kuolleet", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesKilledInTrafficAccident;
    @XmlElement(name = "_Salakaadetut", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesKilledInPoaching;
    @XmlElement(name = "_Kiimatappelussa_kuolleet", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesKilledInRutFight;
    @XmlElement(name = "_N\u00e4lkiintyneet", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfStarvedMooses;
    @XmlElement(name = "_Muu_syy", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfMoosesDeceasedByOtherReason;
    @XmlElement(name = "_Yhteens\u00e4", required = true, type = Integer.class, nillable = true)
    protected Integer totalNumberOfDeceasedMooses;
    @XmlElement(name = "_Muu_syy_2", required = true)
    protected String explanationForOtherReason;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardSection_8_3() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardSection_8_3 copying the state of another MooseDataCardSection_8_3
     * 
     * @param _other
     *     The original MooseDataCardSection_8_3 from which to copy state.
     */
    public MooseDataCardSection_8_3(final MooseDataCardSection_8_3 _other) {
        this.numberOfDrownedMooses = _other.numberOfDrownedMooses;
        this.numberOfMoosesKilledByBear = _other.numberOfMoosesKilledByBear;
        this.numberOfMoosesKilledByWolf = _other.numberOfMoosesKilledByWolf;
        this.numberOfMoosesKilledInTrafficAccident = _other.numberOfMoosesKilledInTrafficAccident;
        this.numberOfMoosesKilledInPoaching = _other.numberOfMoosesKilledInPoaching;
        this.numberOfMoosesKilledInRutFight = _other.numberOfMoosesKilledInRutFight;
        this.numberOfStarvedMooses = _other.numberOfStarvedMooses;
        this.numberOfMoosesDeceasedByOtherReason = _other.numberOfMoosesDeceasedByOtherReason;
        this.totalNumberOfDeceasedMooses = _other.totalNumberOfDeceasedMooses;
        this.explanationForOtherReason = _other.explanationForOtherReason;
    }

    /**
     * Instantiates a MooseDataCardSection_8_3 copying the state of another MooseDataCardSection_8_3
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardSection_8_3 from which to copy state.
     */
    public MooseDataCardSection_8_3(final MooseDataCardSection_8_3 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree numberOfDrownedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfDrownedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfDrownedMoosesPropertyTree!= null):((numberOfDrownedMoosesPropertyTree == null)||(!numberOfDrownedMoosesPropertyTree.isLeaf())))) {
            this.numberOfDrownedMooses = _other.numberOfDrownedMooses;
        }
        final PropertyTree numberOfMoosesKilledByBearPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledByBear"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledByBearPropertyTree!= null):((numberOfMoosesKilledByBearPropertyTree == null)||(!numberOfMoosesKilledByBearPropertyTree.isLeaf())))) {
            this.numberOfMoosesKilledByBear = _other.numberOfMoosesKilledByBear;
        }
        final PropertyTree numberOfMoosesKilledByWolfPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledByWolf"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledByWolfPropertyTree!= null):((numberOfMoosesKilledByWolfPropertyTree == null)||(!numberOfMoosesKilledByWolfPropertyTree.isLeaf())))) {
            this.numberOfMoosesKilledByWolf = _other.numberOfMoosesKilledByWolf;
        }
        final PropertyTree numberOfMoosesKilledInTrafficAccidentPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInTrafficAccident"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInTrafficAccidentPropertyTree!= null):((numberOfMoosesKilledInTrafficAccidentPropertyTree == null)||(!numberOfMoosesKilledInTrafficAccidentPropertyTree.isLeaf())))) {
            this.numberOfMoosesKilledInTrafficAccident = _other.numberOfMoosesKilledInTrafficAccident;
        }
        final PropertyTree numberOfMoosesKilledInPoachingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInPoaching"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInPoachingPropertyTree!= null):((numberOfMoosesKilledInPoachingPropertyTree == null)||(!numberOfMoosesKilledInPoachingPropertyTree.isLeaf())))) {
            this.numberOfMoosesKilledInPoaching = _other.numberOfMoosesKilledInPoaching;
        }
        final PropertyTree numberOfMoosesKilledInRutFightPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInRutFight"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInRutFightPropertyTree!= null):((numberOfMoosesKilledInRutFightPropertyTree == null)||(!numberOfMoosesKilledInRutFightPropertyTree.isLeaf())))) {
            this.numberOfMoosesKilledInRutFight = _other.numberOfMoosesKilledInRutFight;
        }
        final PropertyTree numberOfStarvedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfStarvedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfStarvedMoosesPropertyTree!= null):((numberOfStarvedMoosesPropertyTree == null)||(!numberOfStarvedMoosesPropertyTree.isLeaf())))) {
            this.numberOfStarvedMooses = _other.numberOfStarvedMooses;
        }
        final PropertyTree numberOfMoosesDeceasedByOtherReasonPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesDeceasedByOtherReason"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesDeceasedByOtherReasonPropertyTree!= null):((numberOfMoosesDeceasedByOtherReasonPropertyTree == null)||(!numberOfMoosesDeceasedByOtherReasonPropertyTree.isLeaf())))) {
            this.numberOfMoosesDeceasedByOtherReason = _other.numberOfMoosesDeceasedByOtherReason;
        }
        final PropertyTree totalNumberOfDeceasedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("totalNumberOfDeceasedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(totalNumberOfDeceasedMoosesPropertyTree!= null):((totalNumberOfDeceasedMoosesPropertyTree == null)||(!totalNumberOfDeceasedMoosesPropertyTree.isLeaf())))) {
            this.totalNumberOfDeceasedMooses = _other.totalNumberOfDeceasedMooses;
        }
        final PropertyTree explanationForOtherReasonPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("explanationForOtherReason"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(explanationForOtherReasonPropertyTree!= null):((explanationForOtherReasonPropertyTree == null)||(!explanationForOtherReasonPropertyTree.isLeaf())))) {
            this.explanationForOtherReason = _other.explanationForOtherReason;
        }
    }

    /**
     * Gets the value of the numberOfDrownedMooses property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfDrownedMooses() {
        return numberOfDrownedMooses;
    }

    /**
     * Sets the value of the numberOfDrownedMooses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfDrownedMooses(Integer value) {
        this.numberOfDrownedMooses = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledByBear property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledByBear() {
        return numberOfMoosesKilledByBear;
    }

    /**
     * Sets the value of the numberOfMoosesKilledByBear property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledByBear(Integer value) {
        this.numberOfMoosesKilledByBear = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledByWolf property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledByWolf() {
        return numberOfMoosesKilledByWolf;
    }

    /**
     * Sets the value of the numberOfMoosesKilledByWolf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledByWolf(Integer value) {
        this.numberOfMoosesKilledByWolf = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledInTrafficAccident property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledInTrafficAccident() {
        return numberOfMoosesKilledInTrafficAccident;
    }

    /**
     * Sets the value of the numberOfMoosesKilledInTrafficAccident property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledInTrafficAccident(Integer value) {
        this.numberOfMoosesKilledInTrafficAccident = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledInPoaching property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledInPoaching() {
        return numberOfMoosesKilledInPoaching;
    }

    /**
     * Sets the value of the numberOfMoosesKilledInPoaching property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledInPoaching(Integer value) {
        this.numberOfMoosesKilledInPoaching = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledInRutFight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledInRutFight() {
        return numberOfMoosesKilledInRutFight;
    }

    /**
     * Sets the value of the numberOfMoosesKilledInRutFight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledInRutFight(Integer value) {
        this.numberOfMoosesKilledInRutFight = value;
    }

    /**
     * Gets the value of the numberOfStarvedMooses property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfStarvedMooses() {
        return numberOfStarvedMooses;
    }

    /**
     * Sets the value of the numberOfStarvedMooses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfStarvedMooses(Integer value) {
        this.numberOfStarvedMooses = value;
    }

    /**
     * Gets the value of the numberOfMoosesDeceasedByOtherReason property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesDeceasedByOtherReason() {
        return numberOfMoosesDeceasedByOtherReason;
    }

    /**
     * Sets the value of the numberOfMoosesDeceasedByOtherReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesDeceasedByOtherReason(Integer value) {
        this.numberOfMoosesDeceasedByOtherReason = value;
    }

    /**
     * Gets the value of the totalNumberOfDeceasedMooses property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalNumberOfDeceasedMooses() {
        return totalNumberOfDeceasedMooses;
    }

    /**
     * Sets the value of the totalNumberOfDeceasedMooses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalNumberOfDeceasedMooses(Integer value) {
        this.totalNumberOfDeceasedMooses = value;
    }

    /**
     * Gets the value of the explanationForOtherReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExplanationForOtherReason() {
        return explanationForOtherReason;
    }

    /**
     * Sets the value of the explanationForOtherReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExplanationForOtherReason(String value) {
        this.explanationForOtherReason = value;
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
            Integer theNumberOfDrownedMooses;
            theNumberOfDrownedMooses = this.getNumberOfDrownedMooses();
            strategy.appendField(locator, this, "numberOfDrownedMooses", buffer, theNumberOfDrownedMooses, (this.numberOfDrownedMooses!= null));
        }
        {
            Integer theNumberOfMoosesKilledByBear;
            theNumberOfMoosesKilledByBear = this.getNumberOfMoosesKilledByBear();
            strategy.appendField(locator, this, "numberOfMoosesKilledByBear", buffer, theNumberOfMoosesKilledByBear, (this.numberOfMoosesKilledByBear!= null));
        }
        {
            Integer theNumberOfMoosesKilledByWolf;
            theNumberOfMoosesKilledByWolf = this.getNumberOfMoosesKilledByWolf();
            strategy.appendField(locator, this, "numberOfMoosesKilledByWolf", buffer, theNumberOfMoosesKilledByWolf, (this.numberOfMoosesKilledByWolf!= null));
        }
        {
            Integer theNumberOfMoosesKilledInTrafficAccident;
            theNumberOfMoosesKilledInTrafficAccident = this.getNumberOfMoosesKilledInTrafficAccident();
            strategy.appendField(locator, this, "numberOfMoosesKilledInTrafficAccident", buffer, theNumberOfMoosesKilledInTrafficAccident, (this.numberOfMoosesKilledInTrafficAccident!= null));
        }
        {
            Integer theNumberOfMoosesKilledInPoaching;
            theNumberOfMoosesKilledInPoaching = this.getNumberOfMoosesKilledInPoaching();
            strategy.appendField(locator, this, "numberOfMoosesKilledInPoaching", buffer, theNumberOfMoosesKilledInPoaching, (this.numberOfMoosesKilledInPoaching!= null));
        }
        {
            Integer theNumberOfMoosesKilledInRutFight;
            theNumberOfMoosesKilledInRutFight = this.getNumberOfMoosesKilledInRutFight();
            strategy.appendField(locator, this, "numberOfMoosesKilledInRutFight", buffer, theNumberOfMoosesKilledInRutFight, (this.numberOfMoosesKilledInRutFight!= null));
        }
        {
            Integer theNumberOfStarvedMooses;
            theNumberOfStarvedMooses = this.getNumberOfStarvedMooses();
            strategy.appendField(locator, this, "numberOfStarvedMooses", buffer, theNumberOfStarvedMooses, (this.numberOfStarvedMooses!= null));
        }
        {
            Integer theNumberOfMoosesDeceasedByOtherReason;
            theNumberOfMoosesDeceasedByOtherReason = this.getNumberOfMoosesDeceasedByOtherReason();
            strategy.appendField(locator, this, "numberOfMoosesDeceasedByOtherReason", buffer, theNumberOfMoosesDeceasedByOtherReason, (this.numberOfMoosesDeceasedByOtherReason!= null));
        }
        {
            Integer theTotalNumberOfDeceasedMooses;
            theTotalNumberOfDeceasedMooses = this.getTotalNumberOfDeceasedMooses();
            strategy.appendField(locator, this, "totalNumberOfDeceasedMooses", buffer, theTotalNumberOfDeceasedMooses, (this.totalNumberOfDeceasedMooses!= null));
        }
        {
            String theExplanationForOtherReason;
            theExplanationForOtherReason = this.getExplanationForOtherReason();
            strategy.appendField(locator, this, "explanationForOtherReason", buffer, theExplanationForOtherReason, (this.explanationForOtherReason!= null));
        }
        return buffer;
    }

    public MooseDataCardSection_8_3 withNumberOfDrownedMooses(Integer value) {
        setNumberOfDrownedMooses(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesKilledByBear(Integer value) {
        setNumberOfMoosesKilledByBear(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesKilledByWolf(Integer value) {
        setNumberOfMoosesKilledByWolf(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesKilledInTrafficAccident(Integer value) {
        setNumberOfMoosesKilledInTrafficAccident(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesKilledInPoaching(Integer value) {
        setNumberOfMoosesKilledInPoaching(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesKilledInRutFight(Integer value) {
        setNumberOfMoosesKilledInRutFight(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfStarvedMooses(Integer value) {
        setNumberOfStarvedMooses(value);
        return this;
    }

    public MooseDataCardSection_8_3 withNumberOfMoosesDeceasedByOtherReason(Integer value) {
        setNumberOfMoosesDeceasedByOtherReason(value);
        return this;
    }

    public MooseDataCardSection_8_3 withTotalNumberOfDeceasedMooses(Integer value) {
        setTotalNumberOfDeceasedMooses(value);
        return this;
    }

    public MooseDataCardSection_8_3 withExplanationForOtherReason(String value) {
        setExplanationForOtherReason(value);
        return this;
    }

    @Override
    public MooseDataCardSection_8_3 clone() {
        final MooseDataCardSection_8_3 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_3) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_3 createCopy() {
        final MooseDataCardSection_8_3 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_3) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.numberOfDrownedMooses = this.numberOfDrownedMooses;
        _newObject.numberOfMoosesKilledByBear = this.numberOfMoosesKilledByBear;
        _newObject.numberOfMoosesKilledByWolf = this.numberOfMoosesKilledByWolf;
        _newObject.numberOfMoosesKilledInTrafficAccident = this.numberOfMoosesKilledInTrafficAccident;
        _newObject.numberOfMoosesKilledInPoaching = this.numberOfMoosesKilledInPoaching;
        _newObject.numberOfMoosesKilledInRutFight = this.numberOfMoosesKilledInRutFight;
        _newObject.numberOfStarvedMooses = this.numberOfStarvedMooses;
        _newObject.numberOfMoosesDeceasedByOtherReason = this.numberOfMoosesDeceasedByOtherReason;
        _newObject.totalNumberOfDeceasedMooses = this.totalNumberOfDeceasedMooses;
        _newObject.explanationForOtherReason = this.explanationForOtherReason;
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_3 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardSection_8_3 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_3) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree numberOfDrownedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfDrownedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfDrownedMoosesPropertyTree!= null):((numberOfDrownedMoosesPropertyTree == null)||(!numberOfDrownedMoosesPropertyTree.isLeaf())))) {
            _newObject.numberOfDrownedMooses = this.numberOfDrownedMooses;
        }
        final PropertyTree numberOfMoosesKilledByBearPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledByBear"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledByBearPropertyTree!= null):((numberOfMoosesKilledByBearPropertyTree == null)||(!numberOfMoosesKilledByBearPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesKilledByBear = this.numberOfMoosesKilledByBear;
        }
        final PropertyTree numberOfMoosesKilledByWolfPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledByWolf"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledByWolfPropertyTree!= null):((numberOfMoosesKilledByWolfPropertyTree == null)||(!numberOfMoosesKilledByWolfPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesKilledByWolf = this.numberOfMoosesKilledByWolf;
        }
        final PropertyTree numberOfMoosesKilledInTrafficAccidentPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInTrafficAccident"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInTrafficAccidentPropertyTree!= null):((numberOfMoosesKilledInTrafficAccidentPropertyTree == null)||(!numberOfMoosesKilledInTrafficAccidentPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesKilledInTrafficAccident = this.numberOfMoosesKilledInTrafficAccident;
        }
        final PropertyTree numberOfMoosesKilledInPoachingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInPoaching"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInPoachingPropertyTree!= null):((numberOfMoosesKilledInPoachingPropertyTree == null)||(!numberOfMoosesKilledInPoachingPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesKilledInPoaching = this.numberOfMoosesKilledInPoaching;
        }
        final PropertyTree numberOfMoosesKilledInRutFightPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesKilledInRutFight"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesKilledInRutFightPropertyTree!= null):((numberOfMoosesKilledInRutFightPropertyTree == null)||(!numberOfMoosesKilledInRutFightPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesKilledInRutFight = this.numberOfMoosesKilledInRutFight;
        }
        final PropertyTree numberOfStarvedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfStarvedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfStarvedMoosesPropertyTree!= null):((numberOfStarvedMoosesPropertyTree == null)||(!numberOfStarvedMoosesPropertyTree.isLeaf())))) {
            _newObject.numberOfStarvedMooses = this.numberOfStarvedMooses;
        }
        final PropertyTree numberOfMoosesDeceasedByOtherReasonPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfMoosesDeceasedByOtherReason"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfMoosesDeceasedByOtherReasonPropertyTree!= null):((numberOfMoosesDeceasedByOtherReasonPropertyTree == null)||(!numberOfMoosesDeceasedByOtherReasonPropertyTree.isLeaf())))) {
            _newObject.numberOfMoosesDeceasedByOtherReason = this.numberOfMoosesDeceasedByOtherReason;
        }
        final PropertyTree totalNumberOfDeceasedMoosesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("totalNumberOfDeceasedMooses"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(totalNumberOfDeceasedMoosesPropertyTree!= null):((totalNumberOfDeceasedMoosesPropertyTree == null)||(!totalNumberOfDeceasedMoosesPropertyTree.isLeaf())))) {
            _newObject.totalNumberOfDeceasedMooses = this.totalNumberOfDeceasedMooses;
        }
        final PropertyTree explanationForOtherReasonPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("explanationForOtherReason"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(explanationForOtherReasonPropertyTree!= null):((explanationForOtherReasonPropertyTree == null)||(!explanationForOtherReasonPropertyTree.isLeaf())))) {
            _newObject.explanationForOtherReason = this.explanationForOtherReason;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_3 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardSection_8_3 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardSection_8_3 .Selector<MooseDataCardSection_8_3 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardSection_8_3 .Select _root() {
            return new MooseDataCardSection_8_3 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfDrownedMooses = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledByBear = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledByWolf = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInTrafficAccident = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInPoaching = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInRutFight = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfStarvedMooses = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesDeceasedByOtherReason = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> totalNumberOfDeceasedMooses = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> explanationForOtherReason = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.numberOfDrownedMooses!= null) {
                products.put("numberOfDrownedMooses", this.numberOfDrownedMooses.init());
            }
            if (this.numberOfMoosesKilledByBear!= null) {
                products.put("numberOfMoosesKilledByBear", this.numberOfMoosesKilledByBear.init());
            }
            if (this.numberOfMoosesKilledByWolf!= null) {
                products.put("numberOfMoosesKilledByWolf", this.numberOfMoosesKilledByWolf.init());
            }
            if (this.numberOfMoosesKilledInTrafficAccident!= null) {
                products.put("numberOfMoosesKilledInTrafficAccident", this.numberOfMoosesKilledInTrafficAccident.init());
            }
            if (this.numberOfMoosesKilledInPoaching!= null) {
                products.put("numberOfMoosesKilledInPoaching", this.numberOfMoosesKilledInPoaching.init());
            }
            if (this.numberOfMoosesKilledInRutFight!= null) {
                products.put("numberOfMoosesKilledInRutFight", this.numberOfMoosesKilledInRutFight.init());
            }
            if (this.numberOfStarvedMooses!= null) {
                products.put("numberOfStarvedMooses", this.numberOfStarvedMooses.init());
            }
            if (this.numberOfMoosesDeceasedByOtherReason!= null) {
                products.put("numberOfMoosesDeceasedByOtherReason", this.numberOfMoosesDeceasedByOtherReason.init());
            }
            if (this.totalNumberOfDeceasedMooses!= null) {
                products.put("totalNumberOfDeceasedMooses", this.totalNumberOfDeceasedMooses.init());
            }
            if (this.explanationForOtherReason!= null) {
                products.put("explanationForOtherReason", this.explanationForOtherReason.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfDrownedMooses() {
            return ((this.numberOfDrownedMooses == null)?this.numberOfDrownedMooses = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfDrownedMooses"):this.numberOfDrownedMooses);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledByBear() {
            return ((this.numberOfMoosesKilledByBear == null)?this.numberOfMoosesKilledByBear = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesKilledByBear"):this.numberOfMoosesKilledByBear);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledByWolf() {
            return ((this.numberOfMoosesKilledByWolf == null)?this.numberOfMoosesKilledByWolf = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesKilledByWolf"):this.numberOfMoosesKilledByWolf);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInTrafficAccident() {
            return ((this.numberOfMoosesKilledInTrafficAccident == null)?this.numberOfMoosesKilledInTrafficAccident = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesKilledInTrafficAccident"):this.numberOfMoosesKilledInTrafficAccident);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInPoaching() {
            return ((this.numberOfMoosesKilledInPoaching == null)?this.numberOfMoosesKilledInPoaching = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesKilledInPoaching"):this.numberOfMoosesKilledInPoaching);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesKilledInRutFight() {
            return ((this.numberOfMoosesKilledInRutFight == null)?this.numberOfMoosesKilledInRutFight = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesKilledInRutFight"):this.numberOfMoosesKilledInRutFight);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfStarvedMooses() {
            return ((this.numberOfStarvedMooses == null)?this.numberOfStarvedMooses = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfStarvedMooses"):this.numberOfStarvedMooses);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> numberOfMoosesDeceasedByOtherReason() {
            return ((this.numberOfMoosesDeceasedByOtherReason == null)?this.numberOfMoosesDeceasedByOtherReason = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "numberOfMoosesDeceasedByOtherReason"):this.numberOfMoosesDeceasedByOtherReason);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> totalNumberOfDeceasedMooses() {
            return ((this.totalNumberOfDeceasedMooses == null)?this.totalNumberOfDeceasedMooses = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "totalNumberOfDeceasedMooses"):this.totalNumberOfDeceasedMooses);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>> explanationForOtherReason() {
            return ((this.explanationForOtherReason == null)?this.explanationForOtherReason = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_3 .Selector<TRoot, TParent>>(this._root, this, "explanationForOtherReason"):this.explanationForOtherReason);
        }

    }

}
