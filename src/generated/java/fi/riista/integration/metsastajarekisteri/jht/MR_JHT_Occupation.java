
package fi.riista.integration.metsastajarekisteri.jht;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;


/**
 * <p>Java class for occupation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="occupation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="rhyOfficialCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="occupationType" type="{http://riista.fi/integration/mr/jht/2018/10}occupationTypeEnum"/&gt;
 *         &lt;element name="beginDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="ssn" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *           &lt;element name="hunterNumber" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "occupation", namespace = "http://riista.fi/integration/mr/jht/2018/10", propOrder = {
    "id",
    "rhyOfficialCode",
    "occupationType",
    "beginDate",
    "endDate",
    "ssn",
    "hunterNumber"
})
public class MR_JHT_Occupation {

    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10")
    protected long id;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String rhyOfficialCode;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10", required = true)
    @XmlSchemaType(name = "token")
    protected MR_JHT_OccupationTypeEnum occupationType;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate beginDate;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate endDate;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String ssn;
    @XmlElement(namespace = "http://riista.fi/integration/mr/jht/2018/10")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String hunterNumber;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
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
     * Gets the value of the occupationType property.
     * 
     * @return
     *     possible object is
     *     {@link MR_JHT_OccupationTypeEnum }
     *     
     */
    public MR_JHT_OccupationTypeEnum getOccupationType() {
        return occupationType;
    }

    /**
     * Sets the value of the occupationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MR_JHT_OccupationTypeEnum }
     *     
     */
    public void setOccupationType(MR_JHT_OccupationTypeEnum value) {
        this.occupationType = value;
    }

    /**
     * Gets the value of the beginDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getBeginDate() {
        return beginDate;
    }

    /**
     * Sets the value of the beginDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeginDate(LocalDate value) {
        this.beginDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDate(LocalDate value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the ssn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Sets the value of the ssn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSsn(String value) {
        this.ssn = value;
    }

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

}
