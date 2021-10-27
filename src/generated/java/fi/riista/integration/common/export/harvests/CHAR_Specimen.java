
package fi.riista.integration.common.export.harvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
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
 * <p>Java class for Specimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Specimen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="harvestId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/common/export/2018/10}gameGender" minOccurs="0"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/common/export/2018/10}gameAge" minOccurs="0"/&gt;
 *         &lt;element name="weight" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="weightEstimated" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="weightMeasured" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="fitnessClass" type="{http://riista.fi/integration/common/export/2018/10}gameFitnessClass" minOccurs="0"/&gt;
 *         &lt;element name="antlersType" type="{http://riista.fi/integration/common/export/2018/10}gameAntlersType" minOccurs="0"/&gt;
 *         &lt;element name="antlersWidth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="antlerPointsLeft" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="antlerPointsRight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="notEdible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Specimen", propOrder = {
    "harvestId",
    "gender",
    "age",
    "weight",
    "weightEstimated",
    "weightMeasured",
    "fitnessClass",
    "antlersType",
    "antlersWidth",
    "antlerPointsLeft",
    "antlerPointsRight",
    "notEdible"
})
public class CHAR_Specimen implements Equals2, HashCode2, ToString2
{

    protected long harvestId;
    @XmlSchemaType(name = "token")
    protected CHAR_GameGender gender;
    @XmlSchemaType(name = "token")
    protected CHAR_GameAge age;
    protected Double weight;
    protected Double weightEstimated;
    protected Double weightMeasured;
    @XmlSchemaType(name = "token")
    protected CHAR_GameFitnessClass fitnessClass;
    @XmlSchemaType(name = "token")
    protected CHAR_GameAntlersType antlersType;
    protected Integer antlersWidth;
    protected Integer antlerPointsLeft;
    protected Integer antlerPointsRight;
    protected Boolean notEdible;

    /**
     * Gets the value of the harvestId property.
     * 
     */
    public long getHarvestId() {
        return harvestId;
    }

    /**
     * Sets the value of the harvestId property.
     * 
     */
    public void setHarvestId(long value) {
        this.harvestId = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link CHAR_GameGender }
     *     
     */
    public CHAR_GameGender getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link CHAR_GameGender }
     *     
     */
    public void setGender(CHAR_GameGender value) {
        this.gender = value;
    }

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link CHAR_GameAge }
     *     
     */
    public CHAR_GameAge getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link CHAR_GameAge }
     *     
     */
    public void setAge(CHAR_GameAge value) {
        this.age = value;
    }

    /**
     * Gets the value of the weight property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWeight(Double value) {
        this.weight = value;
    }

    /**
     * Gets the value of the weightEstimated property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWeightEstimated() {
        return weightEstimated;
    }

    /**
     * Sets the value of the weightEstimated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWeightEstimated(Double value) {
        this.weightEstimated = value;
    }

    /**
     * Gets the value of the weightMeasured property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWeightMeasured() {
        return weightMeasured;
    }

    /**
     * Sets the value of the weightMeasured property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWeightMeasured(Double value) {
        this.weightMeasured = value;
    }

    /**
     * Gets the value of the fitnessClass property.
     * 
     * @return
     *     possible object is
     *     {@link CHAR_GameFitnessClass }
     *     
     */
    public CHAR_GameFitnessClass getFitnessClass() {
        return fitnessClass;
    }

    /**
     * Sets the value of the fitnessClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link CHAR_GameFitnessClass }
     *     
     */
    public void setFitnessClass(CHAR_GameFitnessClass value) {
        this.fitnessClass = value;
    }

    /**
     * Gets the value of the antlersType property.
     * 
     * @return
     *     possible object is
     *     {@link CHAR_GameAntlersType }
     *     
     */
    public CHAR_GameAntlersType getAntlersType() {
        return antlersType;
    }

    /**
     * Sets the value of the antlersType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CHAR_GameAntlersType }
     *     
     */
    public void setAntlersType(CHAR_GameAntlersType value) {
        this.antlersType = value;
    }

    /**
     * Gets the value of the antlersWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlersWidth() {
        return antlersWidth;
    }

    /**
     * Sets the value of the antlersWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlersWidth(Integer value) {
        this.antlersWidth = value;
    }

    /**
     * Gets the value of the antlerPointsLeft property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    /**
     * Sets the value of the antlerPointsLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlerPointsLeft(Integer value) {
        this.antlerPointsLeft = value;
    }

    /**
     * Gets the value of the antlerPointsRight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlerPointsRight() {
        return antlerPointsRight;
    }

    /**
     * Sets the value of the antlerPointsRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlerPointsRight(Integer value) {
        this.antlerPointsRight = value;
    }

    /**
     * Gets the value of the notEdible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNotEdible() {
        return notEdible;
    }

    /**
     * Sets the value of the notEdible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNotEdible(Boolean value) {
        this.notEdible = value;
    }

    public CHAR_Specimen withHarvestId(long value) {
        setHarvestId(value);
        return this;
    }

    public CHAR_Specimen withGender(CHAR_GameGender value) {
        setGender(value);
        return this;
    }

    public CHAR_Specimen withAge(CHAR_GameAge value) {
        setAge(value);
        return this;
    }

    public CHAR_Specimen withWeight(Double value) {
        setWeight(value);
        return this;
    }

    public CHAR_Specimen withWeightEstimated(Double value) {
        setWeightEstimated(value);
        return this;
    }

    public CHAR_Specimen withWeightMeasured(Double value) {
        setWeightMeasured(value);
        return this;
    }

    public CHAR_Specimen withFitnessClass(CHAR_GameFitnessClass value) {
        setFitnessClass(value);
        return this;
    }

    public CHAR_Specimen withAntlersType(CHAR_GameAntlersType value) {
        setAntlersType(value);
        return this;
    }

    public CHAR_Specimen withAntlersWidth(Integer value) {
        setAntlersWidth(value);
        return this;
    }

    public CHAR_Specimen withAntlerPointsLeft(Integer value) {
        setAntlerPointsLeft(value);
        return this;
    }

    public CHAR_Specimen withAntlerPointsRight(Integer value) {
        setAntlerPointsRight(value);
        return this;
    }

    public CHAR_Specimen withNotEdible(Boolean value) {
        setNotEdible(value);
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
            long theHarvestId;
            theHarvestId = this.getHarvestId();
            strategy.appendField(locator, this, "harvestId", buffer, theHarvestId, true);
        }
        {
            CHAR_GameGender theGender;
            theGender = this.getGender();
            strategy.appendField(locator, this, "gender", buffer, theGender, (this.gender!= null));
        }
        {
            CHAR_GameAge theAge;
            theAge = this.getAge();
            strategy.appendField(locator, this, "age", buffer, theAge, (this.age!= null));
        }
        {
            Double theWeight;
            theWeight = this.getWeight();
            strategy.appendField(locator, this, "weight", buffer, theWeight, (this.weight!= null));
        }
        {
            Double theWeightEstimated;
            theWeightEstimated = this.getWeightEstimated();
            strategy.appendField(locator, this, "weightEstimated", buffer, theWeightEstimated, (this.weightEstimated!= null));
        }
        {
            Double theWeightMeasured;
            theWeightMeasured = this.getWeightMeasured();
            strategy.appendField(locator, this, "weightMeasured", buffer, theWeightMeasured, (this.weightMeasured!= null));
        }
        {
            CHAR_GameFitnessClass theFitnessClass;
            theFitnessClass = this.getFitnessClass();
            strategy.appendField(locator, this, "fitnessClass", buffer, theFitnessClass, (this.fitnessClass!= null));
        }
        {
            CHAR_GameAntlersType theAntlersType;
            theAntlersType = this.getAntlersType();
            strategy.appendField(locator, this, "antlersType", buffer, theAntlersType, (this.antlersType!= null));
        }
        {
            Integer theAntlersWidth;
            theAntlersWidth = this.getAntlersWidth();
            strategy.appendField(locator, this, "antlersWidth", buffer, theAntlersWidth, (this.antlersWidth!= null));
        }
        {
            Integer theAntlerPointsLeft;
            theAntlerPointsLeft = this.getAntlerPointsLeft();
            strategy.appendField(locator, this, "antlerPointsLeft", buffer, theAntlerPointsLeft, (this.antlerPointsLeft!= null));
        }
        {
            Integer theAntlerPointsRight;
            theAntlerPointsRight = this.getAntlerPointsRight();
            strategy.appendField(locator, this, "antlerPointsRight", buffer, theAntlerPointsRight, (this.antlerPointsRight!= null));
        }
        {
            Boolean theNotEdible;
            theNotEdible = this.isNotEdible();
            strategy.appendField(locator, this, "notEdible", buffer, theNotEdible, (this.notEdible!= null));
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
        final CHAR_Specimen that = ((CHAR_Specimen) object);
        {
            long lhsHarvestId;
            lhsHarvestId = this.getHarvestId();
            long rhsHarvestId;
            rhsHarvestId = that.getHarvestId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "harvestId", lhsHarvestId), LocatorUtils.property(thatLocator, "harvestId", rhsHarvestId), lhsHarvestId, rhsHarvestId, true, true)) {
                return false;
            }
        }
        {
            CHAR_GameGender lhsGender;
            lhsGender = this.getGender();
            CHAR_GameGender rhsGender;
            rhsGender = that.getGender();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gender", lhsGender), LocatorUtils.property(thatLocator, "gender", rhsGender), lhsGender, rhsGender, (this.gender!= null), (that.gender!= null))) {
                return false;
            }
        }
        {
            CHAR_GameAge lhsAge;
            lhsAge = this.getAge();
            CHAR_GameAge rhsAge;
            rhsAge = that.getAge();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "age", lhsAge), LocatorUtils.property(thatLocator, "age", rhsAge), lhsAge, rhsAge, (this.age!= null), (that.age!= null))) {
                return false;
            }
        }
        {
            Double lhsWeight;
            lhsWeight = this.getWeight();
            Double rhsWeight;
            rhsWeight = that.getWeight();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "weight", lhsWeight), LocatorUtils.property(thatLocator, "weight", rhsWeight), lhsWeight, rhsWeight, (this.weight!= null), (that.weight!= null))) {
                return false;
            }
        }
        {
            Double lhsWeightEstimated;
            lhsWeightEstimated = this.getWeightEstimated();
            Double rhsWeightEstimated;
            rhsWeightEstimated = that.getWeightEstimated();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "weightEstimated", lhsWeightEstimated), LocatorUtils.property(thatLocator, "weightEstimated", rhsWeightEstimated), lhsWeightEstimated, rhsWeightEstimated, (this.weightEstimated!= null), (that.weightEstimated!= null))) {
                return false;
            }
        }
        {
            Double lhsWeightMeasured;
            lhsWeightMeasured = this.getWeightMeasured();
            Double rhsWeightMeasured;
            rhsWeightMeasured = that.getWeightMeasured();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "weightMeasured", lhsWeightMeasured), LocatorUtils.property(thatLocator, "weightMeasured", rhsWeightMeasured), lhsWeightMeasured, rhsWeightMeasured, (this.weightMeasured!= null), (that.weightMeasured!= null))) {
                return false;
            }
        }
        {
            CHAR_GameFitnessClass lhsFitnessClass;
            lhsFitnessClass = this.getFitnessClass();
            CHAR_GameFitnessClass rhsFitnessClass;
            rhsFitnessClass = that.getFitnessClass();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fitnessClass", lhsFitnessClass), LocatorUtils.property(thatLocator, "fitnessClass", rhsFitnessClass), lhsFitnessClass, rhsFitnessClass, (this.fitnessClass!= null), (that.fitnessClass!= null))) {
                return false;
            }
        }
        {
            CHAR_GameAntlersType lhsAntlersType;
            lhsAntlersType = this.getAntlersType();
            CHAR_GameAntlersType rhsAntlersType;
            rhsAntlersType = that.getAntlersType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "antlersType", lhsAntlersType), LocatorUtils.property(thatLocator, "antlersType", rhsAntlersType), lhsAntlersType, rhsAntlersType, (this.antlersType!= null), (that.antlersType!= null))) {
                return false;
            }
        }
        {
            Integer lhsAntlersWidth;
            lhsAntlersWidth = this.getAntlersWidth();
            Integer rhsAntlersWidth;
            rhsAntlersWidth = that.getAntlersWidth();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "antlersWidth", lhsAntlersWidth), LocatorUtils.property(thatLocator, "antlersWidth", rhsAntlersWidth), lhsAntlersWidth, rhsAntlersWidth, (this.antlersWidth!= null), (that.antlersWidth!= null))) {
                return false;
            }
        }
        {
            Integer lhsAntlerPointsLeft;
            lhsAntlerPointsLeft = this.getAntlerPointsLeft();
            Integer rhsAntlerPointsLeft;
            rhsAntlerPointsLeft = that.getAntlerPointsLeft();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "antlerPointsLeft", lhsAntlerPointsLeft), LocatorUtils.property(thatLocator, "antlerPointsLeft", rhsAntlerPointsLeft), lhsAntlerPointsLeft, rhsAntlerPointsLeft, (this.antlerPointsLeft!= null), (that.antlerPointsLeft!= null))) {
                return false;
            }
        }
        {
            Integer lhsAntlerPointsRight;
            lhsAntlerPointsRight = this.getAntlerPointsRight();
            Integer rhsAntlerPointsRight;
            rhsAntlerPointsRight = that.getAntlerPointsRight();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "antlerPointsRight", lhsAntlerPointsRight), LocatorUtils.property(thatLocator, "antlerPointsRight", rhsAntlerPointsRight), lhsAntlerPointsRight, rhsAntlerPointsRight, (this.antlerPointsRight!= null), (that.antlerPointsRight!= null))) {
                return false;
            }
        }
        {
            Boolean lhsNotEdible;
            lhsNotEdible = this.isNotEdible();
            Boolean rhsNotEdible;
            rhsNotEdible = that.isNotEdible();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "notEdible", lhsNotEdible), LocatorUtils.property(thatLocator, "notEdible", rhsNotEdible), lhsNotEdible, rhsNotEdible, (this.notEdible!= null), (that.notEdible!= null))) {
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
            long theHarvestId;
            theHarvestId = this.getHarvestId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "harvestId", theHarvestId), currentHashCode, theHarvestId, true);
        }
        {
            CHAR_GameGender theGender;
            theGender = this.getGender();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gender", theGender), currentHashCode, theGender, (this.gender!= null));
        }
        {
            CHAR_GameAge theAge;
            theAge = this.getAge();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "age", theAge), currentHashCode, theAge, (this.age!= null));
        }
        {
            Double theWeight;
            theWeight = this.getWeight();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "weight", theWeight), currentHashCode, theWeight, (this.weight!= null));
        }
        {
            Double theWeightEstimated;
            theWeightEstimated = this.getWeightEstimated();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "weightEstimated", theWeightEstimated), currentHashCode, theWeightEstimated, (this.weightEstimated!= null));
        }
        {
            Double theWeightMeasured;
            theWeightMeasured = this.getWeightMeasured();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "weightMeasured", theWeightMeasured), currentHashCode, theWeightMeasured, (this.weightMeasured!= null));
        }
        {
            CHAR_GameFitnessClass theFitnessClass;
            theFitnessClass = this.getFitnessClass();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "fitnessClass", theFitnessClass), currentHashCode, theFitnessClass, (this.fitnessClass!= null));
        }
        {
            CHAR_GameAntlersType theAntlersType;
            theAntlersType = this.getAntlersType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "antlersType", theAntlersType), currentHashCode, theAntlersType, (this.antlersType!= null));
        }
        {
            Integer theAntlersWidth;
            theAntlersWidth = this.getAntlersWidth();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "antlersWidth", theAntlersWidth), currentHashCode, theAntlersWidth, (this.antlersWidth!= null));
        }
        {
            Integer theAntlerPointsLeft;
            theAntlerPointsLeft = this.getAntlerPointsLeft();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "antlerPointsLeft", theAntlerPointsLeft), currentHashCode, theAntlerPointsLeft, (this.antlerPointsLeft!= null));
        }
        {
            Integer theAntlerPointsRight;
            theAntlerPointsRight = this.getAntlerPointsRight();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "antlerPointsRight", theAntlerPointsRight), currentHashCode, theAntlerPointsRight, (this.antlerPointsRight!= null));
        }
        {
            Boolean theNotEdible;
            theNotEdible = this.isNotEdible();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "notEdible", theNotEdible), currentHashCode, theNotEdible, (this.notEdible!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
