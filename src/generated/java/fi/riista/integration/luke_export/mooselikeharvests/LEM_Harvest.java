
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateTimeAdapter;
import org.joda.time.LocalDateTime;


/**
 * <p>Java class for Harvest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Harvest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoLocation" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}geoLocation"/&gt;
 *         &lt;element name="pointOfTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="gender" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameGender"/&gt;
 *         &lt;element name="alone" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="age" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameAge"/&gt;
 *         &lt;element name="weightEstimated" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="weightMeasured" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="fitnessClass" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameFitnessClass" minOccurs="0"/&gt;
 *         &lt;element name="antlersType" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}gameAntlersType" minOccurs="0"/&gt;
 *         &lt;element name="antlersWidth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="antlerPointsLeft" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="antlerPointsRight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="notEdible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="additionalInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Harvest", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "geoLocation",
    "pointOfTime",
    "gender",
    "alone",
    "age",
    "weightEstimated",
    "weightMeasured",
    "fitnessClass",
    "antlersType",
    "antlersWidth",
    "antlerPointsLeft",
    "antlerPointsRight",
    "notEdible",
    "additionalInfo"
})
public class LEM_Harvest {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected LEM_GeoLocation geoLocation;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime pointOfTime;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    @XmlSchemaType(name = "token")
    protected LEM_GameGender gender;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Boolean alone;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    @XmlSchemaType(name = "token")
    protected LEM_GameAge age;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Double weightEstimated;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Double weightMeasured;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_GameFitnessClass fitnessClass;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_GameAntlersType antlersType;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer antlersWidth;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer antlerPointsLeft;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer antlerPointsRight;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Boolean notEdible;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected String additionalInfo;

    /**
     * Gets the value of the geoLocation property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GeoLocation }
     *     
     */
    public LEM_GeoLocation getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the value of the geoLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GeoLocation }
     *     
     */
    public void setGeoLocation(LEM_GeoLocation value) {
        this.geoLocation = value;
    }

    /**
     * Gets the value of the pointOfTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    /**
     * Sets the value of the pointOfTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPointOfTime(LocalDateTime value) {
        this.pointOfTime = value;
    }

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
     * Gets the value of the alone property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAlone() {
        return alone;
    }

    /**
     * Sets the value of the alone property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlone(Boolean value) {
        this.alone = value;
    }

    /**
     * Gets the value of the age property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GameAge }
     *     
     */
    public LEM_GameAge getAge() {
        return age;
    }

    /**
     * Sets the value of the age property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GameAge }
     *     
     */
    public void setAge(LEM_GameAge value) {
        this.age = value;
    }

    /**
     * Gets the value of the weightEstimated property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWeightEstimated() {
        return weightEstimated;
    }

    /**
     * Sets the value of the weightEstimated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWeightEstimated(Double value) {
        this.weightEstimated = value;
    }

    /**
     * Gets the value of the weightMeasured property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWeightMeasured() {
        return weightMeasured;
    }

    /**
     * Sets the value of the weightMeasured property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWeightMeasured(Double value) {
        this.weightMeasured = value;
    }

    /**
     * Gets the value of the fitnessClass property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GameFitnessClass }
     *     
     */
    public LEM_GameFitnessClass getFitnessClass() {
        return fitnessClass;
    }

    /**
     * Sets the value of the fitnessClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GameFitnessClass }
     *     
     */
    public void setFitnessClass(LEM_GameFitnessClass value) {
        this.fitnessClass = value;
    }

    /**
     * Gets the value of the antlersType property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_GameAntlersType }
     *     
     */
    public LEM_GameAntlersType getAntlersType() {
        return antlersType;
    }

    /**
     * Sets the value of the antlersType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_GameAntlersType }
     *     
     */
    public void setAntlersType(LEM_GameAntlersType value) {
        this.antlersType = value;
    }

    /**
     * Gets the value of the antlersWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlersWidth() {
        return antlersWidth;
    }

    /**
     * Sets the value of the antlersWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlersWidth(Integer value) {
        this.antlersWidth = value;
    }

    /**
     * Gets the value of the antlerPointsLeft property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    /**
     * Sets the value of the antlerPointsLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlerPointsLeft(Integer value) {
        this.antlerPointsLeft = value;
    }

    /**
     * Gets the value of the antlerPointsRight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntlerPointsRight() {
        return antlerPointsRight;
    }

    /**
     * Sets the value of the antlerPointsRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntlerPointsRight(Integer value) {
        this.antlerPointsRight = value;
    }

    /**
     * Gets the value of the notEdible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNotEdible() {
        return notEdible;
    }

    /**
     * Sets the value of the notEdible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNotEdible(Boolean value) {
        this.notEdible = value;
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
