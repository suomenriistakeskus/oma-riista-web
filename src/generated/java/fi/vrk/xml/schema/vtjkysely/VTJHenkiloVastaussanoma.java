
package fi.vrk.xml.schema.vtjkysely;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


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
 *         &lt;element name="Asiakasinfo" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="InfoS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
 *                   &lt;element name="InfoR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
 *                   &lt;element name="InfoE" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Paluukoodi"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;PaluukoodiTekstiTyyppi"&gt;
 *                 &lt;attribute name="koodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaluukoodiTyyppi" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Hakuperusteet"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Henkilotunnus"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
 *                           &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="SahkoinenAsiointitunnus"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;SahkoinenAsiointitunnusTyyppi"&gt;
 *                           &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                           &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Henkilo" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Henkilotunnus"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
 *                           &lt;attribute name="voimassaolokoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}VoimassaolokoodiTyyppi" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="NykyinenSukunimi"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Sukunimi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="NykyisetEtunimet"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Etunimet" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="VakinainenKotimainenLahiosoite"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="LahiosoiteS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
 *                             &lt;element name="LahiosoiteR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
 *                             &lt;element name="Postinumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostinumeroTyyppi"/&gt;
 *                             &lt;element name="PostitoimipaikkaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
 *                             &lt;element name="PostitoimipaikkaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
 *                             &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                             &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="VakinainenUlkomainenLahiosoite"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="UlkomainenLahiosoite" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenLahiosoiteTyyppi"/&gt;
 *                             &lt;element name="UlkomainenPaikkakuntaJaValtioS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
 *                             &lt;element name="UlkomainenPaikkakuntaJaValtioR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
 *                             &lt;element name="UlkomainenPaikkakuntaJaValtioSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
 *                             &lt;element name="Valtiokoodi3" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Valtiokoodi3Tyyppi"/&gt;
 *                             &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                             &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Kotikunta"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Kuntanumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntanumeroTyyppi"/&gt;
 *                             &lt;element name="KuntaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
 *                             &lt;element name="KuntaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
 *                             &lt;element name="KuntasuhdeAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Kuolintiedot"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Kuolinpvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Aidinkieli"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Kielikoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KielikoodiTyyppi"/&gt;
 *                             &lt;element name="KieliS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
 *                             &lt;element name="KieliR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
 *                             &lt;element name="KieliSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="SuomenKansalaisuusTietokoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}SuomenKansalaisuusTietokoodiTyyppi"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="tietojenPoimintaaika" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}TietojenPoimintaaikaTyyppi" /&gt;
 *       &lt;attribute name="sanomatunnus" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}SanomatunnusTyyppi" /&gt;
 *       &lt;attribute name="versio" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}VersioTyyppi" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "asiakasinfo",
    "paluukoodi",
    "hakuperusteet",
    "henkilo"
})
@XmlRootElement(name = "VTJHenkiloVastaussanoma", namespace = "http://xml.vrk.fi/schema/vtjkysely")
public class VTJHenkiloVastaussanoma {

    @XmlElement(name = "Asiakasinfo", namespace = "http://xml.vrk.fi/schema/vtjkysely")
    protected VTJHenkiloVastaussanoma.Asiakasinfo asiakasinfo;
    @XmlElement(name = "Paluukoodi", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
    protected VTJHenkiloVastaussanoma.Paluukoodi paluukoodi;
    @XmlElement(name = "Hakuperusteet", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
    protected VTJHenkiloVastaussanoma.Hakuperusteet hakuperusteet;
    @XmlElement(name = "Henkilo", namespace = "http://xml.vrk.fi/schema/vtjkysely")
    protected VTJHenkiloVastaussanoma.Henkilo henkilo;
    @XmlAttribute(name = "tietojenPoimintaaika", required = true)
    protected String tietojenPoimintaaika;
    @XmlAttribute(name = "sanomatunnus", required = true)
    protected String sanomatunnus;
    @XmlAttribute(name = "versio", required = true)
    protected String versio;

    /**
     * Gets the value of the asiakasinfo property.
     * 
     * @return
     *     possible object is
     *     {@link VTJHenkiloVastaussanoma.Asiakasinfo }
     *     
     */
    public VTJHenkiloVastaussanoma.Asiakasinfo getAsiakasinfo() {
        return asiakasinfo;
    }

    /**
     * Sets the value of the asiakasinfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link VTJHenkiloVastaussanoma.Asiakasinfo }
     *     
     */
    public void setAsiakasinfo(VTJHenkiloVastaussanoma.Asiakasinfo value) {
        this.asiakasinfo = value;
    }

    /**
     * Gets the value of the paluukoodi property.
     * 
     * @return
     *     possible object is
     *     {@link VTJHenkiloVastaussanoma.Paluukoodi }
     *     
     */
    public VTJHenkiloVastaussanoma.Paluukoodi getPaluukoodi() {
        return paluukoodi;
    }

    /**
     * Sets the value of the paluukoodi property.
     * 
     * @param value
     *     allowed object is
     *     {@link VTJHenkiloVastaussanoma.Paluukoodi }
     *     
     */
    public void setPaluukoodi(VTJHenkiloVastaussanoma.Paluukoodi value) {
        this.paluukoodi = value;
    }

    /**
     * Gets the value of the hakuperusteet property.
     * 
     * @return
     *     possible object is
     *     {@link VTJHenkiloVastaussanoma.Hakuperusteet }
     *     
     */
    public VTJHenkiloVastaussanoma.Hakuperusteet getHakuperusteet() {
        return hakuperusteet;
    }

    /**
     * Sets the value of the hakuperusteet property.
     * 
     * @param value
     *     allowed object is
     *     {@link VTJHenkiloVastaussanoma.Hakuperusteet }
     *     
     */
    public void setHakuperusteet(VTJHenkiloVastaussanoma.Hakuperusteet value) {
        this.hakuperusteet = value;
    }

    /**
     * Gets the value of the henkilo property.
     * 
     * @return
     *     possible object is
     *     {@link VTJHenkiloVastaussanoma.Henkilo }
     *     
     */
    public VTJHenkiloVastaussanoma.Henkilo getHenkilo() {
        return henkilo;
    }

    /**
     * Sets the value of the henkilo property.
     * 
     * @param value
     *     allowed object is
     *     {@link VTJHenkiloVastaussanoma.Henkilo }
     *     
     */
    public void setHenkilo(VTJHenkiloVastaussanoma.Henkilo value) {
        this.henkilo = value;
    }

    /**
     * Gets the value of the tietojenPoimintaaika property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTietojenPoimintaaika() {
        return tietojenPoimintaaika;
    }

    /**
     * Sets the value of the tietojenPoimintaaika property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTietojenPoimintaaika(String value) {
        this.tietojenPoimintaaika = value;
    }

    /**
     * Gets the value of the sanomatunnus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSanomatunnus() {
        return sanomatunnus;
    }

    /**
     * Sets the value of the sanomatunnus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSanomatunnus(String value) {
        this.sanomatunnus = value;
    }

    /**
     * Gets the value of the versio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersio() {
        return versio;
    }

    /**
     * Sets the value of the versio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersio(String value) {
        this.versio = value;
    }


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
     *         &lt;element name="InfoS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
     *         &lt;element name="InfoR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
     *         &lt;element name="InfoE" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}AsiakasinfoTyyppi"/&gt;
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
        "infoS",
        "infoR",
        "infoE"
    })
    public static class Asiakasinfo {

        @XmlElement(name = "InfoS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected String infoS;
        @XmlElement(name = "InfoR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected String infoR;
        @XmlElement(name = "InfoE", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected String infoE;

        /**
         * Gets the value of the infoS property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInfoS() {
            return infoS;
        }

        /**
         * Sets the value of the infoS property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInfoS(String value) {
            this.infoS = value;
        }

        /**
         * Gets the value of the infoR property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInfoR() {
            return infoR;
        }

        /**
         * Sets the value of the infoR property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInfoR(String value) {
            this.infoR = value;
        }

        /**
         * Gets the value of the infoE property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInfoE() {
            return infoE;
        }

        /**
         * Sets the value of the infoE property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInfoE(String value) {
            this.infoE = value;
        }

    }


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
     *         &lt;element name="Henkilotunnus"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
     *                 &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="SahkoinenAsiointitunnus"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;SahkoinenAsiointitunnusTyyppi"&gt;
     *                 &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *                 &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
        "henkilotunnus",
        "sahkoinenAsiointitunnus"
    })
    public static class Hakuperusteet {

        @XmlElement(name = "Henkilotunnus", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Hakuperusteet.Henkilotunnus henkilotunnus;
        @XmlElement(name = "SahkoinenAsiointitunnus", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Hakuperusteet.SahkoinenAsiointitunnus sahkoinenAsiointitunnus;

        /**
         * Gets the value of the henkilotunnus property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Hakuperusteet.Henkilotunnus }
         *     
         */
        public VTJHenkiloVastaussanoma.Hakuperusteet.Henkilotunnus getHenkilotunnus() {
            return henkilotunnus;
        }

        /**
         * Sets the value of the henkilotunnus property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Hakuperusteet.Henkilotunnus }
         *     
         */
        public void setHenkilotunnus(VTJHenkiloVastaussanoma.Hakuperusteet.Henkilotunnus value) {
            this.henkilotunnus = value;
        }

        /**
         * Gets the value of the sahkoinenAsiointitunnus property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Hakuperusteet.SahkoinenAsiointitunnus }
         *     
         */
        public VTJHenkiloVastaussanoma.Hakuperusteet.SahkoinenAsiointitunnus getSahkoinenAsiointitunnus() {
            return sahkoinenAsiointitunnus;
        }

        /**
         * Sets the value of the sahkoinenAsiointitunnus property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Hakuperusteet.SahkoinenAsiointitunnus }
         *     
         */
        public void setSahkoinenAsiointitunnus(VTJHenkiloVastaussanoma.Hakuperusteet.SahkoinenAsiointitunnus value) {
            this.sahkoinenAsiointitunnus = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
         *       &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Henkilotunnus {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "hakuperustePaluukoodi", required = true)
            protected String hakuperustePaluukoodi;
            @XmlAttribute(name = "hakuperusteTekstiS", required = true)
            protected String hakuperusteTekstiS;
            @XmlAttribute(name = "hakuperusteTekstiR", required = true)
            protected String hakuperusteTekstiR;
            @XmlAttribute(name = "hakuperusteTekstiE", required = true)
            protected String hakuperusteTekstiE;

            /**
             * Muoto 11 merkkia, syntymaaika ppkkvv, syntymavuosisadan ilmaiseva valimerkki [- tai + tai A], yksilonumero (3 numeroa), tarkistusmerkki (ABCDEFHJKLMNPRSTUVWXY tai numero)= pp paiva arvo valilta 01-31, kk kuukausi arvo valilta 01-12, vv vuosi 2 numeroa eli 00-99, yhden kerran - tai + tai A, 3 numeroa, 1 iso kirjain joukosta ABCDEFHJKLMNPRSTUVWXY tai numero. Voi olla myos tyhja.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the hakuperustePaluukoodi property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperustePaluukoodi() {
                return hakuperustePaluukoodi;
            }

            /**
             * Sets the value of the hakuperustePaluukoodi property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperustePaluukoodi(String value) {
                this.hakuperustePaluukoodi = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiS() {
                return hakuperusteTekstiS;
            }

            /**
             * Sets the value of the hakuperusteTekstiS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiS(String value) {
                this.hakuperusteTekstiS = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiR() {
                return hakuperusteTekstiR;
            }

            /**
             * Sets the value of the hakuperusteTekstiR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiR(String value) {
                this.hakuperusteTekstiR = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiE property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiE() {
                return hakuperusteTekstiE;
            }

            /**
             * Sets the value of the hakuperusteTekstiE property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiE(String value) {
                this.hakuperusteTekstiE = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;SahkoinenAsiointitunnusTyyppi"&gt;
         *       &lt;attribute name="hakuperustePaluukoodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluukoodiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiS" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiR" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *       &lt;attribute name="hakuperusteTekstiE" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}HakuperustePaluuTekstiTyyppi" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class SahkoinenAsiointitunnus {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "hakuperustePaluukoodi", required = true)
            protected String hakuperustePaluukoodi;
            @XmlAttribute(name = "hakuperusteTekstiS", required = true)
            protected String hakuperusteTekstiS;
            @XmlAttribute(name = "hakuperusteTekstiR", required = true)
            protected String hakuperusteTekstiR;
            @XmlAttribute(name = "hakuperusteTekstiE", required = true)
            protected String hakuperusteTekstiE;

            /**
             * Sahkoinen asiointitunnus on muotoa NNNNNNNNT, jossa NNNNNNNN=juokseva numero valilta 10000001-89999999, ja T=tarkistusmerkki, joka muodostetaan samalla menetelmalla kuin henkilotunnuksen tarkistusmerkki. Muoto 8 numeroa ja 1 numero tai iso kirjain joukosta ABCDEFHJKLMNPRSTUVWXY. Voi olla myos tyhja.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the hakuperustePaluukoodi property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperustePaluukoodi() {
                return hakuperustePaluukoodi;
            }

            /**
             * Sets the value of the hakuperustePaluukoodi property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperustePaluukoodi(String value) {
                this.hakuperustePaluukoodi = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiS() {
                return hakuperusteTekstiS;
            }

            /**
             * Sets the value of the hakuperusteTekstiS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiS(String value) {
                this.hakuperusteTekstiS = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiR() {
                return hakuperusteTekstiR;
            }

            /**
             * Sets the value of the hakuperusteTekstiR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiR(String value) {
                this.hakuperusteTekstiR = value;
            }

            /**
             * Gets the value of the hakuperusteTekstiE property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHakuperusteTekstiE() {
                return hakuperusteTekstiE;
            }

            /**
             * Sets the value of the hakuperusteTekstiE property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHakuperusteTekstiE(String value) {
                this.hakuperusteTekstiE = value;
            }

        }

    }


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
     *         &lt;element name="Henkilotunnus"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
     *                 &lt;attribute name="voimassaolokoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}VoimassaolokoodiTyyppi" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="NykyinenSukunimi"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Sukunimi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="NykyisetEtunimet"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Etunimet" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="VakinainenKotimainenLahiosoite"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="LahiosoiteS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
     *                   &lt;element name="LahiosoiteR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
     *                   &lt;element name="Postinumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostinumeroTyyppi"/&gt;
     *                   &lt;element name="PostitoimipaikkaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
     *                   &lt;element name="PostitoimipaikkaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
     *                   &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                   &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="VakinainenUlkomainenLahiosoite"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="UlkomainenLahiosoite" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenLahiosoiteTyyppi"/&gt;
     *                   &lt;element name="UlkomainenPaikkakuntaJaValtioS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
     *                   &lt;element name="UlkomainenPaikkakuntaJaValtioR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
     *                   &lt;element name="UlkomainenPaikkakuntaJaValtioSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
     *                   &lt;element name="Valtiokoodi3" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Valtiokoodi3Tyyppi"/&gt;
     *                   &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                   &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Kotikunta"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Kuntanumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntanumeroTyyppi"/&gt;
     *                   &lt;element name="KuntaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
     *                   &lt;element name="KuntaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
     *                   &lt;element name="KuntasuhdeAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Kuolintiedot"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Kuolinpvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Aidinkieli"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Kielikoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KielikoodiTyyppi"/&gt;
     *                   &lt;element name="KieliS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
     *                   &lt;element name="KieliR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
     *                   &lt;element name="KieliSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="SuomenKansalaisuusTietokoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}SuomenKansalaisuusTietokoodiTyyppi"/&gt;
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
        "henkilotunnus",
        "nykyinenSukunimi",
        "nykyisetEtunimet",
        "vakinainenKotimainenLahiosoite",
        "vakinainenUlkomainenLahiosoite",
        "kotikunta",
        "kuolintiedot",
        "aidinkieli",
        "suomenKansalaisuusTietokoodi"
    })
    public static class Henkilo {

        @XmlElement(name = "Henkilotunnus", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.Henkilotunnus henkilotunnus;
        @XmlElement(name = "NykyinenSukunimi", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi nykyinenSukunimi;
        @XmlElement(name = "NykyisetEtunimet", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet nykyisetEtunimet;
        @XmlElement(name = "VakinainenKotimainenLahiosoite", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite vakinainenKotimainenLahiosoite;
        @XmlElement(name = "VakinainenUlkomainenLahiosoite", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite vakinainenUlkomainenLahiosoite;
        @XmlElement(name = "Kotikunta", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.Kotikunta kotikunta;
        @XmlElement(name = "Kuolintiedot", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.Kuolintiedot kuolintiedot;
        @XmlElement(name = "Aidinkieli", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected VTJHenkiloVastaussanoma.Henkilo.Aidinkieli aidinkieli;
        @XmlElement(name = "SuomenKansalaisuusTietokoodi", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
        protected String suomenKansalaisuusTietokoodi;

        /**
         * Gets the value of the henkilotunnus property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Henkilotunnus }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.Henkilotunnus getHenkilotunnus() {
            return henkilotunnus;
        }

        /**
         * Sets the value of the henkilotunnus property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Henkilotunnus }
         *     
         */
        public void setHenkilotunnus(VTJHenkiloVastaussanoma.Henkilo.Henkilotunnus value) {
            this.henkilotunnus = value;
        }

        /**
         * Gets the value of the nykyinenSukunimi property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi getNykyinenSukunimi() {
            return nykyinenSukunimi;
        }

        /**
         * Sets the value of the nykyinenSukunimi property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi }
         *     
         */
        public void setNykyinenSukunimi(VTJHenkiloVastaussanoma.Henkilo.NykyinenSukunimi value) {
            this.nykyinenSukunimi = value;
        }

        /**
         * Gets the value of the nykyisetEtunimet property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet getNykyisetEtunimet() {
            return nykyisetEtunimet;
        }

        /**
         * Sets the value of the nykyisetEtunimet property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet }
         *     
         */
        public void setNykyisetEtunimet(VTJHenkiloVastaussanoma.Henkilo.NykyisetEtunimet value) {
            this.nykyisetEtunimet = value;
        }

        /**
         * Gets the value of the vakinainenKotimainenLahiosoite property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite getVakinainenKotimainenLahiosoite() {
            return vakinainenKotimainenLahiosoite;
        }

        /**
         * Sets the value of the vakinainenKotimainenLahiosoite property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite }
         *     
         */
        public void setVakinainenKotimainenLahiosoite(VTJHenkiloVastaussanoma.Henkilo.VakinainenKotimainenLahiosoite value) {
            this.vakinainenKotimainenLahiosoite = value;
        }

        /**
         * Gets the value of the vakinainenUlkomainenLahiosoite property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite getVakinainenUlkomainenLahiosoite() {
            return vakinainenUlkomainenLahiosoite;
        }

        /**
         * Sets the value of the vakinainenUlkomainenLahiosoite property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite }
         *     
         */
        public void setVakinainenUlkomainenLahiosoite(VTJHenkiloVastaussanoma.Henkilo.VakinainenUlkomainenLahiosoite value) {
            this.vakinainenUlkomainenLahiosoite = value;
        }

        /**
         * Gets the value of the kotikunta property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Kotikunta }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.Kotikunta getKotikunta() {
            return kotikunta;
        }

        /**
         * Sets the value of the kotikunta property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Kotikunta }
         *     
         */
        public void setKotikunta(VTJHenkiloVastaussanoma.Henkilo.Kotikunta value) {
            this.kotikunta = value;
        }

        /**
         * Gets the value of the kuolintiedot property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Kuolintiedot }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.Kuolintiedot getKuolintiedot() {
            return kuolintiedot;
        }

        /**
         * Sets the value of the kuolintiedot property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Kuolintiedot }
         *     
         */
        public void setKuolintiedot(VTJHenkiloVastaussanoma.Henkilo.Kuolintiedot value) {
            this.kuolintiedot = value;
        }

        /**
         * Gets the value of the aidinkieli property.
         * 
         * @return
         *     possible object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Aidinkieli }
         *     
         */
        public VTJHenkiloVastaussanoma.Henkilo.Aidinkieli getAidinkieli() {
            return aidinkieli;
        }

        /**
         * Sets the value of the aidinkieli property.
         * 
         * @param value
         *     allowed object is
         *     {@link VTJHenkiloVastaussanoma.Henkilo.Aidinkieli }
         *     
         */
        public void setAidinkieli(VTJHenkiloVastaussanoma.Henkilo.Aidinkieli value) {
            this.aidinkieli = value;
        }

        /**
         * Gets the value of the suomenKansalaisuusTietokoodi property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSuomenKansalaisuusTietokoodi() {
            return suomenKansalaisuusTietokoodi;
        }

        /**
         * Sets the value of the suomenKansalaisuusTietokoodi property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSuomenKansalaisuusTietokoodi(String value) {
            this.suomenKansalaisuusTietokoodi = value;
        }


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
         *         &lt;element name="Kielikoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KielikoodiTyyppi"/&gt;
         *         &lt;element name="KieliS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
         *         &lt;element name="KieliR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
         *         &lt;element name="KieliSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KieliNimiTyyppi"/&gt;
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
            "kielikoodi",
            "kieliS",
            "kieliR",
            "kieliSelvakielinen"
        })
        public static class Aidinkieli {

            @XmlElement(name = "Kielikoodi", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kielikoodi;
            @XmlElement(name = "KieliS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kieliS;
            @XmlElement(name = "KieliR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kieliR;
            @XmlElement(name = "KieliSelvakielinen", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kieliSelvakielinen;

            /**
             * Gets the value of the kielikoodi property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKielikoodi() {
                return kielikoodi;
            }

            /**
             * Sets the value of the kielikoodi property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKielikoodi(String value) {
                this.kielikoodi = value;
            }

            /**
             * Gets the value of the kieliS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKieliS() {
                return kieliS;
            }

            /**
             * Sets the value of the kieliS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKieliS(String value) {
                this.kieliS = value;
            }

            /**
             * Gets the value of the kieliR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKieliR() {
                return kieliR;
            }

            /**
             * Sets the value of the kieliR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKieliR(String value) {
                this.kieliR = value;
            }

            /**
             * Gets the value of the kieliSelvakielinen property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKieliSelvakielinen() {
                return kieliSelvakielinen;
            }

            /**
             * Sets the value of the kieliSelvakielinen property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKieliSelvakielinen(String value) {
                this.kieliSelvakielinen = value;
            }

        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;HenkilotunnusTyyppi"&gt;
         *       &lt;attribute name="voimassaolokoodi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}VoimassaolokoodiTyyppi" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Henkilotunnus {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "voimassaolokoodi")
            protected String voimassaolokoodi;

            /**
             * Muoto 11 merkkia, syntymaaika ppkkvv, syntymavuosisadan ilmaiseva valimerkki [- tai + tai A], yksilonumero (3 numeroa), tarkistusmerkki (ABCDEFHJKLMNPRSTUVWXY tai numero)= pp paiva arvo valilta 01-31, kk kuukausi arvo valilta 01-12, vv vuosi 2 numeroa eli 00-99, yhden kerran - tai + tai A, 3 numeroa, 1 iso kirjain joukosta ABCDEFHJKLMNPRSTUVWXY tai numero. Voi olla myos tyhja.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the voimassaolokoodi property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getVoimassaolokoodi() {
                return voimassaolokoodi;
            }

            /**
             * Sets the value of the voimassaolokoodi property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setVoimassaolokoodi(String value) {
                this.voimassaolokoodi = value;
            }

        }


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
         *         &lt;element name="Kuntanumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntanumeroTyyppi"/&gt;
         *         &lt;element name="KuntaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
         *         &lt;element name="KuntaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KuntaNimiTyyppi"/&gt;
         *         &lt;element name="KuntasuhdeAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
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
            "kuntanumero",
            "kuntaS",
            "kuntaR",
            "kuntasuhdeAlkupvm"
        })
        public static class Kotikunta {

            @XmlElement(name = "Kuntanumero", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kuntanumero;
            @XmlElement(name = "KuntaS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kuntaS;
            @XmlElement(name = "KuntaR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kuntaR;
            @XmlElement(name = "KuntasuhdeAlkupvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kuntasuhdeAlkupvm;

            /**
             * Gets the value of the kuntanumero property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKuntanumero() {
                return kuntanumero;
            }

            /**
             * Sets the value of the kuntanumero property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKuntanumero(String value) {
                this.kuntanumero = value;
            }

            /**
             * Gets the value of the kuntaS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKuntaS() {
                return kuntaS;
            }

            /**
             * Sets the value of the kuntaS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKuntaS(String value) {
                this.kuntaS = value;
            }

            /**
             * Gets the value of the kuntaR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKuntaR() {
                return kuntaR;
            }

            /**
             * Sets the value of the kuntaR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKuntaR(String value) {
                this.kuntaR = value;
            }

            /**
             * Gets the value of the kuntasuhdeAlkupvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKuntasuhdeAlkupvm() {
                return kuntasuhdeAlkupvm;
            }

            /**
             * Sets the value of the kuntasuhdeAlkupvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKuntasuhdeAlkupvm(String value) {
                this.kuntasuhdeAlkupvm = value;
            }

        }


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
         *         &lt;element name="Kuolinpvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
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
            "kuolinpvm"
        })
        public static class Kuolintiedot {

            @XmlElement(name = "Kuolinpvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String kuolinpvm;

            /**
             * Gets the value of the kuolinpvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKuolinpvm() {
                return kuolinpvm;
            }

            /**
             * Sets the value of the kuolinpvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKuolinpvm(String value) {
                this.kuolinpvm = value;
            }

        }


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
         *         &lt;element name="Sukunimi" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
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
            "sukunimi"
        })
        public static class NykyinenSukunimi {

            @XmlElement(name = "Sukunimi", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String sukunimi;

            /**
             * Gets the value of the sukunimi property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSukunimi() {
                return sukunimi;
            }

            /**
             * Sets the value of the sukunimi property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSukunimi(String value) {
                this.sukunimi = value;
            }

        }


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
         *         &lt;element name="Etunimet" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Nimi100Tyyppi"/&gt;
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
            "etunimet"
        })
        public static class NykyisetEtunimet {

            @XmlElement(name = "Etunimet", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String etunimet;

            /**
             * Gets the value of the etunimet property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEtunimet() {
                return etunimet;
            }

            /**
             * Sets the value of the etunimet property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEtunimet(String value) {
                this.etunimet = value;
            }

        }


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
         *         &lt;element name="LahiosoiteS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
         *         &lt;element name="LahiosoiteR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}KotimainenLahiosoiteTyyppi"/&gt;
         *         &lt;element name="Postinumero" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostinumeroTyyppi"/&gt;
         *         &lt;element name="PostitoimipaikkaS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
         *         &lt;element name="PostitoimipaikkaR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PostitoimipaikkaTyyppi"/&gt;
         *         &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
         *         &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
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
            "lahiosoiteS",
            "lahiosoiteR",
            "postinumero",
            "postitoimipaikkaS",
            "postitoimipaikkaR",
            "asuminenAlkupvm",
            "asuminenLoppupvm"
        })
        public static class VakinainenKotimainenLahiosoite {

            @XmlElement(name = "LahiosoiteS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String lahiosoiteS;
            @XmlElement(name = "LahiosoiteR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String lahiosoiteR;
            @XmlElement(name = "Postinumero", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String postinumero;
            @XmlElement(name = "PostitoimipaikkaS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String postitoimipaikkaS;
            @XmlElement(name = "PostitoimipaikkaR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String postitoimipaikkaR;
            @XmlElement(name = "AsuminenAlkupvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String asuminenAlkupvm;
            @XmlElement(name = "AsuminenLoppupvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String asuminenLoppupvm;

            /**
             * Gets the value of the lahiosoiteS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLahiosoiteS() {
                return lahiosoiteS;
            }

            /**
             * Sets the value of the lahiosoiteS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLahiosoiteS(String value) {
                this.lahiosoiteS = value;
            }

            /**
             * Gets the value of the lahiosoiteR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLahiosoiteR() {
                return lahiosoiteR;
            }

            /**
             * Sets the value of the lahiosoiteR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLahiosoiteR(String value) {
                this.lahiosoiteR = value;
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
             * Gets the value of the postitoimipaikkaS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPostitoimipaikkaS() {
                return postitoimipaikkaS;
            }

            /**
             * Sets the value of the postitoimipaikkaS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPostitoimipaikkaS(String value) {
                this.postitoimipaikkaS = value;
            }

            /**
             * Gets the value of the postitoimipaikkaR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPostitoimipaikkaR() {
                return postitoimipaikkaR;
            }

            /**
             * Sets the value of the postitoimipaikkaR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPostitoimipaikkaR(String value) {
                this.postitoimipaikkaR = value;
            }

            /**
             * Gets the value of the asuminenAlkupvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAsuminenAlkupvm() {
                return asuminenAlkupvm;
            }

            /**
             * Sets the value of the asuminenAlkupvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAsuminenAlkupvm(String value) {
                this.asuminenAlkupvm = value;
            }

            /**
             * Gets the value of the asuminenLoppupvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAsuminenLoppupvm() {
                return asuminenLoppupvm;
            }

            /**
             * Sets the value of the asuminenLoppupvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAsuminenLoppupvm(String value) {
                this.asuminenLoppupvm = value;
            }

        }


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
         *         &lt;element name="UlkomainenLahiosoite" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenLahiosoiteTyyppi"/&gt;
         *         &lt;element name="UlkomainenPaikkakuntaJaValtioS" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
         *         &lt;element name="UlkomainenPaikkakuntaJaValtioR" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
         *         &lt;element name="UlkomainenPaikkakuntaJaValtioSelvakielinen" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}UlkomainenPaikkakuntaJaValtioTyyppi"/&gt;
         *         &lt;element name="Valtiokoodi3" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}Valtiokoodi3Tyyppi"/&gt;
         *         &lt;element name="AsuminenAlkupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
         *         &lt;element name="AsuminenLoppupvm" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaivamaaraTyyppi"/&gt;
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
            "ulkomainenLahiosoite",
            "ulkomainenPaikkakuntaJaValtioS",
            "ulkomainenPaikkakuntaJaValtioR",
            "ulkomainenPaikkakuntaJaValtioSelvakielinen",
            "valtiokoodi3",
            "asuminenAlkupvm",
            "asuminenLoppupvm"
        })
        public static class VakinainenUlkomainenLahiosoite {

            @XmlElement(name = "UlkomainenLahiosoite", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String ulkomainenLahiosoite;
            @XmlElement(name = "UlkomainenPaikkakuntaJaValtioS", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String ulkomainenPaikkakuntaJaValtioS;
            @XmlElement(name = "UlkomainenPaikkakuntaJaValtioR", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String ulkomainenPaikkakuntaJaValtioR;
            @XmlElement(name = "UlkomainenPaikkakuntaJaValtioSelvakielinen", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String ulkomainenPaikkakuntaJaValtioSelvakielinen;
            @XmlElement(name = "Valtiokoodi3", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String valtiokoodi3;
            @XmlElement(name = "AsuminenAlkupvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String asuminenAlkupvm;
            @XmlElement(name = "AsuminenLoppupvm", namespace = "http://xml.vrk.fi/schema/vtjkysely", required = true)
            protected String asuminenLoppupvm;

            /**
             * Gets the value of the ulkomainenLahiosoite property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUlkomainenLahiosoite() {
                return ulkomainenLahiosoite;
            }

            /**
             * Sets the value of the ulkomainenLahiosoite property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUlkomainenLahiosoite(String value) {
                this.ulkomainenLahiosoite = value;
            }

            /**
             * Gets the value of the ulkomainenPaikkakuntaJaValtioS property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUlkomainenPaikkakuntaJaValtioS() {
                return ulkomainenPaikkakuntaJaValtioS;
            }

            /**
             * Sets the value of the ulkomainenPaikkakuntaJaValtioS property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUlkomainenPaikkakuntaJaValtioS(String value) {
                this.ulkomainenPaikkakuntaJaValtioS = value;
            }

            /**
             * Gets the value of the ulkomainenPaikkakuntaJaValtioR property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUlkomainenPaikkakuntaJaValtioR() {
                return ulkomainenPaikkakuntaJaValtioR;
            }

            /**
             * Sets the value of the ulkomainenPaikkakuntaJaValtioR property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUlkomainenPaikkakuntaJaValtioR(String value) {
                this.ulkomainenPaikkakuntaJaValtioR = value;
            }

            /**
             * Gets the value of the ulkomainenPaikkakuntaJaValtioSelvakielinen property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUlkomainenPaikkakuntaJaValtioSelvakielinen() {
                return ulkomainenPaikkakuntaJaValtioSelvakielinen;
            }

            /**
             * Sets the value of the ulkomainenPaikkakuntaJaValtioSelvakielinen property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUlkomainenPaikkakuntaJaValtioSelvakielinen(String value) {
                this.ulkomainenPaikkakuntaJaValtioSelvakielinen = value;
            }

            /**
             * Gets the value of the valtiokoodi3 property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValtiokoodi3() {
                return valtiokoodi3;
            }

            /**
             * Sets the value of the valtiokoodi3 property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValtiokoodi3(String value) {
                this.valtiokoodi3 = value;
            }

            /**
             * Gets the value of the asuminenAlkupvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAsuminenAlkupvm() {
                return asuminenAlkupvm;
            }

            /**
             * Sets the value of the asuminenAlkupvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAsuminenAlkupvm(String value) {
                this.asuminenAlkupvm = value;
            }

            /**
             * Gets the value of the asuminenLoppupvm property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAsuminenLoppupvm() {
                return asuminenLoppupvm;
            }

            /**
             * Sets the value of the asuminenLoppupvm property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAsuminenLoppupvm(String value) {
                this.asuminenLoppupvm = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://xml.vrk.fi/schema/vtj/henkilotiedot/1&gt;PaluukoodiTekstiTyyppi"&gt;
     *       &lt;attribute name="koodi" use="required" type="{http://xml.vrk.fi/schema/vtj/henkilotiedot/1}PaluukoodiTyyppi" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Paluukoodi {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "koodi", required = true)
        protected String koodi;

        /**
         * Jarjestelman virhetilanteissa kaytettavaa paluukoodia vastaava teksti selvakielisena suomeksi. Muoto 0-200 merkkia. 
         *  0000 Haku onnistui
         *  0001 Hakuperusteella (henkilotunnus, sahkoinen asiointitunnus, .. ) ei loydy tietoja vtj:sta.	
         *  0002 Hakuperusteena kaytetty (tai sovelluksen sahkoisella asiointitunnuksella etsima) henkilotunnus on passivoitu.
         *  0003 Kysyvan sovelluksen lahettama tunnussana puuttuu, on virheellinen tai vanhentunut. 
         *  0004 Kysyvan sovelluksen lahettama kayttajatunnus puuttuu, on virheellinen tai vanhentunut.
         *  0005 ILMOITUS -tiedossa oleva selvakielinen teksti ilmoittaa virheen tai muun ilmoitettavan asian. Selvakielisia teksteja ovat:
         * Sovellus tulostaa seuraavat paluukoodilla 0005:
         * Seuranta ei onnistu / Uppfoljningen misslyckades
         * Sukunimi on pakollinen annettava / Slaktnamn ar obligatoriskt
         * Etunimet on pakollinen annettava / Fornamn ar obligatoriskt
         * Syntymaaika on pakollinen annettava / Fodelsetid ar obligatoriskt
         *  1. etunimi tuntematon nimihakemistolle / 1. fornamn okant for registret
         *  2. etunimi tuntematon nimihakemistolle / 2. fornamn okant for registret
         *  3. etunimi tuntematon nimihakemistolle / 3. fornamn okant for registret
         * Loytyi useampi kuin 1. / Hittades flera an 1.
         * Pakollinen hakuehto puuttuu. / Obligatoriskt sokkriteriet fattas.
         *  0006 Hakuperusteella (henkilotunnus, sahkoinen asiointitunnus, .. ) ei loydy tietoja vtj:sta.
         *  0007 Haettava henkilo on kuollut, joten tietoja ei voida palauttaa, ellei muuta ole sovittu.
         *  0008 Kyselysanomassa on pyynto varmenteen sulkulistatarkistuksesta (SULKULISTATARKISTUS="YES"), mutta sita ei toistaiseksi suoriteta.
         *  0009 Kyselysanomassa on pyynto varmenteen voimassaolotarkistuksesta (VOIMASSAOLOTARKISTUS="YES"), mutta sita ei toistaiseksi suoriteta.
         *  0010 Kysyvan sovelluksen on sovittu kayttavan tunnistukseen sahkoista asiointitunnusta (Finuid, Satu). Sahkoisen asiointitunnuksen sisaltava varmenne kuitenkin puuttuu.
         *  0011 Kysyvan sovelluksen on sovittu kayttavan tunnistukseen 'perinteista' kayttajatunnusta. Kayttajatunnusta ei kuitenkaan laheteta, vaan sen tilalla tule sahkoisen asiointitunnuksen (Finuid, Satu) sisaltama varmenne. 
         *  0012 Varmenteelle suoritetussa voimassaolotarkistuksessa on todettu varmenteen voimassaolon paattyneen.
         *  0013 Varmenteelle suoritetussa sulkulistatarkistuksessa on todettu varmenteen olevan sulkulistalla.
         *  0014 Varmenne ei ole varmenne ollenkaan tai se ei ole vrk:n hyvaksyma.
         *  0015 Varmenne ei ole vrk:n hyvaksyma.
         *  0016 Kysely- ja vastaussanomien vertailussa on todettu niiden tunnistetietojen eroavan. 
         *  0017 Hakuperusteella (henkilotunnus) ei loydy tietoja hollesta (Holhousasiain rekisteri).
         *  0018 Henkilo on alle 15-vuotias
         *  0019 Tietoja ei voida luovuttaa
         * -1500 Virheellinen tunnus/salasana pari
         * -1505 laskutustiedoissa jokin virhe
         * -1800 WebServicen sisainen virhe
         * -1805 Tunnistuksessa kaytettavan kyselysanoman validointivirhe, eli sanoma ei ole skeeman mukainen.
         * -1900 Tuotetta ei loydy
         * -1901 Tuotetta ei loydy
         * 
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the koodi property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKoodi() {
            return koodi;
        }

        /**
         * Sets the value of the koodi property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKoodi(String value) {
            this.koodi = value;
        }

    }

}
