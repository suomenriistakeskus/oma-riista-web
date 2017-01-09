
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ObservationSpecimen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObservationSpecimen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameGender" minOccurs="0"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}observedGameAge" minOccurs="0"/&gt;
 *         &lt;element name="state" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}observedGameState" minOccurs="0"/&gt;
 *         &lt;element name="marking" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameMarking" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationSpecimen", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "gender",
    "age",
    "state",
    "marking"
})
public class LEM_ObservationSpecimen {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_GameGender gender;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_ObservedGameAge age;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_ObservedGameState state;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_GameMarking marking;

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GameGender }
     *     
     */
    public LEM_GameGender getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GameGender }
     *     
     */
    public void setGender(LEM_GameGender value) {
        this.gender = value;
    }

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_ObservedGameAge }
     *     
     */
    public LEM_ObservedGameAge getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_ObservedGameAge }
     *     
     */
    public void setAge(LEM_ObservedGameAge value) {
        this.age = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_ObservedGameState }
     *     
     */
    public LEM_ObservedGameState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_ObservedGameState }
     *     
     */
    public void setState(LEM_ObservedGameState value) {
        this.state = value;
    }

    /**
     * Gets the value of the marking property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GameMarking }
     *     
     */
    public LEM_GameMarking getMarking() {
        return marking;
    }

    /**
     * Sets the value of the marking property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GameMarking }
     *     
     */
    public void setMarking(LEM_GameMarking value) {
        this.marking = value;
    }

}
