
package fi.riista.integration.luke_export.deerharvests;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Permit complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Permit"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="permitNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="contactPerson" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Person" minOccurs="0"/&gt;
 *         &lt;element name="amount" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Amount"/&gt;
 *         &lt;element name="amendmentPermits" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Amount" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="huntingClubs" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Club" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Permit", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "permitNumber",
    "rhyOfficialCode",
    "contactPerson",
    "amount",
    "amendmentPermits",
    "huntingClubs"
})
public class LED_Permit {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String permitNumber;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String rhyOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected LED_Person contactPerson;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected LED_Amount amount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_Amount> amendmentPermits;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true, nillable = true)
    protected List<LED_Club> huntingClubs;

    /**
     * Gets the value of the permitNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitNumber() {
        return permitNumber;
    }

    /**
     * Sets the value of the permitNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitNumber(String value) {
        this.permitNumber = value;
    }

    /**
     * Gets the value of the rhyOfficialCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    /**
     * Sets the value of the rhyOfficialCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRhyOfficialCode(String value) {
        this.rhyOfficialCode = value;
    }

    /**
     * Gets the value of the contactPerson property.
     * 
     * @return
     *     possible object is
     *     {@link LED_Person }
     *     
     */
    public LED_Person getContactPerson() {
        return contactPerson;
    }

    /**
     * Sets the value of the contactPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_Person }
     *     
     */
    public void setContactPerson(LED_Person value) {
        this.contactPerson = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link LED_Amount }
     *     
     */
    public LED_Amount getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_Amount }
     *     
     */
    public void setAmount(LED_Amount value) {
        this.amount = value;
    }

    /**
     * Gets the value of the amendmentPermits property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the amendmentPermits property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAmendmentPermits().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_Amount }
     * 
     * 
     */
    public List<LED_Amount> getAmendmentPermits() {
        if (amendmentPermits == null) {
            amendmentPermits = new ArrayList<LED_Amount>();
        }
        return this.amendmentPermits;
    }

    /**
     * Gets the value of the huntingClubs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the huntingClubs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHuntingClubs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_Club }
     * 
     * 
     */
    public List<LED_Club> getHuntingClubs() {
        if (huntingClubs == null) {
            huntingClubs = new ArrayList<LED_Club>();
        }
        return this.huntingClubs;
    }

    public void setAmendmentPermits(List<LED_Amount> value) {
        this.amendmentPermits = value;
    }

    public void setHuntingClubs(List<LED_Club> value) {
        this.huntingClubs = value;
    }

}
