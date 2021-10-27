
package fi.riista.integration.metsastajarekisteri.shootingtest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
 * <p>Java class for Person complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Person"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="HunterNumber" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="ValidTests" type="{http://riista.fi/integration/mr/export/shootingTest}ShootingTestList"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Person", propOrder = {
    "hunterNumber",
    "validTests"
})
@XmlRootElement(name = "Person")
public class MR_Person implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "HunterNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String hunterNumber;
    @XmlElement(name = "ValidTests", required = true)
    protected MR_ShootingTestList validTests;

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
     * Gets the value of the validTests property.
     * 
     * @return
     *     possible object is
     *     {@link MR_ShootingTestList }
     *     
     */
    public MR_ShootingTestList getValidTests() {
        return validTests;
    }

    /**
     * Sets the value of the validTests property.
     * 
     * @param value
     *     allowed object is
     *     {@link MR_ShootingTestList }
     *     
     */
    public void setValidTests(MR_ShootingTestList value) {
        this.validTests = value;
    }

    public MR_Person withHunterNumber(String value) {
        setHunterNumber(value);
        return this;
    }

    public MR_Person withValidTests(MR_ShootingTestList value) {
        setValidTests(value);
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
            String theHunterNumber;
            theHunterNumber = this.getHunterNumber();
            strategy.appendField(locator, this, "hunterNumber", buffer, theHunterNumber, (this.hunterNumber!= null));
        }
        {
            MR_ShootingTestList theValidTests;
            theValidTests = this.getValidTests();
            strategy.appendField(locator, this, "validTests", buffer, theValidTests, (this.validTests!= null));
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
        final MR_Person that = ((MR_Person) object);
        {
            String lhsHunterNumber;
            lhsHunterNumber = this.getHunterNumber();
            String rhsHunterNumber;
            rhsHunterNumber = that.getHunterNumber();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "hunterNumber", lhsHunterNumber), LocatorUtils.property(thatLocator, "hunterNumber", rhsHunterNumber), lhsHunterNumber, rhsHunterNumber, (this.hunterNumber!= null), (that.hunterNumber!= null))) {
                return false;
            }
        }
        {
            MR_ShootingTestList lhsValidTests;
            lhsValidTests = this.getValidTests();
            MR_ShootingTestList rhsValidTests;
            rhsValidTests = that.getValidTests();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "validTests", lhsValidTests), LocatorUtils.property(thatLocator, "validTests", rhsValidTests), lhsValidTests, rhsValidTests, (this.validTests!= null), (that.validTests!= null))) {
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
            String theHunterNumber;
            theHunterNumber = this.getHunterNumber();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "hunterNumber", theHunterNumber), currentHashCode, theHunterNumber, (this.hunterNumber!= null));
        }
        {
            MR_ShootingTestList theValidTests;
            theValidTests = this.getValidTests();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "validTests", theValidTests), currentHashCode, theValidTests, (this.validTests!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
