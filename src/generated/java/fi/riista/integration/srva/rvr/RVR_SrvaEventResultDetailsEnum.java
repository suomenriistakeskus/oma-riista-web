
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaEventResultDetailsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="srvaEventResultDetailsEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="ANIMAL_CONTACTED_AND_DEPORTED"/&gt;
 *     &lt;enumeration value="ANIMAL_CONTACTED"/&gt;
 *     &lt;enumeration value="UNCERTAIN_RESULT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "srvaEventResultDetailsEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SrvaEventResultDetailsEnum {

    ANIMAL_CONTACTED_AND_DEPORTED,
    ANIMAL_CONTACTED,
    UNCERTAIN_RESULT;

    public String value() {
        return name();
    }

    public static RVR_SrvaEventResultDetailsEnum fromValue(String v) {
        return valueOf(v);
    }

}
