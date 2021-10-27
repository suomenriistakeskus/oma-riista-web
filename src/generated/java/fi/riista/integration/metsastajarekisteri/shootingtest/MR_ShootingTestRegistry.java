
package fi.riista.integration.metsastajarekisteri.shootingtest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;
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
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RegisterDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="Persons" type="{http://riista.fi/integration/mr/export/shootingTest}PersonList"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "registerDate",
    "persons"
})
@XmlRootElement(name = "ShootingTestRegistry")
public class MR_ShootingTestRegistry implements Equals2, HashCode2, ToString2
{

    @XmlElement(name = "RegisterDate", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate registerDate;
    @XmlElement(name = "Persons", required = true)
    protected MR_PersonList persons;

    /**
     * Gets the value of the registerDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getRegisterDate() {
        return registerDate;
    }

    /**
     * Sets the value of the registerDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegisterDate(LocalDate value) {
        this.registerDate = value;
    }

    /**
     * Gets the value of the persons property.
     * 
     * @return
     *     possible object is
     *     {@link MR_PersonList }
     *     
     */
    public MR_PersonList getPersons() {
        return persons;
    }

    /**
     * Sets the value of the persons property.
     * 
     * @param value
     *     allowed object is
     *     {@link MR_PersonList }
     *     
     */
    public void setPersons(MR_PersonList value) {
        this.persons = value;
    }

    public MR_ShootingTestRegistry withRegisterDate(LocalDate value) {
        setRegisterDate(value);
        return this;
    }

    public MR_ShootingTestRegistry withPersons(MR_PersonList value) {
        setPersons(value);
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
            LocalDate theRegisterDate;
            theRegisterDate = this.getRegisterDate();
            strategy.appendField(locator, this, "registerDate", buffer, theRegisterDate, (this.registerDate!= null));
        }
        {
            MR_PersonList thePersons;
            thePersons = this.getPersons();
            strategy.appendField(locator, this, "persons", buffer, thePersons, (this.persons!= null));
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
        final MR_ShootingTestRegistry that = ((MR_ShootingTestRegistry) object);
        {
            LocalDate lhsRegisterDate;
            lhsRegisterDate = this.getRegisterDate();
            LocalDate rhsRegisterDate;
            rhsRegisterDate = that.getRegisterDate();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "registerDate", lhsRegisterDate), LocatorUtils.property(thatLocator, "registerDate", rhsRegisterDate), lhsRegisterDate, rhsRegisterDate, (this.registerDate!= null), (that.registerDate!= null))) {
                return false;
            }
        }
        {
            MR_PersonList lhsPersons;
            lhsPersons = this.getPersons();
            MR_PersonList rhsPersons;
            rhsPersons = that.getPersons();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "persons", lhsPersons), LocatorUtils.property(thatLocator, "persons", rhsPersons), lhsPersons, rhsPersons, (this.persons!= null), (that.persons!= null))) {
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
            LocalDate theRegisterDate;
            theRegisterDate = this.getRegisterDate();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "registerDate", theRegisterDate), currentHashCode, theRegisterDate, (this.registerDate!= null));
        }
        {
            MR_PersonList thePersons;
            thePersons = this.getPersons();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "persons", thePersons), currentHashCode, thePersons, (this.persons!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE2;
        return this.hashCode(null, strategy);
    }

}
