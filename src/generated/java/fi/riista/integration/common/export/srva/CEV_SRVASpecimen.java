
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 * <p>Java class for SRVASpecimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SRVASpecimen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SRVAEventId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/common/export/2018/10}gameGender"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/common/export/2018/10}gameAge"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SRVASpecimen", propOrder = {
    "srvaEventId",
    "gender",
    "age"
})
public class CEV_SRVASpecimen implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "SRVAEventId")
    protected long srvaEventId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected CEV_GameGender gender;
    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected CEV_GameAge age;

    /**
     * Gets the value of the srvaEventId property.
     * 
     */
    public long getSRVAEventId() {
        return srvaEventId;
    }

    /**
     * Sets the value of the srvaEventId property.
     * 
     */
    public void setSRVAEventId(long value) {
        this.srvaEventId = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_GameGender }
     *     
     */
    public CEV_GameGender getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_GameGender }
     *     
     */
    public void setGender(CEV_GameGender value) {
        this.gender = value;
    }

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link CEV_GameAge }
     *     
     */
    public CEV_GameAge getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link CEV_GameAge }
     *     
     */
    public void setAge(CEV_GameAge value) {
        this.age = value;
    }

    public CEV_SRVASpecimen withSRVAEventId(long value) {
        setSRVAEventId(value);
        return this;
    }

    public CEV_SRVASpecimen withGender(CEV_GameGender value) {
        setGender(value);
        return this;
    }

    public CEV_SRVASpecimen withAge(CEV_GameAge value) {
        setAge(value);
        return this;
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
            long theSRVAEventId;
            theSRVAEventId = this.getSRVAEventId();
            strategy.appendField(locator, this, "srvaEventId", buffer, theSRVAEventId, true);
        }
        {
            CEV_GameGender theGender;
            theGender = this.getGender();
            strategy.appendField(locator, this, "gender", buffer, theGender, (this.gender!= null));
        }
        {
            CEV_GameAge theAge;
            theAge = this.getAge();
            strategy.appendField(locator, this, "age", buffer, theAge, (this.age!= null));
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
        final CEV_SRVASpecimen that = ((CEV_SRVASpecimen) object);
        {
            long lhsSRVAEventId;
            lhsSRVAEventId = this.getSRVAEventId();
            long rhsSRVAEventId;
            rhsSRVAEventId = that.getSRVAEventId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "srvaEventId", lhsSRVAEventId), LocatorUtils.property(thatLocator, "srvaEventId", rhsSRVAEventId), lhsSRVAEventId, rhsSRVAEventId, true, true)) {
                return false;
            }
        }
        {
            CEV_GameGender lhsGender;
            lhsGender = this.getGender();
            CEV_GameGender rhsGender;
            rhsGender = that.getGender();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gender", lhsGender), LocatorUtils.property(thatLocator, "gender", rhsGender), lhsGender, rhsGender, (this.gender!= null), (that.gender!= null))) {
                return false;
            }
        }
        {
            CEV_GameAge lhsAge;
            lhsAge = this.getAge();
            CEV_GameAge rhsAge;
            rhsAge = that.getAge();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "age", lhsAge), LocatorUtils.property(thatLocator, "age", rhsAge), lhsAge, rhsAge, (this.age!= null), (that.age!= null))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            long theSRVAEventId;
            theSRVAEventId = this.getSRVAEventId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "srvaEventId", theSRVAEventId), currentHashCode, theSRVAEventId, true);
        }
        {
            CEV_GameGender theGender;
            theGender = this.getGender();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gender", theGender), currentHashCode, theGender, (this.gender!= null));
        }
        {
            CEV_GameAge theAge;
            theAge = this.getAge();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "age", theAge), currentHashCode, theAge, (this.age!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
