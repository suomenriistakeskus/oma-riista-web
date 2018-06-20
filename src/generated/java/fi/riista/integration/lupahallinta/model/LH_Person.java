
package fi.riista.integration.lupahallinta.model;

import java.util.ArrayList;
import java.util.List;
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


/**
 * <p>Java class for Henkilo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Henkilo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="Henkilotunnus" type="{http://xml.riistakeskus.fi/schema/CommonTypes}Henkilotunnus"/&gt;
 *         &lt;element name="Etunimet" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Kutsumanimi" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Sukunimi" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Osoite" type="{http://xml.riistakeskus.fi/schema/CommonTypes}Osoite"/&gt;
 *         &lt;element name="Puhelinnumero" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Sahkopostiosoite" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Kielikoodi" type="{http://xml.riistakeskus.fi/schema/CommonTypes}Kielikoodi" minOccurs="0"/&gt;
 *         &lt;element name="LupaHallintaId" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="Tehtavat" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Tehtava" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Tehtava" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="xmlId" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="rhyJasenyys" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Henkilo", propOrder = {
    "id",
    "henkilotunnus",
    "etunimet",
    "kutsumanimi",
    "sukunimi",
    "osoite",
    "puhelinnumero",
    "sahkopostiosoite",
    "kielikoodi",
    "lupaHallintaId",
    "tehtavat"
})
public class LH_Person {

    @XmlElement(name = "Id")
    protected long id;
    @XmlElement(name = "Henkilotunnus", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String henkilotunnus;
    @XmlElement(name = "Etunimet", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String etunimet;
    @XmlElement(name = "Kutsumanimi", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String kutsumanimi;
    @XmlElement(name = "Sukunimi", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sukunimi;
    @XmlElement(name = "Osoite", required = true, nillable = true)
    protected C_Address osoite;
    @XmlElement(name = "Puhelinnumero", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String puhelinnumero;
    @XmlElement(name = "Sahkopostiosoite", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sahkopostiosoite;
    @XmlElement(name = "Kielikoodi")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String kielikoodi;
    @XmlElement(name = "LupaHallintaId", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String lupaHallintaId;
    @XmlElement(name = "Tehtavat")
    protected LH_Person.LH_Positions tehtavat;
    @XmlAttribute(name = "xmlId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String xmlId;
    @XmlAttribute(name = "rhyJasenyys")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected LH_Organisation rhyJasenyys;

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

    /**
     * Gets the value of the kutsumanimi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKutsumanimi() {
        return kutsumanimi;
    }

    /**
     * Sets the value of the kutsumanimi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKutsumanimi(String value) {
        this.kutsumanimi = value;
    }

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
     * Gets the value of the tehtavat property.
     * 
     * @return
     *     possible object is
     *     {@link LH_Person.LH_Positions }
     *     
     */
    public LH_Person.LH_Positions getTehtavat() {
        return tehtavat;
    }

    /**
     * Sets the value of the tehtavat property.
     * 
     * @param value
     *     allowed object is
     *     {@link LH_Person.LH_Positions }
     *     
     */
    public void setTehtavat(LH_Person.LH_Positions value) {
        this.tehtavat = value;
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
     * Gets the value of the rhyJasenyys property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public LH_Organisation getRhyJasenyys() {
        return rhyJasenyys;
    }

    /**
     * Sets the value of the rhyJasenyys property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRhyJasenyys(LH_Organisation value) {
        this.rhyJasenyys = value;
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
     *         &lt;element name="Tehtava" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Tehtava" maxOccurs="unbounded"/&gt;
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
        "tehtava"
    })
    public static class LH_Positions {

        @XmlElement(name = "Tehtava", required = true)
        protected List<LH_Position> tehtava;

        /**
         * Gets the value of the tehtava property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the tehtava property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTehtava().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LH_Position }
         * 
         * 
         */
        public List<LH_Position> getTehtava() {
            if (tehtava == null) {
                tehtava = new ArrayList<LH_Position>();
            }
            return this.tehtava;
        }

        public void setTehtava(List<LH_Position> value) {
            this.tehtava = value;
        }

    }

}
