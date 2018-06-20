
package fi.riista.integration.metsastajarekisteri.shootingtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 * <p>Java class for ShootingTestList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ShootingTestList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ShootingTest" type="{http://riista.fi/integration/mr/export/shootingTest}ShootingTest" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShootingTestList", propOrder = {
    "shootingTest"
})
public class MR_ShootingTestList implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "ShootingTest", required = true)
    protected List<MR_ShootingTest> shootingTest;

    /**
     * Gets the value of the shootingTest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shootingTest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShootingTest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MR_ShootingTest }
     * 
     * 
     */
    public List<MR_ShootingTest> getShootingTest() {
        if (shootingTest == null) {
            shootingTest = new ArrayList<MR_ShootingTest>();
        }
        return this.shootingTest;
    }

    public MR_ShootingTestList withShootingTest(MR_ShootingTest... values) {
        if (values!= null) {
            for (MR_ShootingTest value: values) {
                getShootingTest().add(value);
            }
        }
        return this;
    }

    public MR_ShootingTestList withShootingTest(Collection<MR_ShootingTest> values) {
        if (values!= null) {
            getShootingTest().addAll(values);
        }
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
            List<MR_ShootingTest> theShootingTest;
            theShootingTest = (((this.shootingTest!= null)&&(!this.shootingTest.isEmpty()))?this.getShootingTest():null);
            strategy.appendField(locator, this, "shootingTest", buffer, theShootingTest, ((this.shootingTest!= null)&&(!this.shootingTest.isEmpty())));
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
        final MR_ShootingTestList that = ((MR_ShootingTestList) object);
        {
            List<MR_ShootingTest> lhsShootingTest;
            lhsShootingTest = (((this.shootingTest!= null)&&(!this.shootingTest.isEmpty()))?this.getShootingTest():null);
            List<MR_ShootingTest> rhsShootingTest;
            rhsShootingTest = (((that.shootingTest!= null)&&(!that.shootingTest.isEmpty()))?that.getShootingTest():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "shootingTest", lhsShootingTest), LocatorUtils.property(thatLocator, "shootingTest", rhsShootingTest), lhsShootingTest, rhsShootingTest, ((this.shootingTest!= null)&&(!this.shootingTest.isEmpty())), ((that.shootingTest!= null)&&(!that.shootingTest.isEmpty())))) {
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
            List<MR_ShootingTest> theShootingTest;
            theShootingTest = (((this.shootingTest!= null)&&(!this.shootingTest.isEmpty()))?this.getShootingTest():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "shootingTest", theShootingTest), currentHashCode, theShootingTest, ((this.shootingTest!= null)&&(!this.shootingTest.isEmpty())));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
