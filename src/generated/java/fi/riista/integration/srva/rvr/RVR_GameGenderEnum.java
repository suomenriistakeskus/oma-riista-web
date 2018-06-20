
package fi.riista.integration.srva.rvr;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameGenderEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameGenderEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="FEMALE"/&gt;
 *     &lt;enumeration value="MALE"/&gt;
 *     &lt;enumeration value="UNKNOWN"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameGenderEnum", namespace = "http://riista.fi/integration/srva/rvr")
@XmlEnum
public enum RVR_GameGenderEnum {

    FEMALE,
    MALE,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static RVR_GameGenderEnum fromValue(String v) {
        return valueOf(v);
    }

}
