
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeerHuntingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeerHuntingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="huntingType" type="{http://riista.fi/integration/luke/export/deerharvests/2020/03}HuntingMethod"/&gt;
 *         &lt;element name="huntingTypeDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeerHuntingType", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", propOrder = {
    "huntingType",
    "huntingTypeDescription"
})
public class LED_DeerHuntingType {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03", required = true)
    @XmlSchemaType(name = "token")
    protected LED_HuntingMethod huntingType;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
    protected String huntingTypeDescription;

    /**
     * Gets the value of the huntingType property.
     * 
     * @return
     *     possible object is
     *     {@link LED_HuntingMethod }
     *     
     */
    public LED_HuntingMethod getHuntingType() {
        return huntingType;
    }

    /**
     * Sets the value of the huntingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LED_HuntingMethod }
     *     
     */
    public void setHuntingType(LED_HuntingMethod value) {
        this.huntingType = value;
    }

    /**
     * Gets the value of the huntingTypeDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHuntingTypeDescription() {
        return huntingTypeDescription;
    }

    /**
     * Sets the value of the huntingTypeDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingTypeDescription(String value) {
        this.huntingTypeDescription = value;
    }

}
