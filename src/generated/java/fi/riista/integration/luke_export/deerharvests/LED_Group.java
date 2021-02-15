
package fi.riista.integration.luke_export.deerharvests;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Group complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Group"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dataSource" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}dataSource"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="harvests" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Harvest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="observations" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}Observation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Group", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "dataSource",
    "nameFinnish",
    "harvests",
    "observations"
})
public class LED_Group {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    @XmlSchemaType(name = "token")
    protected LED_DataSource dataSource;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    protected String nameFinnish;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_Harvest> harvests;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", nillable = true)
    protected List<LED_Observation> observations;

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link LED_DataSource }
     *     
     */
    public LED_DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_DataSource }
     *     
     */
    public void setDataSource(LED_DataSource value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the nameFinnish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameFinnish() {
        return nameFinnish;
    }

    /**
     * Sets the value of the nameFinnish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameFinnish(String value) {
        this.nameFinnish = value;
    }

    /**
     * Gets the value of the harvests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the harvests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHarvests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LED_Harvest }
     * 
     * 
     */
    public List<LED_Harvest> getHarvests() {
        if (harvests == null) {
            harvests = new ArrayList<LED_Harvest>();
        }
        return this.harvests;
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
     * {@link LED_Observation }
     * 
     * 
     */
    public List<LED_Observation> getObservations() {
        if (observations == null) {
            observations = new ArrayList<LED_Observation>();
        }
        return this.observations;
    }

    public void setHarvests(List<LED_Harvest> value) {
        this.harvests = value;
    }

    public void setObservations(List<LED_Observation> value) {
        this.observations = value;
    }

}
