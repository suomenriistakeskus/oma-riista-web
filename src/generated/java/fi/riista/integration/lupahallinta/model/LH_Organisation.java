
package fi.riista.integration.lupahallinta.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.common.model.C_Address;
import fi.riista.integration.common.model.C_TypeCodeAndName;


/**
 * <p>Java class for Organisaatio complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Organisaatio"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="Tyyppi" type="{http://xml.riistakeskus.fi/schema/CommonTypes}TyyppiKoodiJaNimi"/&gt;
 *         &lt;element name="NimiS" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="NimiR" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="RiistakeskusOrganisaatiokoodi" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="Osoite" type="{http://xml.riistakeskus.fi/schema/CommonTypes}Osoite" minOccurs="0"/&gt;
 *         &lt;element name="Puhelinnumero" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="Sahkopostiosoite" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="LupaHallintaId" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="GeoSijainti" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}GeoSijainti" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="xmlId" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="riistakeskusAlue" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Organisaatio", propOrder = {
    "id",
    "tyyppi",
    "nimiS",
    "nimiR",
    "riistakeskusOrganisaatiokoodi",
    "osoite",
    "puhelinnumero",
    "sahkopostiosoite",
    "lupaHallintaId",
    "geoSijainti"
})
public class LH_Organisation {

    @XmlElement(name = "Id")
    protected long id;
    @XmlElement(name = "Tyyppi", required = true)
    protected C_TypeCodeAndName tyyppi;
    @XmlElement(name = "NimiS", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nimiS;
    @XmlElement(name = "NimiR", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nimiR;
    @XmlElement(name = "RiistakeskusOrganisaatiokoodi")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String riistakeskusOrganisaatiokoodi;
    @XmlElement(name = "Osoite")
    protected C_Address osoite;
    @XmlElement(name = "Puhelinnumero")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String puhelinnumero;
    @XmlElement(name = "Sahkopostiosoite")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sahkopostiosoite;
    @XmlElement(name = "LupaHallintaId", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String lupaHallintaId;
    @XmlElement(name = "GeoSijainti")
    protected LH_GeoLocation geoSijainti;
    @XmlAttribute(name = "xmlId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String xmlId;
    @XmlAttribute(name = "riistakeskusAlue")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected LH_Organisation riistakeskusAlue;

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
     *     {@link C_TypeCodeAndName }
     *     
     */
    public C_TypeCodeAndName getTyyppi() {
        return tyyppi;
    }

    /**
     * Sets the value of the tyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link C_TypeCodeAndName }
     *     
     */
    public void setTyyppi(C_TypeCodeAndName value) {
        this.tyyppi = value;
    }

    /**
     * Gets the value of the nimiS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNimiS() {
        return nimiS;
    }

    /**
     * Sets the value of the nimiS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNimiS(String value) {
        this.nimiS = value;
    }

    /**
     * Gets the value of the nimiR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNimiR() {
        return nimiR;
    }

    /**
     * Sets the value of the nimiR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNimiR(String value) {
        this.nimiR = value;
    }

    /**
     * Gets the value of the riistakeskusOrganisaatiokoodi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRiistakeskusOrganisaatiokoodi() {
        return riistakeskusOrganisaatiokoodi;
    }

    /**
     * Sets the value of the riistakeskusOrganisaatiokoodi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRiistakeskusOrganisaatiokoodi(String value) {
        this.riistakeskusOrganisaatiokoodi = value;
    }

    /**
     * Gets the value of the osoite property.
     * 
     * @return
     *     possible object is
     *     {@link C_Address }
     *     
     */
    public C_Address getOsoite() {
        return osoite;
    }

    /**
     * Sets the value of the osoite property.
     * 
     * @param value
     *     allowed object is
     *     {@link C_Address }
     *     
     */
    public void setOsoite(C_Address value) {
        this.osoite = value;
    }

    /**
     * Gets the value of the puhelinnumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPuhelinnumero() {
        return puhelinnumero;
    }

    /**
     * Sets the value of the puhelinnumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPuhelinnumero(String value) {
        this.puhelinnumero = value;
    }

    /**
     * Gets the value of the sahkopostiosoite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSahkopostiosoite() {
        return sahkopostiosoite;
    }

    /**
     * Sets the value of the sahkopostiosoite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSahkopostiosoite(String value) {
        this.sahkopostiosoite = value;
    }

    /**
     * Gets the value of the lupaHallintaId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLupaHallintaId() {
        return lupaHallintaId;
    }

    /**
     * Sets the value of the lupaHallintaId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLupaHallintaId(String value) {
        this.lupaHallintaId = value;
    }

    /**
     * Gets the value of the geoSijainti property.
     * 
     * @return
     *     possible object is
     *     {@link LH_GeoLocation }
     *     
     */
    public LH_GeoLocation getGeoSijainti() {
        return geoSijainti;
    }

    /**
     * Sets the value of the geoSijainti property.
     * 
     * @param value
     *     allowed object is
     *     {@link LH_GeoLocation }
     *     
     */
    public void setGeoSijainti(LH_GeoLocation value) {
        this.geoSijainti = value;
    }

    /**
     * Gets the value of the xmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlId() {
        return xmlId;
    }

    /**
     * Sets the value of the xmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlId(String value) {
        this.xmlId = value;
    }

    /**
     * Gets the value of the riistakeskusAlue property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public LH_Organisation getRiistakeskusAlue() {
        return riistakeskusAlue;
    }

    /**
     * Sets the value of the riistakeskusAlue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRiistakeskusAlue(LH_Organisation value) {
        this.riistakeskusAlue = value;
    }

}
