
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Amount complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Amount"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="restriction" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}restrictionType" minOccurs="0"/&gt;
 *         &lt;element name="restrictedAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Amount", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "amount",
    "restriction",
    "restrictedAmount"
})
public class LED_Amount {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected float amount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    @XmlSchemaType(name = "token")
    protected LED_RestrictionType restriction;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Float restrictedAmount;

    /**
     * Gets the value of the amount property.
     * 
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     */
    public void setAmount(float value) {
        this.amount = value;
    }

    /**
     * Gets the value of the restriction property.
     * 
     * @return
     *     possible object is
     *     {@link LED_RestrictionType }
     *     
     */
    public LED_RestrictionType getRestriction() {
        return restriction;
    }

    /**
     * Sets the value of the restriction property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_RestrictionType }
     *     
     */
    public void setRestriction(LED_RestrictionType value) {
        this.restriction = value;
    }

    /**
     * Gets the value of the restrictedAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRestrictedAmount() {
        return restrictedAmount;
    }

    /**
     * Sets the value of the restrictedAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRestrictedAmount(Float value) {
        this.restrictedAmount = value;
    }

}
