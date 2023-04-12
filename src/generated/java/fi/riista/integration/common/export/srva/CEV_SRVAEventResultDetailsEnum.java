
package fi.riista.integration.common.export.srva;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SRVAEventResultDetailsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SRVAEventResultDetailsEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ANIMAL_CONTACTED_AND_DEPORTED"/&gt;
 *     &lt;enumeration value="ANIMAL_CONTACTED"/&gt;
 *     &lt;enumeration value="UNCERTAIN_RESULT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "SRVAEventResultDetailsEnum")
@XmlEnum
public enum CEV_SRVAEventResultDetailsEnum {

    ANIMAL_CONTACTED_AND_DEPORTED,
    ANIMAL_CONTACTED,
    UNCERTAIN_RESULT;

    public String value() {
        return name();
    }

    public static CEV_SRVAEventResultDetailsEnum fromValue(String v) {
        return valueOf(v);
    }

}
