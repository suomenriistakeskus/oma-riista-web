
package fi.riista.integration.lupahallinta.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;


/**
 * <p>Java class for Tehtava complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Tehtava"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="Tyyppi" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}TehtavaTyyppi"/&gt;
 *         &lt;element name="AlkuPvm" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="LoppuPvm" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="Soittojarjestys" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/&gt;
 *         &lt;element name="Suoritusvuosi" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/&gt;
 *         &lt;element name="Lisatieto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="organisaatio" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Tehtava", propOrder = {
    "id",
    "tyyppi",
    "alkuPvm",
    "loppuPvm",
    "soittojarjestys",
    "suoritusvuosi",
    "lisatieto"
})
public class LH_Position {

    @XmlElement(name = "Id")
    protected long id;
    @XmlElement(name = "Tyyppi", required = true)
    @XmlSchemaType(name = "token")
    protected LH_PositionType tyyppi;
    @XmlElement(name = "AlkuPvm", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate alkuPvm;
    @XmlElement(name = "LoppuPvm", type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate loppuPvm;
    @XmlElement(name = "Soittojarjestys")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer soittojarjestys;
    @XmlElement(name = "Suoritusvuosi")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer suoritusvuosi;
    @XmlElement(name = "Lisatieto")
    protected String lisatieto;
    @XmlAttribute(name = "organisaatio", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected LH_Organisation organisaatio;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the tyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link LH_PositionType }
     *     
     */
    public LH_PositionType getTyyppi() {
        return tyyppi;
    }

    /**
     * Sets the value of the tyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link LH_PositionType }
     *     
     */
    public void setTyyppi(LH_PositionType value) {
        this.tyyppi = value;
    }

    /**
     * Gets the value of the alkuPvm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * Sets the value of the alkuPvm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlkuPvm(LocalDate value) {
        this.alkuPvm = value;
    }

    /**
     * Gets the value of the loppuPvm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getLoppuPvm() {
        return loppuPvm;
    }

    /**
     * Sets the value of the loppuPvm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoppuPvm(LocalDate value) {
        this.loppuPvm = value;
    }

    /**
     * Gets the value of the soittojarjestys property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSoittojarjestys() {
        return soittojarjestys;
    }

    /**
     * Sets the value of the soittojarjestys property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSoittojarjestys(Integer value) {
        this.soittojarjestys = value;
    }

    /**
     * Gets the value of the suoritusvuosi property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSuoritusvuosi() {
        return suoritusvuosi;
    }

    /**
     * Sets the value of the suoritusvuosi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSuoritusvuosi(Integer value) {
        this.suoritusvuosi = value;
    }

    /**
     * Gets the value of the lisatieto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLisatieto() {
        return lisatieto;
    }

    /**
     * Sets the value of the lisatieto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLisatieto(String value) {
        this.lisatieto = value;
    }

    /**
     * Gets the value of the organisaatio property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public LH_Organisation getOrganisaatio() {
        return organisaatio;
    }

    /**
     * Sets the value of the organisaatio property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setOrganisaatio(LH_Organisation value) {
        this.organisaatio = value;
    }

}
