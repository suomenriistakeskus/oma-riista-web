
package fi.riista.integration.luke_export.mooselikeharvests;

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
 *         &lt;element name="dataSource" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}dataSource"/&gt;
 *         &lt;element name="nameFinnish" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="huntingDays" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}HuntingDay" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Group", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "dataSource",
    "nameFinnish",
    "huntingDays"
})
public class LEM_Group {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    @XmlSchemaType(name = "token")
    protected LEM_DataSource dataSource;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    protected String nameFinnish;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected List<LEM_HuntingDay> huntingDays;

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link LEM_DataSource }
     *     
     */
    public LEM_DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEM_DataSource }
     *     
     */
    public void setDataSource(LEM_DataSource value) {
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
     * Gets the value of the huntingDays property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the huntingDays property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHuntingDays().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LEM_HuntingDay }
     * 
     * 
     */
    public List<LEM_HuntingDay> getHuntingDays() {
        if (huntingDays == null) {
            huntingDays = new ArrayList<LEM_HuntingDay>();
        }
        return this.huntingDays;
    }

    public void setHuntingDays(List<LEM_HuntingDay> value) {
        this.huntingDays = value;
    }

}
