
package fi.riista.integration.luke_export.mooselikeharvests;

import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="effectiveHuntingAreaPercentage" type="{http://www.w3.org/2001/XMLSchema}double"
 *         minOccurs="0"/&gt;
 *         &lt;element name="moosesRemainingInTotalHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="moosesRemainingInEffectiveHuntingArea" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="huntingAreaType" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}mooseHuntingAreaType"/&gt;
 *         &lt;element name="numberOfDrownedMooses" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesKilledByBear" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesKilledByWolf" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesKilledInTrafficAccident" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesKilledByPoaching" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesKilledInRutFight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfStarvedMooses" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfMoosesDeceasedByOtherReason" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="causeOfDeath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="whiteTailedDeerAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}EstimatedAppearance" minOccurs="0"/&gt;
 *         &lt;element name="roeDeerAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}EstimatedAppearance" minOccurs="0"/&gt;
 *         &lt;element name="wildForestReindeerAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}EstimatedAppearance" minOccurs="0"/&gt;
 *         &lt;element name="fallowDeerAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}EstimatedAppearance" minOccurs="0"/&gt;
 *         &lt;element name="wildBoarAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}WildBoarEstimatedAppearance" minOccurs="0"/&gt;
 *         &lt;element name="beaverAppearance" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}BeaverAppearance" minOccurs="0"/&gt;
 *         &lt;element name="mooseHeatBeginDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="mooseHeatEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="mooseFawnBeginDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="mooseFawnEndDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="dateOfFirstDeerFlySeen" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="dateOfLastDeerFlySeen" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="numberOfAdultMoosesHavingFlies" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfYoungMoosesHavingFlies" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="deerFliesAppeared" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="trendOfDeerFlyPopulationGrowth" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}trendOfPopulationGrowth" minOccurs="0"/&gt;
 *         &lt;element name="observationPolicyAdhered" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HuntingSummary", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "huntingEndDate",
    "huntingFinished",
    "totalHuntingArea",
    "effectiveHuntingArea",
    "effectiveHuntingAreaPercentage",
    "moosesRemainingInTotalHuntingArea",
    "moosesRemainingInEffectiveHuntingArea",
    "huntingAreaType",
    "numberOfDrownedMooses",
    "numberOfMoosesKilledByBear",
    "numberOfMoosesKilledByWolf",
    "numberOfMoosesKilledInTrafficAccident",
    "numberOfMoosesKilledByPoaching",
    "numberOfMoosesKilledInRutFight",
    "numberOfStarvedMooses",
    "numberOfMoosesDeceasedByOtherReason",
    "causeOfDeath",
    "whiteTailedDeerAppearance",
    "roeDeerAppearance",
    "wildForestReindeerAppearance",
    "fallowDeerAppearance",
    "wildBoarAppearance",
    "beaverAppearance",
    "mooseHeatBeginDate",
    "mooseHeatEndDate",
    "mooseFawnBeginDate",
    "mooseFawnEndDate",
    "dateOfFirstDeerFlySeen",
    "dateOfLastDeerFlySeen",
    "numberOfAdultMoosesHavingFlies",
    "numberOfYoungMoosesHavingFlies",
    "deerFliesAppeared",
    "trendOfDeerFlyPopulationGrowth",
    "observationPolicyAdhered"
})
public class LEM_HuntingSummary {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate huntingEndDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected boolean huntingFinished;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer totalHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer effectiveHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Double effectiveHuntingAreaPercentage;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer moosesRemainingInTotalHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer moosesRemainingInEffectiveHuntingArea;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, nillable = true)
    @XmlSchemaType(name = "token")
    protected LEM_MooseHuntingAreaType huntingAreaType;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfDrownedMooses;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesKilledByBear;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesKilledByWolf;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesKilledInTrafficAccident;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesKilledByPoaching;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesKilledInRutFight;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfStarvedMooses;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfMoosesDeceasedByOtherReason;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected String causeOfDeath;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_EstimatedAppearance whiteTailedDeerAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_EstimatedAppearance roeDeerAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_EstimatedAppearance wildForestReindeerAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_EstimatedAppearance fallowDeerAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_WildBoarEstimatedAppearance wildBoarAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected LEM_BeaverAppearance beaverAppearance;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseHeatBeginDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseHeatEndDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseFawnBeginDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate mooseFawnEndDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dateOfFirstDeerFlySeen;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dateOfLastDeerFlySeen;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfAdultMoosesHavingFlies;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfYoungMoosesHavingFlies;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Boolean deerFliesAppeared;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_TrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Boolean observationPolicyAdhered;

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
     * Gets the value of the effectiveHuntingAreaPercentage property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEffectiveHuntingAreaPercentage() {
        return effectiveHuntingAreaPercentage;
    }

    /**
     * Sets the value of the effectiveHuntingAreaPercentage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEffectiveHuntingAreaPercentage(Double value) {
        this.effectiveHuntingAreaPercentage = value;
    }

    /**
     * Gets the value of the moosesRemainingInTotalHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMoosesRemainingInTotalHuntingArea() {
        return moosesRemainingInTotalHuntingArea;
    }

    /**
     * Sets the value of the moosesRemainingInTotalHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMoosesRemainingInTotalHuntingArea(Integer value) {
        this.moosesRemainingInTotalHuntingArea = value;
    }

    /**
     * Gets the value of the moosesRemainingInEffectiveHuntingArea property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMoosesRemainingInEffectiveHuntingArea() {
        return moosesRemainingInEffectiveHuntingArea;
    }

    /**
     * Sets the value of the moosesRemainingInEffectiveHuntingArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMoosesRemainingInEffectiveHuntingArea(Integer value) {
        this.moosesRemainingInEffectiveHuntingArea = value;
    }

    /**
     * Gets the value of the huntingAreaType property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_MooseHuntingAreaType }
     *     
     */
    public LEM_MooseHuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    /**
     * Sets the value of the huntingAreaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_MooseHuntingAreaType }
     *     
     */
    public void setHuntingAreaType(LEM_MooseHuntingAreaType value) {
        this.huntingAreaType = value;
    }

    /**
     * Gets the value of the numberOfDrownedMooses property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfDrownedMooses() {
        return numberOfDrownedMooses;
    }

    /**
     * Sets the value of the numberOfDrownedMooses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfDrownedMooses(Integer value) {
        this.numberOfDrownedMooses = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledByBear property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledByBear() {
        return numberOfMoosesKilledByBear;
    }

    /**
     * Sets the value of the numberOfMoosesKilledByBear property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledByBear(Integer value) {
        this.numberOfMoosesKilledByBear = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledByWolf property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledByWolf() {
        return numberOfMoosesKilledByWolf;
    }

    /**
     * Sets the value of the numberOfMoosesKilledByWolf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledByWolf(Integer value) {
        this.numberOfMoosesKilledByWolf = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledInTrafficAccident property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledInTrafficAccident() {
        return numberOfMoosesKilledInTrafficAccident;
    }

    /**
     * Sets the value of the numberOfMoosesKilledInTrafficAccident property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledInTrafficAccident(Integer value) {
        this.numberOfMoosesKilledInTrafficAccident = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledByPoaching property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledByPoaching() {
        return numberOfMoosesKilledByPoaching;
    }

    /**
     * Sets the value of the numberOfMoosesKilledByPoaching property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledByPoaching(Integer value) {
        this.numberOfMoosesKilledByPoaching = value;
    }

    /**
     * Gets the value of the numberOfMoosesKilledInRutFight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesKilledInRutFight() {
        return numberOfMoosesKilledInRutFight;
    }

    /**
     * Sets the value of the numberOfMoosesKilledInRutFight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesKilledInRutFight(Integer value) {
        this.numberOfMoosesKilledInRutFight = value;
    }

    /**
     * Gets the value of the numberOfStarvedMooses property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfStarvedMooses() {
        return numberOfStarvedMooses;
    }

    /**
     * Sets the value of the numberOfStarvedMooses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfStarvedMooses(Integer value) {
        this.numberOfStarvedMooses = value;
    }

    /**
     * Gets the value of the numberOfMoosesDeceasedByOtherReason property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfMoosesDeceasedByOtherReason() {
        return numberOfMoosesDeceasedByOtherReason;
    }

    /**
     * Sets the value of the numberOfMoosesDeceasedByOtherReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfMoosesDeceasedByOtherReason(Integer value) {
        this.numberOfMoosesDeceasedByOtherReason = value;
    }

    /**
     * Gets the value of the causeOfDeath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCauseOfDeath() {
        return causeOfDeath;
    }

    /**
     * Sets the value of the causeOfDeath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCauseOfDeath(String value) {
        this.causeOfDeath = value;
    }

    /**
     * Gets the value of the whiteTailedDeerAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public LEM_EstimatedAppearance getWhiteTailedDeerAppearance() {
        return whiteTailedDeerAppearance;
    }

    /**
     * Sets the value of the whiteTailedDeerAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public void setWhiteTailedDeerAppearance(LEM_EstimatedAppearance value) {
        this.whiteTailedDeerAppearance = value;
    }

    /**
     * Gets the value of the roeDeerAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public LEM_EstimatedAppearance getRoeDeerAppearance() {
        return roeDeerAppearance;
    }

    /**
     * Sets the value of the roeDeerAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public void setRoeDeerAppearance(LEM_EstimatedAppearance value) {
        this.roeDeerAppearance = value;
    }

    /**
     * Gets the value of the wildForestReindeerAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public LEM_EstimatedAppearance getWildForestReindeerAppearance() {
        return wildForestReindeerAppearance;
    }

    /**
     * Sets the value of the wildForestReindeerAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public void setWildForestReindeerAppearance(LEM_EstimatedAppearance value) {
        this.wildForestReindeerAppearance = value;
    }

    /**
     * Gets the value of the fallowDeerAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public LEM_EstimatedAppearance getFallowDeerAppearance() {
        return fallowDeerAppearance;
    }

    /**
     * Sets the value of the fallowDeerAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_EstimatedAppearance }
     *     
     */
    public void setFallowDeerAppearance(LEM_EstimatedAppearance value) {
        this.fallowDeerAppearance = value;
    }

    /**
     * Gets the value of the wildBoarAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_WildBoarEstimatedAppearance }
     *     
     */
    public LEM_WildBoarEstimatedAppearance getWildBoarAppearance() {
        return wildBoarAppearance;
    }

    /**
     * Sets the value of the wildBoarAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_WildBoarEstimatedAppearance }
     *     
     */
    public void setWildBoarAppearance(LEM_WildBoarEstimatedAppearance value) {
        this.wildBoarAppearance = value;
    }

    /**
     * Gets the value of the beaverAppearance property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_BeaverAppearance }
     *     
     */
    public LEM_BeaverAppearance getBeaverAppearance() {
        return beaverAppearance;
    }

    /**
     * Sets the value of the beaverAppearance property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_BeaverAppearance }
     *     
     */
    public void setBeaverAppearance(LEM_BeaverAppearance value) {
        this.beaverAppearance = value;
    }

    /**
     * Gets the value of the mooseHeatBeginDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseHeatBeginDate() {
        return mooseHeatBeginDate;
    }

    /**
     * Sets the value of the mooseHeatBeginDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseHeatBeginDate(LocalDate value) {
        this.mooseHeatBeginDate = value;
    }

    /**
     * Gets the value of the mooseHeatEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseHeatEndDate() {
        return mooseHeatEndDate;
    }

    /**
     * Sets the value of the mooseHeatEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseHeatEndDate(LocalDate value) {
        this.mooseHeatEndDate = value;
    }

    /**
     * Gets the value of the mooseFawnBeginDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseFawnBeginDate() {
        return mooseFawnBeginDate;
    }

    /**
     * Sets the value of the mooseFawnBeginDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseFawnBeginDate(LocalDate value) {
        this.mooseFawnBeginDate = value;
    }

    /**
     * Gets the value of the mooseFawnEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getMooseFawnEndDate() {
        return mooseFawnEndDate;
    }

    /**
     * Sets the value of the mooseFawnEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMooseFawnEndDate(LocalDate value) {
        this.mooseFawnEndDate = value;
    }

    /**
     * Gets the value of the dateOfFirstDeerFlySeen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDateOfFirstDeerFlySeen() {
        return dateOfFirstDeerFlySeen;
    }

    /**
     * Sets the value of the dateOfFirstDeerFlySeen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfFirstDeerFlySeen(LocalDate value) {
        this.dateOfFirstDeerFlySeen = value;
    }

    /**
     * Gets the value of the dateOfLastDeerFlySeen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDateOfLastDeerFlySeen() {
        return dateOfLastDeerFlySeen;
    }

    /**
     * Sets the value of the dateOfLastDeerFlySeen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateOfLastDeerFlySeen(LocalDate value) {
        this.dateOfLastDeerFlySeen = value;
    }

    /**
     * Gets the value of the numberOfAdultMoosesHavingFlies property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfAdultMoosesHavingFlies() {
        return numberOfAdultMoosesHavingFlies;
    }

    /**
     * Sets the value of the numberOfAdultMoosesHavingFlies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfAdultMoosesHavingFlies(Integer value) {
        this.numberOfAdultMoosesHavingFlies = value;
    }

    /**
     * Gets the value of the numberOfYoungMoosesHavingFlies property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfYoungMoosesHavingFlies() {
        return numberOfYoungMoosesHavingFlies;
    }

    /**
     * Sets the value of the numberOfYoungMoosesHavingFlies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfYoungMoosesHavingFlies(Integer value) {
        this.numberOfYoungMoosesHavingFlies = value;
    }

    /**
     * Gets the value of the deerFliesAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeerFliesAppeared() {
        return deerFliesAppeared;
    }

    /**
     * Sets the value of the deerFliesAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeerFliesAppeared(Boolean value) {
        this.deerFliesAppeared = value;
    }

    /**
     * Gets the value of the trendOfDeerFlyPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_TrendOfPopulationGrowth }
     *     
     */
    public LEM_TrendOfPopulationGrowth getTrendOfDeerFlyPopulationGrowth() {
        return trendOfDeerFlyPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfDeerFlyPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_TrendOfPopulationGrowth }
     *     
     */
    public void setTrendOfDeerFlyPopulationGrowth(LEM_TrendOfPopulationGrowth value) {
        this.trendOfDeerFlyPopulationGrowth = value;
    }

    /**
     * Gets the value of the observationPolicyAdhered property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isObservationPolicyAdhered() {
        return observationPolicyAdhered;
    }

    /**
     * Sets the value of the observationPolicyAdhered property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setObservationPolicyAdhered(Boolean value) {
        this.observationPolicyAdhered = value;
    }

}
