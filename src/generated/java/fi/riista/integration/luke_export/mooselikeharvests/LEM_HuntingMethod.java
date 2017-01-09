
package fi.riista.integration.luke_export.mooselikeharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HuntingMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HuntingMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA"/&gt;
 *     &lt;enumeration value="HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE"/&gt;
 *     &lt;enumeration value="PASSILINJA_JA_TIIVIS_AJOKETJU"/&gt;
 *     &lt;enumeration value="PASSILINJA_JA_MIESAJO_JALJITYKSENA"/&gt;
 *     &lt;enumeration value="JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA"/&gt;
 *     &lt;enumeration value="VAIJYNTA_KULKUPAIKOILLA"/&gt;
 *     &lt;enumeration value="VAIJYNTA_RAVINTOKOHTEILLA"/&gt;
 *     &lt;enumeration value="HOUKUTTELU"/&gt;
 *     &lt;enumeration value="MUU"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "HuntingMethod", namespace = "http://riista.fi/integration/luke/export/mooselikeharvests/2016/07")
@XmlEnum
public enum LEM_HuntingMethod {

    PASSILINJA_KOIRA_OHJAAJINEEN_METSASSA,
    HIIPIMINEN_PYSAYTTAVALLE_KOIRALLE,
    PASSILINJA_JA_TIIVIS_AJOKETJU,
    PASSILINJA_JA_MIESAJO_JALJITYKSENA,
    JALJITYS_ELI_NAAKIMINEN_ILMAN_PASSEJA,
    VAIJYNTA_KULKUPAIKOILLA,
    VAIJYNTA_RAVINTOKOHTEILLA,
    HOUKUTTELU,
    MUU;

    public String value() {
        return name();
    }

    public static LEM_HuntingMethod fromValue(String v) {
        return valueOf(v);
    }

}
