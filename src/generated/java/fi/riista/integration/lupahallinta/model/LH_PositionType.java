
package fi.riista.integration.lupahallinta.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TehtavaTyyppi.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TehtavaTyyppi"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="TOIMINNANOHJAAJA"/&gt;
 *     &lt;enumeration value="SRVA_YHTEYSHENKILO"/&gt;
 *     &lt;enumeration value="PETOYHDYSHENKILO"/&gt;
 *     &lt;enumeration value="METSASTYKSENVALVOJA"/&gt;
 *     &lt;enumeration value="METSASTAJATUTKINNON_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="AMPUMAKOKEEN_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA"/&gt;
 *     &lt;enumeration value="METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA"/&gt;
 *     &lt;enumeration value="PUHEENJOHTAJA"/&gt;
 *     &lt;enumeration value="VARAPUHEENJOHTAJA"/&gt;
 *     &lt;enumeration value="HALLITUKSEN_JASEN"/&gt;
 *     &lt;enumeration value="HALLITUKSEN_VARAJASEN"/&gt;
 *     &lt;enumeration value="JALJESTYSKOIRAN_OHJAAJA_HIRVI"/&gt;
 *     &lt;enumeration value="JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET"/&gt;
 *     &lt;enumeration value="JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "TehtavaTyyppi")
@XmlEnum
public enum LH_PositionType {

    TOIMINNANOHJAAJA("TOIMINNANOHJAAJA"),
    @XmlEnumValue("SRVA_YHTEYSHENKILO")
    SRVA___YHTEYSHENKILO("SRVA_YHTEYSHENKILO"),
    PETOYHDYSHENKILO("PETOYHDYSHENKILO"),
    METSASTYKSENVALVOJA("METSASTYKSENVALVOJA"),
    @XmlEnumValue("METSASTAJATUTKINNON_VASTAANOTTAJA")
    METSASTAJATUTKINNON___VASTAANOTTAJA("METSASTAJATUTKINNON_VASTAANOTTAJA"),
    @XmlEnumValue("AMPUMAKOKEEN_VASTAANOTTAJA")
    AMPUMAKOKEEN___VASTAANOTTAJA("AMPUMAKOKEEN_VASTAANOTTAJA"),
    @XmlEnumValue("RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA")
    RHYN___EDUSTAJA___RIISTAVAHINKOJEN___MAASTOKATSELMUKSESSA("RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA"),
    @XmlEnumValue("METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA")
    METSASTAJATUTKINTOON___VALMENTAVAN___KOULUTUKSEN___KOULUTTAJA("METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA"),
    PUHEENJOHTAJA("PUHEENJOHTAJA"),
    VARAPUHEENJOHTAJA("VARAPUHEENJOHTAJA"),
    @XmlEnumValue("HALLITUKSEN_JASEN")
    HALLITUKSEN___JASEN("HALLITUKSEN_JASEN"),
    @XmlEnumValue("HALLITUKSEN_VARAJASEN")
    HALLITUKSEN___VARAJASEN("HALLITUKSEN_VARAJASEN"),
    @XmlEnumValue("JALJESTYSKOIRAN_OHJAAJA_HIRVI")
    JALJESTYSKOIRAN___OHJAAJA___HIRVI("JALJESTYSKOIRAN_OHJAAJA_HIRVI"),
    @XmlEnumValue("JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET")
    JALJESTYSKOIRAN___OHJAAJA___PIENET___HIRVIELAIMET("JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET"),
    @XmlEnumValue("JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT")
    JALJESTYSKOIRAN___OHJAAJA___SUURPEDOT("JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT");
    private final String value;

    LH_PositionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LH_PositionType fromValue(String v) {
        for (LH_PositionType c: LH_PositionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
