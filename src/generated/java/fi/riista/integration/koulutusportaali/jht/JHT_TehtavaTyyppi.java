
package fi.riista.integration.koulutusportaali.jht;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TehtavaTyyppi.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TehtavaTyyppi"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="METSASTYKSENVALVOJA"/&gt;
 *     &lt;enumeration value="AMPUMAKOKEEN_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="METSASTAJATUTKINNON_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "TehtavaTyyppi", namespace = "http://riista.fi/integration/koulutusportaali/jht/2016/06")
@XmlEnum
public enum JHT_TehtavaTyyppi {

    METSASTYKSENVALVOJA,
    AMPUMAKOKEEN_VASTAANOTTAJA,
    METSASTAJATUTKINNON_VASTAANOTTAJA,
    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;

    public String value() {
        return name();
    }

    public static JHT_TehtavaTyyppi fromValue(String v) {
        return valueOf(v);
    }

}
