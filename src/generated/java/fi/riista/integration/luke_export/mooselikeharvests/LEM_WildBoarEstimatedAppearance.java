
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WildBoarEstimatedAppearance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WildBoarEstimatedAppearance"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}EstimatedAppearance"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="estimatedAmountOfSowWithPiglets" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WildBoarEstimatedAppearance", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "estimatedAmountOfSowWithPiglets"
})
public class LEM_WildBoarEstimatedAppearance
    extends LEM_EstimatedAppearance
{

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer estimatedAmountOfSowWithPiglets;

    /**
     * Gets the value of the estimatedAmountOfSowWithPiglets property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedAmountOfSowWithPiglets() {
        return estimatedAmountOfSowWithPiglets;
    }

    /**
     * Sets the value of the estimatedAmountOfSowWithPiglets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedAmountOfSowWithPiglets(Integer value) {
        this.estimatedAmountOfSowWithPiglets = value;
    }

}
