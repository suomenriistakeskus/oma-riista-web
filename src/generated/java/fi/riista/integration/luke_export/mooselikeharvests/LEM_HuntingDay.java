
package fi.riista.integration.luke_export.mooselikeharvests;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import fi.riista.integration.support.LocalTimeAdapter;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;


/**
 * <p>Java class for HuntingDay complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HuntingDay"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}time"/&gt;
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="endTime" type="{http://www.w3.org/2001/XMLSchema}time"/&gt;
 *         &lt;element name="breakDurationInMinutes" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="durationInMinutes" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="snowDepth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="huntingMethod" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}HuntingMethod" minOccurs="0"/&gt;
 *         &lt;element name="numberOfHunters" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="numberOfHounds" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="mooseHarvests" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Harvest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="observations" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}Observation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HuntingDay", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "startDate",
    "startTime",
    "endDate",
    "endTime",
    "breakDurationInMinutes",
    "durationInMinutes",
    "snowDepth",
    "huntingMethod",
    "numberOfHunters",
    "numberOfHounds",
    "mooseHarvests",
    "observations"
})
public class LEM_HuntingDay {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate startDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    @XmlSchemaType(name = "time")
    protected LocalTime startTime;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate endDate;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    @XmlSchemaType(name = "time")
    protected LocalTime endTime;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int breakDurationInMinutes;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected int durationInMinutes;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer snowDepth;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    @XmlSchemaType(name = "token")
    protected LEM_HuntingMethod huntingMethod;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfHunters;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer numberOfHounds;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", nillable = true)
    protected List<LEM_Harvest> mooseHarvests;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", nillable = true)
    protected List<LEM_Observation> observations;

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDate(LocalDate value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(LocalTime value) {
        this.startTime = value;
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
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndTime(LocalTime value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the breakDurationInMinutes property.
     * 
     */
    public int getBreakDurationInMinutes() {
        return breakDurationInMinutes;
    }

    /**
     * Sets the value of the breakDurationInMinutes property.
     * 
     */
    public void setBreakDurationInMinutes(int value) {
        this.breakDurationInMinutes = value;
    }

    /**
     * Gets the value of the durationInMinutes property.
     * 
     */
    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * Sets the value of the durationInMinutes property.
     * 
     */
    public void setDurationInMinutes(int value) {
        this.durationInMinutes = value;
    }

    /**
     * Gets the value of the snowDepth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSnowDepth() {
        return snowDepth;
    }

    /**
     * Sets the value of the snowDepth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSnowDepth(Integer value) {
        this.snowDepth = value;
    }

    /**
     * Gets the value of the huntingMethod property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_HuntingMethod }
     *     
     */
    public LEM_HuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    /**
     * Sets the value of the huntingMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_HuntingMethod }
     *     
     */
    public void setHuntingMethod(LEM_HuntingMethod value) {
        this.huntingMethod = value;
    }

    /**
     * Gets the value of the numberOfHunters property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfHunters() {
        return numberOfHunters;
    }

    /**
     * Sets the value of the numberOfHunters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfHunters(Integer value) {
        this.numberOfHunters = value;
    }

    /**
     * Gets the value of the numberOfHounds property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfHounds() {
        return numberOfHounds;
    }

    /**
     * Sets the value of the numberOfHounds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfHounds(Integer value) {
        this.numberOfHounds = value;
    }

    /**
     * Gets the value of the mooseHarvests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mooseHarvests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMooseHarvests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LEM_Harvest }
     * 
     * 
     */
    public List<LEM_Harvest> getMooseHarvests() {
        if (mooseHarvests == null) {
            mooseHarvests = new ArrayList<LEM_Harvest>();
        }
        return this.mooseHarvests;
    }

    /**
     * Gets the value of the observations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LEM_Observation }
     * 
     * 
     */
    public List<LEM_Observation> getObservations() {
        if (observations == null) {
            observations = new ArrayList<LEM_Observation>();
        }
        return this.observations;
    }

}
