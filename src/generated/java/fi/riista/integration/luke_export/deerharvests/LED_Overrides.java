
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Overrides complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Overrides"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="adultMales" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="adultFemales" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="youngMales" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="youngFemales" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="nonEdibleAdults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="nonEdibleYoung" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="totalHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="effectiveHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="remainingPopulationInTotalArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="remainingPopulationInEffectiveArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Overrides", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "adultMales",
    "adultFemales",
    "youngMales",
    "youngFemales",
    "nonEdibleAdults",
    "nonEdibleYoung",
    "totalHuntingArea",
    "effectiveHuntingArea",
    "remainingPopulationInTotalArea",
    "remainingPopulationInEffectiveArea"
})
public class LED_Overrides {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer adultMales;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer adultFemales;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer youngMales;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer youngFemales;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer nonEdibleAdults;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer nonEdibleYoung;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer totalHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer effectiveHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer remainingPopulationInTotalArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected Integer remainingPopulationInEffectiveArea;

    /**
     * Gets the value of the adultMales property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAdultMales() {
        return adultMales;
    }

    /**
     * Sets the value of the adultMales property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAdultMales(Integer value) {
        this.adultMales = value;
    }

    /**
     * Gets the value of the adultFemales property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAdultFemales() {
        return adultFemales;
    }

    /**
     * Sets the value of the adultFemales property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAdultFemales(Integer value) {
        this.adultFemales = value;
    }

    /**
     * Gets the value of the youngMales property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getYoungMales() {
        return youngMales;
    }

    /**
     * Sets the value of the youngMales property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setYoungMales(Integer value) {
        this.youngMales = value;
    }

    /**
     * Gets the value of the youngFemales property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getYoungFemales() {
        return youngFemales;
    }

    /**
     * Sets the value of the youngFemales property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setYoungFemales(Integer value) {
        this.youngFemales = value;
    }

    /**
     * Gets the value of the nonEdibleAdults property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNonEdibleAdults() {
        return nonEdibleAdults;
    }

    /**
     * Sets the value of the nonEdibleAdults property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNonEdibleAdults(Integer value) {
        this.nonEdibleAdults = value;
    }

    /**
     * Gets the value of the nonEdibleYoung property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNonEdibleYoung() {
        return nonEdibleYoung;
    }

    /**
     * Sets the value of the nonEdibleYoung property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNonEdibleYoung(Integer value) {
        this.nonEdibleYoung = value;
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
     * Gets the value of the remainingPopulationInTotalArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    /**
     * Sets the value of the remainingPopulationInTotalArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRemainingPopulationInTotalArea(Integer value) {
        this.remainingPopulationInTotalArea = value;
    }

    /**
     * Gets the value of the remainingPopulationInEffectiveArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    /**
     * Sets the value of the remainingPopulationInEffectiveArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRemainingPopulationInEffectiveArea(Integer value) {
        this.remainingPopulationInEffectiveArea = value;
    }

}
