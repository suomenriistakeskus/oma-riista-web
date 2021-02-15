
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;


/**
 * <p>Java class for HuntingSummary complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HuntingSummary"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="huntingEndDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="huntingFinished" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="totalHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="effectiveHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="populationRemainingInTotalHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="populationRemainingInEffectiveHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HuntingSummary", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "huntingEndDate",
    "huntingFinished",
    "totalHuntingArea",
    "effectiveHuntingArea",
    "populationRemainingInTotalHuntingArea",
    "populationRemainingInEffectiveHuntingArea"
})
public class LED_HuntingSummary {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate huntingEndDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected boolean huntingFinished;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer totalHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer effectiveHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer populationRemainingInTotalHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer populationRemainingInEffectiveHuntingArea;

    /**
     * Gets the value of the huntingEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    /**
     * Sets the value of the huntingEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingEndDate(LocalDate value) {
        this.huntingEndDate = value;
    }

    /**
     * Gets the value of the huntingFinished property.
     * 
     */
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    /**
     * Sets the value of the huntingFinished property.
     * 
     */
    public void setHuntingFinished(boolean value) {
        this.huntingFinished = value;
    }

    /**
     * Gets the value of the totalHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    /**
     * Sets the value of the totalHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalHuntingArea(Integer value) {
        this.totalHuntingArea = value;
    }

    /**
     * Gets the value of the effectiveHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    /**
     * Sets the value of the effectiveHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEffectiveHuntingArea(Integer value) {
        this.effectiveHuntingArea = value;
    }

    /**
     * Gets the value of the populationRemainingInTotalHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPopulationRemainingInTotalHuntingArea() {
        return populationRemainingInTotalHuntingArea;
    }

    /**
     * Sets the value of the populationRemainingInTotalHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPopulationRemainingInTotalHuntingArea(Integer value) {
        this.populationRemainingInTotalHuntingArea = value;
    }

    /**
     * Gets the value of the populationRemainingInEffectiveHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPopulationRemainingInEffectiveHuntingArea() {
        return populationRemainingInEffectiveHuntingArea;
    }

    /**
     * Sets the value of the populationRemainingInEffectiveHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPopulationRemainingInEffectiveHuntingArea(Integer value) {
        this.populationRemainingInEffectiveHuntingArea = value;
    }

}
