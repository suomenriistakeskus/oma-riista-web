
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SoSoNimi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Kayttajatunnus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Salasana" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Loppukayttaja" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Laskutustiedot" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Henkilotunnus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="SahkoinenAsiointitunnus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="VarmenteenMyontaja" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="X509Certificate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="VarmenteenVoimassaolotarkistus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="VarmenteenSulkulistatarkistus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Tunnistusportaali" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Vara1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "soSoNimi",
    "kayttajatunnus",
    "salasana",
    "loppukayttaja",
    "laskutustiedot",
    "henkilotunnus",
    "sahkoinenAsiointitunnus",
    "varmenteenMyontaja",
    "x509Certificate",
    "varmenteenVoimassaolotarkistus",
    "varmenteenSulkulistatarkistus",
    "tunnistusportaali",
    "vara1"
})
@XmlRootElement(name = "TeeHenkilonTunnusKysely")
public class TeeHenkilonTunnusKysely {

    @XmlElement(name = "SoSoNimi")
    protected String soSoNimi;
    @XmlElement(name = "Kayttajatunnus")
    protected String kayttajatunnus;
    @XmlElement(name = "Salasana")
    protected String salasana;
    @XmlElement(name = "Loppukayttaja")
    protected String loppukayttaja;
    @XmlElement(name = "Laskutustiedot")
    protected String laskutustiedot;
    @XmlElement(name = "Henkilotunnus")
    protected String henkilotunnus;
    @XmlElement(name = "SahkoinenAsiointitunnus")
    protected String sahkoinenAsiointitunnus;
    @XmlElement(name = "VarmenteenMyontaja")
    protected String varmenteenMyontaja;
    @XmlElement(name = "X509Certificate")
    protected String x509Certificate;
    @XmlElement(name = "VarmenteenVoimassaolotarkistus")
    protected String varmenteenVoimassaolotarkistus;
    @XmlElement(name = "VarmenteenSulkulistatarkistus")
    protected String varmenteenSulkulistatarkistus;
    @XmlElement(name = "Tunnistusportaali")
    protected String tunnistusportaali;
    @XmlElement(name = "Vara1")
    protected String vara1;

    /**
     * Gets the value of the soSoNimi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoSoNimi() {
        return soSoNimi;
    }

    /**
     * Sets the value of the soSoNimi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoSoNimi(String value) {
        this.soSoNimi = value;
    }

    /**
     * Gets the value of the kayttajatunnus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKayttajatunnus() {
        return kayttajatunnus;
    }

    /**
     * Sets the value of the kayttajatunnus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKayttajatunnus(String value) {
        this.kayttajatunnus = value;
    }

    /**
     * Gets the value of the salasana property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSalasana() {
        return salasana;
    }

    /**
     * Sets the value of the salasana property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSalasana(String value) {
        this.salasana = value;
    }

    /**
     * Gets the value of the loppukayttaja property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoppukayttaja() {
        return loppukayttaja;
    }

    /**
     * Sets the value of the loppukayttaja property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoppukayttaja(String value) {
        this.loppukayttaja = value;
    }

    /**
     * Gets the value of the laskutustiedot property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLaskutustiedot() {
        return laskutustiedot;
    }

    /**
     * Sets the value of the laskutustiedot property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaskutustiedot(String value) {
        this.laskutustiedot = value;
    }

    /**
     * Gets the value of the henkilotunnus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHenkilotunnus() {
        return henkilotunnus;
    }

    /**
     * Sets the value of the henkilotunnus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHenkilotunnus(String value) {
        this.henkilotunnus = value;
    }

    /**
     * Gets the value of the sahkoinenAsiointitunnus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSahkoinenAsiointitunnus() {
        return sahkoinenAsiointitunnus;
    }

    /**
     * Sets the value of the sahkoinenAsiointitunnus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSahkoinenAsiointitunnus(String value) {
        this.sahkoinenAsiointitunnus = value;
    }

    /**
     * Gets the value of the varmenteenMyontaja property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarmenteenMyontaja() {
        return varmenteenMyontaja;
    }

    /**
     * Sets the value of the varmenteenMyontaja property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarmenteenMyontaja(String value) {
        this.varmenteenMyontaja = value;
    }

    /**
     * Gets the value of the x509Certificate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getX509Certificate() {
        return x509Certificate;
    }

    /**
     * Sets the value of the x509Certificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setX509Certificate(String value) {
        this.x509Certificate = value;
    }

    /**
     * Gets the value of the varmenteenVoimassaolotarkistus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarmenteenVoimassaolotarkistus() {
        return varmenteenVoimassaolotarkistus;
    }

    /**
     * Sets the value of the varmenteenVoimassaolotarkistus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarmenteenVoimassaolotarkistus(String value) {
        this.varmenteenVoimassaolotarkistus = value;
    }

    /**
     * Gets the value of the varmenteenSulkulistatarkistus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarmenteenSulkulistatarkistus() {
        return varmenteenSulkulistatarkistus;
    }

    /**
     * Sets the value of the varmenteenSulkulistatarkistus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarmenteenSulkulistatarkistus(String value) {
        this.varmenteenSulkulistatarkistus = value;
    }

    /**
     * Gets the value of the tunnistusportaali property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTunnistusportaali() {
        return tunnistusportaali;
    }

    /**
     * Sets the value of the tunnistusportaali property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTunnistusportaali(String value) {
        this.tunnistusportaali = value;
    }

    /**
     * Gets the value of the vara1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVara1() {
        return vara1;
    }

    /**
     * Sets the value of the vara1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVara1(String value) {
        this.vara1 = value;
    }

}
