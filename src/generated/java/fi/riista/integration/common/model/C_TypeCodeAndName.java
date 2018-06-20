
package fi.riista.integration.common.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for TyyppiKoodiJaNimi complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TyyppiKoodiJaNimi"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TyyppiKoodi" type="{http://xml.riistakeskus.fi/schema/CommonTypes}TyyppiKoodi"/&gt;
 *         &lt;element name="TyyppiNimi" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TyyppiKoodiJaNimi", propOrder = {
    "tyyppiKoodi",
    "tyyppiNimi"
})
public class C_TypeCodeAndName {

    @XmlElement(name = "TyyppiKoodi")
    protected int tyyppiKoodi;
    @XmlElement(name = "TyyppiNimi", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String tyyppiNimi;

    /**
     * Gets the value of the tyyppiKoodi property.
     * 
     */
    public int getTyyppiKoodi() {
        return tyyppiKoodi;
    }

    /**
     * Sets the value of the tyyppiKoodi property.
     * 
     */
    public void setTyyppiKoodi(int value) {
        this.tyyppiKoodi = value;
    }

    /**
     * Gets the value of the tyyppiNimi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTyyppiNimi() {
        return tyyppiNimi;
    }

    /**
     * Sets the value of the tyyppiNimi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTyyppiNimi(String value) {
        this.tyyppiNimi = value;
    }

}
