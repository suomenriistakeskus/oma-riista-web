
package fi.riista.integration.lupahallinta.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.DateTimeAdapter;
import org.joda.time.DateTime;


/**
 * <p>Java class for Riistakeskus element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Riistakeskus"&gt;
 *   &lt;complexType&gt;
 *     &lt;complexContent&gt;
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *         &lt;sequence&gt;
 *           &lt;element name="Versio" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *           &lt;element name="Aikaleima" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *           &lt;element name="Organisaatiot"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="Organisaatio" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Organisaatio" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Henkilot"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name="Henkilo" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Henkilo" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/sequence&gt;
 *       &lt;/restriction&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * &lt;/element&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "versio",
    "aikaleima",
    "organisaatiot",
    "henkilot"
})
@XmlRootElement(name = "Riistakeskus")
public class LH_Export {

    @XmlElement(name = "Versio", required = true, nillable = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String versio;
    @XmlElement(name = "Aikaleima", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime aikaleima;
    @XmlElement(name = "Organisaatiot", required = true)
    protected LH_Export.LH_Organisations organisaatiot;
    @XmlElement(name = "Henkilot", required = true)
    protected LH_Export.LH_Persons henkilot;

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
     * Gets the value of the aikaleima property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DateTime getAikaleima() {
        return aikaleima;
    }

    /**
     * Sets the value of the aikaleima property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAikaleima(DateTime value) {
        this.aikaleima = value;
    }

    /**
     * Gets the value of the organisaatiot property.
     * 
     * @return
     *     possible object is
     *     {@link LH_Export.LH_Organisations }
     *     
     */
    public LH_Export.LH_Organisations getOrganisaatiot() {
        return organisaatiot;
    }

    /**
     * Sets the value of the organisaatiot property.
     * 
     * @param value
     *     allowed object is
     *     {@link LH_Export.LH_Organisations }
     *     
     */
    public void setOrganisaatiot(LH_Export.LH_Organisations value) {
        this.organisaatiot = value;
    }

    /**
     * Gets the value of the henkilot property.
     * 
     * @return
     *     possible object is
     *     {@link LH_Export.LH_Persons }
     *     
     */
    public LH_Export.LH_Persons getHenkilot() {
        return henkilot;
    }

    /**
     * Sets the value of the henkilot property.
     * 
     * @param value
     *     allowed object is
     *     {@link LH_Export.LH_Persons }
     *     
     */
    public void setHenkilot(LH_Export.LH_Persons value) {
        this.henkilot = value;
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
     *         &lt;element name="Organisaatio" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Organisaatio" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "organisaatio"
    })
    public static class LH_Organisations {

        @XmlElement(name = "Organisaatio")
        protected List<LH_Organisation> organisaatio;

        /**
         * Gets the value of the organisaatio property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the organisaatio property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrganisaatio().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LH_Organisation }
         * 
         * 
         */
        public List<LH_Organisation> getOrganisaatio() {
            if (organisaatio == null) {
                organisaatio = new ArrayList<LH_Organisation>();
            }
            return this.organisaatio;
        }

        public void setOrganisaatio(List<LH_Organisation> value) {
            this.organisaatio = value;
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
     *         &lt;element name="Henkilo" type="{http://xml.riistakeskus.fi/schema/LupaHallintaExport}Henkilo" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "henkilo"
    })
    public static class LH_Persons {

        @XmlElement(name = "Henkilo")
        protected List<LH_Person> henkilo;

        /**
         * Gets the value of the henkilo property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the henkilo property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHenkilo().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LH_Person }
         * 
         * 
         */
        public List<LH_Person> getHenkilo() {
            if (henkilo == null) {
                henkilo = new ArrayList<LH_Person>();
            }
            return this.henkilo;
        }

        public void setHenkilo(List<LH_Person> value) {
            this.henkilo = value;
        }

    }

}
