
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaResultEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="srvaResultEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ANIMAL_FOUND_DEAD"/&gt;
 *     &lt;enumeration value="ANIMAL_FOUND_AND_TERMINATED"/&gt;
 *     &lt;enumeration value="ANIMAL_FOUND_AND_NOT_TERMINATED"/&gt;
 *     &lt;enumeration value="ACCIDENT_SITE_NOT_FOUND"/&gt;
 *     &lt;enumeration value="ANIMAL_TERMINATED"/&gt;
 *     &lt;enumeration value="ANIMAL_DEPORTED"/&gt;
 *     &lt;enumeration value="ANIMAL_NOT_FOUND"/&gt;
 *     &lt;enumeration value="UNDUE_ALARM"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "srvaResultEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SrvaResultEnum {

    ANIMAL_FOUND_DEAD,
    ANIMAL_FOUND_AND_TERMINATED,
    ANIMAL_FOUND_AND_NOT_TERMINATED,
    ACCIDENT_SITE_NOT_FOUND,
    ANIMAL_TERMINATED,
    ANIMAL_DEPORTED,
    ANIMAL_NOT_FOUND,
    UNDUE_ALARM;

    public String value() {
        return name();
    }

    public static RVR_SrvaResultEnum fromValue(String v) {
        return valueOf(v);
    }

}
