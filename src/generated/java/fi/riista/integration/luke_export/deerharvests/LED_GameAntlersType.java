
package fi.riista.integration.luke_export.deerharvests;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gameAntlersType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gameAntlersType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="HANKO"/&gt;
 *     &lt;enumeration value="LAPIO"/&gt;
 *     &lt;enumeration value="SEKA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "gameAntlersType", namespace = "http://riista.fi/integration/luke/export/deerharvests/2020/03")
@XmlEnum
public enum LED_GameAntlersType {

    HANKO,
    LAPIO,
    SEKA;

    public String value() {
        return name();
    }

    public static LED_GameAntlersType fromValue(String v) {
        return valueOf(v);
    }

}
