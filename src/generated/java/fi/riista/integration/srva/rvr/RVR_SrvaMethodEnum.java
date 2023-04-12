
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srvaMethodEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="srvaMethodEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="DOG"/&gt;
 *     &lt;enumeration value="PAIN_EQUIPMENT"/&gt;
 *     &lt;enumeration value="SOUND_EQUIPMENT"/&gt;
 *     &lt;enumeration value="TRACED_WITH_DOG"/&gt;
 *     &lt;enumeration value="TRACED_WITHOUT_DOG"/&gt;
 *     &lt;enumeration value="VEHICLE"/&gt;
 *     &lt;enumeration value="CHASING_WITH_PEOPLE"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "srvaMethodEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_SrvaMethodEnum {

    DOG,
    PAIN_EQUIPMENT,
    SOUND_EQUIPMENT,
    TRACED_WITH_DOG,
    TRACED_WITHOUT_DOG,
    VEHICLE,
    CHASING_WITH_PEOPLE,
    OTHER;

    public String value() {
        return name();
    }

    public static RVR_SrvaMethodEnum fromValue(String v) {
        return valueOf(v);
    }

}
