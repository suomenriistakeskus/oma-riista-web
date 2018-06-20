
package fi.riista.integration.koulutusportaali.jht;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fi.riista.integration.support.LocalDateAdapter;
import org.joda.time.LocalDate;


/**
 * <p>Java class for Suoritus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Suoritus"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="hetu" type="{http://riista.fi/integration/koulutusportaali/jht/2016/06}HenkiloTunnus"/&gt;
 *           &lt;element name="metsastajaNumero" type="{http://riista.fi/integration/koulutusportaali/jht/2016/06}MetsastajaNumero"/&gt;
 *           &lt;element name="omaRiistaPersonId" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="tehtavaTyyppi" type="{http://riista.fi/integration/koulutusportaali/jht/2016/06}TehtavaTyyppi"/&gt;
 *         &lt;element name="suoritusPvm" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Suoritus", namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06", propOrder = {
    "id",
    "hetu",
    "metsastajaNumero",
    "omaRiistaPersonId",
    "tehtavaTyyppi",
    "suoritusPvm"
})
public class JHT_Suoritus {

    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String id;
    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String hetu;
    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String metsastajaNumero;
    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
    protected Long omaRiistaPersonId;
    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06", required = true)
    @XmlSchemaType(name = "token")
    protected JHT_TehtavaTyyppi tehtavaTyyppi;
    @XmlElement(namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06", required = true, type = String.class)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate suoritusPvm;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the hetu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHetu() {
        return hetu;
    }

    /**
     * Sets the value of the hetu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHetu(String value) {
        this.hetu = value;
    }

    /**
     * Gets the value of the metsastajaNumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetsastajaNumero() {
        return metsastajaNumero;
    }

    /**
     * Sets the value of the metsastajaNumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetsastajaNumero(String value) {
        this.metsastajaNumero = value;
    }

    /**
     * Gets the value of the omaRiistaPersonId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOmaRiistaPersonId() {
        return omaRiistaPersonId;
    }

    /**
     * Sets the value of the omaRiistaPersonId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOmaRiistaPersonId(Long value) {
        this.omaRiistaPersonId = value;
    }

    /**
     * Gets the value of the tehtavaTyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link JHT_TehtavaTyyppi }
     *     
     */
    public JHT_TehtavaTyyppi getTehtavaTyyppi() {
        return tehtavaTyyppi;
    }

    /**
     * Sets the value of the tehtavaTyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link JHT_TehtavaTyyppi }
     *     
     */
    public void setTehtavaTyyppi(JHT_TehtavaTyyppi value) {
        this.tehtavaTyyppi = value;
    }

    /**
     * Gets the value of the suoritusPvm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getSuoritusPvm() {
        return suoritusPvm;
    }

    /**
     * Sets the value of the suoritusPvm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuoritusPvm(LocalDate value) {
        this.suoritusPvm = value;
    }

}
