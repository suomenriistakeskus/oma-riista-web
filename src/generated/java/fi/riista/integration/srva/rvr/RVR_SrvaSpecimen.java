
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaSpecimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="srvaSpecimen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/srva/rvr}gameAgeEnum" minOccurs="0"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/srva/rvr}gameGenderEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "srvaSpecimen", namespace = "http://riista.fi/integration/srva/rvr", propOrder = {
    "age",
    "gender"
})
public class RVR_SrvaSpecimen {

    @XmlSchemaType(name = "token")
    protected RVR_GameAgeEnum age;
    @XmlSchemaType(name = "token")
    protected RVR_GameGenderEnum gender;

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_GameAgeEnum }
     *     
     */
    public RVR_GameAgeEnum getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_GameAgeEnum }
     *     
     */
    public void setAge(RVR_GameAgeEnum value) {
        this.age = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link RVR_GameGenderEnum }
     *     
     */
    public RVR_GameGenderEnum getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link RVR_GameGenderEnum }
     *     
     */
    public void setGender(RVR_GameGenderEnum value) {
        this.gender = value;
    }

}
