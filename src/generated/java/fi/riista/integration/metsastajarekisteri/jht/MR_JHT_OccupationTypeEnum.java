
package fi.riista.integration.metsastajarekisteri.jht;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for occupationTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="occupationTypeEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="TOIMINNANOHJAAJA"/&gt;
 *     &lt;enumeration value="METSASTYKSENVALVOJA"/&gt;
 *     &lt;enumeration value="METSASTAJATUTKINNON_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="AMPUMAKOKEEN_VASTAANOTTAJA"/&gt;
 *     &lt;enumeration value="RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "occupationTypeEnum", namespace = "http://riista.fi/integration/mr/jht/2018/10")
@XmlEnum
public enum MR_JHT_OccupationTypeEnum {

    TOIMINNANOHJAAJA,
    METSASTYKSENVALVOJA,
    METSASTAJATUTKINNON_VASTAANOTTAJA,
    AMPUMAKOKEEN_VASTAANOTTAJA,
    RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;

    public String value() {
        return name();
    }

    public static MR_JHT_OccupationTypeEnum fromValue(String v) {
        return valueOf(v);
    }

}
