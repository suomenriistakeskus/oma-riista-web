
package fi.riista.integration.luke_export.mooselikeharvests;

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
 *         &lt;element name="contactPerson" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Person" minOccurs="0"/&gt;
 *         &lt;element name="mooseAmount" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Amount"/&gt;
 *         &lt;element name="amendmentPermits" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Amount" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="huntingClubs" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Club" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Permit", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "permitNumber",
    "rhyOfficialCode",
    "contactPerson",
    "mooseAmount",
    "amendmentPermits",
    "huntingClubs"
})
public class LEM_Permit {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String permitNumber;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String rhyOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_Person contactPerson;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected LEM_Amount mooseAmount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", nillable = true)
    protected List<LEM_Amount> amendmentPermits;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, nillable = true)
    protected List<LEM_Club> huntingClubs;

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
     *     {@link LEM_Person }
     *     
     */
    public LEM_Person getContactPerson() {
        return contactPerson;
    }

    /**
     * Sets the value of the contactPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_Person }
     *     
     */
    public void setContactPerson(LEM_Person value) {
        this.contactPerson = value;
    }

    /**
     * Gets the value of the mooseAmount property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_Amount }
     *     
     */
    public LEM_Amount getMooseAmount() {
        return mooseAmount;
    }

    /**
     * Sets the value of the mooseAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_Amount }
     *     
     */
    public void setMooseAmount(LEM_Amount value) {
        this.mooseAmount = value;
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
     * {@link LEM_Amount }
     * 
     * 
     */
    public List<LEM_Amount> getAmendmentPermits() {
        if (amendmentPermits == null) {
            amendmentPermits = new ArrayList<LEM_Amount>();
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
     * {@link LEM_Club }
     * 
     * 
     */
    public List<LEM_Club> getHuntingClubs() {
        if (huntingClubs == null) {
            huntingClubs = new ArrayList<LEM_Club>();
        }
        return this.huntingClubs;
    }

}
