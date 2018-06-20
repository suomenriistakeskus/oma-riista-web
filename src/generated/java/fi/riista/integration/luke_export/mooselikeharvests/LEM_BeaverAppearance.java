
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BeaverAppearance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BeaverAppearance"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="trendOfPopulationGrowth" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}trendOfPopulationGrowth"/&gt;
 *         &lt;element name="amountOfInhabitedWinterNests" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="harvestAmount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="areaOfDamage" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="areaOccupiedByWater" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="additionalInfo" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BeaverAppearance", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "trendOfPopulationGrowth",
    "amountOfInhabitedWinterNests",
    "harvestAmount",
    "areaOfDamage",
    "areaOccupiedByWater",
    "additionalInfo"
})
public class LEM_BeaverAppearance {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    @XmlSchemaType(name = "token")
    protected LEM_TrendOfPopulationGrowth trendOfPopulationGrowth;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int amountOfInhabitedWinterNests;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int harvestAmount;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int areaOfDamage;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int areaOccupiedByWater;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String additionalInfo;

    /**
     * Gets the value of the trendOfPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_TrendOfPopulationGrowth }
     *     
     */
    public LEM_TrendOfPopulationGrowth getTrendOfPopulationGrowth() {
        return trendOfPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_TrendOfPopulationGrowth }
     *     
     */
    public void setTrendOfPopulationGrowth(LEM_TrendOfPopulationGrowth value) {
        this.trendOfPopulationGrowth = value;
    }

    /**
     * Gets the value of the amountOfInhabitedWinterNests property.
     * 
     */
    public int getAmountOfInhabitedWinterNests() {
        return amountOfInhabitedWinterNests;
    }

    /**
     * Sets the value of the amountOfInhabitedWinterNests property.
     * 
     */
    public void setAmountOfInhabitedWinterNests(int value) {
        this.amountOfInhabitedWinterNests = value;
    }

    /**
     * Gets the value of the harvestAmount property.
     * 
     */
    public int getHarvestAmount() {
        return harvestAmount;
    }

    /**
     * Sets the value of the harvestAmount property.
     * 
     */
    public void setHarvestAmount(int value) {
        this.harvestAmount = value;
    }

    /**
     * Gets the value of the areaOfDamage property.
     * 
     */
    public int getAreaOfDamage() {
        return areaOfDamage;
    }

    /**
     * Sets the value of the areaOfDamage property.
     * 
     */
    public void setAreaOfDamage(int value) {
        this.areaOfDamage = value;
    }

    /**
     * Gets the value of the areaOccupiedByWater property.
     * 
     */
    public int getAreaOccupiedByWater() {
        return areaOccupiedByWater;
    }

    /**
     * Sets the value of the areaOccupiedByWater property.
     * 
     */
    public void setAreaOccupiedByWater(int value) {
        this.areaOccupiedByWater = value;
    }

    /**
     * Gets the value of the additionalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets the value of the additionalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalInfo(String value) {
        this.additionalInfo = value;
    }

}
