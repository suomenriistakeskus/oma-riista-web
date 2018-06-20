
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
 * <p>Java class for _8.4Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_8.4Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_Ensimmäiset" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Viimeiset" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Ensimmäiset_2" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Viimeiset_2" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Ensimmäiset_3" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Viimeiset_3" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="_Aikuisessa" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Vasassa" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Alueellamme_esiintyy_hirven_täikärpästä" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_Määrä" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_MääräType"/&gt;
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
@XmlType(name = "_8.4Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "mooseHeatBeginDate",
    "mooseHeatEndDate",
    "mooseFawnBeginDate",
    "mooseFawnEndDate",
    "dateOfFirstDeerFlySeen",
    "dateOfLastDeerFlySeen",
    "numberOfAdultMoosesHavingFlies",
    "numberOfYoungMoosesHavingFlies",
    "deerFlyAppearead",
    "trendOfDeerFlyPopulationGrowth"
})
public class MooseDataCardSection_8_4 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_Ensimm\u00e4iset", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseHeatBeginDate;
    @XmlElement(name = "_Viimeiset", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseHeatEndDate;
    @XmlElement(name = "_Ensimm\u00e4iset_2", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseFawnBeginDate;
    @XmlElement(name = "_Viimeiset_2", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseFawnEndDate;
    @XmlElement(name = "_Ensimm\u00e4iset_3", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dateOfFirstDeerFlySeen;
    @XmlElement(name = "_Viimeiset_3", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dateOfLastDeerFlySeen;
    @XmlElement(name = "_Aikuisessa", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfAdultMoosesHavingFlies;
    @XmlElement(name = "_Vasassa", required = true, type = Integer.class, nillable = true)
    protected Integer numberOfYoungMoosesHavingFlies;
    @XmlElement(name = "_Alueellamme_esiintyy_hirven_t\u00e4ik\u00e4rp\u00e4st\u00e4", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance deerFlyAppearead;
    @XmlElement(name = "_M\u00e4\u00e4r\u00e4", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardTrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardSection_8_4() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardSection_8_4 copying the state of another MooseDataCardSection_8_4
     * 
     * @param _other
     *     The original MooseDataCardSection_8_4 from which to copy state.
     */
    public MooseDataCardSection_8_4(final MooseDataCardSection_8_4 _other) {
        this.mooseHeatBeginDate = _other.mooseHeatBeginDate;
        this.mooseHeatEndDate = _other.mooseHeatEndDate;
        this.mooseFawnBeginDate = _other.mooseFawnBeginDate;
        this.mooseFawnEndDate = _other.mooseFawnEndDate;
        this.dateOfFirstDeerFlySeen = _other.dateOfFirstDeerFlySeen;
        this.dateOfLastDeerFlySeen = _other.dateOfLastDeerFlySeen;
        this.numberOfAdultMoosesHavingFlies = _other.numberOfAdultMoosesHavingFlies;
        this.numberOfYoungMoosesHavingFlies = _other.numberOfYoungMoosesHavingFlies;
        this.deerFlyAppearead = _other.deerFlyAppearead;
        this.trendOfDeerFlyPopulationGrowth = _other.trendOfDeerFlyPopulationGrowth;
    }

    /**
     * Instantiates a MooseDataCardSection_8_4 copying the state of another MooseDataCardSection_8_4
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardSection_8_4 from which to copy state.
     */
    public MooseDataCardSection_8_4(final MooseDataCardSection_8_4 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree mooseHeatBeginDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseHeatBeginDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseHeatBeginDatePropertyTree!= null):((mooseHeatBeginDatePropertyTree == null)||(!mooseHeatBeginDatePropertyTree.isLeaf())))) {
            this.mooseHeatBeginDate = _other.mooseHeatBeginDate;
        }
        final PropertyTree mooseHeatEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseHeatEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseHeatEndDatePropertyTree!= null):((mooseHeatEndDatePropertyTree == null)||(!mooseHeatEndDatePropertyTree.isLeaf())))) {
            this.mooseHeatEndDate = _other.mooseHeatEndDate;
        }
        final PropertyTree mooseFawnBeginDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseFawnBeginDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseFawnBeginDatePropertyTree!= null):((mooseFawnBeginDatePropertyTree == null)||(!mooseFawnBeginDatePropertyTree.isLeaf())))) {
            this.mooseFawnBeginDate = _other.mooseFawnBeginDate;
        }
        final PropertyTree mooseFawnEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseFawnEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseFawnEndDatePropertyTree!= null):((mooseFawnEndDatePropertyTree == null)||(!mooseFawnEndDatePropertyTree.isLeaf())))) {
            this.mooseFawnEndDate = _other.mooseFawnEndDate;
        }
        final PropertyTree dateOfFirstDeerFlySeenPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("dateOfFirstDeerFlySeen"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(dateOfFirstDeerFlySeenPropertyTree!= null):((dateOfFirstDeerFlySeenPropertyTree == null)||(!dateOfFirstDeerFlySeenPropertyTree.isLeaf())))) {
            this.dateOfFirstDeerFlySeen = _other.dateOfFirstDeerFlySeen;
        }
        final PropertyTree dateOfLastDeerFlySeenPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("dateOfLastDeerFlySeen"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(dateOfLastDeerFlySeenPropertyTree!= null):((dateOfLastDeerFlySeenPropertyTree == null)||(!dateOfLastDeerFlySeenPropertyTree.isLeaf())))) {
            this.dateOfLastDeerFlySeen = _other.dateOfLastDeerFlySeen;
        }
        final PropertyTree numberOfAdultMoosesHavingFliesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfAdultMoosesHavingFlies"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfAdultMoosesHavingFliesPropertyTree!= null):((numberOfAdultMoosesHavingFliesPropertyTree == null)||(!numberOfAdultMoosesHavingFliesPropertyTree.isLeaf())))) {
            this.numberOfAdultMoosesHavingFlies = _other.numberOfAdultMoosesHavingFlies;
        }
        final PropertyTree numberOfYoungMoosesHavingFliesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfYoungMoosesHavingFlies"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfYoungMoosesHavingFliesPropertyTree!= null):((numberOfYoungMoosesHavingFliesPropertyTree == null)||(!numberOfYoungMoosesHavingFliesPropertyTree.isLeaf())))) {
            this.numberOfYoungMoosesHavingFlies = _other.numberOfYoungMoosesHavingFlies;
        }
        final PropertyTree deerFlyAppeareadPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deerFlyAppearead"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deerFlyAppeareadPropertyTree!= null):((deerFlyAppeareadPropertyTree == null)||(!deerFlyAppeareadPropertyTree.isLeaf())))) {
            this.deerFlyAppearead = _other.deerFlyAppearead;
        }
        final PropertyTree trendOfDeerFlyPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfDeerFlyPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfDeerFlyPopulationGrowthPropertyTree!= null):((trendOfDeerFlyPopulationGrowthPropertyTree == null)||(!trendOfDeerFlyPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfDeerFlyPopulationGrowth = _other.trendOfDeerFlyPopulationGrowth;
        }
    }

    /**
     * Gets the value of the mooseHeatBeginDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseHeatBeginDate() {
        return mooseHeatBeginDate;
    }

    /**
     * Sets the value of the mooseHeatBeginDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseHeatBeginDate(LocalDate value) {
        this.mooseHeatBeginDate = value;
    }

    /**
     * Gets the value of the mooseHeatEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseHeatEndDate() {
        return mooseHeatEndDate;
    }

    /**
     * Sets the value of the mooseHeatEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseHeatEndDate(LocalDate value) {
        this.mooseHeatEndDate = value;
    }

    /**
     * Gets the value of the mooseFawnBeginDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseFawnBeginDate() {
        return mooseFawnBeginDate;
    }

    /**
     * Sets the value of the mooseFawnBeginDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseFawnBeginDate(LocalDate value) {
        this.mooseFawnBeginDate = value;
    }

    /**
     * Gets the value of the mooseFawnEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseFawnEndDate() {
        return mooseFawnEndDate;
    }

    /**
     * Sets the value of the mooseFawnEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseFawnEndDate(LocalDate value) {
        this.mooseFawnEndDate = value;
    }

    /**
     * Gets the value of the dateOfFirstDeerFlySeen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDateOfFirstDeerFlySeen() {
        return dateOfFirstDeerFlySeen;
    }

    /**
     * Sets the value of the dateOfFirstDeerFlySeen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfFirstDeerFlySeen(LocalDate value) {
        this.dateOfFirstDeerFlySeen = value;
    }

    /**
     * Gets the value of the dateOfLastDeerFlySeen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDateOfLastDeerFlySeen() {
        return dateOfLastDeerFlySeen;
    }

    /**
     * Sets the value of the dateOfLastDeerFlySeen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfLastDeerFlySeen(LocalDate value) {
        this.dateOfLastDeerFlySeen = value;
    }

    /**
     * Gets the value of the numberOfAdultMoosesHavingFlies property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfAdultMoosesHavingFlies() {
        return numberOfAdultMoosesHavingFlies;
    }

    /**
     * Sets the value of the numberOfAdultMoosesHavingFlies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfAdultMoosesHavingFlies(Integer value) {
        this.numberOfAdultMoosesHavingFlies = value;
    }

    /**
     * Gets the value of the numberOfYoungMoosesHavingFlies property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfYoungMoosesHavingFlies() {
        return numberOfYoungMoosesHavingFlies;
    }

    /**
     * Sets the value of the numberOfYoungMoosesHavingFlies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfYoungMoosesHavingFlies(Integer value) {
        this.numberOfYoungMoosesHavingFlies = value;
    }

    /**
     * Gets the value of the deerFlyAppearead property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getDeerFlyAppearead() {
        return deerFlyAppearead;
    }

    /**
     * Sets the value of the deerFlyAppearead property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setDeerFlyAppearead(MooseDataCardGameSpeciesAppearance value) {
        this.deerFlyAppearead = value;
    }

    /**
     * Gets the value of the trendOfDeerFlyPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardTrendOfPopulationGrowth }
     *     
     */
    public MooseDataCardTrendOfPopulationGrowth getTrendOfDeerFlyPopulationGrowth() {
        return trendOfDeerFlyPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfDeerFlyPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardTrendOfPopulationGrowth }
     *     
     */
    public void setTrendOfDeerFlyPopulationGrowth(MooseDataCardTrendOfPopulationGrowth value) {
        this.trendOfDeerFlyPopulationGrowth = value;
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
            LocalDate theMooseHeatBeginDate;
            theMooseHeatBeginDate = this.getMooseHeatBeginDate();
            strategy.appendField(locator, this, "mooseHeatBeginDate", buffer, theMooseHeatBeginDate, (this.mooseHeatBeginDate!= null));
        }
        {
            LocalDate theMooseHeatEndDate;
            theMooseHeatEndDate = this.getMooseHeatEndDate();
            strategy.appendField(locator, this, "mooseHeatEndDate", buffer, theMooseHeatEndDate, (this.mooseHeatEndDate!= null));
        }
        {
            LocalDate theMooseFawnBeginDate;
            theMooseFawnBeginDate = this.getMooseFawnBeginDate();
            strategy.appendField(locator, this, "mooseFawnBeginDate", buffer, theMooseFawnBeginDate, (this.mooseFawnBeginDate!= null));
        }
        {
            LocalDate theMooseFawnEndDate;
            theMooseFawnEndDate = this.getMooseFawnEndDate();
            strategy.appendField(locator, this, "mooseFawnEndDate", buffer, theMooseFawnEndDate, (this.mooseFawnEndDate!= null));
        }
        {
            LocalDate theDateOfFirstDeerFlySeen;
            theDateOfFirstDeerFlySeen = this.getDateOfFirstDeerFlySeen();
            strategy.appendField(locator, this, "dateOfFirstDeerFlySeen", buffer, theDateOfFirstDeerFlySeen, (this.dateOfFirstDeerFlySeen!= null));
        }
        {
            LocalDate theDateOfLastDeerFlySeen;
            theDateOfLastDeerFlySeen = this.getDateOfLastDeerFlySeen();
            strategy.appendField(locator, this, "dateOfLastDeerFlySeen", buffer, theDateOfLastDeerFlySeen, (this.dateOfLastDeerFlySeen!= null));
        }
        {
            Integer theNumberOfAdultMoosesHavingFlies;
            theNumberOfAdultMoosesHavingFlies = this.getNumberOfAdultMoosesHavingFlies();
            strategy.appendField(locator, this, "numberOfAdultMoosesHavingFlies", buffer, theNumberOfAdultMoosesHavingFlies, (this.numberOfAdultMoosesHavingFlies!= null));
        }
        {
            Integer theNumberOfYoungMoosesHavingFlies;
            theNumberOfYoungMoosesHavingFlies = this.getNumberOfYoungMoosesHavingFlies();
            strategy.appendField(locator, this, "numberOfYoungMoosesHavingFlies", buffer, theNumberOfYoungMoosesHavingFlies, (this.numberOfYoungMoosesHavingFlies!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theDeerFlyAppearead;
            theDeerFlyAppearead = this.getDeerFlyAppearead();
            strategy.appendField(locator, this, "deerFlyAppearead", buffer, theDeerFlyAppearead, (this.deerFlyAppearead!= null));
        }
        {
            MooseDataCardTrendOfPopulationGrowth theTrendOfDeerFlyPopulationGrowth;
            theTrendOfDeerFlyPopulationGrowth = this.getTrendOfDeerFlyPopulationGrowth();
            strategy.appendField(locator, this, "trendOfDeerFlyPopulationGrowth", buffer, theTrendOfDeerFlyPopulationGrowth, (this.trendOfDeerFlyPopulationGrowth!= null));
        }
        return buffer;
    }

    public MooseDataCardSection_8_4 withMooseHeatBeginDate(LocalDate value) {
        setMooseHeatBeginDate(value);
        return this;
    }

    public MooseDataCardSection_8_4 withMooseHeatEndDate(LocalDate value) {
        setMooseHeatEndDate(value);
        return this;
    }

    public MooseDataCardSection_8_4 withMooseFawnBeginDate(LocalDate value) {
        setMooseFawnBeginDate(value);
        return this;
    }

    public MooseDataCardSection_8_4 withMooseFawnEndDate(LocalDate value) {
        setMooseFawnEndDate(value);
        return this;
    }

    public MooseDataCardSection_8_4 withDateOfFirstDeerFlySeen(LocalDate value) {
        setDateOfFirstDeerFlySeen(value);
        return this;
    }

    public MooseDataCardSection_8_4 withDateOfLastDeerFlySeen(LocalDate value) {
        setDateOfLastDeerFlySeen(value);
        return this;
    }

    public MooseDataCardSection_8_4 withNumberOfAdultMoosesHavingFlies(Integer value) {
        setNumberOfAdultMoosesHavingFlies(value);
        return this;
    }

    public MooseDataCardSection_8_4 withNumberOfYoungMoosesHavingFlies(Integer value) {
        setNumberOfYoungMoosesHavingFlies(value);
        return this;
    }

    public MooseDataCardSection_8_4 withDeerFlyAppearead(MooseDataCardGameSpeciesAppearance value) {
        setDeerFlyAppearead(value);
        return this;
    }

    public MooseDataCardSection_8_4 withTrendOfDeerFlyPopulationGrowth(MooseDataCardTrendOfPopulationGrowth value) {
        setTrendOfDeerFlyPopulationGrowth(value);
        return this;
    }

    @Override
    public MooseDataCardSection_8_4 clone() {
        final MooseDataCardSection_8_4 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_4) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_4 createCopy() {
        final MooseDataCardSection_8_4 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_4) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.mooseHeatBeginDate = this.mooseHeatBeginDate;
        _newObject.mooseHeatEndDate = this.mooseHeatEndDate;
        _newObject.mooseFawnBeginDate = this.mooseFawnBeginDate;
        _newObject.mooseFawnEndDate = this.mooseFawnEndDate;
        _newObject.dateOfFirstDeerFlySeen = this.dateOfFirstDeerFlySeen;
        _newObject.dateOfLastDeerFlySeen = this.dateOfLastDeerFlySeen;
        _newObject.numberOfAdultMoosesHavingFlies = this.numberOfAdultMoosesHavingFlies;
        _newObject.numberOfYoungMoosesHavingFlies = this.numberOfYoungMoosesHavingFlies;
        _newObject.deerFlyAppearead = this.deerFlyAppearead;
        _newObject.trendOfDeerFlyPopulationGrowth = this.trendOfDeerFlyPopulationGrowth;
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_4 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardSection_8_4 _newObject;
        try {
            _newObject = ((MooseDataCardSection_8_4) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree mooseHeatBeginDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseHeatBeginDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseHeatBeginDatePropertyTree!= null):((mooseHeatBeginDatePropertyTree == null)||(!mooseHeatBeginDatePropertyTree.isLeaf())))) {
            _newObject.mooseHeatBeginDate = this.mooseHeatBeginDate;
        }
        final PropertyTree mooseHeatEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseHeatEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseHeatEndDatePropertyTree!= null):((mooseHeatEndDatePropertyTree == null)||(!mooseHeatEndDatePropertyTree.isLeaf())))) {
            _newObject.mooseHeatEndDate = this.mooseHeatEndDate;
        }
        final PropertyTree mooseFawnBeginDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseFawnBeginDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseFawnBeginDatePropertyTree!= null):((mooseFawnBeginDatePropertyTree == null)||(!mooseFawnBeginDatePropertyTree.isLeaf())))) {
            _newObject.mooseFawnBeginDate = this.mooseFawnBeginDate;
        }
        final PropertyTree mooseFawnEndDatePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("mooseFawnEndDate"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(mooseFawnEndDatePropertyTree!= null):((mooseFawnEndDatePropertyTree == null)||(!mooseFawnEndDatePropertyTree.isLeaf())))) {
            _newObject.mooseFawnEndDate = this.mooseFawnEndDate;
        }
        final PropertyTree dateOfFirstDeerFlySeenPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("dateOfFirstDeerFlySeen"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(dateOfFirstDeerFlySeenPropertyTree!= null):((dateOfFirstDeerFlySeenPropertyTree == null)||(!dateOfFirstDeerFlySeenPropertyTree.isLeaf())))) {
            _newObject.dateOfFirstDeerFlySeen = this.dateOfFirstDeerFlySeen;
        }
        final PropertyTree dateOfLastDeerFlySeenPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("dateOfLastDeerFlySeen"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(dateOfLastDeerFlySeenPropertyTree!= null):((dateOfLastDeerFlySeenPropertyTree == null)||(!dateOfLastDeerFlySeenPropertyTree.isLeaf())))) {
            _newObject.dateOfLastDeerFlySeen = this.dateOfLastDeerFlySeen;
        }
        final PropertyTree numberOfAdultMoosesHavingFliesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfAdultMoosesHavingFlies"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfAdultMoosesHavingFliesPropertyTree!= null):((numberOfAdultMoosesHavingFliesPropertyTree == null)||(!numberOfAdultMoosesHavingFliesPropertyTree.isLeaf())))) {
            _newObject.numberOfAdultMoosesHavingFlies = this.numberOfAdultMoosesHavingFlies;
        }
        final PropertyTree numberOfYoungMoosesHavingFliesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("numberOfYoungMoosesHavingFlies"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(numberOfYoungMoosesHavingFliesPropertyTree!= null):((numberOfYoungMoosesHavingFliesPropertyTree == null)||(!numberOfYoungMoosesHavingFliesPropertyTree.isLeaf())))) {
            _newObject.numberOfYoungMoosesHavingFlies = this.numberOfYoungMoosesHavingFlies;
        }
        final PropertyTree deerFlyAppeareadPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deerFlyAppearead"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deerFlyAppeareadPropertyTree!= null):((deerFlyAppeareadPropertyTree == null)||(!deerFlyAppeareadPropertyTree.isLeaf())))) {
            _newObject.deerFlyAppearead = this.deerFlyAppearead;
        }
        final PropertyTree trendOfDeerFlyPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfDeerFlyPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfDeerFlyPopulationGrowthPropertyTree!= null):((trendOfDeerFlyPopulationGrowthPropertyTree == null)||(!trendOfDeerFlyPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfDeerFlyPopulationGrowth = this.trendOfDeerFlyPopulationGrowth;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardSection_8_4 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardSection_8_4 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardSection_8_4 .Selector<MooseDataCardSection_8_4 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardSection_8_4 .Select _root() {
            return new MooseDataCardSection_8_4 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseHeatBeginDate = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseHeatEndDate = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseFawnBeginDate = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseFawnEndDate = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> dateOfFirstDeerFlySeen = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> dateOfLastDeerFlySeen = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> numberOfAdultMoosesHavingFlies = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> numberOfYoungMoosesHavingFlies = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> deerFlyAppearead = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> trendOfDeerFlyPopulationGrowth = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.mooseHeatBeginDate!= null) {
                products.put("mooseHeatBeginDate", this.mooseHeatBeginDate.init());
            }
            if (this.mooseHeatEndDate!= null) {
                products.put("mooseHeatEndDate", this.mooseHeatEndDate.init());
            }
            if (this.mooseFawnBeginDate!= null) {
                products.put("mooseFawnBeginDate", this.mooseFawnBeginDate.init());
            }
            if (this.mooseFawnEndDate!= null) {
                products.put("mooseFawnEndDate", this.mooseFawnEndDate.init());
            }
            if (this.dateOfFirstDeerFlySeen!= null) {
                products.put("dateOfFirstDeerFlySeen", this.dateOfFirstDeerFlySeen.init());
            }
            if (this.dateOfLastDeerFlySeen!= null) {
                products.put("dateOfLastDeerFlySeen", this.dateOfLastDeerFlySeen.init());
            }
            if (this.numberOfAdultMoosesHavingFlies!= null) {
                products.put("numberOfAdultMoosesHavingFlies", this.numberOfAdultMoosesHavingFlies.init());
            }
            if (this.numberOfYoungMoosesHavingFlies!= null) {
                products.put("numberOfYoungMoosesHavingFlies", this.numberOfYoungMoosesHavingFlies.init());
            }
            if (this.deerFlyAppearead!= null) {
                products.put("deerFlyAppearead", this.deerFlyAppearead.init());
            }
            if (this.trendOfDeerFlyPopulationGrowth!= null) {
                products.put("trendOfDeerFlyPopulationGrowth", this.trendOfDeerFlyPopulationGrowth.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseHeatBeginDate() {
            return ((this.mooseHeatBeginDate == null)?this.mooseHeatBeginDate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "mooseHeatBeginDate"):this.mooseHeatBeginDate);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseHeatEndDate() {
            return ((this.mooseHeatEndDate == null)?this.mooseHeatEndDate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "mooseHeatEndDate"):this.mooseHeatEndDate);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseFawnBeginDate() {
            return ((this.mooseFawnBeginDate == null)?this.mooseFawnBeginDate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "mooseFawnBeginDate"):this.mooseFawnBeginDate);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> mooseFawnEndDate() {
            return ((this.mooseFawnEndDate == null)?this.mooseFawnEndDate = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "mooseFawnEndDate"):this.mooseFawnEndDate);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> dateOfFirstDeerFlySeen() {
            return ((this.dateOfFirstDeerFlySeen == null)?this.dateOfFirstDeerFlySeen = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "dateOfFirstDeerFlySeen"):this.dateOfFirstDeerFlySeen);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> dateOfLastDeerFlySeen() {
            return ((this.dateOfLastDeerFlySeen == null)?this.dateOfLastDeerFlySeen = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "dateOfLastDeerFlySeen"):this.dateOfLastDeerFlySeen);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> numberOfAdultMoosesHavingFlies() {
            return ((this.numberOfAdultMoosesHavingFlies == null)?this.numberOfAdultMoosesHavingFlies = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "numberOfAdultMoosesHavingFlies"):this.numberOfAdultMoosesHavingFlies);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> numberOfYoungMoosesHavingFlies() {
            return ((this.numberOfYoungMoosesHavingFlies == null)?this.numberOfYoungMoosesHavingFlies = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "numberOfYoungMoosesHavingFlies"):this.numberOfYoungMoosesHavingFlies);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> deerFlyAppearead() {
            return ((this.deerFlyAppearead == null)?this.deerFlyAppearead = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "deerFlyAppearead"):this.deerFlyAppearead);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>> trendOfDeerFlyPopulationGrowth() {
            return ((this.trendOfDeerFlyPopulationGrowth == null)?this.trendOfDeerFlyPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardSection_8_4 .Selector<TRoot, TParent>>(this._root, this, "trendOfDeerFlyPopulationGrowth"):this.trendOfDeerFlyPopulationGrowth);
        }

    }

}
