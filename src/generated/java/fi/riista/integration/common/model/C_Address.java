
package fi.riista.integration.common.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for Osoite complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Osoite"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Katuosoite" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Postinumero" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="Postitoimipaikka" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="Maa" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="OsoiteLahde" type="{http://xml.riistakeskus.fi/schema/CommonTypes}OsoiteLahde" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Osoite", propOrder = {
    "katuosoite",
    "postinumero",
    "postitoimipaikka",
    "maa"
})
public class C_Address {

    @XmlElement(name = "Katuosoite")
    protected String katuosoite;
    @XmlElement(name = "Postinumero")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String postinumero;
    @XmlElement(name = "Postitoimipaikka")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String postitoimipaikka;
    @XmlElement(name = "Maa")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String maa;
    @XmlAttribute(name = "OsoiteLahde")
    protected C_AddressSource osoiteLahde;

    /**
     * Gets the value of the katuosoite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKatuosoite() {
        return katuosoite;
    }

    /**
     * Sets the value of the katuosoite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKatuosoite(String value) {
        this.katuosoite = value;
    }

    /**
     * Gets the value of the postinumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostinumero() {
        return postinumero;
    }

    /**
     * Sets the value of the postinumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostinumero(String value) {
        this.postinumero = value;
    }

    /**
     * Gets the value of the postitoimipaikka property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostitoimipaikka() {
        return postitoimipaikka;
    }

    /**
     * Sets the value of the postitoimipaikka property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostitoimipaikka(String value) {
        this.postitoimipaikka = value;
    }

    /**
     * Gets the value of the maa property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaa() {
        return maa;
    }

    /**
     * Sets the value of the maa property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaa(String value) {
        this.maa = value;
    }

    /**
     * Gets the value of the osoiteLahde property.
     * 
     * @return
     *     possible object is
     *     {@link C_AddressSource }
     *     
     */
    public C_AddressSource getOsoiteLahde() {
        return osoiteLahde;
    }

    /**
     * Sets the value of the osoiteLahde property.
     * 
     * @param value
     *     allowed object is
     *     {@link C_AddressSource }
     *     
     */
    public void setOsoiteLahde(C_AddressSource value) {
        this.osoiteLahde = value;
    }

}
