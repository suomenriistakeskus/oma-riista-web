
package fi.riista.integration.common.export.observations;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for observationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="observationType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="NAKO"/&gt;
 *     &lt;enumeration value="JALKI"/&gt;
 *     &lt;enumeration value="ULOSTE"/&gt;
 *     &lt;enumeration value="AANI"/&gt;
 *     &lt;enumeration value="RIISTAKAMERA"/&gt;
 *     &lt;enumeration value="KOIRAN_RIISTATYO"/&gt;
 *     &lt;enumeration value="MAASTOLASKENTA"/&gt;
 *     &lt;enumeration value="KOLMIOLASKENTA"/&gt;
 *     &lt;enumeration value="LENTOLASKENTA"/&gt;
 *     &lt;enumeration value="HAASKA"/&gt;
 *     &lt;enumeration value="SYONNOS"/&gt;
 *     &lt;enumeration value="KELOMISPUU"/&gt;
 *     &lt;enumeration value="KIIMAKUOPPA"/&gt;
 *     &lt;enumeration value="MAKUUPAIKKA"/&gt;
 *     &lt;enumeration value="PATO"/&gt;
 *     &lt;enumeration value="PESA"/&gt;
 *     &lt;enumeration value="PESA_KEKO"/&gt;
 *     &lt;enumeration value="PESA_PENKKA"/&gt;
 *     &lt;enumeration value="PESA_SEKA"/&gt;
 *     &lt;enumeration value="SOIDIN"/&gt;
 *     &lt;enumeration value="LUOLASTO"/&gt;
 *     &lt;enumeration value="PESIMALUOTO"/&gt;
 *     &lt;enumeration value="LEPAILYLUOTO"/&gt;
 *     &lt;enumeration value="PESIMASUO"/&gt;
 *     &lt;enumeration value="MUUTON_AIKAINEN_LEPAILYALUE"/&gt;
 *     &lt;enumeration value="RIISTANKULKUPAIKKA"/&gt;
 *     &lt;enumeration value="POIKUEYMPARISTO"/&gt;
 *     &lt;enumeration value="VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA"/&gt;
 *     &lt;enumeration value="KUUSISEKOTTEINEN_METSA"/&gt;
 *     &lt;enumeration value="VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA"/&gt;
 *     &lt;enumeration value="VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA"/&gt;
 *     &lt;enumeration value="SUON_REUNAMETSA"/&gt;
 *     &lt;enumeration value="HAKOMAMANTY"/&gt;
 *     &lt;enumeration value="RUOKAILUKOIVIKKO"/&gt;
 *     &lt;enumeration value="LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA"/&gt;
 *     &lt;enumeration value="RUOKAILUPAJUKKO_TAI_KOIVIKKO"/&gt;
 *     &lt;enumeration value="MUU"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "observationType")
@XmlEnum
public enum COBS_ObservationType {

    NAKO,
    JALKI,
    ULOSTE,
    AANI,
    RIISTAKAMERA,
    KOIRAN_RIISTATYO,
    MAASTOLASKENTA,
    KOLMIOLASKENTA,
    LENTOLASKENTA,
    HAASKA,
    SYONNOS,
    KELOMISPUU,
    KIIMAKUOPPA,
    MAKUUPAIKKA,
    PATO,
    PESA,
    PESA_KEKO,
    PESA_PENKKA,
    PESA_SEKA,
    SOIDIN,
    LUOLASTO,
    PESIMALUOTO,
    LEPAILYLUOTO,
    PESIMASUO,
    MUUTON_AIKAINEN_LEPAILYALUE,
    RIISTANKULKUPAIKKA,
    POIKUEYMPARISTO,
    VAIHTELEVARAKENTEINEN_MUSTIKKAMETSA,
    KUUSISEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_MANTYSEKOTTEINEN_METSA,
    VAIHTELEVARAKENTEINEN_LEHTIPUUSEKOTTEINEN_METSA,
    SUON_REUNAMETSA,
    HAKOMAMANTY,
    RUOKAILUKOIVIKKO,
    LEPPAKUUSIMETSA_TAI_KOIVUKUUSIMETSA,
    RUOKAILUPAJUKKO_TAI_KOIVIKKO,
    MUU;

    public String value() {
        return name();
    }

    public static COBS_ObservationType fromValue(String v) {
        return valueOf(v);
    }

}
