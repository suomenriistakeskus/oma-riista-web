
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EstimatedAppearance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EstimatedAppearance"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="trendOfPopulationGrowth" type="{http://riista.fi/integration/luke/export/mooselikeharvests/2016/07}trendOfPopulationGrowth"/&gt;
 *         &lt;element name="estimatedAmountOfSpecimens" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EstimatedAppearance", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", propOrder = {
    "trendOfPopulationGrowth",
    "estimatedAmountOfSpecimens"
})
@XmlSeeAlso({
    LEM_WildBoarEstimatedAppearance.class
})
public class LEM_EstimatedAppearance {

    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07", required = true)
    @XmlSchemaType(name = "token")
    protected LEM_TrendOfPopulationGrowth trendOfPopulationGrowth;
    @XmlElement(namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
    protected Integer estimatedAmountOfSpecimens;

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
     * Gets the value of the estimatedAmountOfSpecimens property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedAmountOfSpecimens() {
        return estimatedAmountOfSpecimens;
    }

    /**
     * Sets the value of the estimatedAmountOfSpecimens property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedAmountOfSpecimens(Integer value) {
        this.estimatedAmountOfSpecimens = value;
    }

}
